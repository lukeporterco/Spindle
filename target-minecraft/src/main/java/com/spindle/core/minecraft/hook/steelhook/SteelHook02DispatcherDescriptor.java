package com.spindle.core.minecraft.hook.steelhook;

public record SteelHook02DispatcherDescriptor(
    String id,
    String ownerInternalName,
    String binaryName,
    String methodName,
    String descriptor,
    String opcodeMnemonic,
    String opcodeHex,
    int instructionLength,
    int maxStackDelta,
    int maxLocalsDelta,
    boolean requiresVoidNoArgs,
    boolean publicApiExposed) {}
