package com.spindle.core.minecraft.hook.place;

public record MinecraftMethodCodeSummary(
    Integer maxStack,
    Integer maxLocals,
    Integer codeLength,
    String codeSha256,
    Integer exceptionTableCount,
    Integer nestedCodeAttributeCount,
    boolean hasCodeAttribute,
    boolean abstractOrNative,
    Integer methodEntryOffset) {}
