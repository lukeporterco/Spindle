package com.spindle.core.minecraft.hook.install;

import java.util.List;

public record MinecraftHookInstallationPlan(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    String side,
    String catalogId,
    boolean sourceContractValidationPassed,
    int sourceContractErrorCount,
    String minecraftMainClass,
    boolean gatePassed,
    String gateFailureReason,
    boolean installationPlanned,
    MinecraftHookInstallationMode installationMode,
    int plannedHookCount,
    List<MinecraftPlannedHookInstallation> plannedHooks,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean bytecodeModified,
    boolean javaAgentUsed,
    boolean mixinUsed,
    boolean remappingOccurred,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed) {
  public MinecraftHookInstallationPlan {
    plannedHooks = List.copyOf(plannedHooks == null ? List.of() : plannedHooks);
  }
}
