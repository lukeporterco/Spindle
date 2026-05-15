package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchEligibility;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchKind;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchMode;
import java.util.List;

public record SteelHook02GeneralizedPatchPlan(
    String id,
    String sourcePatchPlanMilestone,
    String sourcePatchId,
    String sourceCandidateId,
    String targetDescriptorId,
    String dispatcherDescriptorId,
    MinecraftHookPatchKind patchKind,
    MinecraftHookPatchMode patchMode,
    MinecraftHookPatchEligibility patchEligibility,
    int requiredConstantPoolEntryCount,
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
    boolean fixtureTransformReady,
    boolean minecraftRuntimeTransformReady,
    boolean eligibleForTarget25TransformerExtraction,
    boolean eligibleForTarget26RuntimeTransformation,
    List<String> notes) {
  public SteelHook02GeneralizedPatchPlan {
    notes = List.copyOf(notes == null ? List.of() : notes);
  }
}
