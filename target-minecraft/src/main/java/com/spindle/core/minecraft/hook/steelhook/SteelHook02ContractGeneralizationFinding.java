package com.spindle.core.minecraft.hook.steelhook;

public record SteelHook02ContractGeneralizationFinding(
    String id,
    String checkName,
    SteelHook02ContractGeneralizationFindingStatus status,
    boolean blocking,
    String summary,
    String notes) {}
