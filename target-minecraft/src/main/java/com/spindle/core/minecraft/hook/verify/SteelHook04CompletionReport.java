package com.spindle.core.minecraft.hook.verify;

import java.util.List;

public record SteelHook04CompletionReport(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    boolean completionReady,
    SteelHook04CompletionStatus status,
    SteelHook04CompletionHandoffStatus handoffStatus,
    SteelHook04CompletionNextDirection nextDirection,
    String nextRecommendedAction,
    String sourceTarget32Milestone,
    String sourceTarget32BoundaryStatus,
    boolean sourceTarget32GatePassed,
    int sourceTarget32ApprovedPrimitiveCount,
    String sourceTarget33Milestone,
    String sourceTarget33ProofStatus,
    boolean sourceTarget33ProofReady,
    String sourceTarget33PrimitiveKind,
    int sourceTarget33SuccessfulProofCaseCount,
    String sourceTarget34Milestone,
    String sourceTarget34ProofStatus,
    boolean sourceTarget34ProofReady,
    int sourceTarget34SuccessfulProofCaseCount,
    String sourceTarget35Milestone,
    String sourceTarget35Status,
    boolean sourceTarget35GatedRuntimeProofReady,
    int sourceTarget35RuntimeClassLoaderProofCount,
    int sourceTarget35RuntimeClassLoaderSuccessCount,
    List<String> completedPrimitiveKinds,
    List<String> completedCapabilities,
    List<String> unsupportedCapabilities,
    List<SteelHook04CompletionStageVerification> stageVerifications,
    List<SteelHook04CompletionSafetyInvariant> safetyInvariants,
    List<SteelHook04CompletionFinding> forbiddenReportChecks,
    boolean returnValueInterceptVerified,
    boolean invokeRedirectVerified,
    boolean invokeWrapVerified,
    boolean offlineProofChainVerified,
    boolean gatedRuntimeProofVerified,
    boolean unsupportedPrimitiveRejectionVerified,
    boolean rawBytePayloadsAbsent,
    boolean unsupportedPrimitiveLeakageAbsent,
    boolean runtimeClassLoadingPathEnabled,
    boolean classLoadingOccurred,
    boolean targetClassDefinitionOccurred,
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
    List<SteelHook04CompletionFinding> findings) {
  public SteelHook04CompletionReport {
    completedPrimitiveKinds =
        List.copyOf(completedPrimitiveKinds == null ? List.of() : completedPrimitiveKinds);
    completedCapabilities =
        List.copyOf(completedCapabilities == null ? List.of() : completedCapabilities);
    unsupportedCapabilities =
        List.copyOf(unsupportedCapabilities == null ? List.of() : unsupportedCapabilities);
    stageVerifications = List.copyOf(stageVerifications == null ? List.of() : stageVerifications);
    safetyInvariants = List.copyOf(safetyInvariants == null ? List.of() : safetyInvariants);
    forbiddenReportChecks =
        List.copyOf(forbiddenReportChecks == null ? List.of() : forbiddenReportChecks);
    findings = List.copyOf(findings == null ? List.of() : findings);
  }
}
