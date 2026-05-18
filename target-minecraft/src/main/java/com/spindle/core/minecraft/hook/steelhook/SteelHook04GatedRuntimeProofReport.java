package com.spindle.core.minecraft.hook.steelhook;

import java.util.List;

public record SteelHook04GatedRuntimeProofReport(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    String sourceTarget32Milestone,
    String sourceTarget32BoundaryStatus,
    boolean sourceTarget32GatePassed,
    int sourceTarget32ApprovedPrimitiveCount,
    String sourceTarget33Milestone,
    String sourceTarget33ProofStatus,
    boolean sourceTarget33ProofReady,
    int sourceTarget33SuccessfulProofCaseCount,
    String sourceTarget34Milestone,
    String sourceTarget34ProofStatus,
    boolean sourceTarget34ProofReady,
    int sourceTarget34SuccessfulProofCaseCount,
    boolean gatedRuntimeProofReady,
    SteelHook04GatedRuntimeProofStatus status,
    SteelHook04GatedRuntimeProofNextDirection nextDirection,
    String nextRecommendedAction,
    List<SteelHook04PrimitiveKind> approvedPrimitiveKinds,
    int runtimeClassLoaderProofCount,
    int runtimeClassLoaderSuccessCount,
    SteelHook04RuntimePrimitiveProof returnValueInterceptProof,
    SteelHook04RuntimePrimitiveProof invokeRedirectProof,
    SteelHook04RuntimePrimitiveProof invokeWrapProof,
    boolean unsupportedPrimitivePlanRejectedBeforeClassDefinition,
    boolean unsupportedPrimitivePlanClassDefinitionAttempted,
    boolean runtimeClassLoadingPathEnabled,
    boolean classLoadingOccurred,
    boolean targetClassDefinitionOccurred,
    List<String> targetClassesDefined,
    boolean classInitialized,
    boolean targetMethodInvoked,
    boolean wrapperExecuted,
    boolean serverLaunchOccurred,
    boolean minecraftMainInvoked,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean publicApiExposed,
    boolean javaAgentUsed,
    boolean mixinUsed,
    boolean javaModExecutionSandboxed,
    String failureReason,
    List<SteelHook04GatedRuntimeProofFinding> findings) {
  public SteelHook04GatedRuntimeProofReport {
    approvedPrimitiveKinds =
        List.copyOf(approvedPrimitiveKinds == null ? List.of() : approvedPrimitiveKinds);
    targetClassesDefined =
        List.copyOf(targetClassesDefined == null ? List.of() : targetClassesDefined);
    findings = List.copyOf(findings == null ? List.of() : findings);
  }
}
