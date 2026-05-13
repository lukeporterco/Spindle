package com.spindle.core.minecraft.command;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;

public record MinecraftCommandRegistrationAnalysis(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    MinecraftSide side,
    String conceptId,
    int conceptOrder,
    String conceptDisplayName,
    String upstreamConceptId,
    String sourceLifecycleDispatchPlanMilestone,
    boolean analysisOnly,
    boolean classLoadingOccurred,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean commandRegistrationOccurred,
    boolean commandExecutionOccurred,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed,
    boolean sourceLifecycleDispatchGatePassed,
    boolean gatePassed,
    String gateFailureReason,
    int boundaryCount,
    int anchoredBoundaryCount,
    int unboundBoundaryCount,
    int blockedBoundaryCount,
    int boundMinecraftSymbolCount,
    int implementedBoundaryCount,
    List<MinecraftAnalyzedCommandRegistrationBoundary> boundaries) {
  public MinecraftCommandRegistrationAnalysis {
    boundaries = List.copyOf(boundaries == null ? List.of() : boundaries);
  }
}
