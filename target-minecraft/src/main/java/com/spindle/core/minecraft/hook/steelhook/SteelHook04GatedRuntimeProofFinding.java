package com.spindle.core.minecraft.hook.steelhook;

public record SteelHook04GatedRuntimeProofFinding(
    String id,
    String checkName,
    SteelHook04GatedRuntimeProofFindingStatus status,
    boolean blocking,
    String summary,
    String details) {}
