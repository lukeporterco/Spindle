package com.spindle.core.minecraft.hook.steelhook;

public record SteelHook02PrimitiveFinding(
    String id,
    String checkName,
    SteelHook02PrimitiveFindingStatus status,
    boolean blocking,
    String summary,
    String notes) {}
