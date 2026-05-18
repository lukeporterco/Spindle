package com.spindle.core.minecraft.hook.steelhook;

import java.util.List;

public record SteelHook03RuntimeProofReport(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    String sourceTarget29Milestone,
    String sourceTarget29Status,
    boolean sourceTarget29MethodExitDispatchReady,
    String sourceTarget29NextDirection,
    boolean gatedRuntimeProofReady,
    SteelHook03RuntimeProofStatus status,
    SteelHook03RuntimeProofNextDirection nextDirection,
    int runtimeClassLoaderProofCount,
    int runtimeClassLoaderSuccessCount,
    SteelHook03RuntimePrimitiveProof entryPrimitiveProof,
    SteelHook03RuntimePrimitiveProof exitPrimitiveProof,
    boolean runtimeClassLoadingPathEnabled,
    boolean classLoadingOccurred,
    List<String> targetClassesDefined,
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
    List<SteelHook03RuntimeProofFinding> findings) {
  public SteelHook03RuntimeProofReport {
    targetClassesDefined =
        List.copyOf(targetClassesDefined == null ? List.of() : targetClassesDefined);
    findings = List.copyOf(findings == null ? List.of() : findings);
  }
}
