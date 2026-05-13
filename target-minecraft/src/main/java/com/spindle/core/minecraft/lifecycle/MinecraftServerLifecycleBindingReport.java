package com.spindle.core.minecraft.lifecycle;

import java.util.List;

public record MinecraftServerLifecycleBindingReport(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    String side,
    String conceptId,
    int conceptOrder,
    String conceptDisplayName,
    boolean analysisOnly,
    boolean classLoadingOccurred,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean hookInstallationOccurred,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed,
    String contractCatalogId,
    boolean sourceContractValidationPassed,
    boolean gatePassed,
    String gateFailureReason,
    int lifecyclePhaseCount,
    int boundPhaseCount,
    int unboundPhaseCount,
    int bindingCount,
    List<MinecraftServerLifecycleBinding> bindings) {
  public MinecraftServerLifecycleBindingReport {
    bindings = List.copyOf(bindings == null ? List.of() : bindings);
  }
}
