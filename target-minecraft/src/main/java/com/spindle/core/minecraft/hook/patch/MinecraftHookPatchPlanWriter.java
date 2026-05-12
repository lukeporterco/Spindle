package com.spindle.core.minecraft.hook.patch;

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

public final class MinecraftHookPatchPlanWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftHookPatchPlan plan) throws LoaderException {
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(toJson(plan), writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write Minecraft hook patch plan " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftHookPatchPlan plan) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", plan.schema());
    addString(root, "milestoneName", plan.milestoneName());
    addString(root, "target", plan.target());
    addString(root, "minecraftVersion", plan.minecraftVersion());
    addString(root, "side", plan.side());
    addString(root, "catalogId", plan.catalogId());
    root.addProperty("sourceContractValidationPassed", plan.sourceContractValidationPassed());
    root.addProperty("sourceContractErrorCount", plan.sourceContractErrorCount());
    addString(root, "minecraftMainClass", plan.minecraftMainClass());
    root.addProperty("gatePassed", plan.gatePassed());
    addString(root, "gateFailureReason", plan.gateFailureReason());
    root.addProperty("patchPlanningSucceeded", plan.patchPlanningSucceeded());
    root.addProperty("patchPlanned", plan.patchPlanned());
    root.addProperty("plannedPatchCount", plan.plannedPatchCount());
    addString(
        root,
        "patchEligibility",
        plan.patchEligibility() == null ? null : plan.patchEligibility().id());
    addString(root, "selectedPlacementId", plan.selectedPlacementId());
    addInteger(root, "selectedBytecodeAnalysisSchema", plan.selectedBytecodeAnalysisSchema());
    addString(root, "selectedBytecodeAnalysisMilestone", plan.selectedBytecodeAnalysisMilestone());
    addString(root, "targetClass", plan.targetClass());
    addString(root, "targetMethod", plan.targetMethod());
    addString(root, "targetDescriptor", plan.targetDescriptor());
    addInteger(root, "originalCodeLength", plan.originalCodeLength());
    addInteger(root, "plannedCodeLength", plan.plannedCodeLength());
    addInteger(root, "codeLengthDelta", plan.codeLengthDelta());
    addString(root, "originalCodeSha256", plan.originalCodeSha256());
    addInteger(root, "insertionOffset", plan.insertionOffset());
    root.addProperty("insertionInstructionBoundary", plan.insertionInstructionBoundary());
    addInteger(
        root,
        "insertBeforeOriginalInstructionOffset",
        plan.insertBeforeOriginalInstructionOffset());
    addString(root, "insertedInstructionHex", plan.insertedInstructionHex());
    root.add(
        "requiredConstantPoolEntries",
        constantPoolRequirements(plan.requiredConstantPoolEntries()));
    root.addProperty("constantPoolRewriteRequired", plan.constantPoolRewriteRequired());
    root.addProperty("codeRewriteRequired", plan.codeRewriteRequired());
    root.addProperty("maxStackRewriteRequired", plan.maxStackRewriteRequired());
    root.addProperty("maxLocalsRewriteRequired", plan.maxLocalsRewriteRequired());
    root.addProperty("exceptionTableRewriteRequired", plan.exceptionTableRewriteRequired());
    root.addProperty("stackMapTableRewriteRequired", plan.stackMapTableRewriteRequired());
    root.addProperty(
        "nestedCodeAttributeRewriteRequired", plan.nestedCodeAttributeRewriteRequired());
    root.addProperty("lineNumberTableRewriteRequired", plan.lineNumberTableRewriteRequired());
    root.addProperty("localVariableTableRewriteRequired", plan.localVariableTableRewriteRequired());
    root.addProperty("branchOffsetRewriteRequired", plan.branchOffsetRewriteRequired());
    root.addProperty("switchOffsetRewriteRequired", plan.switchOffsetRewriteRequired());
    root.addProperty("transformReadyForFixtureOnly", plan.transformReadyForFixtureOnly());
    root.addProperty("transformReadyForMinecraftRuntime", plan.transformReadyForMinecraftRuntime());
    root.addProperty("codeAttributeParsed", plan.codeAttributeParsed());
    root.addProperty("instructionInspectionOccurred", plan.instructionInspectionOccurred());
    root.addProperty("patchPlanningOccurred", plan.patchPlanningOccurred());
    root.addProperty("injectionOccurred", plan.injectionOccurred());
    root.addProperty("transformationOccurred", plan.transformationOccurred());
    root.addProperty("patchingOccurred", plan.patchingOccurred());
    root.addProperty("bytecodeModified", plan.bytecodeModified());
    root.addProperty("javaAgentUsed", plan.javaAgentUsed());
    root.addProperty("mixinUsed", plan.mixinUsed());
    root.addProperty("remappingOccurred", plan.remappingOccurred());
    root.addProperty("publicApiExposed", plan.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", plan.javaModExecutionSandboxed());
    addObject(
        root,
        "branchTargetAdjustmentSummary",
        offsetAdjustmentSummary(plan.branchTargetAdjustmentSummary()));
    addObject(
        root,
        "switchTargetAdjustmentSummary",
        offsetAdjustmentSummary(plan.switchTargetAdjustmentSummary()));
    addObject(root, "exceptionTableImpact", exceptionTableImpact(plan.exceptionTableImpact()));
    addObject(root, "stackMapImpact", stackMapImpact(plan.stackMapImpact()));
    addObject(root, "nestedAttributeImpact", nestedAttributeImpact(plan.nestedAttributeImpact()));
    root.add("plannedPatches", plannedPatches(plan.plannedPatches()));
    return root;
  }

