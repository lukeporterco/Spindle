package com.spindle.core.minecraft.hook.steelhook;

import java.util.List;

public record SteelHook03FramedMethodFoundationReport(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    String sourceSteelHook02Milestone,
    boolean sourceSteelHook02CompletionReady,
    String sourceSteelHook02HandoffStatus,
    boolean framedMethodFoundationReady,
    SteelHook03FramedMethodFoundationStatus status,
    SteelHook03FramedMethodFoundationNextDirection nextDirection,
    boolean stackMapTableRewriteSupported,
    boolean stackMapTableRewriteApplied,
    boolean stackMapTableFrameShiftApplied,
    Integer stackMapTableEntryCountBefore,
    Integer stackMapTableEntryCountAfter,
    Integer firstFrameOffsetDeltaBefore,
    Integer firstFrameOffsetDeltaAfter,
    int insertionOffset,
    int insertedInstructionLength,
    boolean methodEntryTransformationOccurred,
    boolean bytecodeModified,
    boolean transformedClassBytesProduced,
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
    List<SteelHook03FramedMethodFoundationFinding> findings) {
  public SteelHook03FramedMethodFoundationReport {
    findings = List.copyOf(findings == null ? List.of() : findings);
  }
}
