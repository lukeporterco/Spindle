package com.mcmodloader.core.pipeline;

import com.mcmodloader.core.classpath.RuntimeClasspathPlan;
import com.mcmodloader.core.classpath.RuntimeClasspathPlanner;
import com.mcmodloader.core.diagnostics.DiagnosticEvent;
import com.mcmodloader.core.diagnostics.DiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.discovery.ModCandidate;
import com.mcmodloader.core.discovery.ModDiscoverer;
import com.mcmodloader.core.game.GameProvider;
import com.mcmodloader.core.graph.DependencyGraphWriter;
import com.mcmodloader.core.graph.FrozenModGraph;
import com.mcmodloader.core.graph.FrozenModGraphBuilder;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.launch.LaunchPhase;
import com.mcmodloader.core.lockfile.LockfileVerifier;
import com.mcmodloader.core.lockfile.LockfileWriter;
import com.mcmodloader.core.metadata.ModMetadataParser;
import com.mcmodloader.core.ownership.ClassOwnershipIndex;
import com.mcmodloader.core.ownership.PackageOwnershipIndex;
import com.mcmodloader.core.report.DiagnosticMeasurements;
import com.mcmodloader.core.report.DisplayPaths;
import com.mcmodloader.core.resolve.DependencyResolver;
import com.mcmodloader.core.resolve.ResolvedModSet;
import com.mcmodloader.core.resource.ResourceConflict;
import com.mcmodloader.core.resource.ResourceConflictIndex;
import com.mcmodloader.core.state.ModpackState;
import com.mcmodloader.core.state.ModpackStateWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ModpackPlanningPipeline {
  public ModpackPlanningResult plan(
      LaunchContext context, GameProvider gameProvider, DiagnosticSink diagnosticSink)
      throws LoaderException {
    ModDiscoverer modDiscoverer = new ModDiscoverer();
    ModMetadataParser metadataParser = new ModMetadataParser();
    DependencyResolver dependencyResolver = new DependencyResolver();
    LockfileWriter lockfileWriter = new LockfileWriter();
    LockfileVerifier lockfileVerifier = new LockfileVerifier();
    RuntimeClasspathPlanner classpathPlanner = new RuntimeClasspathPlanner();
    FrozenModGraphBuilder frozenModGraphBuilder = new FrozenModGraphBuilder();
    ModpackStateWriter modpackStateWriter = new ModpackStateWriter();
    DependencyGraphWriter dependencyGraphWriter = new DependencyGraphWriter();

    List<ModCandidate> discoveredMods =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "mod.discovery",
            LaunchPhase.MOD_DISCOVERY,
            () -> modDiscoverer.discover(context),
            candidates ->
                DiagnosticMeasurements.details(
                    "discoveredModCount", Integer.toString(candidates.size())));
    System.out.println(
        "[loader] discovered " + discoveredMods.size() + " " + pluralize(discoveredMods.size()));

    List<ModCandidate> parsedMods =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "metadata.parse",
            LaunchPhase.METADATA_PARSE,
            () -> parseMetadata(discoveredMods, metadataParser),
            candidates ->
                DiagnosticMeasurements.details(
                    "parsedModCount", Integer.toString(candidates.size())));

    ResolvedModSet resolvedMods =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "dependency.resolution",
            LaunchPhase.DEPENDENCY_RESOLUTION,
            () -> dependencyResolver.resolve(context, parsedMods),
            resolvedModSet ->
                DiagnosticMeasurements.details(
                    "resolvedModCount", Integer.toString(resolvedModSet.mods().size())));
    System.out.println(
        "[loader] resolved "
            + resolvedMods.mods().size()
            + " "
            + pluralize(resolvedMods.mods().size()));

    Path lockfilePath = context.workingDirectory().resolve("loader.lock.json");
    String lockfileAction =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "lockfile.verify_or_write",
            LaunchPhase.LOCKFILE,
            () ->
                verifyOrWriteLockfile(
                    lockfilePath, context, resolvedMods, lockfileWriter, lockfileVerifier),
            action -> DiagnosticMeasurements.details("lockfileAction", action));
    System.out.println(
        "wrote".equals(lockfileAction)
            ? "[loader] wrote loader.lock.json"
            : "[loader] verified loader.lock.json");

    RuntimeClasspathPlan classpathPlan =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "classpath.plan",
            LaunchPhase.CLASSPATH_PLAN,
            () -> classpathPlanner.plan(context, resolvedMods),
            plan ->
                DiagnosticMeasurements.details(
                    "modJarCount",
                    Integer.toString(plan.modJars().size()),
                    "modJars",
                    String.join(",", plan.modJarDisplayPaths(context.workingDirectory()))));

    ClassOwnershipIndex classOwnershipIndex =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "ownership.index",
            LaunchPhase.CLASSPATH_PLAN,
            () -> ClassOwnershipIndex.build(resolvedMods),
            index ->
                DiagnosticMeasurements.details(
                    "classOwnershipCount", Integer.toString(index.totalClasses())));

    PackageOwnershipIndex packageOwnershipIndex =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "package.index",
            LaunchPhase.CLASSPATH_PLAN,
            () -> PackageOwnershipIndex.build(resolvedMods),
            index ->
                DiagnosticMeasurements.details(
                    "splitPackageCount", Integer.toString(index.splitPackages().size())));
    recordSplitPackageDiagnostics(diagnosticSink, packageOwnershipIndex);
    enforceStrictPackages(context, packageOwnershipIndex);

    ResourceConflictIndex resourceConflictIndex =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "resource.index",
            LaunchPhase.CLASSPATH_PLAN,
            () -> ResourceConflictIndex.build(resolvedMods),
            index ->
                DiagnosticMeasurements.details(
                    "duplicateResourceCount", Integer.toString(index.conflicts().size())));
    recordResourceDiagnostics(diagnosticSink, resourceConflictIndex);
    enforceStrictResources(context, resourceConflictIndex);

    FrozenModGraph frozenModGraph =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "frozen_mod_graph.create",
            LaunchPhase.FROZEN_MOD_GRAPH,
            () ->
                frozenModGraphBuilder.build(
                    context,
                    gameProvider,
                    resolvedMods,
                    classpathPlan,
                    classOwnershipIndex,
                    packageOwnershipIndex,
                    resourceConflictIndex),
            graph ->
                DiagnosticMeasurements.details(
                    "frozenModCount",
                    Integer.toString(graph.mods().size()),
                    "gameProviderId",
                    graph.gameProviderId(),
                    "gameProviderVersion",
                    graph.gameProviderVersion()));

    if (context.explain()) {
      printExplain(context, frozenModGraph, discoveredMods.size(), lockfileAction);
      diagnosticSink.record(
          new DiagnosticEvent(
              "explain.print",
              LaunchPhase.COMPLETE.name(),
              0L,
              "ok",
              "Explain summary printed",
              DiagnosticMeasurements.details(
                  "dependencyGraphOutputPath",
                  "dependency-graph.json",
                  "modpackStateOutputPath",
                  "modpack-state.json")));
    }

    Path modpackStatePath = context.workingDirectory().resolve("modpack-state.json");
    ModpackState modpackState =
        modpackStateWriter.create(frozenModGraph, context.workingDirectory());
    DiagnosticMeasurements.measure(
        diagnosticSink,
        "modpack_state.write",
        LaunchPhase.COMPLETE,
        () -> {
          modpackStateWriter.write(modpackStatePath, modpackState);
          return modpackStatePath;
        },
        outputPath ->
            DiagnosticMeasurements.details(
                "modpackStateOutputPath", DisplayPaths.displayPath(context, outputPath)));

    Path dependencyGraphPath = context.workingDirectory().resolve("dependency-graph.json");
    DiagnosticMeasurements.measure(
        diagnosticSink,
        "dependency_graph.write",
        LaunchPhase.COMPLETE,
        () -> {
          dependencyGraphWriter.write(dependencyGraphPath, frozenModGraph);
          return dependencyGraphPath;
        },
        outputPath ->
            DiagnosticMeasurements.details(
                "dependencyGraphOutputPath", DisplayPaths.displayPath(context, outputPath)));

    return new ModpackPlanningResult(
        List.copyOf(discoveredMods),
        List.copyOf(parsedMods),
        resolvedMods,
        lockfileAction,
        classpathPlan,
        classOwnershipIndex,
        packageOwnershipIndex,
        resourceConflictIndex,
        frozenModGraph,
        modpackStatePath,
        dependencyGraphPath);
  }

  private static void enforceStrictPackages(
      LaunchContext context, PackageOwnershipIndex packageOwnershipIndex) throws LoaderException {
    if (!context.strictPackages() || packageOwnershipIndex.splitPackages().isEmpty()) {
      return;
    }
    PackageOwnershipIndex.SplitPackage splitPackage =
        packageOwnershipIndex.splitPackages().getFirst();
    throw new LoaderException(
        "Split package "
            + splitPackage.packageName()
            + " found in mods "
            + String.join(",", splitPackage.modIds()));
  }

  private static void enforceStrictResources(
      LaunchContext context, ResourceConflictIndex resourceConflictIndex) throws LoaderException {
    if (!context.strictResources() || resourceConflictIndex.conflicts().isEmpty()) {
      return;
    }
    ResourceConflict conflict = resourceConflictIndex.conflicts().getFirst();
    throw new LoaderException(
        "Duplicate resource "
            + conflict.resourcePath()
            + " found in mods "
            + String.join(",", conflict.modIds()));
  }

  private static List<ModCandidate> parseMetadata(
      List<ModCandidate> discoveredMods, ModMetadataParser metadataParser) throws LoaderException {
    List<ModCandidate> parsedMods = new ArrayList<>(discoveredMods.size());
    for (ModCandidate candidate : discoveredMods) {
      parsedMods.add(candidate.withMetadata(metadataParser.parse(candidate)));
    }
    return List.copyOf(parsedMods);
  }

  private static void printExplain(
      LaunchContext context,
      FrozenModGraph frozenModGraph,
      int discoveredModCount,
      String lockfileAction) {
    System.out.println(
        "[loader] explain: provider "
            + frozenModGraph.gameProviderId()
            + " "
            + frozenModGraph.gameProviderVersion());
    System.out.println(
        "[loader] explain: discovered " + discoveredModCount + " " + pluralize(discoveredModCount));
    System.out.println(
        "[loader] explain: resolved "
            + frozenModGraph.mods().size()
            + " "
            + pluralize(frozenModGraph.mods().size()));
    System.out.println("[loader] explain: lockfile " + lockfileAction);
    System.out.println(
        "[loader] explain: duplicate resources " + frozenModGraph.resourceConflicts().size());
    System.out.println(
        "[loader] explain: split packages "
            + frozenModGraph.packageOwnershipIndex().splitPackages().size());
    System.out.println(
        "[loader] explain: dependency graph "
            + DisplayPaths.displayPath(
                context, context.workingDirectory().resolve("dependency-graph.json")));
    System.out.println(
        "[loader] explain: modpack state "
            + DisplayPaths.displayPath(
                context, context.workingDirectory().resolve("modpack-state.json")));
  }

  private static String pluralize(int count) {
    return count == 1 ? "mod" : "mods";
  }

  private static void recordResourceDiagnostics(
      DiagnosticSink diagnosticSink, ResourceConflictIndex resourceConflictIndex) {
    for (ResourceConflict conflict : resourceConflictIndex.conflicts()) {
      diagnosticSink.record(
          new DiagnosticEvent(
              "resource.duplicate",
              LaunchPhase.CLASSPATH_PLAN.name(),
              0L,
              "ok",
              "Duplicate resource detected",
              DiagnosticMeasurements.details(
                  "resource",
                  conflict.resourcePath(),
                  "mods",
                  String.join(",", conflict.modIds()))));
    }
  }

  private static void recordSplitPackageDiagnostics(
      DiagnosticSink diagnosticSink, PackageOwnershipIndex packageOwnershipIndex) {
    for (PackageOwnershipIndex.SplitPackage splitPackage : packageOwnershipIndex.splitPackages()) {
      diagnosticSink.record(
          new DiagnosticEvent(
              "package.split",
              LaunchPhase.CLASSPATH_PLAN.name(),
              0L,
              "ok",
              "Split package detected",
              DiagnosticMeasurements.details(
                  "package",
                  splitPackage.packageName(),
                  "mods",
                  String.join(",", splitPackage.modIds()))));
    }
  }

  private static String verifyOrWriteLockfile(
      Path lockfilePath,
      LaunchContext context,
      ResolvedModSet resolvedMods,
      LockfileWriter lockfileWriter,
      LockfileVerifier lockfileVerifier)
      throws LoaderException {
    if (lockfileVerifier.exists(lockfilePath)) {
      lockfileVerifier.verify(lockfilePath, context, resolvedMods);
      return "verified";
    }

    lockfileWriter.write(lockfilePath, context, resolvedMods);
    return "wrote";
  }
}
