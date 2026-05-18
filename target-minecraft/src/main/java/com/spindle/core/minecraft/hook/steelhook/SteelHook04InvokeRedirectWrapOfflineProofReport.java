package com.spindle.core.minecraft.hook.steelhook;

import java.util.List;

public record SteelHook04InvokeRedirectWrapOfflineProofReport(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    String sourceTarget32Milestone,
    String sourceTarget32BoundaryStatus,
    boolean sourceTarget32GatePassed,
    int sourceTarget32ApprovedPrimitiveCount,
    boolean sourceInvokeRedirectCandidatePresent,
    boolean sourceInvokeRedirectCandidateInternalOnly,
    boolean sourceInvokeRedirectCandidatePublicApiExposed,
    boolean sourceInvokeRedirectCandidateRuntimeReady,
    boolean sourceInvokeRedirectCandidateGatedRuntimeReady,
    boolean sourceInvokeRedirectCandidateImplementedInTarget32,
    boolean sourceInvokeWrapCandidatePresent,
    boolean sourceInvokeWrapCandidateInternalOnly,
    boolean sourceInvokeWrapCandidatePublicApiExposed,
    boolean sourceInvokeWrapCandidateRuntimeReady,
    boolean sourceInvokeWrapCandidateGatedRuntimeReady,
    boolean sourceInvokeWrapCandidateImplementedInTarget32,
    boolean sourceTarget32RuntimeSideEffectsSafe,
    String sourceTarget33Milestone,
    String sourceTarget33ProofStatus,
    boolean sourceTarget33ProofReady,
    SteelHook04PrimitiveKind sourceTarget33PrimitiveKind,
    int sourceTarget33SuccessfulProofCaseCount,
    boolean sourceTarget33RuntimeSideEffectsSafe,
    boolean proofReady,
    SteelHook04InvokeRedirectWrapOfflineProofStatus proofStatus,
    SteelHook04InvokeRedirectWrapOfflineProofNextDirection nextDirection,
    String nextRecommendedAction,
    List<SteelHook04PrimitiveKind> approvedPrimitiveKinds,
    SteelHook04FixtureShape approvedFixtureShape,
    boolean unsupportedFixtureShapesRejected,
    int successfulProofCaseCount,
    int rejectionProofCaseCount,
    List<SteelHook04InvokeCallsiteProofCase> proofCases,
    List<SteelHook04InvokeCallsiteFinding> findings,
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
  public SteelHook04InvokeRedirectWrapOfflineProofReport {
    approvedPrimitiveKinds =
        List.copyOf(approvedPrimitiveKinds == null ? List.of() : approvedPrimitiveKinds);
    proofCases = List.copyOf(proofCases == null ? List.of() : proofCases);
    findings = List.copyOf(findings == null ? List.of() : findings);
  }
}
