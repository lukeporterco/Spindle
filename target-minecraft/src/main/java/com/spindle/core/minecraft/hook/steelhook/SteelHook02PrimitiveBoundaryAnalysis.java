package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;

public record SteelHook02PrimitiveBoundaryAnalysis(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    String minecraftVersion,
    MinecraftSide side,
    String sourcePatchPlanMilestone,
    String sourceSteelHookCompletionMilestone,
    String sourceRegistryHardeningMilestone,
    boolean analysisOnly,
    boolean classLoadingOccurred,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed,
    int supportedPrimitiveCount,
    int approvedCandidateCount,
    int deferredCandidateCount,
    int rejectedCandidateCount,
    boolean gatePassed,
    String gateFailureReason,
    SteelHook02PrimitiveBoundaryStatus boundaryStatus,
    SteelHook02NextDirection nextDirection,
    String nextRecommendedAction,
    List<SteelHook02PrimitiveCandidate> candidates,
    List<SteelHook02PrimitiveFinding> findings) {
  public SteelHook02PrimitiveBoundaryAnalysis {
    candidates = List.copyOf(candidates == null ? List.of() : candidates);
    findings = List.copyOf(findings == null ? List.of() : findings);
  }
}
