package com.spindle.core.minecraft.hook.transform;

public record MinecraftFixtureCodePatchResult(
    Integer originalCodeLength,
    Integer transformedCodeLength,
    String originalCodeSha256,
    String transformedCodeSha256,
    Integer maxStackBefore,
    Integer maxStackAfter,
    Integer maxLocalsBefore,
    Integer maxLocalsAfter,
    int exceptionTableCount,
    boolean exceptionTableShiftApplied,
    String insertedInstructionHex) {}
