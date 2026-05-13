package com.spindle.core.minecraft.lifecycle;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;

public record MinecraftServerLifecycleDispatchPlan(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    MinecraftSide side,
    String conceptId,
    String sourceBindingReportMilestone,
    boolean analysisOnly,
    boolean classLoadingOccurred,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed,
    boolean sourceBindingGatePassed,
    boolean gatePassed,
    String gateFailureReason,
    int lifecyclePhaseCount,
    int dispatchCount,
    int plannedDispatchCount,
    int blockedDispatchCount,
    int unsupportedDispatchCount,
    List<MinecraftPlannedServerLifecycleDispatch> dispatches) {
  public MinecraftServerLifecycleDispatchPlan {
    dispatches = List.copyOf(dispatches == null ? List.of() : dispatches);
  }
}
