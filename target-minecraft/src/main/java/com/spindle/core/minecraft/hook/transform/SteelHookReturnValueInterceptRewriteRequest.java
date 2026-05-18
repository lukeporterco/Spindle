package com.spindle.core.minecraft.hook.transform;

import com.spindle.core.minecraft.hook.steelhook.SteelHook04PrimitiveKind;

public record SteelHookReturnValueInterceptRewriteRequest(
    String id,
    String scope,
    String sourceMilestone,
    String sourceReportId,
    SteelHook04PrimitiveKind primitiveKind,
    SteelHookReturnValueInterceptMode mode,
    String targetOwnerInternalName,
    String targetBinaryName,
    String targetClassEntryName,
    String targetMethodName,
    String targetDescriptor,
    SteelHookReturnValueInterceptKind interceptKind,
    Integer replacementPrimitiveValue,
    String replacementReferenceValue,
    boolean runtimeClassLoadingPathEnabled,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed) {}
