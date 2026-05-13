package com.spindle.core.minecraft.resource;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;

public record MinecraftResourceReloadSymbolAnalysis(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    MinecraftSide side,
    String conceptId,
    String sourceResourceReloadAnalysisMilestone,
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
    boolean sourceResourceReloadGatePassed,
    boolean resourceLifecycleAnchorAvailable,
    boolean reloadDiscoveryBoundaryDeclared,
    boolean gatePassed,
    String gateFailureReason,
    List<String> discoveryTokens,
    int candidateCount,
    int classNameCandidateCount,
    int fieldNameCandidateCount,
    int fieldDescriptorCandidateCount,
    int methodNameCandidateCount,
    int methodDescriptorCandidateCount,
    int selectableCandidateCount,
    int rejectedCandidateCount,
    MinecraftResourceReloadSymbolDiscoveryStatus discoveryStatus,
    boolean bindingStrategyAnalysisEligible,
    List<MinecraftResourceReloadSymbolCandidate> candidates) {
  public MinecraftResourceReloadSymbolAnalysis {
    discoveryTokens = List.copyOf(discoveryTokens == null ? List.of() : discoveryTokens);
    candidates = List.copyOf(candidates == null ? List.of() : candidates);
  }
}
