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

public final class SteelHook02MethodEntryTransformerResultWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook02MethodEntryTransformerResult result)
      throws LoaderException {
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(toJson(result), writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write SteelHook 0.2 method-entry transformer result " + outputPath, exception);
    }
  }

  JsonObject toJson(SteelHook02MethodEntryTransformerResult result) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", result.schema());
    addString(root, "milestoneName", result.milestoneName());
    addString(root, "target", result.target());
    addString(root, "steelHookVersion", result.steelHookVersion());
    addString(root, "sourcePatchPlanMilestone", result.sourcePatchPlanMilestone());
    addString(root, "sourcePrimitiveBoundaryMilestone", result.sourcePrimitiveBoundaryMilestone());
    addString(
        root,
        "sourceContractGeneralizationMilestone",
        result.sourceContractGeneralizationMilestone());
    root.addProperty("localTransformationOnly", result.localTransformationOnly());
    root.addProperty("runtimeClassLoadingPathEnabled", result.runtimeClassLoadingPathEnabled());
    root.addProperty("classLoadingOccurred", result.classLoadingOccurred());
    root.addProperty("hookInstallationOccurred", result.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", result.runtimeDispatchOccurred());
    root.addProperty("realMinecraftRuntimeTransformed", result.realMinecraftRuntimeTransformed());
    root.addProperty("publicApiExposed", result.publicApiExposed());
    root.addProperty("javaAgentUsed", result.javaAgentUsed());
    root.addProperty("mixinUsed", result.mixinUsed());
    root.addProperty("javaModExecutionSandboxed", result.javaModExecutionSandboxed());
    root.addProperty("minecraftRuntimeTransformReady", result.minecraftRuntimeTransformReady());
    root.addProperty(
        "target25TransformerExtractionOccurred", result.target25TransformerExtractionOccurred());
    root.addProperty(
        "methodEntryTransformationOccurred", result.methodEntryTransformationOccurred());
    root.addProperty("bytecodeModified", result.bytecodeModified());
    root.addProperty("transformedClassBytesProduced", result.transformedClassBytesProduced());
    root.addProperty(
        "eligibleForTarget26GatedRuntimeTransformation",
        result.eligibleForTarget26GatedRuntimeTransformation());
    root.addProperty("gatePassed", result.gatePassed());
    addString(root, "status", result.status() == null ? null : result.status().name());
    addString(
        root,
        "nextDirection",
        result.nextDirection() == null ? null : result.nextDirection().name());
    addString(root, "failureReason", result.failureReason());
    addString(root, "originalClassSha256", result.originalClassSha256());
    addString(root, "transformedClassSha256", result.transformedClassSha256());
    addString(root, "originalCodeSha256", result.originalCodeSha256());
    addString(root, "transformedCodeSha256", result.transformedCodeSha256());
    addInteger(root, "originalCodeLength", result.originalCodeLength());
    addInteger(root, "transformedCodeLength", result.transformedCodeLength());
    addInteger(root, "constantPoolCountBefore", result.constantPoolCountBefore());
    addInteger(root, "constantPoolCountAfter", result.constantPoolCountAfter());
    addInteger(root, "methodrefIndex", result.methodrefIndex());
    addString(root, "insertedInstructionHex", result.insertedInstructionHex());
    addObject(root, "gate", gate(result.gate()));
    addObject(root, "targetDescriptor", targetDescriptor(result.targetDescriptor()));
    addObject(root, "dispatcherDescriptor", dispatcherDescriptor(result.dispatcherDescriptor()));
    addObject(root, "primitiveContract", primitiveContract(result.primitiveContract()));
    addObject(root, "generalizedPatchPlan", generalizedPatchPlan(result.generalizedPatchPlan()));
    addObject(root, "targetClassBytes", targetClassBytes(result.targetClassBytes()));
    root.add("findings", findings(result.findings()));
    return root;
  }

  private JsonObject gate(SteelHook02MethodEntryTransformerGate gate) {
    if (gate == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    object.addProperty("passed", gate.passed());
    addString(object, "failureReason", gate.failureReason());
    object.addProperty("contractGeneralizationGatePassed", gate.contractGeneralizationGatePassed());
    object.addProperty("contractGeneralizationReady", gate.contractGeneralizationReady());
    object.addProperty(
        "eligibleForTarget25TransformerExtraction",
        gate.eligibleForTarget25TransformerExtraction());
    object.addProperty("targetDescriptorPresent", gate.targetDescriptorPresent());
    object.addProperty("dispatcherDescriptorPresent", gate.dispatcherDescriptorPresent());
    object.addProperty("primitiveContractPresent", gate.primitiveContractPresent());
    object.addProperty("generalizedPatchPlanPresent", gate.generalizedPatchPlanPresent());
    object.addProperty("targetClassBytesPresent", gate.targetClassBytesPresent());
    object.addProperty("minecraftRuntimeTransformReady", gate.minecraftRuntimeTransformReady());
    object.addProperty("runtimeClassLoadingPathEnabled", gate.runtimeClassLoadingPathEnabled());
    return object;
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
    JsonArray notes = new JsonArray();
    for (String note : plan.notes()) {
      if (note == null) {
        notes.add(JsonNull.INSTANCE);
      } else {
        notes.add(note);
      }
    }
    object.add("notes", notes);
    return object;
  }

  private JsonObject targetClassBytes(SteelHook02TargetClassBytes targetClassBytes) {
    if (targetClassBytes == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    addString(object, "classEntryName", targetClassBytes.classEntryName());
    addString(object, "sourcePath", targetClassBytes.sourcePath());
    addString(object, "sourceKind", targetClassBytes.sourceKind());
    addString(object, "classSha256", targetClassBytes.classSha256());
    object.addProperty("present", targetClassBytes.present());
    object.addProperty("readable", targetClassBytes.readable());
    addString(object, "failureReason", targetClassBytes.failureReason());
    return object;
  }

  private JsonArray findings(List<SteelHook02MethodEntryTransformerFinding> findings) {
    JsonArray array = new JsonArray();
    for (SteelHook02MethodEntryTransformerFinding finding : findings) {
      JsonObject object = new JsonObject();
      addString(object, "id", finding.id());
      addString(object, "checkName", finding.checkName());
      addString(object, "status", finding.status() == null ? null : finding.status().name());
      object.addProperty("blocking", finding.blocking());
      addString(object, "summary", finding.summary());
      addString(object, "notes", finding.notes());
      array.add(object);
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
