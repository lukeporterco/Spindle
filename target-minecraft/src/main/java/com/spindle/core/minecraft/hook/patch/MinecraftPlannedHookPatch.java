package com.spindle.core.minecraft.hook.patch;

import java.util.List;

public record MinecraftPlannedHookPatch(
    String id,
    String sourcePlacementId,
    String sourceContractId,
    String sourceBytecodeAnalysisMilestone,
    String catalogId,
    MinecraftHookPatchKind kind,
    MinecraftHookPatchMode mode,
    MinecraftHookPatchEligibility patchEligibility,
    String ownerInternalName,
    String memberName,
    String descriptor,
    int insertionOffset,
    boolean required,
    MinecraftPatchCodeInsertion codeInsertion,
    List<MinecraftPatchConstantPoolRequirement> requiredConstantPoolEntries,
    boolean constantPoolRewriteRequired,
    boolean codeRewriteRequired,
    boolean maxStackRewriteRequired,
    boolean maxLocalsRewriteRequired,
    boolean exceptionTableRewriteRequired,
    boolean stackMapTableRewriteRequired,
    boolean nestedCodeAttributeRewriteRequired,
    boolean lineNumberTableRewriteRequired,
    boolean localVariableTableRewriteRequired,
    boolean branchOffsetRewriteRequired,
    boolean switchOffsetRewriteRequired,
    MinecraftPatchOffsetAdjustmentSummary branchTargetAdjustmentSummary,
    MinecraftPatchOffsetAdjustmentSummary switchTargetAdjustmentSummary,
    MinecraftPatchExceptionTableImpact exceptionTableImpact,
    MinecraftPatchStackMapImpact stackMapImpact,
    MinecraftPatchNestedAttributeImpact nestedAttributeImpact,
    boolean transformReadyForFixtureOnly,
    boolean transformReadyForMinecraftRuntime) {
  public MinecraftPlannedHookPatch {
    requiredConstantPoolEntries =
        List.copyOf(requiredConstantPoolEntries == null ? List.of() : requiredConstantPoolEntries);
  }
}
