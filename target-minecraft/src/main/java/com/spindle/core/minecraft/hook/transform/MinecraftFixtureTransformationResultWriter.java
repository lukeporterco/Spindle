package com.spindle.core.minecraft.hook.transform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftFixtureTransformationResultWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftFixtureTransformationResult result)
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
          "Failed to write Minecraft fixture transformation result " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftFixtureTransformationResult result) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", result.schema());
    addString(root, "milestoneName", result.milestoneName());
    addString(root, "transformationScope", result.transformationScope());
    addString(root, "status", result.status() == null ? null : result.status().id());
    addObject(root, "gate", gate(result.gate()));
    addString(root, "sourcePatchId", result.sourcePatchId());
    addString(root, "sourcePlacementId", result.sourcePlacementId());
    addString(root, "sourceContractId", result.sourceContractId());
    addString(root, "targetClass", result.targetClass());
    addString(root, "targetMethod", result.targetMethod());
    addString(root, "targetDescriptor", result.targetDescriptor());
    addInteger(root, "insertionOffset", result.insertionOffset());
    addString(root, "insertedInstructionHex", result.insertedInstructionHex());
    addString(root, "originalClassSha256", result.originalClassSha256());
    addString(root, "transformedClassSha256", result.transformedClassSha256());
    addString(root, "originalCodeSha256", result.originalCodeSha256());
    addString(root, "transformedCodeSha256", result.transformedCodeSha256());
    addInteger(root, "originalCodeLength", result.originalCodeLength());
    addInteger(root, "transformedCodeLength", result.transformedCodeLength());
    addInteger(root, "constantPoolCountBefore", result.constantPoolCountBefore());
    addInteger(root, "constantPoolCountAfter", result.constantPoolCountAfter());
    addInteger(root, "methodrefIndex", result.methodrefIndex());
    root.addProperty("fixtureTransformationOccurred", result.fixtureTransformationOccurred());
    root.addProperty("fixtureBytecodeModified", result.fixtureBytecodeModified());
    root.addProperty("transformedClassBytesProduced", result.transformedClassBytesProduced());
    root.addProperty("minecraftRuntimeTransformed", result.minecraftRuntimeTransformed());
    root.addProperty(
        "minecraftRuntimeClassLoadingChanged", result.minecraftRuntimeClassLoadingChanged());
    root.addProperty("bootstrapTransformationEnabled", result.bootstrapTransformationEnabled());
    root.addProperty("publicApiExposed", result.publicApiExposed());
    root.addProperty("javaAgentUsed", result.javaAgentUsed());
    root.addProperty("mixinUsed", result.mixinUsed());
    root.addProperty("remappingOccurred", result.remappingOccurred());
    root.addProperty("javaModExecutionSandboxed", result.javaModExecutionSandboxed());
    addString(root, "failureReason", result.failureReason());
    addObject(root, "constantPoolPatch", constantPoolPatch(result.constantPoolPatch()));
    addObject(root, "codePatch", codePatch(result.codePatch()));
    addObject(root, "transformedClass", transformedClass(result.transformedClass()));
    return root;
  }

  private JsonObject gate(MinecraftFixtureTransformationGate gate) {
    if (gate == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    object.addProperty("passed", gate.passed());
    addString(object, "failureReason", gate.failureReason());
    object.addProperty("patchPlanGatePassed", gate.patchPlanGatePassed());
    object.addProperty("patchPlanningSucceeded", gate.patchPlanningSucceeded());
    object.addProperty("patchPlanned", gate.patchPlanned());
    object.addProperty("plannedPatchCount", gate.plannedPatchCount());
    addString(object, "selectedPatchId", gate.selectedPatchId());
    object.addProperty("transformReadyForFixtureOnly", gate.transformReadyForFixtureOnly());
    object.addProperty(
        "transformReadyForMinecraftRuntime", gate.transformReadyForMinecraftRuntime());
    return object;
  }

  private JsonObject constantPoolPatch(MinecraftFixtureConstantPoolPatch patch) {
    if (patch == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    addInteger(object, "constantPoolCountBefore", patch.constantPoolCountBefore());
    addInteger(object, "constantPoolCountAfter", patch.constantPoolCountAfter());
    object.addProperty("appendedEntryCount", patch.appendedEntryCount());
    addInteger(object, "dispatcherOwnerUtf8Index", patch.dispatcherOwnerUtf8Index());
    addInteger(object, "dispatcherClassIndex", patch.dispatcherClassIndex());
    addInteger(object, "dispatcherMethodNameUtf8Index", patch.dispatcherMethodNameUtf8Index());
    addInteger(object, "dispatcherDescriptorUtf8Index", patch.dispatcherDescriptorUtf8Index());
    addInteger(object, "dispatcherNameAndTypeIndex", patch.dispatcherNameAndTypeIndex());
    addInteger(object, "dispatcherMethodrefIndex", patch.dispatcherMethodrefIndex());
    return object;
  }

  private JsonObject codePatch(MinecraftFixtureCodePatchResult patch) {
    if (patch == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    addInteger(object, "originalCodeLength", patch.originalCodeLength());
    addInteger(object, "transformedCodeLength", patch.transformedCodeLength());
    addString(object, "originalCodeSha256", patch.originalCodeSha256());
    addString(object, "transformedCodeSha256", patch.transformedCodeSha256());
    addInteger(object, "maxStackBefore", patch.maxStackBefore());
    addInteger(object, "maxStackAfter", patch.maxStackAfter());
    addInteger(object, "maxLocalsBefore", patch.maxLocalsBefore());
    addInteger(object, "maxLocalsAfter", patch.maxLocalsAfter());
    object.addProperty("exceptionTableCount", patch.exceptionTableCount());
    object.addProperty("exceptionTableShiftApplied", patch.exceptionTableShiftApplied());
    addString(object, "insertedInstructionHex", patch.insertedInstructionHex());
    return object;
  }

  private JsonObject transformedClass(MinecraftFixtureTransformedClass transformedClass) {
    if (transformedClass == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    addString(object, "internalName", transformedClass.internalName());
    addString(object, "classSha256", transformedClass.classSha256());
    return object;
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
