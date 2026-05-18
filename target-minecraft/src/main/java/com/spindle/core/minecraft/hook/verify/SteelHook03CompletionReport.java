package com.spindle.core.minecraft.hook.verify;

import java.util.List;

public record SteelHook03CompletionReport(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    boolean completionReady,
    SteelHook03CompletionStatus status,
    SteelHook03CompletionHandoffStatus handoffStatus,
    String sourceSteelHook02Milestone,
    String sourceSteelHook02Status,
    boolean sourceSteelHook02CompletionReady,
    String sourceSteelHook02HandoffStatus,
    String sourceTarget28Milestone,
    String sourceTarget28Status,
    boolean sourceTarget28FramedMethodFoundationReady,
    String sourceTarget29Milestone,
    String sourceTarget29Status,
    boolean sourceTarget29MethodExitDispatchReady,
    String sourceTarget30Milestone,
    String sourceTarget30Status,
    boolean sourceTarget30GatedRuntimeProofReady,
    List<String> completedCapabilities,
    List<String> unsupportedCapabilities,
    List<SteelHook03CompletionStageVerification> stageVerifications,
    List<SteelHook03CompletionSafetyInvariant> safetyInvariants,
    List<SteelHook03CompletionFinding> forbiddenReportChecks,
    int runtimeClassLoaderProofCount,
    int runtimeClassLoaderSuccessCount,
    boolean entryPrimitiveVerified,
    boolean exitPrimitiveVerified,
    boolean runtimeClassLoadingPathEnabled,
    boolean classLoadingOccurred,
    boolean serverLaunchOccurred,
    boolean minecraftMainInvoked,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean beforeDispatcherInvocationObserved,
    boolean afterDispatcherInvocationObserved,
    boolean publicApiExposed,
    boolean javaAgentUsed,
    boolean mixinUsed,
    boolean javaModExecutionSandboxed,
    String failureReason,
    List<SteelHook03CompletionFinding> findings) {
  public SteelHook03CompletionReport {
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
