package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.MinecraftSide;

public record SteelHook02TargetDescriptor(
    String id,
    String ownerInternalName,
    String binaryName,
    String classEntryName,
    String memberName,
    String descriptor,
    MinecraftSide side,
    String minecraftVersion,
    String sourceContractId,
    String sourcePlacementId,
    String sourcePatchId,
    int insertionOffset,
    boolean methodEntryOnly) {}
