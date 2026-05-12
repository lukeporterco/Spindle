package com.spindle.core.minecraft.hook.bytecode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftHookBytecodeAnalysisWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftHookBytecodeAnalysisReport report)
      throws LoaderException {
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(toJson(report), writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write Minecraft hook bytecode analysis report " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftHookBytecodeAnalysisReport report) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", report.schema());
    addString(root, "milestoneName", report.milestoneName());
    addString(root, "target", report.target());
    addString(root, "minecraftVersion", report.minecraftVersion());
    addString(root, "side", report.side());
    addString(root, "catalogId", report.catalogId());
    root.addProperty("sourceContractValidationPassed", report.sourceContractValidationPassed());
    root.addProperty("sourceContractErrorCount", report.sourceContractErrorCount());
    addString(root, "minecraftMainClass", report.minecraftMainClass());
    addString(root, "placementId", report.placementId());
    addString(root, "sourceContractId", report.sourceContractId());
    addString(root, "ownerInternalName", report.ownerInternalName());
    addString(root, "memberName", report.memberName());
    addString(root, "descriptor", report.descriptor());
    addInteger(root, "bytecodeOffset", report.bytecodeOffset());
    root.addProperty("gatePassed", report.gatePassed());
    addString(root, "gateFailureReason", report.gateFailureReason());
    root.addProperty("bytecodeAnalysisSucceeded", report.bytecodeAnalysisSucceeded());
    root.addProperty("codeAttributeParsed", report.codeAttributeParsed());
    root.addProperty("instructionInspectionOccurred", report.instructionInspectionOccurred());
    root.addProperty("instructionStreamDecoded", report.instructionStreamDecoded());
    root.addProperty(
        "instructionBoundaryValidationPassed", report.instructionBoundaryValidationPassed());
    root.addProperty("branchTargetValidationPassed", report.branchTargetValidationPassed());
    root.addProperty("switchTargetValidationPassed", report.switchTargetValidationPassed());
    root.addProperty("exceptionTableValidationPassed", report.exceptionTableValidationPassed());
    root.addProperty("injectionOccurred", report.injectionOccurred());
    root.addProperty("transformationOccurred", report.transformationOccurred());
    root.addProperty("patchingOccurred", report.patchingOccurred());
    root.addProperty("bytecodeModified", report.bytecodeModified());
    root.addProperty("javaAgentUsed", report.javaAgentUsed());
    root.addProperty("mixinUsed", report.mixinUsed());
    root.addProperty("remappingOccurred", report.remappingOccurred());
    root.addProperty("publicApiExposed", report.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", report.javaModExecutionSandboxed());
    root.addProperty("instructionCount", report.instructionCount());
    addInteger(root, "firstInstructionOffset", report.firstInstructionOffset());
    addInteger(root, "lastInstructionOffset", report.lastInstructionOffset());
    addInteger(root, "codeLength", report.codeLength());
    addString(root, "codeSha256", report.codeSha256());
    root.addProperty("stackMapTablePresent", report.stackMapTablePresent());
    addInteger(root, "stackMapTableEntryCount", report.stackMapTableEntryCount());
    root.addProperty("nestedCodeAttributeCount", report.nestedCodeAttributeCount());
    root.addProperty("exceptionTableCount", report.exceptionTableCount());
    root.addProperty("returnInstructionCount", report.returnInstructionCount());
    root.addProperty("throwInstructionCount", report.throwInstructionCount());
    root.addProperty("invokeInstructionCount", report.invokeInstructionCount());
    root.addProperty("branchInstructionCount", report.branchInstructionCount());
    root.addProperty("switchInstructionCount", report.switchInstructionCount());
    root.addProperty("wideInstructionCount", report.wideInstructionCount());
    root.addProperty("reservedOpcodeCount", report.reservedOpcodeCount());
    root.addProperty("unsupportedOpcodeCount", report.unsupportedOpcodeCount());
    root.addProperty("methodEntryInstructionBoundary", report.methodEntryInstructionBoundary());
    root.add("decodedInstructions", decodedInstructions(report.decodedInstructions()));
    root.add("exceptionHandlers", exceptionHandlers(report.exceptionHandlers()));
    root.add("nestedCodeAttributes", nestedCodeAttributes(report.nestedCodeAttributes()));
    return root;
  }

  private JsonArray decodedInstructions(
      java.util.List<MinecraftDecodedInstruction> decodedInstructions) {
    JsonArray array = new JsonArray();
    for (MinecraftDecodedInstruction instruction : decodedInstructions) {
      JsonObject object = new JsonObject();
      object.addProperty("offset", instruction.offset());
      object.addProperty("opcode", instruction.opcode());
      addString(object, "mnemonic", instruction.mnemonic());
      object.addProperty("length", instruction.length());
      addString(object, "kind", instruction.kind() == null ? null : instruction.kind().name());
      addString(object, "operandHex", instruction.operandHex());
      object.add("branchTargetOffsets", integers(instruction.branchTargetOffsets()));
      addInteger(object, "switchDefaultTargetOffset", instruction.switchDefaultTargetOffset());
      object.add("switchMatchTargetPairs", branchTargets(instruction.switchMatchTargetPairs()));
      addInteger(object, "wideModifiedOpcode", instruction.wideModifiedOpcode());
      array.add(object);
    }
    return array;
  }

  private JsonArray exceptionHandlers(
      java.util.List<MinecraftDecodedExceptionHandler> exceptionHandlers) {
    JsonArray array = new JsonArray();
    for (MinecraftDecodedExceptionHandler handler : exceptionHandlers) {
      JsonObject object = new JsonObject();
      object.addProperty("startPc", handler.startPc());
      object.addProperty("endPc", handler.endPc());
      object.addProperty("handlerPc", handler.handlerPc());
      addInteger(object, "catchTypeConstantPoolIndex", handler.catchTypeConstantPoolIndex());
      array.add(object);
    }
    return array;
  }

  private JsonArray nestedCodeAttributes(
      java.util.List<MinecraftCodeNestedAttributeSummary> nestedCodeAttributes) {
    JsonArray array = new JsonArray();
    for (MinecraftCodeNestedAttributeSummary nestedAttribute : nestedCodeAttributes) {
      JsonObject object = new JsonObject();
      addString(object, "name", nestedAttribute.name());
      object.addProperty("length", nestedAttribute.length());
      addInteger(object, "entryCount", nestedAttribute.entryCount());
      array.add(object);
    }
    return array;
  }

  private JsonArray branchTargets(java.util.List<MinecraftDecodedBranchTarget> branchTargets) {
    JsonArray array = new JsonArray();
    for (MinecraftDecodedBranchTarget branchTarget : branchTargets) {
      JsonObject object = new JsonObject();
      addInteger(object, "matchValue", branchTarget.matchValue());
      object.addProperty("targetOffset", branchTarget.targetOffset());
      array.add(object);
    }
    return array;
  }

  private JsonArray integers(java.util.List<Integer> values) {
    JsonArray array = new JsonArray();
    for (Integer value : values) {
      if (value == null) {
        array.add(JsonNull.INSTANCE);
      } else {
        array.add(value);
      }
    }
    return array;
  }

  private void addInteger(JsonObject object, String name, Integer value) {
    if (value == null) {
      object.add(name, JsonNull.INSTANCE);
    } else {
      object.addProperty(name, value);
    }
  }

  private void addString(JsonObject object, String name, String value) {
    if (value == null) {
      object.add(name, JsonNull.INSTANCE);
    } else {
      object.addProperty(name, value);
    }
  }
}
