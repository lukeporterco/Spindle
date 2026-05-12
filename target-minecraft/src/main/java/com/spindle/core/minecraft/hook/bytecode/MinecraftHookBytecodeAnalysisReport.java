package com.spindle.core.minecraft.hook.bytecode;

import java.util.List;

public record MinecraftHookBytecodeAnalysisReport(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    String side,
    String catalogId,
    boolean sourceContractValidationPassed,
    int sourceContractErrorCount,
    String minecraftMainClass,
    String placementId,
    String sourceContractId,
    String ownerInternalName,
    String memberName,
    String descriptor,
    Integer bytecodeOffset,
    boolean gatePassed,
    String gateFailureReason,
    boolean bytecodeAnalysisSucceeded,
    boolean codeAttributeParsed,
    boolean instructionInspectionOccurred,
    boolean instructionStreamDecoded,
    boolean instructionBoundaryValidationPassed,
    boolean branchTargetValidationPassed,
    boolean switchTargetValidationPassed,
    boolean exceptionTableValidationPassed,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean bytecodeModified,
    boolean javaAgentUsed,
    boolean mixinUsed,
    boolean remappingOccurred,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed,
    int instructionCount,
    Integer firstInstructionOffset,
    Integer lastInstructionOffset,
    Integer codeLength,
    String codeSha256,
    boolean stackMapTablePresent,
    Integer stackMapTableEntryCount,
    int nestedCodeAttributeCount,
    int exceptionTableCount,
    int returnInstructionCount,
    int throwInstructionCount,
    int invokeInstructionCount,
    int branchInstructionCount,
    int switchInstructionCount,
    int wideInstructionCount,
    int reservedOpcodeCount,
    int unsupportedOpcodeCount,
    boolean methodEntryInstructionBoundary,
    List<MinecraftDecodedInstruction> decodedInstructions,
    List<MinecraftDecodedExceptionHandler> exceptionHandlers,
    List<MinecraftCodeNestedAttributeSummary> nestedCodeAttributes) {
  public MinecraftHookBytecodeAnalysisReport {
    decodedInstructions =
        List.copyOf(decodedInstructions == null ? List.of() : decodedInstructions);
    exceptionHandlers = List.copyOf(exceptionHandlers == null ? List.of() : exceptionHandlers);
    nestedCodeAttributes =
        List.copyOf(nestedCodeAttributes == null ? List.of() : nestedCodeAttributes);
  }
}
