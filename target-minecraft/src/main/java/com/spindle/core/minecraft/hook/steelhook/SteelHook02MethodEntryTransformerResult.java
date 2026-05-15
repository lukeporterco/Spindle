package com.spindle.core.minecraft.hook.steelhook;

import java.util.List;

public record SteelHook02MethodEntryTransformerResult(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    String sourcePatchPlanMilestone,
    String sourcePrimitiveBoundaryMilestone,
    String sourceContractGeneralizationMilestone,
    boolean localTransformationOnly,
    boolean runtimeClassLoadingPathEnabled,
    boolean classLoadingOccurred,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean realMinecraftRuntimeTransformed,
    boolean publicApiExposed,
    boolean javaAgentUsed,
    boolean mixinUsed,
    boolean javaModExecutionSandboxed,
    boolean minecraftRuntimeTransformReady,
    boolean target25TransformerExtractionOccurred,
    boolean methodEntryTransformationOccurred,
    boolean bytecodeModified,
    boolean transformedClassBytesProduced,
    boolean eligibleForTarget26GatedRuntimeTransformation,
    boolean gatePassed,
    SteelHook02MethodEntryTransformerStatus status,
    SteelHook02MethodEntryTransformerNextDirection nextDirection,
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
    SteelHook02MethodEntryTransformerGate gate,
    SteelHook02TargetDescriptor targetDescriptor,
    SteelHook02DispatcherDescriptor dispatcherDescriptor,
    SteelHook02PrimitiveContract primitiveContract,
    SteelHook02GeneralizedPatchPlan generalizedPatchPlan,
    SteelHook02TargetClassBytes targetClassBytes,
    List<SteelHook02MethodEntryTransformerFinding> findings) {
  public SteelHook02MethodEntryTransformerResult {
    findings = List.copyOf(findings == null ? List.of() : findings);
  }
}
