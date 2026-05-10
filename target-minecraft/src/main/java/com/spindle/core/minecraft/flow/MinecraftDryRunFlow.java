package com.spindle.core.minecraft.flow;

import com.spindle.core.artifact.MinecraftArtifactCache;
import com.spindle.core.artifact.MinecraftArtifactInspector;
import com.spindle.core.artifact.MinecraftArtifactResolver;
import com.spindle.core.diagnostics.DiagnosticEvent;
import com.spindle.core.diagnostics.DiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.launch.LaunchPhase;
import com.spindle.core.mache.MacheReferenceReport;
import com.spindle.core.mache.MacheReferenceScanner;
import com.spindle.core.mache.MacheReferenceWriter;
import com.spindle.core.minecraft.MinecraftArgumentResolver;
import com.spindle.core.minecraft.MinecraftBootstrapClassLoaderGraph;
import com.spindle.core.minecraft.MinecraftBootstrapClassLoaderGraphBuilder;
import com.spindle.core.minecraft.MinecraftBootstrapClassLoaderGraphWriter;
import com.spindle.core.minecraft.MinecraftDryRunResult;
import com.spindle.core.minecraft.MinecraftFileVerifier;
import com.spindle.core.minecraft.MinecraftGameProvider;
import com.spindle.core.minecraft.MinecraftLaunchPlan;
import com.spindle.core.minecraft.MinecraftLaunchPlanBuilder;
import com.spindle.core.minecraft.MinecraftLaunchPlanWriter;
import com.spindle.core.minecraft.MinecraftLibrarySelector;
import com.spindle.core.minecraft.MinecraftMetadataResolver;
import com.spindle.core.minecraft.MinecraftModExecutionPlan;
import com.spindle.core.minecraft.MinecraftModExecutionPlanWriter;
import com.spindle.core.minecraft.MinecraftModExecutionPlanner;
import com.spindle.core.minecraft.MinecraftModIntegrationPlan;
import com.spindle.core.minecraft.MinecraftModIntegrationPlanWriter;
import com.spindle.core.minecraft.MinecraftModIntegrationPlanner;
import com.spindle.core.minecraft.MinecraftPlanFingerprint;
import com.spindle.core.minecraft.MinecraftPreflightResult;
import com.spindle.core.minecraft.MinecraftPreflightResultWriter;
import com.spindle.core.minecraft.MinecraftProviderConfig;
import com.spindle.core.minecraft.MinecraftReproducibilityCheck;
import com.spindle.core.minecraft.MinecraftReproducibilityCheckWriter;
import com.spindle.core.minecraft.MinecraftRuntimeBoundary;
import com.spindle.core.minecraft.MinecraftRuntimeBoundaryBuilder;
import com.spindle.core.minecraft.MinecraftRuntimeBoundaryWriter;
import com.spindle.core.minecraft.MinecraftRuntimeProvenanceWriter;
import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import com.spindle.core.minecraft.MinecraftServerRuntimePlanWriter;
import com.spindle.core.minecraft.MinecraftServerRuntimePlanner;
import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.MinecraftVersionMetadata;
import com.spindle.core.pipeline.ModpackPlanningResult;
import com.spindle.core.report.DiagnosticMeasurements;
import com.spindle.core.report.DisplayPaths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftDryRunFlow {
  private final MinecraftPreflightFlow minecraftPreflightFlow = new MinecraftPreflightFlow();
  private final MinecraftReproducibilityFlow minecraftReproducibilityFlow =
      new MinecraftReproducibilityFlow();

  public MinecraftDryRunResult run(
      LaunchContext context,
      boolean macheReferenceScan,
      Path macheDirectory,
      String macheVersion,
      MinecraftGameProvider minecraftGameProvider,
      ModpackPlanningResult planningResult,
      DiagnosticSink diagnosticSink)
      throws LoaderException {
    MinecraftProviderConfig config = minecraftGameProvider.config();
    MinecraftArtifactCache artifactCache =
        new MinecraftArtifactCache(context.workingDirectory(), config.cacheDirectory());
    MinecraftArtifactResolver artifactResolver = new MinecraftArtifactResolver(artifactCache);
    com.spindle.core.minecraft.MinecraftInstallLocator installLocator =
        new com.spindle.core.minecraft.MinecraftInstallLocator();
    MinecraftLibrarySelector librarySelector = new MinecraftLibrarySelector();
    MinecraftArgumentResolver argumentResolver = new MinecraftArgumentResolver();
    MinecraftLaunchPlanBuilder launchPlanBuilder = new MinecraftLaunchPlanBuilder();
    MinecraftLaunchPlanWriter launchPlanWriter = new MinecraftLaunchPlanWriter();
    MinecraftFileVerifier fileVerifier = new MinecraftFileVerifier();
    MacheReferenceScanner macheReferenceScanner = new MacheReferenceScanner();
    MacheReferenceWriter macheReferenceWriter = new MacheReferenceWriter();

    if (config.cacheInspect()) {
      DiagnosticMeasurements.measure(
          diagnosticSink,
          "minecraft.metadata.resolve",
          LaunchPhase.COMPLETE,
          () -> {
            new MinecraftArtifactInspector(artifactCache).inspect(config, diagnosticSink);
            return config.requestedVersion();
          },
          ignored ->
              DiagnosticMeasurements.details(
                  "minecraftVersion",
                  config.requestedVersion(),
                  "minecraftSide",
                  config.side().id(),
                  "artifactReportOutputPath",
                  DisplayPaths.displayPath(context, artifactCache.artifactReportPath())));
      return new MinecraftDryRunResult(null, null, null, "missing", null);
    }

    MinecraftArtifactResolver.Resolution artifactResolution =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "minecraft.metadata.resolve",
            LaunchPhase.COMPLETE,
            () -> artifactResolver.resolve(context.workingDirectory(), config, diagnosticSink),
            resolved ->
                DiagnosticMeasurements.details(
                    "minecraftVersion",
                    resolved.metadata().id(),
                    "minecraftSide",
                    config.side().id(),
                    "versionJsonPath",
                    DisplayPaths.displayPath(
                        context, resolved.resolvedVersionJson().versionJsonPath()),
                    "metadataSource",
                    resolved.resolvedVersionJson().metadataSource()));
    MinecraftMetadataResolver.ResolvedVersionJson resolvedVersionJson =
        artifactResolution.resolvedVersionJson();
    MinecraftVersionMetadata metadata = artifactResolution.metadata();

    MinecraftLibrarySelector.Selection selection =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "minecraft.library.select",
            LaunchPhase.COMPLETE,
            () ->
                config.side() == MinecraftSide.SERVER
                    ? new MinecraftLibrarySelector.Selection(List.of(), List.of())
                    : librarySelector.select(
                        metadata, installLocator.librariesRoot(config.minecraftDirectory())),
            selected ->
                DiagnosticMeasurements.details(
                    "minecraftVersion",
                    metadata.id(),
                    "minecraftSide",
                    config.side().id(),
                    "selectedLibraryCount",
                    Integer.toString(selected.libraries().size()),
                    "nativeLibraryCount",
                    Integer.toString(selected.nativeLibraries().size())));

    List<Path> launchClasspath = new ArrayList<>();
    selection.libraries().forEach(library -> launchClasspath.add(library.path()));
    if (config.side() == MinecraftSide.CLIENT) {
      launchClasspath.add(installLocator.clientJarPath(config.minecraftDirectory(), metadata.id()));
    } else if (artifactResolution.serverJarPath() != null) {
      launchClasspath.add(artifactResolution.serverJarPath());
    }

    MinecraftArgumentResolver.ResolvedArguments resolvedArguments =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "minecraft.arguments.resolve",
            LaunchPhase.COMPLETE,
            () ->
                argumentResolver.resolve(
                    config,
                    metadata,
                    config.minecraftDirectory(),
                    config.minecraftDirectory() == null
                        ? null
                        : installLocator.assetsRoot(config.minecraftDirectory()),
                    installLocator.nativesDirectory(
                        context.workingDirectory(), metadata.id(), config.side()),
                    launchClasspath),
            arguments ->
                DiagnosticMeasurements.details(
                    "minecraftVersion",
                    metadata.id(),
                    "minecraftSide",
                    config.side().id(),
                    "jvmArgumentCount",
                    Integer.toString(arguments.jvmArguments().size()),
                    "gameArgumentCount",
                    Integer.toString(arguments.gameArguments().size())));

    MinecraftLaunchPlan launchPlan =
        launchPlanBuilder.build(
            context.workingDirectory(),
            config,
            resolvedVersionJson,
            metadata,
            artifactResolution.serverJarPath(),
            artifactResolution.serverJarSource(),
            selection,
            resolvedArguments,
            installLocator);

    List<Path> missingFiles =
        config.verifyFiles()
            ? DiagnosticMeasurements.measure(
                diagnosticSink,
                "minecraft.file_verify",
                LaunchPhase.COMPLETE,
                () ->
                    fileVerifier.verify(
                        config,
                        resolvedVersionJson,
                        metadata,
                        artifactResolution.serverJarPath(),
                        selection,
                        installLocator),
                missing ->
                    DiagnosticMeasurements.details(
                        "minecraftVersion",
                        metadata.id(),
                        "minecraftSide",
                        config.side().id(),
                        "missingFileCount",
                        Integer.toString(missing.size())))
            : fileVerifier.verify(
                config,
                resolvedVersionJson,
                metadata,
                artifactResolution.serverJarPath(),
                selection,
                installLocator);

    launchPlan =
        launchPlan.withMissingFiles(
            missingFiles.stream().map(path -> DisplayPaths.displayPath(context, path)).toList());

    MinecraftLaunchPlan finalLaunchPlan = launchPlan;
    DiagnosticMeasurements.measure(
        diagnosticSink,
        "minecraft.launch_plan.write",
        LaunchPhase.COMPLETE,
        () -> {
          launchPlanWriter.write(config.outputPlanPath(), finalLaunchPlan);
          return config.outputPlanPath();
        },
        outputPath ->
            DiagnosticMeasurements.details(
                "minecraftVersion",
                metadata.id(),
                "minecraftSide",
                config.side().id(),
                "missingFileCount",
                Integer.toString(finalLaunchPlan.missingFiles().size()),
                "launchPlanOutputPath",
                DisplayPaths.displayPath(context, outputPath)));

    MacheReferenceReport macheReferenceReport = null;
    if (macheReferenceScan && macheDirectory != null) {
      Path reportPath = context.workingDirectory().resolve("mache-reference-report.json");
      String requestedMacheVersion =
          macheVersion == null || macheVersion.isBlank() ? metadata.id() : macheVersion;
      macheReferenceReport =
          DiagnosticMeasurements.measure(
              diagnosticSink,
              "mache.reference.scan",
              LaunchPhase.COMPLETE,
              () -> macheReferenceScanner.scan(macheDirectory, requestedMacheVersion),
              report ->
                  DiagnosticMeasurements.details(
                      "minecraftVersion",
                      metadata.id(),
                      "macheReferenceScan",
                      "true",
                      "macheReportOutputPath",
                      DisplayPaths.displayPath(context, reportPath)));
      MacheReferenceReport finalMacheReferenceReport = macheReferenceReport;
      DiagnosticMeasurements.measure(
          diagnosticSink,
          "mache.reference_report.write",
          LaunchPhase.COMPLETE,
          () -> {
            macheReferenceWriter.write(reportPath, finalMacheReferenceReport);
            return reportPath;
          },
          outputPath ->
              DiagnosticMeasurements.details(
                  "macheReportOutputPath", DisplayPaths.displayPath(context, outputPath)));
    }

    MinecraftServerRuntimePlanner.PlannedRuntime plannedRuntime = null;
    MinecraftRuntimeBoundary runtimeBoundary = null;
    MinecraftModIntegrationPlan integrationPlan = null;
    MinecraftModExecutionPlan executionPlan = null;
    List<String> megaMilestoneReports = new ArrayList<>();
    boolean needsRuntimePlanning =
        config.side() == MinecraftSide.SERVER
            && artifactResolution.serverJarPath() != null
            && (config.runtimePlan()
                || config.planMods()
                || config.boundaryReport()
                || config.integrationPlan()
                || config.preflight()
                || config.reproducibilityCheck()
                || config.executionPlan()
                || config.bootstrapClassloaderGraph()
                || config.bootstrapServer()
                || config.launch());
    if (needsRuntimePlanning) {
      MinecraftArtifactCache finalArtifactCache = artifactCache;
      plannedRuntime =
          DiagnosticMeasurements.measure(
              diagnosticSink,
              "minecraft.runtime.plan",
              LaunchPhase.COMPLETE,
              () ->
                  new MinecraftServerRuntimePlanner()
                      .plan(
                          context.workingDirectory(),
                          config,
                          finalArtifactCache,
                          artifactResolution,
                          path -> DisplayPaths.displayPath(context, path)),
              planned ->
                  DiagnosticMeasurements.details(
                      "minecraftVersion",
                      metadata.id(),
                      "launchMode",
                      planned.plan().launchMode(),
                      "runtimePlanOutputPath",
                      DisplayPaths.displayPath(
                          context,
                          context
                              .workingDirectory()
                              .resolve("minecraft-server-runtime-plan.json"))));
      MinecraftServerRuntimePlanner.PlannedRuntime finalPlannedRuntime = plannedRuntime;
      DiagnosticMeasurements.measure(
          diagnosticSink,
          "minecraft.runtime_plan.write",
          LaunchPhase.COMPLETE,
          () -> {
            Path outputPath =
                context.workingDirectory().resolve("minecraft-server-runtime-plan.json");
            new MinecraftServerRuntimePlanWriter().write(outputPath, finalPlannedRuntime.plan());
            return outputPath;
          },
          outputPath ->
              DiagnosticMeasurements.details(
                  "runtimePlanOutputPath", DisplayPaths.displayPath(context, outputPath)));
      DiagnosticMeasurements.measure(
          diagnosticSink,
          "minecraft.runtime_provenance.write",
          LaunchPhase.COMPLETE,
          () -> {
            Path outputPath =
                context.workingDirectory().resolve("minecraft-runtime-provenance.json");
            new MinecraftRuntimeProvenanceWriter()
                .write(outputPath, finalPlannedRuntime.plan().provenance());
            return outputPath;
          },
          outputPath ->
              DiagnosticMeasurements.details(
                  "runtimeProvenanceOutputPath", DisplayPaths.displayPath(context, outputPath)));
      megaMilestoneReports.add("minecraft-server-runtime-plan.json");
      megaMilestoneReports.add("minecraft-runtime-provenance.json");
      if (config.explainRuntime()) {
        printMinecraftRuntimeExplain(finalPlannedRuntime.plan());
      }

      if (config.boundaryReport()
          || config.planMods()
          || config.integrationPlan()
          || config.preflight()
          || config.reproducibilityCheck()) {
        runtimeBoundary =
            DiagnosticMeasurements.measure(
                diagnosticSink,
                "minecraft.runtime_boundary.create",
                LaunchPhase.COMPLETE,
                () ->
                    new MinecraftRuntimeBoundaryBuilder()
                        .build(
                            finalPlannedRuntime.plan(),
                            minecraftReproducibilityRuntimeJars(
                                context,
                                artifactResolution.serverJarPath(),
                                finalPlannedRuntime.plan()),
                            path -> DisplayPaths.displayPath(context, path),
                            config.strictBoundary(),
                            config.strictRuntimeConflicts()),
                boundary ->
                    DiagnosticMeasurements.details(
                        "packageCount",
                        Integer.toString(boundary.packageOwnership().size()),
                        "resourceCount",
                        Integer.toString(boundary.resourceOwnership().size())));
        MinecraftRuntimeBoundary finalRuntimeBoundary = runtimeBoundary;
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "minecraft.runtime_boundary.write",
            LaunchPhase.COMPLETE,
            () -> {
              Path outputPath =
                  context.workingDirectory().resolve("minecraft-runtime-boundary.json");
              new MinecraftRuntimeBoundaryWriter().write(outputPath, finalRuntimeBoundary);
              return outputPath;
            },
            outputPath ->
                DiagnosticMeasurements.details(
                    "runtimeBoundaryOutputPath", DisplayPaths.displayPath(context, outputPath)));
        megaMilestoneReports.add("minecraft-runtime-boundary.json");
        if (config.explainBoundary()) {
          printMinecraftBoundaryExplain(finalRuntimeBoundary);
        }
      }

      if ((config.integrationPlan()
              || config.planMods()
              || config.preflight()
              || config.reproducibilityCheck()
              || config.executionPlan()
              || config.bootstrapClassloaderGraph()
              || config.bootstrapServer())
          && runtimeBoundary != null) {
        MinecraftRuntimeBoundary finalRuntimeBoundary = runtimeBoundary;
        integrationPlan =
            DiagnosticMeasurements.measure(
                diagnosticSink,
                "minecraft.mod_integration.plan",
                LaunchPhase.COMPLETE,
                () ->
                    new MinecraftModIntegrationPlanner()
                        .plan(
                            context,
                            planningResult.parsedMods(),
                            planningResult.resolvedMods(),
                            finalRuntimeBoundary,
                            metadata.id(),
                            config.strictSide(),
                            config.strictClassVersions(),
                            config.strictRuntimeConflicts(),
                            path -> DisplayPaths.displayPath(context, path)),
                plan ->
                    DiagnosticMeasurements.details(
                        "acceptedModCount",
                        Integer.toString(plan.acceptedMods().size()),
                        "rejectedModCount",
                        Integer.toString(plan.rejectedMods().size())));
        MinecraftModIntegrationPlan finalIntegrationPlan = integrationPlan;
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "minecraft.mod_integration.write",
            LaunchPhase.COMPLETE,
            () -> {
              Path outputPath =
                  context.workingDirectory().resolve("minecraft-mod-integration-plan.json");
              new MinecraftModIntegrationPlanWriter().write(outputPath, finalIntegrationPlan);
              return outputPath;
            },
            outputPath ->
                DiagnosticMeasurements.details(
                    "modIntegrationOutputPath", DisplayPaths.displayPath(context, outputPath)));
        megaMilestoneReports.add("minecraft-mod-integration-plan.json");
        if (config.explainMods()) {
          printMinecraftModsExplain(finalIntegrationPlan);
        }
      }

      if ((config.executionPlan()
              || config.bootstrapClassloaderGraph()
              || config.bootstrapServer()
              || config.reproducibilityCheck())
          && runtimeBoundary != null
          && integrationPlan != null
          && plannedRuntime != null) {
        MinecraftServerRuntimePlanner.PlannedRuntime executionPlannedRuntime = plannedRuntime;
        MinecraftRuntimeBoundary finalRuntimeBoundary = runtimeBoundary;
        MinecraftModIntegrationPlan finalIntegrationPlan = integrationPlan;
        MinecraftPlanFingerprint runtimePlanFingerprint =
            MinecraftPlanFingerprint.fromFile(
                "runtime-plan",
                context.workingDirectory().resolve("minecraft-server-runtime-plan.json"));
        MinecraftPlanFingerprint boundaryFingerprint =
            MinecraftPlanFingerprint.fromFile(
                "runtime-boundary",
                context.workingDirectory().resolve("minecraft-runtime-boundary.json"));
        MinecraftPlanFingerprint integrationFingerprint =
            MinecraftPlanFingerprint.fromFile(
                "integration-plan",
                context.workingDirectory().resolve("minecraft-mod-integration-plan.json"));
        executionPlan =
            DiagnosticMeasurements.measure(
                diagnosticSink,
                "minecraft.mod_execution.plan",
                LaunchPhase.COMPLETE,
                () ->
                    new MinecraftModExecutionPlanner()
                        .plan(
                            context,
                            config,
                            planningResult.parsedMods(),
                            executionPlannedRuntime.plan(),
                            finalRuntimeBoundary,
                            finalIntegrationPlan,
                            runtimePlanFingerprint,
                            boundaryFingerprint,
                            integrationFingerprint),
                plan ->
                    DiagnosticMeasurements.details(
                        "executableModCount",
                        Integer.toString(plan.acceptedExecutableMods().size()),
                        "entrypointCount",
                        Integer.toString(plan.executableEntrypoints().size())));
        MinecraftModExecutionPlan finalExecutionPlan = executionPlan;
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "minecraft.mod_execution.write",
            LaunchPhase.COMPLETE,
            () -> {
              Path outputPath =
                  context.workingDirectory().resolve("minecraft-mod-execution-plan.json");
              new MinecraftModExecutionPlanWriter().write(outputPath, finalExecutionPlan);
              return outputPath;
            },
            outputPath ->
                DiagnosticMeasurements.details(
                    "modExecutionPlanOutputPath", DisplayPaths.displayPath(context, outputPath)));
        megaMilestoneReports.add("minecraft-mod-execution-plan.json");

        if (config.bootstrapClassloaderGraph()) {
          MinecraftBootstrapClassLoaderGraph graph =
              new MinecraftBootstrapClassLoaderGraphBuilder().build(finalExecutionPlan);
          DiagnosticMeasurements.measure(
              diagnosticSink,
              "minecraft.bootstrap_classloader_graph.write",
              LaunchPhase.COMPLETE,
              () -> {
                Path outputPath =
                    context
                        .workingDirectory()
                        .resolve("minecraft-bootstrap-classloader-graph.json");
                new MinecraftBootstrapClassLoaderGraphWriter().write(outputPath, graph);
                return outputPath;
              },
              outputPath ->
                  DiagnosticMeasurements.details(
                      "bootstrapClassloaderGraphOutputPath",
                      DisplayPaths.displayPath(context, outputPath)));
          megaMilestoneReports.add("minecraft-bootstrap-classloader-graph.json");
        }
      }

      if (config.preflight() && runtimeBoundary != null) {
        MinecraftPreflightResult preflightResult =
            minecraftPreflightFlow.buildResult(
                metadata.id(), megaMilestoneReports, runtimeBoundary, integrationPlan);
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "minecraft.preflight.write",
            LaunchPhase.COMPLETE,
            () -> {
              Path outputPath = context.workingDirectory().resolve("spindle.preflight.json");
              new MinecraftPreflightResultWriter().write(outputPath, preflightResult);
              return outputPath;
            },
            outputPath ->
                DiagnosticMeasurements.details(
                    "preflightOutputPath", DisplayPaths.displayPath(context, outputPath)));
        megaMilestoneReports.add("spindle.preflight.json");
        if (!preflightResult.succeeded()) {
          throw new LoaderException(
              "Minecraft preflight failed. See spindle.preflight.json for failure reasons.");
        }
      }

      if (config.reproducibilityCheck()) {
        MinecraftReproducibilityCheck check =
            minecraftReproducibilityFlow.createCheck(
                context,
                config,
                artifactCache,
                artifactResolution,
                planningResult.parsedMods(),
                planningResult.resolvedMods(),
                metadata.id(),
                plannedRuntime.plan(),
                runtimeBoundary,
                integrationPlan,
                executionPlan,
                megaMilestoneReports);
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "minecraft.reproducibility.write",
            LaunchPhase.COMPLETE,
            () -> {
              Path outputPath =
                  context.workingDirectory().resolve("minecraft-reproducibility-check.json");
              new MinecraftReproducibilityCheckWriter().write(outputPath, check);
              return outputPath;
            },
            outputPath ->
                DiagnosticMeasurements.details(
                    "reproducibilityOutputPath", DisplayPaths.displayPath(context, outputPath)));
        if (!check.byteForByteEqual() || !check.failures().isEmpty()) {
          throw new LoaderException(
              "Minecraft reproducibility check failed. See minecraft-reproducibility-check.json for details.");
        }
      }
    }

    diagnosticSink.record(
        new DiagnosticEvent(
            "minecraft.dry_run.complete",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            "Minecraft dry run complete",
            DiagnosticMeasurements.details(
                "minecraftVersion",
                metadata.id(),
                "minecraftSide",
                config.side().id(),
                "selectedLibraryCount",
                Integer.toString(selection.libraries().size()),
                "nativeLibraryCount",
                Integer.toString(selection.nativeLibraries().size()),
                "missingFileCount",
                Integer.toString(launchPlan.missingFiles().size()),
                "launchPlanOutputPath",
                DisplayPaths.displayPath(context, config.outputPlanPath()),
                "versionJsonPath",
                DisplayPaths.displayPath(context, resolvedVersionJson.versionJsonPath()),
                "metadataSource",
                resolvedVersionJson.metadataSource(),
                "serverJarSource",
                artifactResolution.serverJarSource(),
                "macheReferenceScan",
                Boolean.toString(macheReferenceScan && macheDirectory != null),
                "macheReportOutputPath",
                macheReferenceScan && macheDirectory != null
                    ? DisplayPaths.displayPath(
                        context, context.workingDirectory().resolve("mache-reference-report.json"))
                    : null)));

    return new MinecraftDryRunResult(
        launchPlan,
        macheReferenceReport,
        artifactResolution.serverJarPath(),
        artifactResolution.serverJarSource(),
        artifactResolution,
        plannedRuntime,
        runtimeBoundary,
        integrationPlan,
        executionPlan);
  }

  private static List<Path> minecraftReproducibilityRuntimeJars(
      LaunchContext context, Path serverJarPath, MinecraftServerRuntimePlan runtimePlan) {
    List<Path> runtimeJars = new ArrayList<>();
    runtimeJars.add(serverJarPath);
    for (com.spindle.core.minecraft.MinecraftServerRuntimeClasspath.Entry entry :
        runtimePlan.classpathEntries()) {
      Path path = Path.of(entry.path());
      runtimeJars.add(path.isAbsolute() ? path : context.workingDirectory().resolve(path));
    }
    return runtimeJars;
  }

  private static void printMinecraftBoundaryExplain(MinecraftRuntimeBoundary boundary) {
    System.out.println("[spindle] explain-boundary: Mega-Milestone 7 boundary is analysis-only");
    System.out.println(
        "[spindle] explain-boundary: packages " + boundary.packageOwnership().size());
    System.out.println(
        "[spindle] explain-boundary: resources " + boundary.resourceOwnership().size());
    System.out.println(
        "[spindle] explain-boundary: services " + boundary.serviceProviderOwnership().size());
    System.out.println("[spindle] explain-boundary: violations " + boundary.violations().size());
    System.out.println("[spindle] explain-boundary: wrote minecraft-runtime-boundary.json");
  }

  private static void printMinecraftModsExplain(MinecraftModIntegrationPlan plan) {
    System.out.println(
        "[spindle] explain-mods: Mega-Milestone 7 integration plan is analysis-only");
    System.out.println(
        "[spindle] explain-mods: discovered "
            + plan.discoveredModCandidates().size()
            + " candidates");
    System.out.println("[spindle] explain-mods: accepted " + plan.acceptedMods().size() + " mods");
    System.out.println("[spindle] explain-mods: rejected " + plan.rejectedMods().size() + " mods");
    System.out.println(
        "[spindle] explain-mods: future classpath entries "
            + plan.modClasspathPlan().plannedFutureModClasspathEntries().size());
    System.out.println("[spindle] explain-mods: wrote minecraft-mod-integration-plan.json");
  }

  private static void printMinecraftRuntimeExplain(MinecraftServerRuntimePlan plan) {
    System.out.println(
        "[spindle] explain-runtime: Mega-Milestone 7 runtime plan "
            + plan.resolvedMinecraftVersion());
    System.out.println(
        "[spindle] explain-runtime: selector "
            + plan.selectorUsed()
            + " via "
            + plan.selectorResolutionReason());
    System.out.println(
        "[spindle] explain-runtime: server artifact "
            + plan.serverJarSource()
            + " "
            + plan.serverJarPath());
    System.out.println(
        "[spindle] explain-runtime: launch mode "
            + plan.launchMode()
            + " because "
            + plan.launchModeReason());
    System.out.println(
        "[spindle] explain-runtime: classpath entries " + plan.classpathEntries().size());
    System.out.println("[spindle] explain-runtime: wrote minecraft-server-runtime-plan.json");
  }
}
