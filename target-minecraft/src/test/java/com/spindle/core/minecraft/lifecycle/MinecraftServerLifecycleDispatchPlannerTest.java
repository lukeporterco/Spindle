package com.spindle.core.minecraft.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftServerLifecycleDispatchPlannerTest {
  private final MinecraftServerLifecycleDispatchPlanner planner =
      new MinecraftServerLifecycleDispatchPlanner();

  @Test
  void validStartingBindingProducesOneSymbolicPlannedDispatch() {
    MinecraftServerLifecycleDispatchPlan plan = planner.plan(bindingReport(true, true));

    assertTrue(plan.gatePassed());
    assertEquals(1, plan.plannedDispatchCount());
    MinecraftPlannedServerLifecycleDispatch starting = plan.dispatches().getFirst();
    assertEquals("target-12.minecraft.server.lifecycle.starting.dispatch", starting.id());
    assertEquals(MinecraftServerLifecycleDispatchStatus.PLANNED, starting.status());
    assertEquals(
        MinecraftServerLifecycleDispatchMode.INTERNAL_STATIC_DISPATCH_SYMBOLIC, starting.mode());
    assertEquals("BEFORE_MINECRAFT_SERVER_MAIN", starting.dispatchTiming());
    assertEquals(
        "com/spindle/core/minecraft/lifecycle/runtime/MinecraftServerLifecycleDispatcher",
        starting.dispatcherOwnerInternalName());
    assertEquals("beforeMinecraftServerMain", starting.dispatcherMethodName());
    assertEquals("()V", starting.dispatcherDescriptor());
    assertNull(plan.gateFailureReason());
  }

  @Test
  void failedBindingGateBlocksStartingDispatch() {
    MinecraftServerLifecycleDispatchPlan plan = planner.plan(bindingReport(false, false));

    assertFalse(plan.sourceBindingGatePassed());
    assertFalse(plan.gatePassed());
    assertEquals(0, plan.plannedDispatchCount());
    assertEquals(1, plan.blockedDispatchCount());
    assertNotNull(plan.gateFailureReason());
    assertFalse(plan.gateFailureReason().isBlank());
    MinecraftPlannedServerLifecycleDispatch starting = plan.dispatches().getFirst();
    assertEquals(MinecraftServerLifecycleDispatchStatus.BLOCKED, starting.status());
    assertEquals(MinecraftServerLifecycleDispatchMode.NONE, starting.mode());
    assertEquals(plan.gateFailureReason(), starting.notes());
  }

  @Test
  void allSixLifecyclePhasesHaveDispatchEntries() {
    MinecraftServerLifecycleDispatchPlan plan = planner.plan(bindingReport(true, true));

    assertEquals(6, plan.lifecyclePhaseCount());
    assertEquals(6, plan.dispatchCount());
    assertEquals(
        List.of(
            "minecraft.server.lifecycle.starting",
            "minecraft.server.lifecycle.started",
            "minecraft.server.lifecycle.stopping",
            "minecraft.server.lifecycle.stopped",
            "minecraft.server.lifecycle.crashed",
            "minecraft.server.lifecycle.reload_requested"),
        plan.dispatches().stream().map(MinecraftPlannedServerLifecycleDispatch::phaseId).toList());
  }

  @Test
  void futureLifecyclePhasesRemainDeclaredUnsupported() {
    MinecraftServerLifecycleDispatchPlan plan = planner.plan(bindingReport(true, true));

    List<MinecraftPlannedServerLifecycleDispatch> futureDispatches =
        plan.dispatches().subList(1, 6);
    assertEquals(5, plan.unsupportedDispatchCount());
    assertTrue(
        futureDispatches.stream()
            .allMatch(
                dispatch ->
                    dispatch.status() == MinecraftServerLifecycleDispatchStatus.DECLARED_UNSUPPORTED
                        && dispatch.mode() == MinecraftServerLifecycleDispatchMode.NONE
                        && dispatch.sourceContractId() == null
                        && dispatch.dispatcherOwnerInternalName() == null
                        && dispatch.dispatcherMethodName() == null
                        && dispatch.dispatcherDescriptor() == null));
  }

  @Test
  void dispatchPlanFlagsRemainAnalysisOnlyAndMutationFree() {
    MinecraftServerLifecycleDispatchPlan plan = planner.plan(bindingReport(true, true));

    assertEquals(1, plan.schema());
    assertEquals("Target-12", plan.milestoneName());
    assertEquals("minecraft", plan.target());
    assertEquals(MinecraftSide.SERVER, plan.side());
    assertEquals("minecraft.concept.server_lifecycle", plan.conceptId());
    assertEquals("Target-11", plan.sourceBindingReportMilestone());
    assertTrue(plan.analysisOnly());
    assertFalse(plan.classLoadingOccurred());
    assertFalse(plan.injectionOccurred());
    assertFalse(plan.transformationOccurred());
    assertFalse(plan.patchingOccurred());
    assertFalse(plan.hookInstallationOccurred());
    assertFalse(plan.runtimeDispatchOccurred());
    assertFalse(plan.publicApiExposed());
    assertFalse(plan.javaModExecutionSandboxed());
  }

  @Test
  void startingDispatchDoesNotAllowCancellationResultReplacementOrPublicListeners() {
    MinecraftServerLifecycleDispatchPlan plan = planner.plan(bindingReport(true, true));

    MinecraftPlannedServerLifecycleDispatch starting = plan.dispatches().getFirst();
    assertFalse(starting.cancellable());
    assertFalse(starting.allowsResultReplacement());
    assertFalse(starting.publicListenerRegistration());
    assertFalse(starting.modCallbackExecution());
    assertFalse(starting.runtimeDispatcherImplemented());
    assertTrue(starting.symbolicOnly());
  }

  private MinecraftServerLifecycleBindingReport bindingReport(
      boolean gatePassed, boolean startingBound) {
    return new MinecraftServerLifecycleBindingReport(
        1,
        "Target-11",
        "minecraft",
        "26.1.2",
        "server",
        "minecraft.concept.server_lifecycle",
        1,
        "Server Lifecycle",
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        "minecraft-26.1.2-server-known-symbols",
        gatePassed,
        gatePassed,
        gatePassed
            ? null
            : "Target-11 requires a passing Target-3 hook contract validation report.",
        6,
        startingBound ? 1 : 0,
        5,
        6,
        List.of(
            new MinecraftServerLifecycleBinding(
                "target-11.minecraft.server.lifecycle.starting",
                "minecraft.server.lifecycle.starting",
                "Starting",
                startingBound
                    ? MinecraftServerLifecycleBindingStatus.BOUND
                    : MinecraftServerLifecycleBindingStatus.UNSUPPORTED,
                startingBound,
                "minecraft.26_1_2.server.main.entrypoint",
                "net/minecraft/server/Main",
                "main",
                "([Ljava/lang/String;)V",
                "known-main-entrypoint-analysis",
                gatePassed ? "Bound." : "Blocked."),
            declaredUnbound("started", "Started"),
            declaredUnbound("stopping", "Stopping"),
            declaredUnbound("stopped", "Stopped"),
            declaredUnbound("crashed", "Crashed"),
            declaredUnbound("reload_requested", "Reload Requested")));
  }

  private MinecraftServerLifecycleBinding declaredUnbound(String phaseSuffix, String displayName) {
    return new MinecraftServerLifecycleBinding(
        "target-11.minecraft.server.lifecycle." + phaseSuffix,
        "minecraft.server.lifecycle." + phaseSuffix,
        displayName,
        MinecraftServerLifecycleBindingStatus.DECLARED_UNBOUND,
        false,
        null,
        null,
        null,
        null,
        null,
        "Future.");
  }
}
