package com.spindle.core.minecraft.hook.bytecode;

import java.util.List;

public record MinecraftDecodedInstruction(
    int offset,
    int opcode,
    String mnemonic,
    int length,
    MinecraftDecodedInstructionKind kind,
    String operandHex,
    List<Integer> branchTargetOffsets,
    Integer switchDefaultTargetOffset,
    List<MinecraftDecodedBranchTarget> switchMatchTargetPairs,
    Integer wideModifiedOpcode) {
  public MinecraftDecodedInstruction {
    branchTargetOffsets =
        List.copyOf(branchTargetOffsets == null ? List.of() : branchTargetOffsets);
    switchMatchTargetPairs =
        List.copyOf(switchMatchTargetPairs == null ? List.of() : switchMatchTargetPairs);
    operandHex = operandHex == null ? "" : operandHex;
  }
}
