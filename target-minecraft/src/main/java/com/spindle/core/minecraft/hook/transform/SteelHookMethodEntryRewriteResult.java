package com.spindle.core.minecraft.hook.transform;

public record SteelHookMethodEntryRewriteResult(
    SteelHookMethodEntryRewriteStatus status,
    String failureReason,
    SteelHookMethodEntryRewriteRequest request,
    String originalClassSha256,
    String transformedClassSha256,
    String originalCodeSha256,
    String transformedCodeSha256,
    Integer originalCodeLength,
    Integer transformedCodeLength,
    Integer constantPoolCountBefore,
    Integer constantPoolCountAfter,
    Integer methodrefIndex,
    String insertedInstructionHex,
    boolean methodEntryTransformationOccurred,
    boolean bytecodeModified,
    boolean transformedClassBytesProduced,
    boolean stackMapTablePresent,
    boolean stackMapTableRejected,
    SteelHookMethodEntryConstantPoolPatch constantPoolPatch,
    SteelHookMethodEntryCodePatchResult codePatch,
    SteelHookMethodEntryTransformedClass transformedClass) {}
