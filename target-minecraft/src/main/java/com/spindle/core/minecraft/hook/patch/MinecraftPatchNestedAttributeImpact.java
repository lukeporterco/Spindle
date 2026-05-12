package com.spindle.core.minecraft.hook.patch;

import java.util.List;

public record MinecraftPatchNestedAttributeImpact(
    int nestedCodeAttributeCount,
    boolean stackMapTablePresent,
    boolean lineNumberTablePresent,
    boolean localVariableTablePresent,
    boolean localVariableTypeTablePresent,
    boolean stackMapTableRewriteRequired,
    boolean lineNumberTableRewriteRequired,
    boolean localVariableTableRewriteRequired,
    boolean localVariableTypeTableRewriteRequired,
    boolean futureRewriteRequired,
    List<String> presentAttributeNames) {
  public MinecraftPatchNestedAttributeImpact {
    presentAttributeNames =
        List.copyOf(presentAttributeNames == null ? List.of() : presentAttributeNames);
  }
}
