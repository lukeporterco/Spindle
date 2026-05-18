package com.spindle.core.minecraft.hook.steelhook;

public record SteelHook04InvokeCallsiteFinding(
    String id,
    String checkName,
    SteelHook04InvokeCallsiteFindingStatus status,
    boolean blocking,
    String summary,
    String details) {}