  private JsonArray plannedPatches(java.util.List<MinecraftPlannedHookPatch> plannedPatches) {
    JsonArray array = new JsonArray();
    for (MinecraftPlannedHookPatch plannedPatch : plannedPatches) {
      JsonObject object = new JsonObject();
      addString(object, "id", plannedPatch.id());
      addString(object, "sourcePlacementId", plannedPatch.sourcePlacementId());
      addString(object, "sourceContractId", plannedPatch.sourceContractId());
      addString(
          object,
          "sourceBytecodeAnalysisMilestone",
          plannedPatch.sourceBytecodeAnalysisMilestone());
      addString(object, "catalogId", plannedPatch.catalogId());
      addString(object, "kind", plannedPatch.kind() == null ? null : plannedPatch.kind().name());
      addString(object, "mode", plannedPatch.mode() == null ? null : plannedPatch.mode().id());
      addString(
          object,
          "patchEligibility",
          plannedPatch.patchEligibility() == null ? null : plannedPatch.patchEligibility().id());
      addString(object, "ownerInternalName", plannedPatch.ownerInternalName());
      addString(object, "memberName", plannedPatch.memberName());
      addString(object, "descriptor", plannedPatch.descriptor());
      object.addProperty("insertionOffset", plannedPatch.insertionOffset());
      object.addProperty("required", plannedPatch.required());
      addObject(object, "codeInsertion", codeInsertion(plannedPatch.codeInsertion()));
      object.add(
          "requiredConstantPoolEntries",
          constantPoolRequirements(plannedPatch.requiredConstantPoolEntries()));
      object.addProperty("constantPoolRewriteRequired", plannedPatch.constantPoolRewriteRequired());
      object.addProperty("codeRewriteRequired", plannedPatch.codeRewriteRequired());
      object.addProperty("maxStackRewriteRequired", plannedPatch.maxStackRewriteRequired());
      object.addProperty("maxLocalsRewriteRequired", plannedPatch.maxLocalsRewriteRequired());
      object.addProperty(
          "exceptionTableRewriteRequired", plannedPatch.exceptionTableRewriteRequired());
      object.addProperty(
          "stackMapTableRewriteRequired", plannedPatch.stackMapTableRewriteRequired());
      object.addProperty(
          "nestedCodeAttributeRewriteRequired", plannedPatch.nestedCodeAttributeRewriteRequired());
      object.addProperty(
          "lineNumberTableRewriteRequired", plannedPatch.lineNumberTableRewriteRequired());
      object.addProperty(
          "localVariableTableRewriteRequired", plannedPatch.localVariableTableRewriteRequired());
      object.addProperty("branchOffsetRewriteRequired", plannedPatch.branchOffsetRewriteRequired());
      object.addProperty("switchOffsetRewriteRequired", plannedPatch.switchOffsetRewriteRequired());
      addObject(
          object,
          "branchTargetAdjustmentSummary",
          offsetAdjustmentSummary(plannedPatch.branchTargetAdjustmentSummary()));
      addObject(
          object,
          "switchTargetAdjustmentSummary",
          offsetAdjustmentSummary(plannedPatch.switchTargetAdjustmentSummary()));
      addObject(
          object,
          "exceptionTableImpact",
          exceptionTableImpact(plannedPatch.exceptionTableImpact()));
      addObject(object, "stackMapImpact", stackMapImpact(plannedPatch.stackMapImpact()));
      addObject(
          object,
          "nestedAttributeImpact",
          nestedAttributeImpact(plannedPatch.nestedAttributeImpact()));
      object.addProperty(
          "transformReadyForFixtureOnly", plannedPatch.transformReadyForFixtureOnly());
      object.addProperty(
          "transformReadyForMinecraftRuntime", plannedPatch.transformReadyForMinecraftRuntime());
      array.add(object);
    }
    return array;
  }

