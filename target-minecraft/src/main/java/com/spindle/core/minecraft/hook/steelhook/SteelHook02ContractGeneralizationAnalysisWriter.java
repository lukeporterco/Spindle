package com.spindle.core.minecraft.hook.steelhook;

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
import java.util.List;

public final class SteelHook02ContractGeneralizationAnalysisWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook02ContractGeneralizationAnalysis analysis)
      throws LoaderException {
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(toJson(analysis), writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write SteelHook 0.2 contract generalization analysis " + outputPath,
          exception);
    }
  }

  JsonObject toJson(SteelHook02ContractGeneralizationAnalysis analysis) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", analysis.schema());
    addString(root, "milestoneName", analysis.milestoneName());
    addString(root, "target", analysis.target());
    addString(root, "steelHookVersion", analysis.steelHookVersion());
    addString(root, "minecraftVersion", analysis.minecraftVersion());
    addString(root, "side", analysis.side() == null ? null : analysis.side().id());
    addString(root, "sourcePatchPlanMilestone", analysis.sourcePatchPlanMilestone());
    addString(
        root, "sourcePrimitiveBoundaryMilestone", analysis.sourcePrimitiveBoundaryMilestone());
    root.addProperty("analysisOnly", analysis.analysisOnly());
    root.addProperty("classLoadingOccurred", analysis.classLoadingOccurred());
    root.addProperty("injectionOccurred", analysis.injectionOccurred());
    root.addProperty("transformationOccurred", analysis.transformationOccurred());
    root.addProperty("patchingOccurred", analysis.patchingOccurred());
    root.addProperty("bytecodeModified", analysis.bytecodeModified());
    root.addProperty("hookInstallationOccurred", analysis.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", analysis.runtimeDispatchOccurred());
    root.addProperty("publicApiExposed", analysis.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", analysis.javaModExecutionSandboxed());
    root.addProperty("contractGeneralizationOccurred", analysis.contractGeneralizationOccurred());
    root.addProperty("contractGeneralizationReady", analysis.contractGeneralizationReady());
    root.addProperty("minecraftRuntimeTransformReady", analysis.minecraftRuntimeTransformReady());
    root.addProperty(
        "eligibleForTarget25TransformerExtraction",
        analysis.eligibleForTarget25TransformerExtraction());
    root.addProperty(
        "eligibleForTarget26RuntimeTransformation",
        analysis.eligibleForTarget26RuntimeTransformation());
    root.addProperty("gatePassed", analysis.gatePassed());
    addString(root, "gateFailureReason", analysis.gateFailureReason());
    addString(root, "status", analysis.status().name());
    addString(root, "nextDirection", analysis.nextDirection().name());
    addString(root, "nextRecommendedAction", analysis.nextRecommendedAction());
    addNullableObject(root, "targetDescriptor", targetDescriptor(analysis.targetDescriptor()));
    addNullableObject(
        root, "dispatcherDescriptor", dispatcherDescriptor(analysis.dispatcherDescriptor()));
    addNullableObject(root, "primitiveContract", primitiveContract(analysis.primitiveContract()));
    addNullableObject(
        root, "generalizedPatchPlan", generalizedPatchPlan(analysis.generalizedPatchPlan()));
    root.add("findings", findings(analysis.findings()));
    return root;
  }

  private JsonObject targetDescriptor(SteelHook02TargetDescriptor descriptor) {
    if (descriptor == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    addString(object, "id", descriptor.id());
    addString(object, "ownerInternalName", descriptor.ownerInternalName());
    addString(object, "binaryName", descriptor.binaryName());
    addString(object, "classEntryName", descriptor.classEntryName());
    addString(object, "memberName", descriptor.memberName());
    addString(object, "descriptor", descriptor.descriptor());
    addString(object, "side", descriptor.side() == null ? null : descriptor.side().id());
    addString(object, "minecraftVersion", descriptor.minecraftVersion());
    addString(object, "sourceContractId", descriptor.sourceContractId());
    addString(object, "sourcePlacementId", descriptor.sourcePlacementId());
    addString(object, "sourcePatchId", descriptor.sourcePatchId());
    object.addProperty("insertionOffset", descriptor.insertionOffset());
    object.addProperty("methodEntryOnly", descriptor.methodEntryOnly());
    return object;
  }

  private JsonObject dispatcherDescriptor(SteelHook02DispatcherDescriptor descriptor) {
    if (descriptor == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    addString(object, "id", descriptor.id());
    addString(object, "ownerInternalName", descriptor.ownerInternalName());
    addString(object, "binaryName", descriptor.binaryName());
    addString(object, "methodName", descriptor.methodName());
    addString(object, "descriptor", descriptor.descriptor());
    addString(object, "opcodeMnemonic", descriptor.opcodeMnemonic());
    addString(object, "opcodeHex", descriptor.opcodeHex());
    object.addProperty("instructionLength", descriptor.instructionLength());
    object.addProperty("maxStackDelta", descriptor.maxStackDelta());
    object.addProperty("maxLocalsDelta", descriptor.maxLocalsDelta());
    object.addProperty("requiresVoidNoArgs", descriptor.requiresVoidNoArgs());
    object.addProperty("publicApiExposed", descriptor.publicApiExposed());
    return object;
  }

  private JsonObject primitiveContract(SteelHook02PrimitiveContract contract) {
    if (contract == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    addString(object, "id", contract.id());
    addString(object, "primitiveKind", contract.primitiveKind().name());
    addString(object, "sourceCandidateId", contract.sourceCandidateId());
    addString(object, "targetDescriptorId", contract.targetDescriptorId());
    addString(object, "dispatcherDescriptorId", contract.dispatcherDescriptorId());
    addString(object, "patchKind", contract.patchKind().name());
    addString(object, "patchMode", contract.patchMode().id());
    addString(object, "patchEligibility", contract.patchEligibility().id());
    addString(object, "insertionOffsetPolicy", contract.insertionOffsetPolicy());
    object.addProperty("contractGeneralized", contract.contractGeneralized());
    object.addProperty("fixtureTransformReady", contract.fixtureTransformReady());
    object.addProperty("minecraftRuntimeTransformReady", contract.minecraftRuntimeTransformReady());
    object.addProperty("publicApiExposed", contract.publicApiExposed());
    object.addProperty("javaModExecutionSandboxed", contract.javaModExecutionSandboxed());
    return object;
  }

  private JsonObject generalizedPatchPlan(SteelHook02GeneralizedPatchPlan plan) {
    if (plan == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    addString(object, "id", plan.id());
    addString(object, "sourcePatchPlanMilestone", plan.sourcePatchPlanMilestone());
    addString(object, "sourcePatchId", plan.sourcePatchId());
    addString(object, "sourceCandidateId", plan.sourceCandidateId());
    addString(object, "targetDescriptorId", plan.targetDescriptorId());
    addString(object, "dispatcherDescriptorId", plan.dispatcherDescriptorId());
    addString(object, "patchKind", plan.patchKind().name());
    addString(object, "patchMode", plan.patchMode().id());
    addString(object, "patchEligibility", plan.patchEligibility().id());
    object.addProperty("requiredConstantPoolEntryCount", plan.requiredConstantPoolEntryCount());
    object.addProperty("constantPoolRewriteRequired", plan.constantPoolRewriteRequired());
    object.addProperty("codeRewriteRequired", plan.codeRewriteRequired());
    object.addProperty("maxStackRewriteRequired", plan.maxStackRewriteRequired());
    object.addProperty("maxLocalsRewriteRequired", plan.maxLocalsRewriteRequired());
    object.addProperty("exceptionTableRewriteRequired", plan.exceptionTableRewriteRequired());
    object.addProperty("stackMapTableRewriteRequired", plan.stackMapTableRewriteRequired());
    object.addProperty(
        "nestedCodeAttributeRewriteRequired", plan.nestedCodeAttributeRewriteRequired());
    object.addProperty("lineNumberTableRewriteRequired", plan.lineNumberTableRewriteRequired());
    object.addProperty(
        "localVariableTableRewriteRequired", plan.localVariableTableRewriteRequired());
    object.addProperty("branchOffsetRewriteRequired", plan.branchOffsetRewriteRequired());
    object.addProperty("switchOffsetRewriteRequired", plan.switchOffsetRewriteRequired());
    object.addProperty("fixtureTransformReady", plan.fixtureTransformReady());
    object.addProperty("minecraftRuntimeTransformReady", plan.minecraftRuntimeTransformReady());
    object.addProperty(
        "eligibleForTarget25TransformerExtraction",
        plan.eligibleForTarget25TransformerExtraction());
    object.addProperty(
        "eligibleForTarget26RuntimeTransformation",
        plan.eligibleForTarget26RuntimeTransformation());
    object.add("notes", notes(plan.notes()));
    return object;
  }

  private JsonArray findings(List<SteelHook02ContractGeneralizationFinding> findings) {
    JsonArray array = new JsonArray();
    for (SteelHook02ContractGeneralizationFinding finding : findings) {
      JsonObject object = new JsonObject();
      addString(object, "id", finding.id());
      addString(object, "checkName", finding.checkName());
      addString(object, "status", finding.status().name());
      object.addProperty("blocking", finding.blocking());
      addString(object, "summary", finding.summary());
      addString(object, "notes", finding.notes());
      array.add(object);
    }
    return array;
  }

  private JsonArray notes(List<String> notes) {
    JsonArray array = new JsonArray();
    for (String note : notes) {
      if (note == null) {
        array.add(JsonNull.INSTANCE);
      } else {
        array.add(note);
      }
    }
    return array;
  }

  private void addNullableObject(JsonObject object, String name, JsonObject value) {
    if (value == null) {
      object.add(name, JsonNull.INSTANCE);
    } else {
      object.add(name, value);
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
