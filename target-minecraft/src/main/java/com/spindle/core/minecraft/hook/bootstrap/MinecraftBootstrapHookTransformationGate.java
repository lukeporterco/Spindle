package com.spindle.core.minecraft.hook.bootstrap;

public record MinecraftBootstrapHookTransformationGate(
    boolean passed,
    String failureReason,
    Integer patchPlanSchema,
    String patchPlanMilestoneName,
    boolean patchPlanGatePassed,
    boolean patchPlanningSucceeded,
    boolean patchPlanned,
    int plannedPatchCount,
    String selectedPatchId,
    boolean transformReadyForFixtureOnly,
    boolean transformReadyForMinecraftRuntime) {}
