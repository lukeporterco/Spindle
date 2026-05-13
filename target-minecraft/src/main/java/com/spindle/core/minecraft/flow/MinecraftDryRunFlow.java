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
import com.spindle.core.minecraft.command.MinecraftCommandDispatcherBindingAnalysis;
import com.spindle.core.minecraft.command.MinecraftCommandDispatcherBindingAnalysisWriter;
import com.spindle.core.minecraft.command.MinecraftCommandDispatcherBindingAnalyzer;
import com.spindle.core.minecraft.command.MinecraftCommandDispatcherSymbolAnalysis;
import com.spindle.core.minecraft.command.MinecraftCommandDispatcherSymbolAnalysisWriter;
import com.spindle.core.minecraft.command.MinecraftCommandDispatcherSymbolAnalyzer;
import com.spindle.core.minecraft.command.MinecraftCommandRegistrationAnalysis;
import com.spindle.core.minecraft.command.MinecraftCommandRegistrationAnalysisWriter;
import com.spindle.core.minecraft.command.MinecraftCommandRegistrationAnalyzer;
import com.spindle.core.minecraft.concept.MinecraftTargetConceptCatalog;
import com.spindle.core.minecraft.hook.MinecraftHookContractCatalogProvider;
import com.spindle.core.minecraft.hook.MinecraftHookContractReport;
import com.spindle.core.minecraft.hook.MinecraftHookContractReportWriter;
import com.spindle.core.minecraft.hook.MinecraftHookContractValidator;
import com.spindle.core.minecraft.hook.bytecode.MinecraftHookBytecodeAnalysisReport;
import com.spindle.core.minecraft.hook.bytecode.MinecraftHookBytecodeAnalysisWriter;
import com.spindle.core.minecraft.hook.bytecode.MinecraftHookBytecodeAnalyzer;
import com.spindle.core.minecraft.hook.install.MinecraftHookInstallationPlan;
import com.spindle.core.minecraft.hook.install.MinecraftHookInstallationPlanWriter;
import com.spindle.core.minecraft.hook.install.MinecraftHookInstallationPlanner;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlan;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlanWriter;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlanner;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementPlan;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementPlanWriter;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementPlanner;
import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretation;
import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretationWriter;
import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpreter;
import com.spindle.core.minecraft.lifecycle.MinecraftServerLifecycleBindingAnalyzer;
import com.spindle.core.minecraft.lifecycle.MinecraftServerLifecycleBindingReport;
import com.spindle.core.minecraft.lifecycle.MinecraftServerLifecycleBindingReportWriter;
import com.spindle.core.minecraft.lifecycle.MinecraftServerLifecycleDispatchPlan;
import com.spindle.core.minecraft.lifecycle.MinecraftServerLifecycleDispatchPlanWriter;
import com.spindle.core.minecraft.lifecycle.MinecraftServerLifecycleDispatchPlanner;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadAnalysis;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadAnalysisWriter;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadAnalyzer;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadSymbolAnalysis;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadSymbolAnalysisWriter;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadSymbolAnalyzer;
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

    if ((config.interpretArtifact()
            || config.hookContracts()
            || config.explainHookContracts()
            || config.serverLifecycleBindings()
            || config.explainServerLifecycleBindings()
            || config.serverLifecycleDispatchPlan()
            || config.explainServerLifecycleDispatchPlan()
            || config.resourceReloadAnalysis()
            || config.explainResourceReloadAnalysis()
            || config.resourceReloadSymbolAnalysis()
            || config.explainResourceReloadSymbolAnalysis()
            || config.commandRegistrationAnalysis()
            || config.explainCommandRegistrationAnalysis()
            || config.commandDispatcherSymbolAnalysis()
            || config.explainCommandDispatcherSymbolAnalysis()
            || config.commandDispatcherBindingAnalysis()
            || config.explainCommandDispatcherBindingAnalysis()
            || config.hookPlacementPlan()
            || config.explainHookPlacement()
            || config.hookBytecodeAnalysis()
            || config.explainHookBytecodeAnalysis()
            || config.hookPatchPlan()
            || config.bootstrapTransformHooks()
            || config.explainHookPatchPlan()
            || config.hookInstallationPlan()
            || config.installHooks())
        && config.side() != MinecraftSide.SERVER) {
      throw new LoaderException(
          "Minecraft artifact interpretation, hook contract diagnostics, server lifecycle binding analysis, and hook installation planning currently support the server-side Minecraft runtime only.");
    }

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
    MinecraftHookContractReport hookContractReport = null;
    List<String> megaMilestoneReports = new ArrayList<>();
    boolean needsRuntimePlanning =
        config.side() == MinecraftSide.SERVER
            && artifactResolution.serverJarPath() != null
            && (config.runtimePlan()
                || config.planMods()
                || config.boundaryReport()
                || config.interpretArtifact()
                || config.hookContracts()
                || config.explainHookContracts()
                || config.serverLifecycleBindings()
                || config.explainServerLifecycleBindings()
                || config.serverLifecycleDispatchPlan()
                || config.explainServerLifecycleDispatchPlan()
                || config.resourceReloadAnalysis()
                || config.explainResourceReloadAnalysis()
                || config.resourceReloadSymbolAnalysis()
                || config.explainResourceReloadSymbolAnalysis()
                || config.commandRegistrationAnalysis()
                || config.explainCommandRegistrationAnalysis()
                || config.commandDispatcherSymbolAnalysis()
                || config.explainCommandDispatcherSymbolAnalysis()
                || config.commandDispatcherBindingAnalysis()
                || config.explainCommandDispatcherBindingAnalysis()
                || config.hookPlacementPlan()
                || config.explainHookPlacement()
                || config.hookBytecodeAnalysis()
                || config.explainHookBytecodeAnalysis()
                || config.hookPatchPlan()
                || config.bootstrapTransformHooks()
                || config.explainHookPatchPlan()
                || config.hookInstallationPlan()
                || config.installHooks()
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
      MinecraftArtifactInterpretation interpretation = null;
      boolean shouldCreateInterpretation =
          config.interpretArtifact()
              || config.hookContracts()
              || config.explainHookContracts()
              || config.hookPlacementPlan()
              || config.explainHookPlacement()
              || config.hookBytecodeAnalysis()
              || config.explainHookBytecodeAnalysis()
              || config.hookPatchPlan()
              || config.bootstrapTransformHooks()
              || config.explainHookPatchPlan()
              || config.hookInstallationPlan()
              || config.installHooks();
      if (shouldCreateInterpretation) {
        interpretation =
            DiagnosticMeasurements.measure(
                diagnosticSink,
                "minecraft.artifact_interpretation.create",
                LaunchPhase.COMPLETE,
                () ->
                    new MinecraftArtifactInterpreter()
                        .interpret(
                            metadata.id(),
                            config.side(),
                            minecraftArtifactInterpretationInputs(
                                context, finalPlannedRuntime.plan())),
                report ->
                    DiagnosticMeasurements.details(
                        "jarCount",
                        Integer.toString(report.jars().size()),
                        "packageCount",
                        Integer.toString(report.packageCount()),
                        "classCount",
                        Integer.toString(report.classCount()),
                        "methodCount",
                        Integer.toString(report.methodCount())));
        MinecraftArtifactInterpretation finalInterpretation = interpretation;
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "minecraft.artifact_interpretation.write",
            LaunchPhase.COMPLETE,
            () -> {
              Path outputPath =
                  context.workingDirectory().resolve("minecraft-artifact-interpretation.json");
              new MinecraftArtifactInterpretationWriter().write(outputPath, finalInterpretation);
              return outputPath;
            },
            outputPath ->
                DiagnosticMeasurements.details(
                    "artifactInterpretationOutputPath",
                    DisplayPaths.displayPath(context, outputPath)));
        megaMilestoneReports.add("minecraft-artifact-interpretation.json");
        if (config.explainInterpretation()) {
          printMinecraftArtifactInterpretationExplain(interpretation);
        }
      }
      if (config.hookContracts()
          || config.explainHookContracts()
          || config.serverLifecycleBindings()
          || config.explainServerLifecycleBindings()
          || config.serverLifecycleDispatchPlan()
          || config.explainServerLifecycleDispatchPlan()
          || config.resourceReloadAnalysis()
          || config.explainResourceReloadAnalysis()
          || config.resourceReloadSymbolAnalysis()
          || config.explainResourceReloadSymbolAnalysis()
          || config.commandRegistrationAnalysis()
          || config.explainCommandRegistrationAnalysis()
          || config.commandDispatcherSymbolAnalysis()
          || config.explainCommandDispatcherSymbolAnalysis()
          || config.commandDispatcherBindingAnalysis()
          || config.explainCommandDispatcherBindingAnalysis()
          || config.hookPlacementPlan()
          || config.explainHookPlacement()
          || config.hookBytecodeAnalysis()
          || config.explainHookBytecodeAnalysis()
          || config.hookPatchPlan()
          || config.explainHookPatchPlan()
          || config.hookInstallationPlan()
          || config.installHooks()) {
        MinecraftArtifactInterpretation finalInterpretation =
            interpretation == null
                ? DiagnosticMeasurements.measure(
                    diagnosticSink,
                    "minecraft.artifact_interpretation.create",
                    LaunchPhase.COMPLETE,
                    () ->
                        new MinecraftArtifactInterpreter()
                            .interpret(
                                metadata.id(),
                                config.side(),
                                minecraftArtifactInterpretationInputs(
                                    context, finalPlannedRuntime.plan())),
                    report ->
                        DiagnosticMeasurements.details(
                            "jarCount",
                            Integer.toString(report.jars().size()),
                            "packageCount",
                            Integer.toString(report.packageCount()),
                            "classCount",
                            Integer.toString(report.classCount()),
                            "methodCount",
                            Integer.toString(report.methodCount())))
                : interpretation;
        hookContractReport =
            DiagnosticMeasurements.measure(
                diagnosticSink,
                "minecraft.hook_contracts.validate",
                LaunchPhase.COMPLETE,
                () ->
                    new MinecraftHookContractValidator()
                        .validate(
                            finalInterpretation,
                            new MinecraftHookContractCatalogProvider()
                                .catalogFor(metadata.id(), config.side())),
                report ->
                    DiagnosticMeasurements.details(
                        "contractCount",
                        Integer.toString(report.contractCount()),
                        "validContractCount",
                        Integer.toString(report.validContractCount()),
                        "warningCount",
                        Integer.toString(report.warningCount()),
                        "errorCount",
                        Integer.toString(report.errorCount())));
        MinecraftHookContractReport finalHookContractReportForWrite = hookContractReport;
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "minecraft.hook_contracts.write",
            LaunchPhase.COMPLETE,
            () -> {
              Path outputPath = context.workingDirectory().resolve("minecraft-hook-contracts.json");
              new MinecraftHookContractReportWriter()
                  .write(outputPath, finalHookContractReportForWrite);
              return outputPath;
            },
            outputPath ->
                DiagnosticMeasurements.details(
                    "hookContractOutputPath", DisplayPaths.displayPath(context, outputPath)));
        megaMilestoneReports.add("minecraft-hook-contracts.json");
        if (config.explainHookContracts()) {
          printMinecraftHookContractsExplain(hookContractReport);
        }
        if (config.serverLifecycleBindings()
            || config.explainServerLifecycleBindings()
            || config.serverLifecycleDispatchPlan()
            || config.explainServerLifecycleDispatchPlan()
            || config.commandRegistrationAnalysis()
            || config.explainCommandRegistrationAnalysis()
            || config.commandDispatcherSymbolAnalysis()
            || config.explainCommandDispatcherSymbolAnalysis()
            || config.commandDispatcherBindingAnalysis()
            || config.explainCommandDispatcherBindingAnalysis()) {
          MinecraftHookContractReport finalHookContractReportForLifecycle = hookContractReport;
          MinecraftServerLifecycleBindingReport lifecycleBindingReport =
              DiagnosticMeasurements.measure(
                  diagnosticSink,
                  "minecraft.server_lifecycle_bindings.analyze",
                  LaunchPhase.COMPLETE,
                  () ->
                      new MinecraftServerLifecycleBindingAnalyzer()
                          .analyze(
                              new MinecraftTargetConceptCatalog(),
                              finalHookContractReportForLifecycle),
                  report ->
                      DiagnosticMeasurements.details(
                          "gatePassed",
                          Boolean.toString(report.gatePassed()),
                          "boundPhaseCount",
                          Integer.toString(report.boundPhaseCount()),
                          "unboundPhaseCount",
                          Integer.toString(report.unboundPhaseCount())));
          DiagnosticMeasurements.measure(
              diagnosticSink,
              "minecraft.server_lifecycle_bindings.write",
              LaunchPhase.COMPLETE,
              () -> {
                Path outputPath =
                    context.workingDirectory().resolve("minecraft-server-lifecycle-bindings.json");
                new MinecraftServerLifecycleBindingReportWriter()
                    .write(outputPath, lifecycleBindingReport);
                return outputPath;
              },
              outputPath ->
                  DiagnosticMeasurements.details(
                      "serverLifecycleBindingOutputPath",
                      DisplayPaths.displayPath(context, outputPath)));
          megaMilestoneReports.add("minecraft-server-lifecycle-bindings.json");
          if (config.explainServerLifecycleBindings()) {
            printMinecraftServerLifecycleBindingsExplain(lifecycleBindingReport);
          }
          if (config.serverLifecycleDispatchPlan() || config.explainServerLifecycleDispatchPlan()) {
            MinecraftServerLifecycleDispatchPlan dispatchPlan =
                DiagnosticMeasurements.measure(
                    diagnosticSink,
                    "minecraft.server_lifecycle_dispatch_plan.plan",
                    LaunchPhase.COMPLETE,
                    () ->
                        new MinecraftServerLifecycleDispatchPlanner().plan(lifecycleBindingReport),
                    report ->
                        DiagnosticMeasurements.details(
                            "gatePassed",
                            Boolean.toString(report.gatePassed()),
                            "plannedDispatchCount",
                            Integer.toString(report.plannedDispatchCount()),
                            "blockedDispatchCount",
                            Integer.toString(report.blockedDispatchCount()),
                            "unsupportedDispatchCount",
                            Integer.toString(report.unsupportedDispatchCount())));
            DiagnosticMeasurements.measure(
                diagnosticSink,
                "minecraft.server_lifecycle_dispatch_plan.write",
                LaunchPhase.COMPLETE,
                () -> {
                  Path outputPath =
                      context
                          .workingDirectory()
                          .resolve("minecraft-server-lifecycle-dispatch-plan.json");
                  new MinecraftServerLifecycleDispatchPlanWriter().write(outputPath, dispatchPlan);
                  return outputPath;
                },
                outputPath ->
                    DiagnosticMeasurements.details(
                        "serverLifecycleDispatchPlanOutputPath",
                        DisplayPaths.displayPath(context, outputPath)));
            megaMilestoneReports.add("minecraft-server-lifecycle-dispatch-plan.json");
            if (config.explainServerLifecycleDispatchPlan()) {
              printMinecraftServerLifecycleDispatchPlanExplain(dispatchPlan);
            }
            if (config.resourceReloadAnalysis() || config.explainResourceReloadAnalysis()) {
              MinecraftResourceReloadAnalysis resourceReloadAnalysis =
                  DiagnosticMeasurements.measure(
                      diagnosticSink,
                      "minecraft.resource_reload_analysis.analyze",
                      LaunchPhase.COMPLETE,
                      () ->
                          new MinecraftResourceReloadAnalyzer()
                              .analyze(new MinecraftTargetConceptCatalog(), dispatchPlan),
                      analysis ->
                          DiagnosticMeasurements.details(
                              "gatePassed",
                              Boolean.toString(analysis.gatePassed()),
                              "availableBoundaryCount",
                              Integer.toString(analysis.availableBoundaryCount()),
                              "declaredUnboundBoundaryCount",
                              Integer.toString(analysis.declaredUnboundBoundaryCount()),
                              "upstreamBlockedBoundaryCount",
                              Integer.toString(analysis.upstreamBlockedBoundaryCount())));
              DiagnosticMeasurements.measure(
                  diagnosticSink,
                  "minecraft.resource_reload_analysis.write",
                  LaunchPhase.COMPLETE,
                  () -> {
                    Path outputPath =
                        context
                            .workingDirectory()
                            .resolve("minecraft-resource-reload-analysis.json");
                    new MinecraftResourceReloadAnalysisWriter()
                        .write(outputPath, resourceReloadAnalysis);
                    return outputPath;
                  },
                  outputPath ->
                      DiagnosticMeasurements.details(
                          "resourceReloadAnalysisOutputPath",
                          DisplayPaths.displayPath(context, outputPath)));
              megaMilestoneReports.add("minecraft-resource-reload-analysis.json");
              if (config.explainResourceReloadAnalysis()) {
                printMinecraftResourceReloadAnalysisExplain(resourceReloadAnalysis);
              }
              if (config.resourceReloadSymbolAnalysis()
                  || config.explainResourceReloadSymbolAnalysis()) {
                MinecraftArtifactInterpretation resourceReloadSymbolInterpretation = interpretation;
                MinecraftResourceReloadSymbolAnalysis resourceReloadSymbolAnalysis =
                    DiagnosticMeasurements.measure(
                        diagnosticSink,
                        "minecraft.resource_reload_symbol_analysis.analyze",
                        LaunchPhase.COMPLETE,
                        () ->
                            new MinecraftResourceReloadSymbolAnalyzer()
                                .analyze(
                                    resourceReloadSymbolInterpretation, resourceReloadAnalysis),
                        analysis ->
                            DiagnosticMeasurements.details(
                                "gatePassed",
                                Boolean.toString(analysis.gatePassed()),
                                "candidateCount",
                                Integer.toString(analysis.candidateCount()),
                                "selectableCandidateCount",
                                Integer.toString(analysis.selectableCandidateCount()),
                                "discoveryStatus",
                                analysis.discoveryStatus().name()));
                DiagnosticMeasurements.measure(
                    diagnosticSink,
                    "minecraft.resource_reload_symbol_analysis.write",
                    LaunchPhase.COMPLETE,
                    () -> {
                      Path outputPath =
                          context
                              .workingDirectory()
                              .resolve("minecraft-resource-reload-symbol-analysis.json");
                      new MinecraftResourceReloadSymbolAnalysisWriter()
                          .write(outputPath, resourceReloadSymbolAnalysis);
                      return outputPath;
                    },
                    outputPath ->
                        DiagnosticMeasurements.details(
                            "resourceReloadSymbolAnalysisOutputPath",
                            DisplayPaths.displayPath(context, outputPath)));
                megaMilestoneReports.add("minecraft-resource-reload-symbol-analysis.json");
                if (config.explainResourceReloadSymbolAnalysis()) {
                  printMinecraftResourceReloadSymbolAnalysisExplain(resourceReloadSymbolAnalysis);
                }
              }
            }
            if (config.commandRegistrationAnalysis()
                || config.explainCommandRegistrationAnalysis()) {
              MinecraftCommandRegistrationAnalysis commandRegistrationAnalysis =
                  DiagnosticMeasurements.measure(
                      diagnosticSink,
                      "minecraft.command_registration_analysis.analyze",
                      LaunchPhase.COMPLETE,
                      () ->
                          new MinecraftCommandRegistrationAnalyzer()
                              .analyze(new MinecraftTargetConceptCatalog(), dispatchPlan),
                      analysis ->
                          DiagnosticMeasurements.details(
                              "gatePassed",
                              Boolean.toString(analysis.gatePassed()),
                              "anchoredBoundaryCount",
                              Integer.toString(analysis.anchoredBoundaryCount()),
                              "unboundBoundaryCount",
                              Integer.toString(analysis.unboundBoundaryCount()),
                              "blockedBoundaryCount",
                              Integer.toString(analysis.blockedBoundaryCount())));
              DiagnosticMeasurements.measure(
                  diagnosticSink,
                  "minecraft.command_registration_analysis.write",
                  LaunchPhase.COMPLETE,
                  () -> {
                    Path outputPath =
                        context
                            .workingDirectory()
                            .resolve("minecraft-command-registration-analysis.json");
                    new MinecraftCommandRegistrationAnalysisWriter()
                        .write(outputPath, commandRegistrationAnalysis);
                    return outputPath;
                  },
                  outputPath ->
                      DiagnosticMeasurements.details(
                          "commandRegistrationAnalysisOutputPath",
                          DisplayPaths.displayPath(context, outputPath)));
              megaMilestoneReports.add("minecraft-command-registration-analysis.json");
              if (config.explainCommandRegistrationAnalysis()) {
                printMinecraftCommandRegistrationAnalysisExplain(commandRegistrationAnalysis);
              }
              if (config.commandDispatcherSymbolAnalysis()
                  || config.explainCommandDispatcherSymbolAnalysis()
                  || config.commandDispatcherBindingAnalysis()
                  || config.explainCommandDispatcherBindingAnalysis()) {
                MinecraftArtifactInterpretation symbolAnalysisInterpretation = interpretation;
                MinecraftCommandDispatcherSymbolAnalysis commandDispatcherSymbolAnalysis =
                    DiagnosticMeasurements.measure(
                        diagnosticSink,
                        "minecraft.command_dispatcher_symbol_analysis.analyze",
                        LaunchPhase.COMPLETE,
                        () ->
                            new MinecraftCommandDispatcherSymbolAnalyzer()
                                .analyze(symbolAnalysisInterpretation, commandRegistrationAnalysis),
                        analysis ->
                            DiagnosticMeasurements.details(
                                "gatePassed",
                                Boolean.toString(analysis.gatePassed()),
                                "candidateCount",
                                Integer.toString(analysis.candidateCount()),
                                "selectableCandidateCount",
                                Integer.toString(analysis.selectableCandidateCount()),
                                "selectionStatus",
                                analysis.selectionStatus().name()));
                DiagnosticMeasurements.measure(
                    diagnosticSink,
                    "minecraft.command_dispatcher_symbol_analysis.write",
                    LaunchPhase.COMPLETE,
                    () -> {
                      Path outputPath =
                          context
                              .workingDirectory()
                              .resolve("minecraft-command-dispatcher-symbol-analysis.json");
                      new MinecraftCommandDispatcherSymbolAnalysisWriter()
                          .write(outputPath, commandDispatcherSymbolAnalysis);
                      return outputPath;
                    },
                    outputPath ->
                        DiagnosticMeasurements.details(
                            "commandDispatcherSymbolAnalysisOutputPath",
                            DisplayPaths.displayPath(context, outputPath)));
                megaMilestoneReports.add("minecraft-command-dispatcher-symbol-analysis.json");
                if (config.explainCommandDispatcherSymbolAnalysis()) {
                  printMinecraftCommandDispatcherSymbolAnalysisExplain(
                      commandDispatcherSymbolAnalysis);
                }
                if (config.commandDispatcherBindingAnalysis()
                    || config.explainCommandDispatcherBindingAnalysis()) {
                  MinecraftCommandDispatcherBindingAnalysis commandDispatcherBindingAnalysis =
                      DiagnosticMeasurements.measure(
                          diagnosticSink,
                          "minecraft.command_dispatcher_binding_analysis.analyze",
                          LaunchPhase.COMPLETE,
                          () ->
                              new MinecraftCommandDispatcherBindingAnalyzer()
                                  .analyze(commandDispatcherSymbolAnalysis),
                          analysis ->
                              DiagnosticMeasurements.details(
                                  "gatePassed",
                                  Boolean.toString(analysis.gatePassed()),
                                  "bindingStatus",
                                  analysis.bindingStatus().name(),
                                  "accessStrategy",
                                  analysis.accessStrategy().name(),
                                  "minimalCommandRegistrationProofRecommended",
                                  Boolean.toString(
                                      analysis.minimalCommandRegistrationProofRecommended())));
                  DiagnosticMeasurements.measure(
                      diagnosticSink,
                      "minecraft.command_dispatcher_binding_analysis.write",
                      LaunchPhase.COMPLETE,
                      () -> {
                        Path outputPath =
                            context
                                .workingDirectory()
                                .resolve("minecraft-command-dispatcher-binding-analysis.json");
                        new MinecraftCommandDispatcherBindingAnalysisWriter()
                            .write(outputPath, commandDispatcherBindingAnalysis);
                        return outputPath;
                      },
                      outputPath ->
                          DiagnosticMeasurements.details(
                              "commandDispatcherBindingAnalysisOutputPath",
                              DisplayPaths.displayPath(context, outputPath)));
                  megaMilestoneReports.add("minecraft-command-dispatcher-binding-analysis.json");
                  if (config.explainCommandDispatcherBindingAnalysis()) {
                    printMinecraftCommandDispatcherBindingAnalysisExplain(
                        commandDispatcherBindingAnalysis);
                  }
                }
              }
            }
          }
        }
      }
      if (config.explainRuntime()) {
        printMinecraftRuntimeExplain(finalPlannedRuntime.plan());
      }

      if (config.boundaryReport()
          || config.planMods()
          || config.integrationPlan()
          || config.hookPlacementPlan()
          || config.hookBytecodeAnalysis()
          || config.hookPatchPlan()
          || config.hookInstallationPlan()
          || config.installHooks()
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
              || config.hookInstallationPlan()
              || config.installHooks()
              || config.preflight()
              || config.reproducibilityCheck()
              || config.executionPlan()
              || config.hookPlacementPlan()
              || config.hookBytecodeAnalysis()
              || config.hookPatchPlan()
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
              || config.hookInstallationPlan()
              || config.hookPlacementPlan()
              || config.hookBytecodeAnalysis()
              || config.hookPatchPlan()
              || config.installHooks()
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

        if (config.hookPlacementPlan()
            || config.hookBytecodeAnalysis()
            || config.hookPatchPlan()
            || config.bootstrapTransformHooks()) {
          MinecraftHookContractReport finalHookContractReport = hookContractReport;
          MinecraftHookPlacementPlan hookPlacementPlan =
              DiagnosticMeasurements.measure(
                  diagnosticSink,
                  "minecraft.hook_placement.plan",
                  LaunchPhase.COMPLETE,
                  () ->
                      new MinecraftHookPlacementPlanner()
                          .plan(
                              finalHookContractReport,
                              finalExecutionPlan,
                              minecraftHookPlacementRuntimePlan(
                                  context, executionPlannedRuntime.plan())),
                  plan ->
                      DiagnosticMeasurements.details(
                          "gatePassed",
                          Boolean.toString(plan.gatePassed()),
                          "placementPlanned",
                          Boolean.toString(plan.placementPlanned()),
                          "plannedPlacementCount",
                          Integer.toString(plan.plannedPlacementCount())));
          DiagnosticMeasurements.measure(
              diagnosticSink,
              "minecraft.hook_placement_plan.write",
              LaunchPhase.COMPLETE,
              () -> {
                Path outputPath =
                    context.workingDirectory().resolve("minecraft-hook-placement-plan.json");
                new MinecraftHookPlacementPlanWriter().write(outputPath, hookPlacementPlan);
                return outputPath;
              },
              outputPath ->
                  DiagnosticMeasurements.details(
                      "hookPlacementPlanOutputPath",
                      DisplayPaths.displayPath(context, outputPath)));
          megaMilestoneReports.add("minecraft-hook-placement-plan.json");
          if (config.explainHookPlacement()) {
            printMinecraftHookPlacementExplain(hookPlacementPlan);
          }

          MinecraftHookBytecodeAnalysisReport hookBytecodeAnalysisReport = null;
          if (config.hookBytecodeAnalysis()
              || config.hookPatchPlan()
              || config.bootstrapTransformHooks()) {
            hookBytecodeAnalysisReport =
                DiagnosticMeasurements.measure(
                    diagnosticSink,
                    "minecraft.hook_bytecode.analysis",
                    LaunchPhase.COMPLETE,
                    () ->
                        new MinecraftHookBytecodeAnalyzer()
                            .analyze(
                                hookPlacementPlan,
                                finalExecutionPlan,
                                minecraftHookPlacementRuntimePlan(
                                    context, executionPlannedRuntime.plan())),
                    report ->
                        DiagnosticMeasurements.details(
                            "gatePassed",
                            Boolean.toString(report.gatePassed()),
                            "bytecodeAnalysisSucceeded",
                            Boolean.toString(report.bytecodeAnalysisSucceeded()),
                            "instructionCount",
                            Integer.toString(report.instructionCount())));
            MinecraftHookBytecodeAnalysisReport finalHookBytecodeAnalysisReport =
                hookBytecodeAnalysisReport;
            DiagnosticMeasurements.measure(
                diagnosticSink,
                "minecraft.hook_bytecode_analysis.write",
                LaunchPhase.COMPLETE,
                () -> {
                  Path outputPath =
                      context.workingDirectory().resolve("minecraft-hook-bytecode-analysis.json");
                  new MinecraftHookBytecodeAnalysisWriter()
                      .write(outputPath, finalHookBytecodeAnalysisReport);
                  return outputPath;
                },
                outputPath ->
                    DiagnosticMeasurements.details(
                        "hookBytecodeAnalysisOutputPath",
                        DisplayPaths.displayPath(context, outputPath)));
            megaMilestoneReports.add("minecraft-hook-bytecode-analysis.json");
            if (config.explainHookBytecodeAnalysis()) {
              printMinecraftHookBytecodeAnalysisExplain(finalHookBytecodeAnalysisReport);
            }
          }

          if (config.hookPatchPlan() || config.bootstrapTransformHooks()) {
            MinecraftHookBytecodeAnalysisReport finalHookBytecodeAnalysisReport =
                hookBytecodeAnalysisReport;
            MinecraftHookPatchPlan hookPatchPlan =
                DiagnosticMeasurements.measure(
                    diagnosticSink,
                    "minecraft.hook_patch.plan",
                    LaunchPhase.COMPLETE,
                    () ->
                        new MinecraftHookPatchPlanner()
                            .plan(
                                finalHookBytecodeAnalysisReport,
                                hookPlacementPlan,
                                finalExecutionPlan,
                                minecraftHookPlacementRuntimePlan(
                                    context, executionPlannedRuntime.plan())),
                    plan ->
                        DiagnosticMeasurements.details(
                            "gatePassed",
                            Boolean.toString(plan.gatePassed()),
                            "patchPlanned",
                            Boolean.toString(plan.patchPlanned()),
                            "plannedPatchCount",
                            Integer.toString(plan.plannedPatchCount())));
            DiagnosticMeasurements.measure(
                diagnosticSink,
                "minecraft.hook_patch_plan.write",
                LaunchPhase.COMPLETE,
                () -> {
                  Path outputPath =
                      context.workingDirectory().resolve("minecraft-hook-patch-plan.json");
                  new MinecraftHookPatchPlanWriter().write(outputPath, hookPatchPlan);
                  return outputPath;
                },
                outputPath ->
                    DiagnosticMeasurements.details(
                        "hookPatchPlanOutputPath", DisplayPaths.displayPath(context, outputPath)));
            megaMilestoneReports.add("minecraft-hook-patch-plan.json");
            if (config.explainHookPatchPlan()) {
              printMinecraftHookPatchPlanExplain(hookPatchPlan);
            }
          }
        }

        if (config.hookInstallationPlan() || config.installHooks()) {
          MinecraftHookContractReport finalHookContractReport = hookContractReport;
          MinecraftHookInstallationPlan hookInstallationPlan =
              DiagnosticMeasurements.measure(
                  diagnosticSink,
                  "minecraft.hook_installation.plan",
                  LaunchPhase.COMPLETE,
                  () ->
                      new MinecraftHookInstallationPlanner()
                          .plan(finalHookContractReport, finalExecutionPlan),
                  plan ->
                      DiagnosticMeasurements.details(
                          "gatePassed",
                          Boolean.toString(plan.gatePassed()),
                          "installationPlanned",
                          Boolean.toString(plan.installationPlanned()),
                          "plannedHookCount",
                          Integer.toString(plan.plannedHookCount())));
          DiagnosticMeasurements.measure(
              diagnosticSink,
              "minecraft.hook_installation_plan.write",
              LaunchPhase.COMPLETE,
              () -> {
                Path outputPath =
                    context.workingDirectory().resolve("minecraft-hook-installation-plan.json");
                new MinecraftHookInstallationPlanWriter().write(outputPath, hookInstallationPlan);
                return outputPath;
              },
              outputPath ->
                  DiagnosticMeasurements.details(
                      "hookInstallationPlanOutputPath",
                      DisplayPaths.displayPath(context, outputPath)));
          megaMilestoneReports.add("minecraft-hook-installation-plan.json");
          if (config.installHooks() && !hookInstallationPlan.gatePassed()) {
            throw new LoaderException(
                "Minecraft hook installation gate failed. See minecraft-hook-installation-plan.json and minecraft-hook-contracts.json for details.");
          }
        }

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

  private static List<MinecraftArtifactInterpreter.JarInput> minecraftArtifactInterpretationInputs(
      LaunchContext context, MinecraftServerRuntimePlan runtimePlan) {
    List<MinecraftArtifactInterpreter.JarInput> inputs = new ArrayList<>();
    for (com.spindle.core.minecraft.MinecraftServerRuntimeClasspath.Entry entry :
        runtimePlan.classpathEntries()) {
      Path path = Path.of(entry.path());
      Path resolvedPath = path.isAbsolute() ? path : context.workingDirectory().resolve(path);
      inputs.add(
          MinecraftArtifactInterpreter.JarInput.of(
              resolvedPath, entry.path(), entry.ownership(), entry.origin(), entry.sha256()));
    }
    return List.copyOf(inputs);
  }

  private static MinecraftServerRuntimePlan minecraftHookPlacementRuntimePlan(
      LaunchContext context, MinecraftServerRuntimePlan runtimePlan) {
    return new MinecraftServerRuntimePlan(
        runtimePlan.schema(),
        runtimePlan.milestoneName(),
        runtimePlan.projectJavaBaseline(),
        runtimePlan.projectTargetMinecraft(),
        runtimePlan.resolvedMinecraftVersion(),
        runtimePlan.selectorUsed(),
        runtimePlan.selectorResolutionReason(),
        runtimePlan.manifestSource(),
        runtimePlan.versionJsonSource(),
        absoluteDisplayPath(context, runtimePlan.serverJarPath()),
        runtimePlan.serverJarSource(),
        runtimePlan.serverJarSha1(),
        runtimePlan.serverJarSha256(),
        runtimePlan.serverJarSize(),
        runtimePlan.launchMode(),
        runtimePlan.launchModeReason(),
        runtimePlan.mainClass(),
        runtimePlan.classpathEntries().stream()
            .map(
                entry ->
                    new com.spindle.core.minecraft.MinecraftServerRuntimeClasspath.Entry(
                        absoluteDisplayPath(context, entry.path()),
                        entry.ownership(),
                        entry.origin(),
                        entry.sha256()))
            .toList(),
        runtimePlan.bundledRuntimeFiles(),
        runtimePlan.jvmArgs(),
        runtimePlan.serverArgs(),
        runtimePlan.workingDirectory(),
        runtimePlan.javaExecutable(),
        runtimePlan.commandPreview(),
        runtimePlan.cacheDirectory(),
        runtimePlan.runtimeCacheDirectory(),
        runtimePlan.offline(),
        runtimePlan.strict(),
        runtimePlan.networkRequestCount(),
        runtimePlan.generatedFromCacheOnly(),
        runtimePlan.replayableOffline(),
        runtimePlan.modJarsOnMinecraftRuntimeClasspath(),
        runtimePlan.injectionOccurred(),
        runtimePlan.minecraftModClassesLoaded(),
        runtimePlan.minecraftModClassLoaderAttachedToMinecraft(),
        runtimePlan.minecraftEntrypointsInvoked(),
        runtimePlan.transformationsOccurred(),
        runtimePlan.remappingOccurred(),
        runtimePlan.mixinOccurred(),
        runtimePlan.patchingOccurred(),
        runtimePlan.provenance());
  }

  private static String absoluteDisplayPath(LaunchContext context, String pathValue) {
    if (pathValue == null || pathValue.isBlank()) {
      return pathValue;
    }
    Path path = Path.of(pathValue);
    Path resolved = path.isAbsolute() ? path : context.workingDirectory().resolve(path);
    return resolved.toAbsolutePath().normalize().toString().replace('\\', '/');
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

  private static void printMinecraftArtifactInterpretationExplain(
      MinecraftArtifactInterpretation interpretation) {
    System.out.println(
        "[spindle] explain-interpretation: Target-1 artifact interpretation is analysis-only");
    System.out.println("[spindle] explain-interpretation: jars " + interpretation.jars().size());
    System.out.println(
        "[spindle] explain-interpretation: packages " + interpretation.packageCount());
    System.out.println("[spindle] explain-interpretation: classes " + interpretation.classCount());
    System.out.println("[spindle] explain-interpretation: methods " + interpretation.methodCount());
    System.out.println(
        "[spindle] explain-interpretation: wrote minecraft-artifact-interpretation.json");
  }

  private static void printMinecraftHookContractsExplain(MinecraftHookContractReport report) {
    System.out.println(
        "[spindle] explain-hook-contracts: Target-3 known-symbol hook validation is analysis-only");
    System.out.println("[spindle] explain-hook-contracts: catalog " + report.catalogId());
    System.out.println("[spindle] explain-hook-contracts: contracts " + report.contractCount());
    System.out.println("[spindle] explain-hook-contracts: valid " + report.validContractCount());
    System.out.println("[spindle] explain-hook-contracts: warnings " + report.warningCount());
    System.out.println("[spindle] explain-hook-contracts: errors " + report.errorCount());
    System.out.println("[spindle] explain-hook-contracts: wrote minecraft-hook-contracts.json");
  }

  private static void printMinecraftServerLifecycleBindingsExplain(
      MinecraftServerLifecycleBindingReport report) {
    System.out.println(
        "[spindle] explain-server-lifecycle-bindings: Server lifecycle bindings: "
            + report.boundPhaseCount()
            + " bound, "
            + report.unboundPhaseCount()
            + " declared unbound.");
    if (report.gatePassed()) {
      System.out.println(
          "[spindle] explain-server-lifecycle-bindings: Starting phase: bound to minecraft.26_1_2.server.main.entrypoint.");
    } else {
      System.out.println(
          "[spindle] explain-server-lifecycle-bindings: lifecycle binding gate failed: "
              + report.gateFailureReason());
    }
    System.out.println(
        "[spindle] explain-server-lifecycle-bindings: wrote minecraft-server-lifecycle-bindings.json");
  }

  private static void printMinecraftServerLifecycleDispatchPlanExplain(
      MinecraftServerLifecycleDispatchPlan plan) {
    if (plan.gatePassed()) {
      System.out.println(
          "[spindle] explain-server-lifecycle-dispatch-plan: Server lifecycle dispatch plan: "
              + plan.plannedDispatchCount()
              + " planned, "
              + plan.unsupportedDispatchCount()
              + " unsupported.");
      System.out.println(
          "[spindle] explain-server-lifecycle-dispatch-plan: Starting dispatch: symbolic internal static dispatch before Minecraft server main.");
    } else {
      System.out.println(
          "[spindle] explain-server-lifecycle-dispatch-plan: Server lifecycle dispatch plan gate failed: "
              + plan.gateFailureReason());
    }
    System.out.println(
        "[spindle] explain-server-lifecycle-dispatch-plan: wrote minecraft-server-lifecycle-dispatch-plan.json");
  }

  private static void printMinecraftCommandRegistrationAnalysisExplain(
      MinecraftCommandRegistrationAnalysis analysis) {
    if (analysis.gatePassed()) {
      System.out.println(
          "[spindle] explain-command-registration-analysis: Command registration analysis: lifecycle anchor available, "
              + analysis.unboundBoundaryCount()
              + " command boundaries declared unbound.");
      System.out.println(
          "[spindle] explain-command-registration-analysis: Command registration is not implemented in this pass.");
    } else {
      System.out.println(
          "[spindle] explain-command-registration-analysis: Command registration analysis gate failed: "
              + analysis.gateFailureReason());
    }
    System.out.println(
        "[spindle] explain-command-registration-analysis: wrote minecraft-command-registration-analysis.json");
  }

  private static void printMinecraftResourceReloadAnalysisExplain(
      MinecraftResourceReloadAnalysis analysis) {
    if (analysis.gatePassed()) {
      System.out.println(
          "[spindle] explain-resource-reload-analysis: Target-16 resource/reload analysis is analysis-only.");
      System.out.println(
          "[spindle] explain-resource-reload-analysis: The lifecycle anchor is available only as a coarse server lifecycle anchor when Target-12 starting dispatch exists.");
      System.out.println(
          "[spindle] explain-resource-reload-analysis: Reload discovery, reload window, reload apply, datapack view, resource manager view, and future data generation remain declared unbound.");
      System.out.println(
          "[spindle] explain-resource-reload-analysis: No reload handling, resource access, datapack access, data generation, registry mutation, public API, runtime dispatch, hook installation, or transformation occurred.");
    } else {
      System.out.println(
          "[spindle] explain-resource-reload-analysis: Target-16 resource/reload analysis gate failed: "
              + analysis.gateFailureReason());
      System.out.println(
          "[spindle] explain-resource-reload-analysis: Reload discovery, reload window, reload apply, datapack view, resource manager view, and future data generation remain declared unbound.");
      System.out.println(
          "[spindle] explain-resource-reload-analysis: No reload handling, resource access, datapack access, data generation, registry mutation, public API, runtime dispatch, hook installation, or transformation occurred.");
    }
    System.out.println(
        "[spindle] explain-resource-reload-analysis: wrote minecraft-resource-reload-analysis.json");
  }

  private static void printMinecraftResourceReloadSymbolAnalysisExplain(
      MinecraftResourceReloadSymbolAnalysis analysis) {
    System.out.println(
        "[spindle] explain-resource-reload-symbol-analysis: Target-17 resource/reload symbol analysis is analysis-only.");
    System.out.println(
        "[spindle] explain-resource-reload-symbol-analysis: It scans Target-1 interpreted metadata for resource/reload-like class, method, and field symbols.");
    System.out.println(
        "[spindle] explain-resource-reload-symbol-analysis: It does not inspect bytecode instructions, mappings, decompiled source, live classes, resources, datapacks, generated JSON, registries, or command trees.");
    System.out.println(
        "[spindle] explain-resource-reload-symbol-analysis: It does not perform reload handling, resource access, datapack access, data generation, registry mutation, public API exposure, runtime dispatch, hook installation, or transformation.");
    switch (analysis.discoveryStatus()) {
      case UPSTREAM_GATE_BLOCKED ->
          System.out.println(
              "[spindle] explain-resource-reload-symbol-analysis: Resource/reload symbol discovery gate failed: "
                  + analysis.gateFailureReason());
      case NO_CANDIDATES ->
          System.out.println(
              "[spindle] explain-resource-reload-symbol-analysis: No resource/reload symbol candidates were discovered.");
      case ONLY_REJECTED_CANDIDATES ->
          System.out.println(
              "[spindle] explain-resource-reload-symbol-analysis: Only rejected resource/reload symbol candidates were discovered.");
      case CANDIDATES_DISCOVERED ->
          System.out.println(
              "[spindle] explain-resource-reload-symbol-analysis: Selectable resource/reload symbol candidates were discovered for future binding strategy analysis.");
    }
    System.out.println(
        "[spindle] explain-resource-reload-symbol-analysis: wrote minecraft-resource-reload-symbol-analysis.json");
  }

  private static void printMinecraftCommandDispatcherSymbolAnalysisExplain(
      MinecraftCommandDispatcherSymbolAnalysis analysis) {
    if (!analysis.gatePassed()) {
      System.out.println(
          "[spindle] explain-command-dispatcher-symbol-analysis: Command dispatcher symbol analysis gate failed: "
              + analysis.gateFailureReason());
    } else {
      switch (analysis.selectionStatus()) {
        case STABLE_TARGET_SELECTED ->
            System.out.println(
                "[spindle] explain-command-dispatcher-symbol-analysis: Command dispatcher symbol analysis: stable target selected. Minimal command registration proof eligible: true.");
        case NO_CANDIDATES ->
            System.out.println(
                "[spindle] explain-command-dispatcher-symbol-analysis: Command dispatcher symbol analysis: no selectable command dispatcher candidates found. Minimal command registration proof eligible: false.");
        case AMBIGUOUS_CANDIDATES ->
            System.out.println(
                "[spindle] explain-command-dispatcher-symbol-analysis: Command dispatcher symbol analysis: multiple selectable command dispatcher candidates found. Minimal command registration proof eligible: false.");
        case UPSTREAM_GATE_BLOCKED ->
            System.out.println(
                "[spindle] explain-command-dispatcher-symbol-analysis: Command dispatcher symbol analysis gate failed: "
                    + analysis.gateFailureReason());
      }
    }
    System.out.println(
        "[spindle] explain-command-dispatcher-symbol-analysis: wrote minecraft-command-dispatcher-symbol-analysis.json");
  }

  private static void printMinecraftCommandDispatcherBindingAnalysisExplain(
      MinecraftCommandDispatcherBindingAnalysis analysis) {
    switch (analysis.bindingStatus()) {
      case UPSTREAM_GATE_BLOCKED ->
          System.out.println(
              "[spindle] explain-command-dispatcher-binding-analysis: Command dispatcher binding analysis gate failed: "
                  + analysis.gateFailureReason());
      case NO_SYMBOL_TARGET ->
          System.out.println(
              "[spindle] explain-command-dispatcher-binding-analysis: Command dispatcher binding analysis: no selectable dispatcher symbol target is known. Command registration is not recommended in this pass.");
      case AMBIGUOUS_SYMBOL_TARGETS ->
          System.out.println(
              "[spindle] explain-command-dispatcher-binding-analysis: Command dispatcher binding analysis: multiple selectable dispatcher symbol targets remain. Command registration is not recommended in this pass.");
      case SELECTED_SYMBOL_ANALYZED ->
          System.out.println(
              "[spindle] explain-command-dispatcher-binding-analysis: Command dispatcher binding analysis: selected symbol requires "
                  + analysis.accessStrategy().name()
                  + ". Command registration is still not recommended in this pass because dispatcher access requires a future primitive.");
      case UNSUPPORTED_SYMBOL_KIND ->
          System.out.println(
              "[spindle] explain-command-dispatcher-binding-analysis: Command dispatcher binding analysis: selected symbol kind is unsupported for Target-15 planning.");
    }
    System.out.println(
        "[spindle] explain-command-dispatcher-binding-analysis: wrote minecraft-command-dispatcher-binding-analysis.json");
  }

  private static void printMinecraftHookPlacementExplain(MinecraftHookPlacementPlan plan) {
    System.out.println(
        "[spindle] explain-hook-placement: Target-5 hook placement analysis is analysis-only");
    System.out.println("[spindle] explain-hook-placement: catalog " + plan.catalogId());
    System.out.println("[spindle] explain-hook-placement: gatePassed " + plan.gatePassed());
    System.out.println(
        "[spindle] explain-hook-placement: planned placements " + plan.plannedPlacementCount());
    System.out.println(
        "[spindle] explain-hook-placement: wrote minecraft-hook-placement-plan.json");
  }

  private static void printMinecraftHookBytecodeAnalysisExplain(
      MinecraftHookBytecodeAnalysisReport report) {
    System.out.println(
        "[spindle] explain-hook-bytecode-analysis: Target-6 bytecode analysis is analysis-only");
    System.out.println(
        "[spindle] explain-hook-bytecode-analysis: gatePassed " + report.gatePassed());
    System.out.println(
        "[spindle] explain-hook-bytecode-analysis: instruction count " + report.instructionCount());
    System.out.println(
        "[spindle] explain-hook-bytecode-analysis: wrote minecraft-hook-bytecode-analysis.json");
  }

  private static void printMinecraftHookPatchPlanExplain(MinecraftHookPatchPlan plan) {
    System.out.println(
        "[spindle] explain-hook-patch-plan: Target-7 patch planning is analysis-only");
    System.out.println("[spindle] explain-hook-patch-plan: gatePassed " + plan.gatePassed());
    System.out.println(
        "[spindle] explain-hook-patch-plan: planned patches " + plan.plannedPatchCount());
    System.out.println("[spindle] explain-hook-patch-plan: wrote minecraft-hook-patch-plan.json");
  }
}
