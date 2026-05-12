package com.spindle.core.minecraft.hook.bootstrap;

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

public final class MinecraftBootstrapHookTransformationResultWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftBootstrapHookTransformationResult result)
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
          "Failed to write Minecraft bootstrap hook transformation result " + outputPath,
          exception);
    }
  }

  JsonObject toJson(MinecraftBootstrapHookTransformationResult result) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", result.schema());
    addString(root, "milestoneName", result.milestoneName());
    addString(
        root,
        "transformationMode",
        result.transformationMode() == null ? null : result.transformationMode().id());
    addString(root, "scope", result.scope());
    addString(root, "status", result.status() == null ? null : result.status().id());
    addObject(root, "gate", gate(result.gate()));
    addString(root, "sourcePatchId", result.sourcePatchId());
    addString(root, "sourcePlacementId", result.sourcePlacementId());
    addString(root, "sourceContractId", result.sourceContractId());
    addString(root, "targetBinaryName", result.targetBinaryName());
    addString(root, "targetInternalName", result.targetInternalName());
    addString(root, "targetMethod", result.targetMethod());
    addString(root, "targetDescriptor", result.targetDescriptor());
    addString(root, "dispatcher", result.dispatcher());
    addString(root, "originalClassSha256", result.originalClassSha256());
    addString(root, "transformedClassSha256", result.transformedClassSha256());
    addString(root, "originalCodeSha256", result.originalCodeSha256());
    addString(root, "transformedCodeSha256", result.transformedCodeSha256());
    addInteger(root, "originalCodeLength", result.originalCodeLength());
    addInteger(root, "transformedCodeLength", result.transformedCodeLength());
    addInteger(root, "constantPoolCountBefore", result.constantPoolCountBefore());
    addInteger(root, "constantPoolCountAfter", result.constantPoolCountAfter());
    addInteger(root, "methodrefIndex", result.methodrefIndex());
    root.addProperty("bootstrapTransformationEnabled", result.bootstrapTransformationEnabled());
    root.addProperty(
        "runtimeClassLoaderTransformationEnabled",
        result.runtimeClassLoaderTransformationEnabled());
    root.addProperty("fakeServerRuntimeTransformed", result.fakeServerRuntimeTransformed());
    root.addProperty("realMinecraftRuntimeTransformed", result.realMinecraftRuntimeTransformed());
    root.addProperty("transformationOccurred", result.transformationOccurred());
    root.addProperty("patchingOccurred", result.patchingOccurred());
    root.addProperty("bytecodeModified", result.bytecodeModified());
    root.addProperty("publicApiExposed", result.publicApiExposed());
    root.addProperty("javaAgentUsed", result.javaAgentUsed());
    root.addProperty("mixinUsed", result.mixinUsed());
    root.addProperty("remappingOccurred", result.remappingOccurred());
    root.addProperty("accessWidenersUsed", result.accessWidenersUsed());
    root.addProperty("javaModExecutionSandboxed", result.javaModExecutionSandboxed());
    root.addProperty("dispatcherInvocationCount", result.dispatcherInvocationCount());
    root.addProperty("dispatcherInvocationObserved", result.dispatcherInvocationObserved());
    root.addProperty("minecraftMainInvoked", result.minecraftMainInvoked());
    addString(root, "fixtureTransformationStatus", result.fixtureTransformationStatus());
    addString(
        root, "fixtureTransformationFailureReason", result.fixtureTransformationFailureReason());
    addString(root, "failureReason", result.failureReason());
    return root;
  }

  private JsonObject gate(MinecraftBootstrapHookTransformationGate gate) {
    if (gate == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    object.addProperty("passed", gate.passed());
    addString(object, "failureReason", gate.failureReason());
    addInteger(object, "patchPlanSchema", gate.patchPlanSchema());
    addString(object, "patchPlanMilestoneName", gate.patchPlanMilestoneName());
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