  private JsonObject codeInsertion(MinecraftPatchCodeInsertion codeInsertion) {
    if (codeInsertion == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    addString(object, "dispatcherOwnerInternalName", codeInsertion.dispatcherOwnerInternalName());
    addString(object, "dispatcherMethodName", codeInsertion.dispatcherMethodName());
    addString(object, "dispatcherDescriptor", codeInsertion.dispatcherDescriptor());
    addString(object, "plannedOpcode", codeInsertion.plannedOpcode());
    addString(object, "plannedOpcodeHex", codeInsertion.plannedOpcodeHex());
    object.addProperty("plannedInstructionLength", codeInsertion.plannedInstructionLength());
    object.addProperty("plannedStackDelta", codeInsertion.plannedStackDelta());
    object.addProperty("requiredMaxStackIncrease", codeInsertion.requiredMaxStackIncrease());
    addString(object, "insertedInstructionHex", codeInsertion.insertedInstructionHex());
    return object;
  }

  private JsonObject offsetAdjustmentSummary(MinecraftPatchOffsetAdjustmentSummary summary) {
    if (summary == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    addString(object, "category", summary.category());
    object.addProperty("offsetDelta", summary.offsetDelta());
    object.addProperty("adjustedTargetCount", summary.adjustedTargetCount());
    object.add("sourceInstructionOffsets", integers(summary.sourceInstructionOffsets()));
    object.add("adjustedTargetOffsets", integers(summary.adjustedTargetOffsets()));
    object.addProperty("futureRewriteRequired", summary.futureRewriteRequired());
    addString(object, "adjustmentRule", summary.adjustmentRule());
    return object;
  }

  private JsonObject exceptionTableImpact(MinecraftPatchExceptionTableImpact impact) {
    if (impact == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    object.addProperty("exceptionTablePresent", impact.exceptionTablePresent());
    object.addProperty("exceptionTableCount", impact.exceptionTableCount());
    object.addProperty("offsetDelta", impact.offsetDelta());
    object.addProperty("futureRewriteRequired", impact.futureRewriteRequired());
    object.addProperty("adjustedFieldCount", impact.adjustedFieldCount());
    object.add("adjustedFields", strings(impact.adjustedFields()));
    return object;
  }

  private JsonObject stackMapImpact(MinecraftPatchStackMapImpact impact) {
    if (impact == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    object.addProperty("stackMapTablePresent", impact.stackMapTablePresent());
    addInteger(object, "stackMapTableEntryCount", impact.stackMapTableEntryCount());
    object.addProperty("futureRewriteRequired", impact.futureRewriteRequired());
    addString(object, "rewriteReason", impact.rewriteReason());
    return object;
  }

  private JsonObject nestedAttributeImpact(MinecraftPatchNestedAttributeImpact impact) {
    if (impact == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    object.addProperty("nestedCodeAttributeCount", impact.nestedCodeAttributeCount());
    object.addProperty("stackMapTablePresent", impact.stackMapTablePresent());
    object.addProperty("lineNumberTablePresent", impact.lineNumberTablePresent());
    object.addProperty("localVariableTablePresent", impact.localVariableTablePresent());
    object.addProperty("localVariableTypeTablePresent", impact.localVariableTypeTablePresent());
    object.addProperty("stackMapTableRewriteRequired", impact.stackMapTableRewriteRequired());
    object.addProperty("lineNumberTableRewriteRequired", impact.lineNumberTableRewriteRequired());
    object.addProperty(
        "localVariableTableRewriteRequired", impact.localVariableTableRewriteRequired());
    object.addProperty(
        "localVariableTypeTableRewriteRequired", impact.localVariableTypeTableRewriteRequired());
    object.addProperty("futureRewriteRequired", impact.futureRewriteRequired());
    object.add("presentAttributeNames", strings(impact.presentAttributeNames()));
    return object;
  }

  private JsonArray constantPoolRequirements(
      java.util.List<MinecraftPatchConstantPoolRequirement> requirements) {
    JsonArray array = new JsonArray();
    for (MinecraftPatchConstantPoolRequirement requirement : requirements) {
      JsonObject object = new JsonObject();
      addString(object, "entryKind", requirement.entryKind());
      addString(object, "symbolicValue", requirement.symbolicValue());
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

  private JsonArray strings(java.util.List<String> values) {
    JsonArray array = new JsonArray();
    for (String value : values) {
      if (value == null) {
        array.add(JsonNull.INSTANCE);
      } else {
        array.add(value);
      }
    }
    return array;
  }

  private void addObject(JsonObject object, String name, JsonObject child) {
    if (child == null) {
      object.add(name, JsonNull.INSTANCE);
    } else {
      object.add(name, child);
    }
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
