package com.spindle.core.minecraft.resource;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;

public record MinecraftResourceReloadAnalysis(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    MinecraftSide side,
    String conceptId,
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
    boolean registryMutationOccurred,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed,
    String sourceServerLifecycleDispatchPlanMilestone,
    boolean sourceLifecycleGatePassed,
    boolean sourceLifecycleStartingDispatchAvailable,
    boolean gatePassed,
    String gateFailureReason,
    int availableBoundaryCount,
    int declaredUnboundBoundaryCount,
    int upstreamBlockedBoundaryCount,
    List<MinecraftAnalyzedResourceReloadBoundary> boundaries) {
  public MinecraftResourceReloadAnalysis {
    boundaries = List.copyOf(boundaries == null ? List.of() : boundaries);
  }
}
