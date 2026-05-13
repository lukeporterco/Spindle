package com.spindle.core.minecraft.registry;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadNextDirection;
import java.util.List;

public record MinecraftRegistryArcHardeningAnalysis(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    MinecraftSide side,
    String conceptId,
    String sourceResourceReloadArcDecisionMilestone,
    String sourceRegistryBootstrapAnalysisMilestone,
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
    MinecraftResourceReloadNextDirection sourceResourceReloadArcDecisionNextDirection,
    boolean sourceRegistryBootstrapGatePassed,
    MinecraftRegistryDiscoveryStatus sourceRegistryDiscoveryStatus,
    MinecraftRegistryBindingStatus sourceRegistryBindingStatus,
    int sourceRegistryCandidateCount,
    int sourceRegistrySelectableCandidateCount,
    int sourceRegistryRejectedCandidateCount,
    int sourceRegistryFutureSteelHookPrimitiveRequiredCount,
    boolean gatePassed,
    String gateFailureReason,
    MinecraftRegistryArcHardeningStatus hardeningStatus,
    MinecraftRegistryArcNextDirection nextDirection,
    boolean registryArcCompleteForNow,
    boolean registryImplementationReady,
    boolean registryProofRecommended,
    boolean currentSteelHookMethodEntryCompatible,
    boolean steelHook02PrimitiveDesignRecommended,
    boolean continueRegistryAnalysisRecommended,
    int blockingFindingCount,
    int warningFindingCount,
    int passingFindingCount,
    String nextRecommendedAction,
    List<MinecraftRegistryArcHardeningFinding> findings) {
  public MinecraftRegistryArcHardeningAnalysis {
    findings = List.copyOf(findings == null ? List.of() : findings);
  }
}
