package com.spindle.core.minecraft.hook.transform;

import java.util.List;

public record SteelHookMethodExitCodePatchResult(
    int originalCodeLength,
    int transformedCodeLength,
    String originalCodeSha256,
    String transformedCodeSha256,
    int maxStackBefore,
    int maxStackAfter,
    int maxLocalsBefore,
    int maxLocalsAfter,
    int exceptionTableCount,
    boolean exceptionTablePresent,
    int normalReturnOpcodeCount,
    int insertionCount,
    List<Integer> insertionOffsetsOriginal,
    List<Integer> insertionOffsetsTransformed,
    List<String> supportedReturnOpcodes,
    String insertedInstructionHex) {
  public SteelHookMethodExitCodePatchResult {
    insertionOffsetsOriginal =
        List.copyOf(insertionOffsetsOriginal == null ? List.of() : insertionOffsetsOriginal);
    insertionOffsetsTransformed =
        List.copyOf(insertionOffsetsTransformed == null ? List.of() : insertionOffsetsTransformed);
    supportedReturnOpcodes =
        List.copyOf(supportedReturnOpcodes == null ? List.of() : supportedReturnOpcodes);
  }
}
