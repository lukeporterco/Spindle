package com.spindle.core.minecraft.hook.bytecode;

public record MinecraftDecodedExceptionHandler(
    int startPc, int endPc, int handlerPc, Integer catchTypeConstantPoolIndex) {}
