package com.spindle.core.minecraft.hook.transform;

public record SteelHookMethodExitRewriteRequest(
    String id,
    String scope,
    String sourceMilestone,
    String sourceReportId,
    String targetOwnerInternalName,
    String targetBinaryName,
    String targetClassEntryName,
    String targetMethodName,
    String targetDescriptor,
    String dispatcherOwnerInternalName,
    String dispatcherBinaryName,
    String dispatcherMethodName,
    String dispatcherDescriptor,
    String opcodeMnemonic,
    String opcodeHex,
    int instructionLength,
    boolean stackMapTableRewriteSupported,
    boolean runtimeClassLoadingPathEnabled,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed) {}
