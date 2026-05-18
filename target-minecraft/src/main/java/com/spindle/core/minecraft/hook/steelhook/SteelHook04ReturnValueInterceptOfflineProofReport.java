package com.spindle.core.minecraft.hook.steelhook;

import java.util.List;

public record SteelHook04ReturnValueInterceptOfflineProofReport(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    String sourceTarget32Milestone,
    String sourceTarget32BoundaryStatus,
    boolean sourceTarget32GatePassed,
    int sourceTarget32ApprovedPrimitiveCount,
    boolean sourceReturnValueInterceptCandidatePresent,
    boolean sourceReturnValueInterceptCandidateInternalOnly,
    boolean sourceReturnValueInterceptCandidatePublicApiExposed,
    boolean sourceReturnValueInterceptCandidateRuntimeReady,
    boolean sourceReturnValueInterceptCandidateGatedRuntimeReady,
    boolean sourceReturnValueInterceptCandidateImplementedInTarget32,
    boolean sourceRuntimeSideEffectsSafe,
    boolean proofReady,
    SteelHook04ReturnValueInterceptOfflineProofStatus proofStatus,
    SteelHook04ReturnValueInterceptOfflineProofNextDirection nextDirection,
    String nextRecommendedAction,
    SteelHook04PrimitiveKind primitiveKind,
    List<SteelHook04FixtureShape> approvedFixtureShapes,
    boolean unsupportedFixtureShapesRejected,
    int successfulProofCaseCount,
    int rejectionProofCaseCount,
    List<SteelHook04ReturnValueInterceptProofCase> proofCases,
    List<SteelHook04ReturnValueInterceptFinding> findings,
    boolean offlineOnly,
    boolean runtimeClassLoadingPathEnabled,
    boolean classLoadingOccurred,
    boolean serverLaunchOccurred,
    boolean minecraftMainInvoked,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean publicApiExposed,
    boolean javaAgentUsed,
    boolean mixinUsed,
    boolean javaModExecutionSandboxed) {
  public SteelHook04ReturnValueInterceptOfflineProofReport {
    approvedFixtureShapes =
        List.copyOf(approvedFixtureShapes == null ? List.of() : approvedFixtureShapes);
    proofCases = List.copyOf(proofCases == null ? List.of() : proofCases);
    findings = List.copyOf(findings == null ? List.of() : findings);
  }
}
