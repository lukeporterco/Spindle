package com.spindle.core.minecraft.hook.patch;

import java.util.List;

public record MinecraftPatchExceptionTableImpact(
    boolean exceptionTablePresent,
    int exceptionTableCount,
    int offsetDelta,
    boolean futureRewriteRequired,
    int adjustedFieldCount,
    List<String> adjustedFields) {
  public MinecraftPatchExceptionTableImpact {
    adjustedFields = List.copyOf(adjustedFields == null ? List.of() : adjustedFields);
  }
}
