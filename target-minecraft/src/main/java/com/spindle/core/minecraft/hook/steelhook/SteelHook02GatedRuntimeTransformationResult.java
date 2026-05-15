package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.MinecraftClassLoadingAudit;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationMode;
import java.util.List;

public record SteelHook02GatedRuntimeTransformationResult(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    String sourcePatchPlanMilestone,
    String sourcePrimitiveBoundaryMilestone,
    String sourceContractGeneralizationMilestone,
    String sourceMethodEntryTransformerMilestone,
    boolean runtimeClassLoadingPathEnabled,
    boolean runtimeClassLoadingAttempted,
    boolean runtimeClassLoadingSucceeded,
    boolean classLoadingOccurred,
    boolean targetClassDefined,
    MinecraftBootstrapHookTransformationMode transformationMode,
    String targetBinaryName,
    String targetClassEntryName,
    String definedClassName,
    boolean definedBySteelHookRuntimeClassLoader,
    boolean realMinecraftRuntimeTransformed,
    boolean methodEntryTransformationOccurred,
    boolean bytecodeModified,
    boolean transformedClassBytesProduced,
    boolean minecraftMainInvoked,
    boolean minecraftServerLaunched,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean dispatcherInvocationObserved,
    boolean publicApiExposed,
    boolean javaAgentUsed,
    boolean mixinUsed,
    boolean remappingOccurred,
    boolean accessWidenersUsed,
    boolean javaModExecutionSandboxed,
    boolean minecraftRuntimeTransformReady,
    boolean eligibleForTarget27CompletionVerification,
    boolean gatePassed,
    SteelHook02GatedRuntimeTransformationStatus status,
    SteelHook02GatedRuntimeTransformationNextDirection nextDirection,
    String failureReason,
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
    MinecraftClassLoadingAudit.Summary classLoadingAuditSummary,
    SteelHook02GatedRuntimeTransformationGate gate,
    List<SteelHook02GatedRuntimeTransformationFinding> findings) {
  public SteelHook02GatedRuntimeTransformationResult {
    findings = List.copyOf(findings == null ? List.of() : findings);
  }
}
