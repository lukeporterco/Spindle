package com.spindle.core.minecraft.lifecycle;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;
import java.util.Objects;

public final class MinecraftServerLifecycleDispatchPlanner {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-12";
  private static final String TARGET = "minecraft";
  private static final String SOURCE_BINDING_REPORT_MILESTONE = "Target-11";
  private static final String STARTING_DISPATCH_ID =
      "target-12.minecraft.server.lifecycle.starting.dispatch";
  private static final String STARTING_SOURCE_BINDING_ID =
      "target-11.minecraft.server.lifecycle.starting";
  private static final String STARTING_CONTRACT_ID = "minecraft.26_1_2.server.main.entrypoint";
  private static final String STARTING_DISPATCH_TIMING = "BEFORE_MINECRAFT_SERVER_MAIN";
  private static final String STARTING_DISPATCH_OWNER =
      "com/spindle/core/minecraft/lifecycle/runtime/MinecraftServerLifecycleDispatcher";
  private static final String STARTING_DISPATCH_METHOD = "beforeMinecraftServerMain";
  private static final String STARTING_DISPATCH_DESCRIPTOR = "()V";
  private static final String BINDING_GATE_FAILURE_REASON =
      "Target-12 requires a passing Target-11 server lifecycle binding report.";
  private static final String STARTING_BINDING_FAILURE_REASON =
      "Target-12 requires `minecraft.server.lifecycle.starting` to be BOUND in Target-11.";

  public MinecraftServerLifecycleDispatchPlan plan(
      MinecraftServerLifecycleBindingReport bindingReport) {
    Objects.requireNonNull(bindingReport, "bindingReport");

    MinecraftServerLifecycleBinding startingBinding =
        bindingFor(bindingReport, MinecraftServerLifecyclePhase.SERVER_STARTING);
    boolean sourceBindingGatePassed = bindingReport.gatePassed();
    boolean startingBound = startingBinding.status() == MinecraftServerLifecycleBindingStatus.BOUND;
    boolean gatePassed = sourceBindingGatePassed && startingBound;
    String gateFailureReason =
        gatePassed
            ? null
            : (!sourceBindingGatePassed
                ? nonBlankFailureReason(
                    bindingReport.gateFailureReason(), BINDING_GATE_FAILURE_REASON)
                : STARTING_BINDING_FAILURE_REASON);

    List<MinecraftPlannedServerLifecycleDispatch> dispatches =
        List.of(
            startingDispatch(startingBinding, gatePassed, gateFailureReason),
            unsupportedDispatch(
                bindingFor(bindingReport, MinecraftServerLifecyclePhase.SERVER_STARTED)),
            unsupportedDispatch(
                bindingFor(bindingReport, MinecraftServerLifecyclePhase.SERVER_STOPPING)),
            unsupportedDispatch(
                bindingFor(bindingReport, MinecraftServerLifecyclePhase.SERVER_STOPPED)),
            unsupportedDispatch(
                bindingFor(bindingReport, MinecraftServerLifecyclePhase.SERVER_CRASHED)),
            unsupportedDispatch(
                bindingFor(bindingReport, MinecraftServerLifecyclePhase.SERVER_RELOAD_REQUESTED)));

    int plannedDispatchCount =
        (int)
            dispatches.stream()
                .filter(
                    dispatch -> dispatch.status() == MinecraftServerLifecycleDispatchStatus.PLANNED)
                .count();
    int blockedDispatchCount =
        (int)
            dispatches.stream()
                .filter(
                    dispatch -> dispatch.status() == MinecraftServerLifecycleDispatchStatus.BLOCKED)
                .count();
    int unsupportedDispatchCount =
        (int)
            dispatches.stream()
                .filter(
                    dispatch ->
                        dispatch.status()
                            == MinecraftServerLifecycleDispatchStatus.DECLARED_UNSUPPORTED)
                .count();

    return new MinecraftServerLifecycleDispatchPlan(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        bindingReport.minecraftVersion(),
        side(bindingReport.side()),
        bindingReport.conceptId(),
        SOURCE_BINDING_REPORT_MILESTONE,
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        sourceBindingGatePassed,
        gatePassed,
        gateFailureReason,
        MinecraftServerLifecyclePhase.values().length,
        dispatches.size(),
        plannedDispatchCount,
        blockedDispatchCount,
        unsupportedDispatchCount,
        dispatches);
  }

  private MinecraftPlannedServerLifecycleDispatch startingDispatch(
      MinecraftServerLifecycleBinding binding, boolean gatePassed, String gateFailureReason) {
    if (gatePassed) {
      return new MinecraftPlannedServerLifecycleDispatch(
          STARTING_DISPATCH_ID,
          binding.phaseId(),
          binding.displayName(),
          STARTING_SOURCE_BINDING_ID,
          STARTING_CONTRACT_ID,
          MinecraftServerLifecycleDispatchStatus.PLANNED,
          MinecraftServerLifecycleDispatchMode.INTERNAL_STATIC_DISPATCH_SYMBOLIC,
          STARTING_DISPATCH_TIMING,
          STARTING_DISPATCH_OWNER,
          STARTING_DISPATCH_METHOD,
          STARTING_DISPATCH_DESCRIPTOR,
          false,
          false,
          false,
          false,
          false,
          true,
          "Symbolic internal static dispatch planned before the Minecraft dedicated server main entrypoint.");
    }
    return new MinecraftPlannedServerLifecycleDispatch(
        STARTING_DISPATCH_ID,
        binding.phaseId(),
        binding.displayName(),
        STARTING_SOURCE_BINDING_ID,
        STARTING_CONTRACT_ID,
        MinecraftServerLifecycleDispatchStatus.BLOCKED,
        MinecraftServerLifecycleDispatchMode.NONE,
        null,
        null,
        null,
        null,
        false,
        false,
        false,
        false,
        false,
        true,
        gateFailureReason);
  }

  private MinecraftPlannedServerLifecycleDispatch unsupportedDispatch(
      MinecraftServerLifecycleBinding binding) {
    return new MinecraftPlannedServerLifecycleDispatch(
        "target-12." + binding.phaseId() + ".dispatch",
        binding.phaseId(),
        binding.displayName(),
        binding.id(),
        null,
        MinecraftServerLifecycleDispatchStatus.DECLARED_UNSUPPORTED,
        MinecraftServerLifecycleDispatchMode.NONE,
        null,
        null,
        null,
        null,
        false,
        false,
        false,
        false,
        false,
        true,
        "Declared for future Target Layer lifecycle dispatch work but unsupported in this pass.");
  }

  private MinecraftServerLifecycleBinding bindingFor(
      MinecraftServerLifecycleBindingReport report, MinecraftServerLifecyclePhase phase) {
    return report.bindings().stream()
        .filter(binding -> phase.id().equals(binding.phaseId()))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("Missing binding for `" + phase.id() + "`."));
  }

  private String nonBlankFailureReason(String candidate, String fallback) {
    return candidate == null || candidate.isBlank() ? fallback : candidate;
  }

  private MinecraftSide side(String sideId) {
    if (MinecraftSide.SERVER.id().equals(sideId)) {
      return MinecraftSide.SERVER;
    }
    if (MinecraftSide.CLIENT.id().equals(sideId)) {
      return MinecraftSide.CLIENT;
    }
    throw new IllegalArgumentException("Unsupported Minecraft side `" + sideId + "`.");
  }
}
