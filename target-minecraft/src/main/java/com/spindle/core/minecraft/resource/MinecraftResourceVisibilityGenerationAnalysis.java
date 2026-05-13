package com.spindle.core.minecraft.resource;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;

public record MinecraftResourceVisibilityGenerationAnalysis(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    MinecraftSide side,
    String conceptId,
    String sourceResourceReloadAnalysisMilestone,
    String sourceResourceReloadBindingAnalysisMilestone,
    boolean analysisOnly,
    boolean classLoadingOccurred,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean resourceReloadOccurred,
    boolean resourceAccessOccurred,
    boolean datapackAccessOccurred,
    boolean dataGenerationOccurred,
    boolean generatedFileWriteOccurred,
    boolean registryMutationOccurred,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed,
    boolean sourceResourceReloadGatePassed,
    boolean sourceBindingGatePassed,
    String sourceBindingStatus,
    boolean sourceReloadProofRecommended,
    boolean sourceCurrentSteelHookMethodEntryCompatible,
    boolean gatePassed,
    String gateFailureReason,
    MinecraftResourceVisibilityGenerationStatus separationStatus,
    int runtimeFacingSurfaceCount,
    int offlineGenerationSurfaceCount,
    int runtimeReloadTimingSurfaceCount,
    int runtimeResourceVisibilitySurfaceCount,
    int offlineDataGenerationSurfaceCount,
    int implementationReadySurfaceCount,
    int futureSteelHookPrimitiveRequiredSurfaceCount,
    boolean runtimeVisibilitySeparatedFromOfflineGeneration,
    boolean dataGenerationRequiresOfflineDesign,
    boolean runtimeReloadRequiresFutureBindingDecision,
    boolean reloadProofRecommended,
    boolean currentSteelHookMethodEntryCompatible,
    String nextRecommendedAction,
    List<MinecraftResourceVisibilityGenerationSurface> surfaces) {
  public MinecraftResourceVisibilityGenerationAnalysis {
    surfaces = List.copyOf(surfaces == null ? List.of() : surfaces);
  }
}
