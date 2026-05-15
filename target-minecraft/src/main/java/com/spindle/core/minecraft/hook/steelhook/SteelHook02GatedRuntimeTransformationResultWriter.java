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

public final class SteelHook02GatedRuntimeTransformationResultWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook02GatedRuntimeTransformationResult result)
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
          "Failed to write SteelHook 0.2 gated runtime transformation result " + outputPath,
          exception);
    }
  }

  JsonObject toJson(SteelHook02GatedRuntimeTransformationResult result) {
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
    addString(
        root,
        "sourceMethodEntryTransformerMilestone",
        result.sourceMethodEntryTransformerMilestone());
    root.addProperty("runtimeClassLoadingPathEnabled", result.runtimeClassLoadingPathEnabled());
    root.addProperty("runtimeClassLoadingAttempted", result.runtimeClassLoadingAttempted());
    root.addProperty("runtimeClassLoadingSucceeded", result.runtimeClassLoadingSucceeded());
    root.addProperty("classLoadingOccurred", result.classLoadingOccurred());
    root.addProperty("targetClassDefined", result.targetClassDefined());
    addString(
        root,
        "transformationMode",
        result.transformationMode() == null ? null : result.transformationMode().id());
    addString(root, "targetBinaryName", result.targetBinaryName());
    addString(root, "targetClassEntryName", result.targetClassEntryName());
    addString(root, "definedClassName", result.definedClassName());
    root.addProperty(
        "definedBySteelHookRuntimeClassLoader", result.definedBySteelHookRuntimeClassLoader());
    root.addProperty("realMinecraftRuntimeTransformed", result.realMinecraftRuntimeTransformed());
    root.addProperty(
        "methodEntryTransformationOccurred", result.methodEntryTransformationOccurred());
    root.addProperty("bytecodeModified", result.bytecodeModified());
    root.addProperty("transformedClassBytesProduced", result.transformedClassBytesProduced());
    root.addProperty("minecraftMainInvoked", result.minecraftMainInvoked());
    root.addProperty("minecraftServerLaunched", result.minecraftServerLaunched());
    root.addProperty("hookInstallationOccurred", result.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", result.runtimeDispatchOccurred());
    root.addProperty("dispatcherInvocationObserved", result.dispatcherInvocationObserved());
    root.addProperty("publicApiExposed", result.publicApiExposed());
    root.addProperty("javaAgentUsed", result.javaAgentUsed());
    root.addProperty("mixinUsed", result.mixinUsed());
    root.addProperty("remappingOccurred", result.remappingOccurred());
    root.addProperty("accessWidenersUsed", result.accessWidenersUsed());
    root.addProperty("javaModExecutionSandboxed", result.javaModExecutionSandboxed());
    root.addProperty("minecraftRuntimeTransformReady", result.minecraftRuntimeTransformReady());
    root.addProperty(
        "eligibleForTarget27CompletionVerification",
        result.eligibleForTarget27CompletionVerification());
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
    addObject(root, "classLoadingAuditSummary", auditSummary(result.classLoadingAuditSummary()));
    addObject(root, "gate", gate(result.gate()));
    root.add("findings", findings(result.findings()));
    return root;
  }

  private JsonObject auditSummary(
      com.spindle.core.minecraft.MinecraftClassLoadingAudit.Summary summary) {
    if (summary == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    object.add(
        "attemptedClassLoadsByLoader", gson.toJsonTree(summary.attemptedClassLoadsByLoader()));
    object.add("definedClassLoadsByLoader", gson.toJsonTree(summary.definedClassLoadsByLoader()));
    object.add("deniedClassLoads", gson.toJsonTree(summary.deniedClassLoads()));
    return object;
  }

  private JsonObject gate(SteelHook02GatedRuntimeTransformationGate gate) {
    if (gate == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    object.addProperty("passed", gate.passed());
    addString(object, "failureReason", gate.failureReason());
    object.addProperty("target25GatePassed", gate.target25GatePassed());
    object.addProperty("target25Transformed", gate.target25Transformed());
    object.addProperty(
        "eligibleForTarget26GatedRuntimeTransformation",
        gate.eligibleForTarget26GatedRuntimeTransformation());
    object.addProperty(
        "runtimeClassLoadingPathPreviouslyDisabled",
        gate.runtimeClassLoadingPathPreviouslyDisabled());
    object.addProperty(
        "runtimeClassLoadingPathEnabledForTarget26",
        gate.runtimeClassLoadingPathEnabledForTarget26());
    object.addProperty("targetDescriptorPresent", gate.targetDescriptorPresent());
    object.addProperty("dispatcherDescriptorPresent", gate.dispatcherDescriptorPresent());
    object.addProperty("primitiveContractPresent", gate.primitiveContractPresent());
    object.addProperty("generalizedPatchPlanPresent", gate.generalizedPatchPlanPresent());
    object.addProperty("targetClassBytesMetadataPresent", gate.targetClassBytesMetadataPresent());
    object.addProperty("runtimeClasspathUrlsPresent", gate.runtimeClasspathUrlsPresent());
    object.addProperty(
        "minecraftRuntimeTransformReadyBeforeTarget26",
        gate.minecraftRuntimeTransformReadyBeforeTarget26());
    object.addProperty("minecraftMainInvocationAllowed", gate.minecraftMainInvocationAllowed());
    return object;
  }

  private JsonArray findings(
      java.util.List<SteelHook02GatedRuntimeTransformationFinding> findings) {
    JsonArray array = new JsonArray();
    for (SteelHook02GatedRuntimeTransformationFinding finding : findings) {
      JsonObject object = new JsonObject();
      addString(object, "id", finding.id());
      addString(object, "status", finding.status() == null ? null : finding.status().name());
      addString(object, "message", finding.message());
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
