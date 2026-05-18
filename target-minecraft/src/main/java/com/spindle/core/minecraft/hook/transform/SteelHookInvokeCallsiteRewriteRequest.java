package com.spindle.core.minecraft.hook.transform;

import com.spindle.core.minecraft.hook.steelhook.SteelHook04PrimitiveKind;

public record SteelHookInvokeCallsiteRewriteRequest(
    String id,
    String scope,
    String sourceMilestone,
    String sourceReportId,
    SteelHook04PrimitiveKind primitiveKind,
    SteelHookInvokeCallsiteRewriteMode rewriteMode,
    String targetOwnerInternalName,
    String targetBinaryName,
    String targetClassEntryName,
    String targetMethodName,
    String targetDescriptor,
    String expectedInvokeOwnerInternalName,
    String expectedInvokeName,
    String expectedInvokeDescriptor,
    SteelHookInvokeOpcode expectedInvokeOpcode,
    String replacementInvokeOwnerInternalName,
    String replacementInvokeName,
    String replacementInvokeDescriptor,
    SteelHookInvokeOpcode replacementInvokeOpcode,
    boolean runtimeClassLoadingPathEnabled,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed) {}
