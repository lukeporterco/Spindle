package com.spindle.core.minecraft;

import java.nio.file.Path;

public record MinecraftProviderConfig(
    String requestedVersion,
    Path minecraftDirectory,
    Path explicitVersionJson,
    Path manifestJson,
    MinecraftSide side,
    boolean dryRun,
    boolean verifyFiles,
    boolean fetchMetadata,
    boolean downloadServer,
    Path cacheDirectory,
    boolean offline,
    boolean cacheInspect,
    boolean cacheRepair,
    boolean cacheStrict,
    boolean forceRedownload,
    Path outputPlanPath,
    boolean launch,
    Path serverDirectory,
    boolean acceptEulaForTest,
    java.util.List<String> serverJvmArgs,
    java.util.List<String> serverArgs,
    int launchTimeoutSeconds,
    boolean stopAfterReady,
    int readyTimeoutSeconds,
    boolean baselineServer,
    String baselineVersion,
    Path baselineReportPath,
    boolean offlineReplay,
    boolean requireReady,
    boolean realSmoke,
    String manifestUrl,
    boolean runtimePlan,
    boolean planMods,
    boolean integrationPlan,
    boolean boundaryReport,
    boolean preflight,
    boolean offlinePreflight,
    boolean strictBoundary,
    boolean strictRuntimeConflicts,
    boolean strictSide,
    boolean strictClassVersions,
    boolean explainBoundary,
    boolean explainRuntime,
    boolean explainMods,
    boolean interpretArtifact,
    boolean explainInterpretation,
    boolean hookContracts,
    boolean explainHookContracts,
    boolean serverLifecycleBindings,
    boolean explainServerLifecycleBindings,
    boolean serverLifecycleDispatchPlan,
    boolean explainServerLifecycleDispatchPlan,
    boolean resourceReloadAnalysis,
    boolean explainResourceReloadAnalysis,
    boolean resourceReloadSymbolAnalysis,
    boolean explainResourceReloadSymbolAnalysis,
    boolean resourceReloadBindingAnalysis,
    boolean explainResourceReloadBindingAnalysis,
    boolean resourceVisibilityGenerationAnalysis,
    boolean explainResourceVisibilityGenerationAnalysis,
    boolean resourceReloadArcDecision,
    boolean explainResourceReloadArcDecision,
    boolean registryBootstrapAnalysis,
    boolean explainRegistryBootstrapAnalysis,
    boolean registryArcHardening,
    boolean explainRegistryArcHardening,
    boolean commandRegistrationAnalysis,
    boolean explainCommandRegistrationAnalysis,
    boolean commandDispatcherSymbolAnalysis,
    boolean explainCommandDispatcherSymbolAnalysis,
    boolean commandDispatcherBindingAnalysis,
    boolean explainCommandDispatcherBindingAnalysis,
    boolean hookPlacementPlan,
    boolean explainHookPlacement,
    boolean hookBytecodeAnalysis,
    boolean explainHookBytecodeAnalysis,
    boolean hookPatchPlan,
    boolean explainHookPatchPlan,
    boolean steelHook02PrimitiveBoundary,
    boolean explainSteelHook02PrimitiveBoundary,
    boolean steelHook02ContractGeneralization,
    boolean explainSteelHook02ContractGeneralization,
    boolean steelHook02MethodEntryTransformer,
    boolean explainSteelHook02MethodEntryTransformer,
    boolean steelHook02GatedRuntimeTransformation,
    boolean explainSteelHook02GatedRuntimeTransformation,
    boolean steelHook02CompletionCheck,
    boolean explainSteelHook02CompletionCheck,
    boolean steelHook03FramedMethodFoundation,
    boolean explainSteelHook03FramedMethodFoundation,
    boolean steelHook03MethodExitStaticDispatch,
    boolean explainSteelHook03MethodExitStaticDispatch,
    boolean steelHook03GatedRuntimeProof,
    boolean explainSteelHook03GatedRuntimeProof,
    boolean steelHook03CompletionCheck,
    boolean explainSteelHook03CompletionCheck,
    boolean steelHook04PrimitiveBoundary,
    boolean explainSteelHook04PrimitiveBoundary,
    boolean hookInstallationPlan,
    boolean reproducibilityCheck,
    boolean executionPlan,
    boolean bootstrapClassloaderGraph,
    boolean bootstrapServer,
    boolean bootstrapTransformHooks,
    boolean installHooks,
    boolean strictExecution,
    boolean denyLoaderInternals,
    boolean verifyPlanFingerprints,
    boolean bootstrapOffline,
    boolean bootstrapFakeServer,
    boolean steelHookCompletionCheck,
    boolean explainSteelHookCompletionCheck) {
  public MinecraftProviderConfig {
    side = side == null ? MinecraftSide.CLIENT : side;
    cacheDirectory =
        cacheDirectory == null ? Path.of("minecraft-cache") : cacheDirectory.normalize();
    outputPlanPath =
        outputPlanPath == null ? Path.of("minecraft-launch-plan.json") : outputPlanPath.normalize();
    baselineReportPath =
        baselineReportPath == null
            ? Path.of("minecraft-server-baseline.json")
            : baselineReportPath.normalize();
    serverJvmArgs =
        java.util.List.copyOf(serverJvmArgs == null ? java.util.List.of() : serverJvmArgs);
    serverArgs = java.util.List.copyOf(serverArgs == null ? java.util.List.of() : serverArgs);
  }

  public MinecraftProviderConfig(
      String requestedVersion,
      Path minecraftDirectory,
      Path explicitVersionJson,
      Path manifestJson,
      MinecraftSide side,
      boolean dryRun,
      boolean verifyFiles,
      boolean fetchMetadata,
      boolean downloadServer,
      Path cacheDirectory,
      boolean offline,
      boolean cacheInspect,
      boolean cacheRepair,
      boolean cacheStrict,
      boolean forceRedownload,
      Path outputPlanPath,
      boolean launch,
      Path serverDirectory,
      boolean acceptEulaForTest,
      java.util.List<String> serverJvmArgs,
      java.util.List<String> serverArgs,
      int launchTimeoutSeconds,
      boolean stopAfterReady,
      int readyTimeoutSeconds) {
    this(
        requestedVersion,
        minecraftDirectory,
        explicitVersionJson,
        manifestJson,
        side,
        dryRun,
        verifyFiles,
        fetchMetadata,
        downloadServer,
        cacheDirectory,
        offline,
        cacheInspect,
        cacheRepair,
        cacheStrict,
        forceRedownload,
        outputPlanPath,
        launch,
        serverDirectory,
        acceptEulaForTest,
        serverJvmArgs,
        serverArgs,
        launchTimeoutSeconds,
        stopAfterReady,
        readyTimeoutSeconds,
        false, // baselineServer
        null, // baselineVersion
        Path.of("minecraft-server-baseline.json"),
        false, // offlineReplay
        false, // requireReady
        false, // realSmoke
        MinecraftMetadataResolver.DEFAULT_MANIFEST_URL,
        false, // runtimePlan
        false, // planMods
        false, // integrationPlan
        false, // boundaryReport
        false, // preflight
        false, // offlinePreflight
        false, // strictBoundary
        false, // strictRuntimeConflicts
        false, // strictSide
        false, // strictClassVersions
        false, // explainBoundary
        false, // explainRuntime
        false, // explainMods
        false, // interpretArtifact
        false, // explainInterpretation
        false, // hookContracts
        false, // explainHookContracts
        false, // serverLifecycleBindings
        false, // explainServerLifecycleBindings
        false, // serverLifecycleDispatchPlan
        false, // explainServerLifecycleDispatchPlan
        false, // resourceReloadAnalysis
        false, // explainResourceReloadAnalysis
        false, // resourceReloadSymbolAnalysis
        false, // explainResourceReloadSymbolAnalysis
        false, // resourceReloadBindingAnalysis
        false, // explainResourceReloadBindingAnalysis
        false, // resourceVisibilityGenerationAnalysis
        false, // explainResourceVisibilityGenerationAnalysis
        false, // resourceReloadArcDecision
        false, // explainResourceReloadArcDecision
        false, // registryBootstrapAnalysis
        false, // explainRegistryBootstrapAnalysis
        false, // registryArcHardening
        false, // explainRegistryArcHardening
        false, // commandRegistrationAnalysis
        false, // explainCommandRegistrationAnalysis
        false, // commandDispatcherSymbolAnalysis
        false, // explainCommandDispatcherSymbolAnalysis
        false, // commandDispatcherBindingAnalysis
        false, // explainCommandDispatcherBindingAnalysis
        false, // hookPlacementPlan
        false, // explainHookPlacement
        false, // hookBytecodeAnalysis
        false, // explainHookBytecodeAnalysis
        false, // hookPatchPlan
        false, // explainHookPatchPlan
        false, // steelHook02PrimitiveBoundary
        false, // explainSteelHook02PrimitiveBoundary
        false, // steelHook02ContractGeneralization
        false, // explainSteelHook02ContractGeneralization
        false, // steelHook02MethodEntryTransformer
        false, // explainSteelHook02MethodEntryTransformer
        false, // steelHook02GatedRuntimeTransformation
        false, // explainSteelHook02GatedRuntimeTransformation
        false, // steelHook02CompletionCheck
        false, // explainSteelHook02CompletionCheck
        false, // steelHook03FramedMethodFoundation
        false, // explainSteelHook03FramedMethodFoundation
        false, // steelHook03MethodExitStaticDispatch
        false, // explainSteelHook03MethodExitStaticDispatch
        false, // steelHook03GatedRuntimeProof
        false, // explainSteelHook03GatedRuntimeProof
        false, // steelHook03CompletionCheck
        false, // explainSteelHook03CompletionCheck
        false, // steelHook04PrimitiveBoundary
        false, // explainSteelHook04PrimitiveBoundary
        false, // hookInstallationPlan
        false, // reproducibilityCheck
        false, // executionPlan
        false, // bootstrapClassloaderGraph
        false, // bootstrapServer
        false, // bootstrapTransformHooks
        false, // installHooks
        false, // strictExecution
        false, // denyLoaderInternals
        false, // verifyPlanFingerprints
        false, // bootstrapOffline
        false, // bootstrapFakeServer
        false, // steelHookCompletionCheck
        false // explainSteelHookCompletionCheck
        );
  }

  public MinecraftProviderConfig resolveAgainst(Path workingDirectory) {
    return copy(
        requestedVersion,
        resolvePath(workingDirectory, minecraftDirectory),
        resolveNullablePath(workingDirectory, explicitVersionJson),
        resolveNullablePath(workingDirectory, manifestJson),
        resolvePath(
            workingDirectory, cacheDirectory == null ? Path.of("minecraft-cache") : cacheDirectory),
        resolvePath(workingDirectory, outputPlanPath),
        resolveNullablePath(workingDirectory, serverDirectory),
        resolvePath(workingDirectory, baselineReportPath));
  }

  public boolean prefersCacheOrDownload() {
    return downloadServer || cacheRepair || forceRedownload;
  }

  public boolean baselineServerEnabled() {
    return baselineServer;
  }

  public String requestedVersionOrBaseline() {
    if (baselineServer && baselineVersion != null && !baselineVersion.isBlank()) {
      return baselineVersion;
    }
    return requestedVersion;
  }

  public MinecraftProviderConfig withMinecraftDirectory(Path updatedMinecraftDirectory) {
    return copy(
        requestedVersion,
        updatedMinecraftDirectory,
        explicitVersionJson,
        manifestJson,
        cacheDirectory,
        outputPlanPath,
        serverDirectory,
        baselineReportPath);
  }

  public MinecraftProviderConfig withRequestedVersion(String updatedRequestedVersion) {
    return copy(
        updatedRequestedVersion,
        minecraftDirectory,
        explicitVersionJson,
        manifestJson,
        cacheDirectory,
        outputPlanPath,
        serverDirectory,
        baselineReportPath);
  }

  public MinecraftProviderConfig withServerDirectory(Path updatedServerDirectory) {
    return copy(
        requestedVersion,
        minecraftDirectory,
        explicitVersionJson,
        manifestJson,
        cacheDirectory,
        outputPlanPath,
        updatedServerDirectory,
        baselineReportPath);
  }

  public MinecraftProviderConfig withBaselineVersion(String updatedBaselineVersion) {
    return new MinecraftProviderConfig(
        requestedVersion,
        minecraftDirectory,
        explicitVersionJson,
        manifestJson,
        side,
        dryRun,
        verifyFiles,
        fetchMetadata,
        downloadServer,
        cacheDirectory,
        offline,
        cacheInspect,
        cacheRepair,
        cacheStrict,
        forceRedownload,
        outputPlanPath,
        launch,
        serverDirectory,
        acceptEulaForTest,
        serverJvmArgs,
        serverArgs,
        launchTimeoutSeconds,
        stopAfterReady,
        readyTimeoutSeconds,
        baselineServer,
        updatedBaselineVersion,
        baselineReportPath,
        offlineReplay,
        requireReady,
        realSmoke,
        manifestUrl,
        runtimePlan,
        planMods,
        integrationPlan,
        boundaryReport,
        preflight,
        offlinePreflight,
        strictBoundary,
        strictRuntimeConflicts,
        strictSide,
        strictClassVersions,
        explainBoundary,
        explainRuntime,
        explainMods,
        interpretArtifact,
        explainInterpretation,
        hookContracts,
        explainHookContracts,
        serverLifecycleBindings,
        explainServerLifecycleBindings,
        serverLifecycleDispatchPlan,
        explainServerLifecycleDispatchPlan,
        resourceReloadAnalysis,
        explainResourceReloadAnalysis,
        resourceReloadSymbolAnalysis,
        explainResourceReloadSymbolAnalysis,
        resourceReloadBindingAnalysis,
        explainResourceReloadBindingAnalysis,
        resourceVisibilityGenerationAnalysis,
        explainResourceVisibilityGenerationAnalysis,
        resourceReloadArcDecision,
        explainResourceReloadArcDecision,
        registryBootstrapAnalysis,
        explainRegistryBootstrapAnalysis,
        registryArcHardening,
        explainRegistryArcHardening,
        commandRegistrationAnalysis,
        explainCommandRegistrationAnalysis,
        commandDispatcherSymbolAnalysis,
        explainCommandDispatcherSymbolAnalysis,
        commandDispatcherBindingAnalysis,
        explainCommandDispatcherBindingAnalysis,
        hookPlacementPlan,
        explainHookPlacement,
        hookBytecodeAnalysis,
        explainHookBytecodeAnalysis,
        hookPatchPlan,
        explainHookPatchPlan,
        steelHook02PrimitiveBoundary,
        explainSteelHook02PrimitiveBoundary,
        steelHook02ContractGeneralization,
        explainSteelHook02ContractGeneralization,
        steelHook02MethodEntryTransformer,
        explainSteelHook02MethodEntryTransformer,
        steelHook02GatedRuntimeTransformation,
        explainSteelHook02GatedRuntimeTransformation,
        steelHook02CompletionCheck,
        explainSteelHook02CompletionCheck,
        steelHook03FramedMethodFoundation,
        explainSteelHook03FramedMethodFoundation,
        steelHook03MethodExitStaticDispatch,
        explainSteelHook03MethodExitStaticDispatch,
        steelHook03GatedRuntimeProof,
        explainSteelHook03GatedRuntimeProof,
        steelHook03CompletionCheck,
        explainSteelHook03CompletionCheck,
        steelHook04PrimitiveBoundary,
        explainSteelHook04PrimitiveBoundary,
        hookInstallationPlan,
        reproducibilityCheck,
        executionPlan,
        bootstrapClassloaderGraph,
        bootstrapServer,
        bootstrapTransformHooks,
        installHooks,
        strictExecution,
        denyLoaderInternals,
        verifyPlanFingerprints,
        bootstrapOffline,
        bootstrapFakeServer,
        steelHookCompletionCheck,
        explainSteelHookCompletionCheck);
  }

  private MinecraftProviderConfig copy(
      String updatedRequestedVersion,
      Path updatedMinecraftDirectory,
      Path updatedExplicitVersionJson,
      Path updatedManifestJson,
      Path updatedCacheDirectory,
      Path updatedOutputPlanPath,
      Path updatedServerDirectory,
      Path updatedBaselineReportPath) {
    return new MinecraftProviderConfig(
        updatedRequestedVersion,
        updatedMinecraftDirectory,
        updatedExplicitVersionJson,
        updatedManifestJson,
        side,
        dryRun,
        verifyFiles,
        fetchMetadata,
        downloadServer,
        updatedCacheDirectory,
        offline,
        cacheInspect,
        cacheRepair,
        cacheStrict,
        forceRedownload,
        updatedOutputPlanPath,
        launch,
        updatedServerDirectory,
        acceptEulaForTest,
        serverJvmArgs,
        serverArgs,
        launchTimeoutSeconds,
        stopAfterReady,
        readyTimeoutSeconds,
        baselineServer,
        baselineVersion,
        updatedBaselineReportPath,
        offlineReplay,
        requireReady,
        realSmoke,
        manifestUrl,
        runtimePlan,
        planMods,
        integrationPlan,
        boundaryReport,
        preflight,
        offlinePreflight,
        strictBoundary,
        strictRuntimeConflicts,
        strictSide,
        strictClassVersions,
        explainBoundary,
        explainRuntime,
        explainMods,
        interpretArtifact,
        explainInterpretation,
        hookContracts,
        explainHookContracts,
        serverLifecycleBindings,
        explainServerLifecycleBindings,
        serverLifecycleDispatchPlan,
        explainServerLifecycleDispatchPlan,
        resourceReloadAnalysis,
        explainResourceReloadAnalysis,
        resourceReloadSymbolAnalysis,
        explainResourceReloadSymbolAnalysis,
        resourceReloadBindingAnalysis,
        explainResourceReloadBindingAnalysis,
        resourceVisibilityGenerationAnalysis,
        explainResourceVisibilityGenerationAnalysis,
        resourceReloadArcDecision,
        explainResourceReloadArcDecision,
        registryBootstrapAnalysis,
        explainRegistryBootstrapAnalysis,
        registryArcHardening,
        explainRegistryArcHardening,
        commandRegistrationAnalysis,
        explainCommandRegistrationAnalysis,
        commandDispatcherSymbolAnalysis,
        explainCommandDispatcherSymbolAnalysis,
        commandDispatcherBindingAnalysis,
        explainCommandDispatcherBindingAnalysis,
        hookPlacementPlan,
        explainHookPlacement,
        hookBytecodeAnalysis,
        explainHookBytecodeAnalysis,
        hookPatchPlan,
        explainHookPatchPlan,
        steelHook02PrimitiveBoundary,
        explainSteelHook02PrimitiveBoundary,
        steelHook02ContractGeneralization,
        explainSteelHook02ContractGeneralization,
        steelHook02MethodEntryTransformer,
        explainSteelHook02MethodEntryTransformer,
        steelHook02GatedRuntimeTransformation,
        explainSteelHook02GatedRuntimeTransformation,
        steelHook02CompletionCheck,
        explainSteelHook02CompletionCheck,
        steelHook03FramedMethodFoundation,
        explainSteelHook03FramedMethodFoundation,
        steelHook03MethodExitStaticDispatch,
        explainSteelHook03MethodExitStaticDispatch,
        steelHook03GatedRuntimeProof,
        explainSteelHook03GatedRuntimeProof,
        steelHook03CompletionCheck,
        explainSteelHook03CompletionCheck,
        steelHook04PrimitiveBoundary,
        explainSteelHook04PrimitiveBoundary,
        hookInstallationPlan,
        reproducibilityCheck,
        executionPlan,
        bootstrapClassloaderGraph,
        bootstrapServer,
        bootstrapTransformHooks,
        installHooks,
        strictExecution,
        denyLoaderInternals,
        verifyPlanFingerprints,
        bootstrapOffline,
        bootstrapFakeServer,
        steelHookCompletionCheck,
        explainSteelHookCompletionCheck);
  }

  private static Path resolveNullablePath(Path workingDirectory, Path path) {
    return path == null ? null : resolvePath(workingDirectory, path);
  }

  private static Path resolvePath(Path workingDirectory, Path path) {
    if (path == null) {
      return null;
    }
    if (path.isAbsolute()) {
      return path.toAbsolutePath().normalize();
    }
    return workingDirectory.resolve(path).toAbsolutePath().normalize();
  }

  public MinecraftProviderConfig(
      String requestedVersion,
      Path minecraftDirectory,
      Path explicitVersionJson,
      Path manifestJson,
      MinecraftSide side,
      boolean dryRun,
      boolean verifyFiles,
      boolean fetchMetadata,
      boolean downloadServer,
      Path cacheDirectory,
      boolean offline,
      boolean cacheInspect,
      boolean cacheRepair,
      boolean cacheStrict,
      boolean forceRedownload,
      Path outputPlanPath,
      boolean launch,
      Path serverDirectory,
      boolean acceptEulaForTest,
      java.util.List<String> serverJvmArgs,
      java.util.List<String> serverArgs,
      int launchTimeoutSeconds,
      boolean stopAfterReady,
      int readyTimeoutSeconds,
      boolean baselineServer,
      String baselineVersion,
      Path baselineReportPath,
      boolean offlineReplay,
      boolean requireReady,
      boolean realSmoke,
      String manifestUrl,
      boolean runtimePlan,
      boolean planMods,
      boolean integrationPlan,
      boolean boundaryReport,
      boolean preflight,
      boolean offlinePreflight,
      boolean strictBoundary,
      boolean strictRuntimeConflicts,
      boolean strictSide,
      boolean strictClassVersions,
      boolean explainBoundary,
      boolean explainRuntime,
      boolean explainMods,
      boolean interpretArtifact,
      boolean explainInterpretation,
      boolean hookContracts,
      boolean explainHookContracts,
      boolean serverLifecycleBindings,
      boolean explainServerLifecycleBindings,
      boolean serverLifecycleDispatchPlan,
      boolean explainServerLifecycleDispatchPlan,
      boolean resourceReloadAnalysis,
      boolean explainResourceReloadAnalysis,
      boolean resourceReloadSymbolAnalysis,
      boolean explainResourceReloadSymbolAnalysis,
      boolean resourceReloadBindingAnalysis,
      boolean explainResourceReloadBindingAnalysis,
      boolean resourceVisibilityGenerationAnalysis,
      boolean explainResourceVisibilityGenerationAnalysis,
      boolean resourceReloadArcDecision,
      boolean explainResourceReloadArcDecision,
      boolean registryBootstrapAnalysis,
      boolean explainRegistryBootstrapAnalysis,
      boolean registryArcHardening,
      boolean explainRegistryArcHardening,
      boolean commandRegistrationAnalysis,
      boolean explainCommandRegistrationAnalysis,
      boolean commandDispatcherSymbolAnalysis,
      boolean explainCommandDispatcherSymbolAnalysis,
      boolean commandDispatcherBindingAnalysis,
      boolean explainCommandDispatcherBindingAnalysis,
      boolean hookPlacementPlan,
      boolean explainHookPlacement,
      boolean hookBytecodeAnalysis,
      boolean explainHookBytecodeAnalysis,
      boolean hookPatchPlan,
      boolean explainHookPatchPlan,
      boolean steelHook02PrimitiveBoundary,
      boolean explainSteelHook02PrimitiveBoundary,
      boolean steelHook02ContractGeneralization,
      boolean explainSteelHook02ContractGeneralization,
      boolean steelHook02MethodEntryTransformer,
      boolean explainSteelHook02MethodEntryTransformer,
      boolean steelHook02GatedRuntimeTransformation,
      boolean explainSteelHook02GatedRuntimeTransformation,
      boolean steelHook02CompletionCheck,
      boolean explainSteelHook02CompletionCheck,
      boolean steelHook03FramedMethodFoundation,
      boolean explainSteelHook03FramedMethodFoundation,
      boolean steelHook03MethodExitStaticDispatch,
      boolean explainSteelHook03MethodExitStaticDispatch,
      boolean steelHook03GatedRuntimeProof,
      boolean explainSteelHook03GatedRuntimeProof,
      boolean steelHook03CompletionCheck,
      boolean explainSteelHook03CompletionCheck,
      boolean steelHook04PrimitiveBoundary,
      boolean explainSteelHook04PrimitiveBoundary,
      boolean hookInstallationPlan,
      boolean reproducibilityCheck,
      boolean executionPlan,
      boolean bootstrapClassloaderGraph,
      boolean bootstrapServer,
      boolean installHooks,
      boolean strictExecution,
      boolean denyLoaderInternals,
      boolean verifyPlanFingerprints,
      boolean bootstrapOffline,
      boolean bootstrapFakeServer,
      boolean steelHookCompletionCheck,
      boolean explainSteelHookCompletionCheck) {
    this(
        requestedVersion,
        minecraftDirectory,
        explicitVersionJson,
        manifestJson,
        side,
        dryRun,
        verifyFiles,
        fetchMetadata,
        downloadServer,
        cacheDirectory,
        offline,
        cacheInspect,
        cacheRepair,
        cacheStrict,
        forceRedownload,
        outputPlanPath,
        launch,
        serverDirectory,
        acceptEulaForTest,
        serverJvmArgs,
        serverArgs,
        launchTimeoutSeconds,
        stopAfterReady,
        readyTimeoutSeconds,
        baselineServer,
        baselineVersion,
        baselineReportPath,
        offlineReplay,
        requireReady,
        realSmoke,
        manifestUrl,
        runtimePlan,
        planMods,
        integrationPlan,
        boundaryReport,
        preflight,
        offlinePreflight,
        strictBoundary,
        strictRuntimeConflicts,
        strictSide,
        strictClassVersions,
        explainBoundary,
        explainRuntime,
        explainMods,
        interpretArtifact,
        explainInterpretation,
        hookContracts,
        explainHookContracts,
        serverLifecycleBindings,
        explainServerLifecycleBindings,
        serverLifecycleDispatchPlan,
        explainServerLifecycleDispatchPlan,
        resourceReloadAnalysis,
        explainResourceReloadAnalysis,
        resourceReloadSymbolAnalysis,
        explainResourceReloadSymbolAnalysis,
        resourceReloadBindingAnalysis,
        explainResourceReloadBindingAnalysis,
        resourceVisibilityGenerationAnalysis,
        explainResourceVisibilityGenerationAnalysis,
        resourceReloadArcDecision,
        explainResourceReloadArcDecision,
        registryBootstrapAnalysis,
        explainRegistryBootstrapAnalysis,
        registryArcHardening,
        explainRegistryArcHardening,
        commandRegistrationAnalysis,
        explainCommandRegistrationAnalysis,
        commandDispatcherSymbolAnalysis,
        explainCommandDispatcherSymbolAnalysis,
        commandDispatcherBindingAnalysis,
        explainCommandDispatcherBindingAnalysis,
        hookPlacementPlan,
        explainHookPlacement,
        hookBytecodeAnalysis,
        explainHookBytecodeAnalysis,
        hookPatchPlan,
        explainHookPatchPlan,
        steelHook02PrimitiveBoundary,
        explainSteelHook02PrimitiveBoundary,
        steelHook02ContractGeneralization,
        explainSteelHook02ContractGeneralization,
        steelHook02MethodEntryTransformer,
        explainSteelHook02MethodEntryTransformer,
        steelHook02GatedRuntimeTransformation,
        explainSteelHook02GatedRuntimeTransformation,
        steelHook02CompletionCheck,
        explainSteelHook02CompletionCheck,
        steelHook03FramedMethodFoundation,
        explainSteelHook03FramedMethodFoundation,
        steelHook03MethodExitStaticDispatch,
        explainSteelHook03MethodExitStaticDispatch,
        steelHook03GatedRuntimeProof,
        explainSteelHook03GatedRuntimeProof,
        steelHook03CompletionCheck,
        explainSteelHook03CompletionCheck,
        steelHook04PrimitiveBoundary,
        explainSteelHook04PrimitiveBoundary,
        hookInstallationPlan,
        reproducibilityCheck,
        executionPlan,
        bootstrapClassloaderGraph,
        bootstrapServer,
        false,
        installHooks,
        strictExecution,
        denyLoaderInternals,
        verifyPlanFingerprints,
        bootstrapOffline,
        bootstrapFakeServer,
        steelHookCompletionCheck,
        explainSteelHookCompletionCheck);
  }
}
