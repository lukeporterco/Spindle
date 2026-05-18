package com.spindle.core.minecraft.hook.steelhook;

public record SteelHook04PrimitiveFinding(
    String id,
    String checkName,
    SteelHook04PrimitiveFindingStatus status,
    boolean blocking,
    String summary,
    String details) {}
