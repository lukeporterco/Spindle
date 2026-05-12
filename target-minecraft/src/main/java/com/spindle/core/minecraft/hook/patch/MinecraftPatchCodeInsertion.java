package com.spindle.core.minecraft.hook.patch;

public record MinecraftPatchCodeInsertion(
    String dispatcherOwnerInternalName,
    String dispatcherMethodName,
    String dispatcherDescriptor,
    String plannedOpcode,
    String plannedOpcodeHex,
    int plannedInstructionLength,
    int plannedStackDelta,
    int requiredMaxStackIncrease,
    String insertedInstructionHex) {}
