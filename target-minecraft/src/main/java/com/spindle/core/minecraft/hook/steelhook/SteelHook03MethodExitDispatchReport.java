package com.spindle.core.minecraft.hook.steelhook;

import java.util.List;

public record SteelHook03MethodExitDispatchReport(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    SteelHook03PrimitiveKind primitiveKind,
    String sourceTarget28Milestone,
    String sourceTarget28Status,
    boolean sourceTarget28FramedMethodFoundationReady,
    String sourceTarget28NextDirection,
    boolean methodExitDispatchReady,
    SteelHook03MethodExitDispatchStatus status,
    SteelHook03MethodExitDispatchNextDirection nextDirection,
    String targetOwnerInternalName,
    String targetMethodName,
    String targetDescriptor,
    String dispatcherOwnerInternalName,
    String dispatcherMethodName,
    String dispatcherDescriptor,
    String opcodeMnemonic,
    String opcodeHex,
    int insertedInstructionLength,
    List<String> supportedReturnOpcodes,
    Integer normalReturnOpcodeCount,
    Integer insertionCount,
    List<Integer> insertionOffsetsOriginal,
    List<Integer> insertionOffsetsTransformed,
    Integer originalCodeLength,
    Integer transformedCodeLength,
    Integer constantPoolCountBefore,
    Integer constantPoolCountAfter,
    boolean methodExitTransformationOccurred,
    boolean bytecodeModified,
    boolean transformedClassBytesProduced,
    boolean stackMapTablePresent,
    boolean stackMapTableRewriteSupported,
    boolean stackMapTableRewriteApplied,
    boolean exceptionTablePresent,
    boolean branchRewriteRequired,
    boolean switchRewriteRequired,
    boolean runtimeClassLoadingPathEnabled,
    boolean classLoadingOccurred,
    boolean serverLaunchOccurred,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean publicApiExposed,
    boolean javaAgentUsed,
    boolean mixinUsed,
    boolean javaModExecutionSandboxed,
    String failureReason,
    List<SteelHook03MethodExitDispatchFinding> findings) {
  public SteelHook03MethodExitDispatchReport {
    supportedReturnOpcodes =
        List.copyOf(supportedReturnOpcodes == null ? List.of() : supportedReturnOpcodes);
    insertionOffsetsOriginal =
        List.copyOf(insertionOffsetsOriginal == null ? List.of() : insertionOffsetsOriginal);
    insertionOffsetsTransformed =
        List.copyOf(insertionOffsetsTransformed == null ? List.of() : insertionOffsetsTransformed);
    findings = List.copyOf(findings == null ? List.of() : findings);
  }
}
