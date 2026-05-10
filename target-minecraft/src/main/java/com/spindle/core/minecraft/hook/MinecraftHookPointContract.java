package com.spindle.core.minecraft.hook;

import com.spindle.core.minecraft.MinecraftSide;

public record MinecraftHookPointContract(
    String id,
    String description,
    MinecraftSide side,
    MinecraftHookPointKind kind,
    String ownerInternalName,
    String memberName,
    String descriptor,
    MinecraftHookRequirement requirement) {}
