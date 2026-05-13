package com.spindle.core.minecraft.registry;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;

public record MinecraftRegistryBootstrapAnalysis(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    MinecraftSide side,
    String conceptId,
    String sourceArtifactInterpretationMilestone,
    String sourceResourceReloadArcDecisionMilestone,
    boolean analysisOnly,
    boolean classLoadingOccurred,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean registryBootstrapOccurred,
    boolean registryMutationOccurred,
    boolean contentRegistrationOccurred,
    boolean resourceAccessOccurred,
    boolean datapackAccessOccurred,
    boolean dataGenerationOccurred,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed,
    boolean sourceResourceReloadArcDecisionGatePassed,
    boolean sourceResourceReloadArcDecisionRegistryBootstrapRecommended,
    String sourceResourceReloadArcDecisionNextDirection,
    boolean gatePassed,
    String gateFailureReason,
    List<String> discoveryTokens,
    int boundaryCount,
    int anchorBoundaryCount,
    int metadataAnalyzedBoundaryCount,
    int declaredUnboundBoundaryCount,
    int blockedBoundaryCount,
    int candidateCount,
    int classNameCandidateCount,
    int fieldNameCandidateCount,
    int fieldDescriptorCandidateCount,
    int methodNameCandidateCount,
    int methodDescriptorCandidateCount,
    int selectableCandidateCount,
    int rejectedCandidateCount,
    int classReferenceOnlyCount,
    int methodBoundaryAnalysisRequiredCount,
    int fieldAccessRequiredCount,
    int receiverCaptureRequiredCount,
    int futureSteelHookPrimitiveRequiredCount,
    MinecraftRegistryDiscoveryStatus discoveryStatus,
    MinecraftRegistryBindingStatus bindingStatus,
    boolean registryProofRecommended,
    boolean currentSteelHookMethodEntryCompatible,
    boolean steelHookPrimitiveDesignRecommended,
    String nextRecommendedAction,
    List<MinecraftAnalyzedRegistryBoundary> boundaries,
    List<MinecraftRegistryCandidate> candidates) {
  public MinecraftRegistryBootstrapAnalysis {
    discoveryTokens = List.copyOf(discoveryTokens == null ? List.of() : discoveryTokens);
    boundaries = List.copyOf(boundaries == null ? List.of() : boundaries);
    candidates = List.copyOf(candidates == null ? List.of() : candidates);
  }
}
