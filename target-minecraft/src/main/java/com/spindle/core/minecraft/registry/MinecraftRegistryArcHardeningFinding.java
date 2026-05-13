package com.spindle.core.minecraft.registry;

public record MinecraftRegistryArcHardeningFinding(
    String id,
    String sourceMilestoneName,
    String checkName,
    MinecraftRegistryArcHardeningFindingStatus status,
    boolean blocking,
    String summary,
    String notes) {}
