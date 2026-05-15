package com.spindle.core.minecraft.hook.steelhook;

public record SteelHook02MethodEntryTransformerFinding(
    String id,
    String checkName,
    SteelHook02MethodEntryTransformerFindingStatus status,
    boolean blocking,
    String summary,
    String notes) {}
