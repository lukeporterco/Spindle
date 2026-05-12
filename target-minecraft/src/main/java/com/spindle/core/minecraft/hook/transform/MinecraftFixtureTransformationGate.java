package com.spindle.core.minecraft.hook.transform;

public record MinecraftFixtureTransformationGate(
    boolean passed,
    String failureReason,
    boolean patchPlanGatePassed,
    boolean patchPlanningSucceeded,
    boolean patchPlanned,
    int plannedPatchCount,
    String selectedPatchId,
    boolean transformReadyForFixtureOnly,
    boolean transformReadyForMinecraftRuntime) {}
