package com.spindle.core.minecraft.hook.steelhook;

public record SteelHook04ReturnValueInterceptFinding(
    String id,
    String checkName,
    SteelHook04ReturnValueInterceptFindingStatus status,
    boolean blocking,
    String summary,
    String details) {}
