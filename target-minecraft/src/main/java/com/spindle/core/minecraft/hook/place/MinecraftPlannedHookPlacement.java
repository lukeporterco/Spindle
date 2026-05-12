package com.spindle.core.minecraft.hook.place;

public record MinecraftPlannedHookPlacement(
    String id,
    String sourceContractId,
    String catalogId,
    MinecraftHookPlacementKind kind,
    String ownerInternalName,
    String memberName,
    String descriptor,
    int bytecodeOffset,
    MinecraftHookPlacementMode mode,
    boolean required,
    MinecraftMethodCodeSummary methodCodeSummary) {}
