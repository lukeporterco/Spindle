package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptKind;
import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptMode;

public record SteelHook04ReturnValueInterceptProofCase(
    String id,
    String label,
    SteelHookReturnValueInterceptMode mode,
    SteelHook04FixtureShape fixtureShape,
    SteelHookReturnValueInterceptKind interceptKind,
    String targetOwnerInternalName,
    String targetMethodName,
    String targetDescriptor,
    String returnOpcode,
    String producerOpcode,
    int matchCount,
    boolean bytecodeModified,
    boolean transformedClassBytesProduced,
    String originalClassSha256,
    String transformedClassSha256,
    String originalCodeSha256,
    String transformedCodeSha256,
    int originalCodeLength,
    int transformedCodeLength,
    String replacementSummary) {}
