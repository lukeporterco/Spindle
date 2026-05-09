package com.spindle.core.pipeline;

import com.spindle.core.classpath.RuntimeClasspathPlan;
import com.spindle.core.classpath.RuntimeClasspathPlanner;
import com.spindle.core.diagnostics.DiagnosticEvent;
import com.spindle.core.diagnostics.DiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.discovery.ModCandidate;
import com.spindle.core.discovery.ModDiscoverer;
import com.spindle.core.game.GameProvider;
import com.spindle.core.graph.DependencyGraphWriter;
import com.spindle.core.graph.FrozenModGraph;
import com.spindle.core.graph.FrozenModGraphBuilder;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.launch.LaunchPhase;
import com.spindle.core.lockfile.LockfileVerifier;
import com.spindle.core.lockfile.LockfileWriter;
import com.spindle.core.metadata.ModMetadataParser;
import com.spindle.core.ownership.ClassOwnershipIndex;
import com.spindle.core.ownership.PackageOwnershipIndex;
import com.spindle.core.report.DiagnosticMeasurements;
import com.spindle.core.report.DisplayPaths;
import com.spindle.core.resolve.DependencyResolver;
import com.spindle.core.resolve.ResolvedModSet;
import com.spindle.core.resource.ResourceConflict;
import com.spindle.core.resource.ResourceConflictIndex;
import com.spindle.core.runtime.ProtectedPackageViolation;
import com.spindle.core.runtime.RuntimePackagePolicyInspector;
import com.spindle.core.state.ModpackState;
import com.spindle.core.state.ModpackStateWriter;
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
    RuntimePackagePolicyInspector runtimePackagePolicyInspector =
        new RuntimePackagePolicyInspector();

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
        "[spindle] discovered " + discoveredMods.size() + " " + pluralize(discoveredMods.size()));

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
        "[spindle] resolved "
            + resolvedMods.mods().size()
            + " "
            + pluralize(resolvedMods.mods().size()));

    Path lockfilePath = context.workingDirectory().resolve("spindle.lock.json");
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
            ? "[spindle] wrote spindle.lock.json"
            : "[spindle] verified spindle.lock.json");

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
    List<ProtectedPackageViolation> protectedPackageViolations =
        runtimePackagePolicyInspector.findProtectedPackageViolations(
            resolvedMods, packageOwnershipIndex);
    recordProtectedPackageDiagnostics(diagnosticSink, protectedPackageViolations);
    enforceProtectedPackages(gameProvider, protectedPackageViolations);

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
                  "spindle.graph.json",
                  "modpackStateOutputPath",
                  "spindle.report.json")));
    }

    Path modpackStatePath = context.workingDirectory().resolve("spindle.report.json");
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

    Path dependencyGraphPath = context.workingDirectory().resolve("spindle.graph.json");
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
        lockfilePath,
        lockfileAction,
        classpathPlan,
        classOwnershipIndex,
        packageOwnershipIndex,
        protectedPackageViolations,
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
        "[spindle] explain: provider "
            + frozenModGraph.gameProviderId()
            + " "
            + frozenModGraph.gameProviderVersion());
    System.out.println(
        "[spindle] explain: discovered "
            + discoveredModCount
            + " "
            + pluralize(discoveredModCount));
    System.out.println(
        "[spindle] explain: resolved "
            + frozenModGraph.mods().size()
            + " "
            + pluralize(frozenModGraph.mods().size()));
    System.out.println("[spindle] explain: lockfile " + lockfileAction);
    System.out.println(
        "[spindle] explain: duplicate resources " + frozenModGraph.resourceConflicts().size());
    System.out.println(
        "[spindle] explain: split packages "
            + frozenModGraph.packageOwnershipIndex().splitPackages().size());
    System.out.println(
        "[spindle] explain: dependency graph "
            + DisplayPaths.displayPath(
                context, context.workingDirectory().resolve("spindle.graph.json")));
    System.out.println(
        "[spindle] explain: modpack state "
            + DisplayPaths.displayPath(
                context, context.workingDirectory().resolve("spindle.report.json")));
  }

  private static String pluralize(int count) {
    return count == 1 ? "mod" : "mods";
  }

  private static void enforceProtectedPackages(
      GameProvider gameProvider, List<ProtectedPackageViolation> protectedPackageViolations)
      throws LoaderException {
    if ("minecraft".equals(gameProvider.id()) || protectedPackageViolations.isEmpty()) {
      return;
    }
    ProtectedPackageViolation violation = protectedPackageViolations.getFirst();
    throw new LoaderException(
        "Mod `"
            + violation.modId()
            + "` defines protected package `"
            + violation.packageName()
            + "`. "
            + violation.reason());
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

  private static void recordProtectedPackageDiagnostics(
      DiagnosticSink diagnosticSink, List<ProtectedPackageViolation> protectedPackageViolations) {
    for (ProtectedPackageViolation violation : protectedPackageViolations) {
      diagnosticSink.record(
          new DiagnosticEvent(
              "package.protected",
              LaunchPhase.CLASSPATH_PLAN.name(),
              0L,
              "ok",
              "Protected package definition detected",
              DiagnosticMeasurements.details(
                  "modId", violation.modId(), "package", violation.packageName())));
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
