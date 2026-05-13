package com.spindle.core.minecraft.resource;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;

public record MinecraftResourceReloadBindingAnalysis(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    MinecraftSide side,
    String conceptId,
    String sourceResourceReloadSymbolAnalysisMilestone,
    String resourceBoundaryId,
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
    boolean sourceSymbolGatePassed,
    String sourceDiscoveryStatus,
    boolean sourceBindingStrategyAnalysisEligible,
    boolean gatePassed,
    String gateFailureReason,
    MinecraftResourceReloadBindingStatus bindingStatus,
    int sourceCandidateCount,
    int analyzedCandidateCount,
    int selectableCandidateCount,
    int rejectedCandidateCount,
    int classReferenceOnlyCount,
    int methodBoundaryAnalysisRequiredCount,
    int fieldAccessRequiredCount,
    int receiverCaptureRequiredCount,
    int futureSteelHookPrimitiveRequiredCount,
    boolean reloadProofRecommended,
    boolean currentSteelHookMethodEntryCompatible,
    String nextRecommendedAction,
    List<MinecraftResourceReloadBindingCandidate> candidates) {
  public MinecraftResourceReloadBindingAnalysis {
    candidates = List.copyOf(candidates == null ? List.of() : candidates);
  }
}
