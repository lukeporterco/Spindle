package com.spindle.core.minecraft.hook.patch;

import java.util.List;

public record MinecraftPatchOffsetAdjustmentSummary(
    String category,
    int offsetDelta,
    int adjustedTargetCount,
    List<Integer> sourceInstructionOffsets,
    List<Integer> adjustedTargetOffsets,
    boolean futureRewriteRequired,
    String adjustmentRule) {
  public MinecraftPatchOffsetAdjustmentSummary {
    sourceInstructionOffsets =
        List.copyOf(sourceInstructionOffsets == null ? List.of() : sourceInstructionOffsets);
    adjustedTargetOffsets =
        List.copyOf(adjustedTargetOffsets == null ? List.of() : adjustedTargetOffsets);
  }
}
