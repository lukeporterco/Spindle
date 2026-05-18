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

public final class SteelHook03RuntimeProofReportWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook03RuntimeProofReport report) throws LoaderException {
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
          "Failed to write SteelHook 0.3 gated runtime proof report " + outputPath.getFileName(),
          exception);
    }
  }

  JsonObject toJson(SteelHook03RuntimeProofReport report) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", report.schema());
    addString(root, "milestoneName", report.milestoneName());
    addString(root, "target", report.target());
    addString(root, "steelHookVersion", report.steelHookVersion());
    addString(root, "sourceTarget29Milestone", report.sourceTarget29Milestone());
    addString(root, "sourceTarget29Status", report.sourceTarget29Status());
    root.addProperty(
        "sourceTarget29MethodExitDispatchReady", report.sourceTarget29MethodExitDispatchReady());
    addString(root, "sourceTarget29NextDirection", report.sourceTarget29NextDirection());
    root.addProperty("gatedRuntimeProofReady", report.gatedRuntimeProofReady());
    addString(root, "status", report.status() == null ? null : report.status().id());
    addString(
        root, "nextDirection", report.nextDirection() == null ? null : report.nextDirection().id());
    root.addProperty("runtimeClassLoaderProofCount", report.runtimeClassLoaderProofCount());
    root.addProperty("runtimeClassLoaderSuccessCount", report.runtimeClassLoaderSuccessCount());
    root.add("entryPrimitiveProof", primitiveProof(report.entryPrimitiveProof()));
    root.add("exitPrimitiveProof", primitiveProof(report.exitPrimitiveProof()));
    root.addProperty("runtimeClassLoadingPathEnabled", report.runtimeClassLoadingPathEnabled());
    root.addProperty("classLoadingOccurred", report.classLoadingOccurred());
    root.add("targetClassesDefined", strings(report.targetClassesDefined()));
    root.addProperty("serverLaunchOccurred", report.serverLaunchOccurred());
    root.addProperty("minecraftMainInvoked", report.minecraftMainInvoked());
    root.addProperty("hookInstallationOccurred", report.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", report.runtimeDispatchOccurred());
    root.addProperty(
        "beforeDispatcherInvocationObserved", report.beforeDispatcherInvocationObserved());
    root.addProperty(
        "afterDispatcherInvocationObserved", report.afterDispatcherInvocationObserved());
    root.addProperty("publicApiExposed", report.publicApiExposed());
    root.addProperty("javaAgentUsed", report.javaAgentUsed());
    root.addProperty("mixinUsed", report.mixinUsed());
    root.addProperty("javaModExecutionSandboxed", report.javaModExecutionSandboxed());
    addString(root, "failureReason", report.failureReason());
    JsonArray findings = new JsonArray();
    for (SteelHook03RuntimeProofFinding finding : report.findings()) {
      JsonObject object = new JsonObject();
      addString(object, "id", finding.id());
      object.addProperty("blocking", finding.blocking());
      addString(object, "summary", finding.summary());
      findings.add(object);
    }
    root.add("findings", findings);
    return root;
  }

  private JsonObject primitiveProof(SteelHook03RuntimePrimitiveProof proof) {
    if (proof == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    addString(
        object, "primitiveKind", proof.primitiveKind() == null ? null : proof.primitiveKind().id());
    addString(object, "sourceMilestone", proof.sourceMilestone());
    addString(object, "status", proof.status() == null ? null : proof.status().id());
    addString(object, "targetBinaryName", proof.targetBinaryName());
    addString(object, "targetInternalName", proof.targetInternalName());
    addString(object, "targetClassEntryName", proof.targetClassEntryName());
    addString(object, "targetMethodName", proof.targetMethodName());
    addString(object, "targetDescriptor", proof.targetDescriptor());
    addString(object, "dispatcherOwnerInternalName", proof.dispatcherOwnerInternalName());
    addString(object, "dispatcherMethodName", proof.dispatcherMethodName());
    addString(object, "dispatcherDescriptor", proof.dispatcherDescriptor());
    addString(object, "transformationMode", proof.transformationMode());
    addString(object, "runtimeLoaderId", proof.runtimeLoaderId());
    object.addProperty("runtimeClasspathEntryCount", proof.runtimeClasspathEntryCount());
    object.addProperty("runtimeClassLoadingAttempted", proof.runtimeClassLoadingAttempted());
    object.addProperty("runtimeClassLoadingSucceeded", proof.runtimeClassLoadingSucceeded());
    object.addProperty("classLoadingOccurred", proof.classLoadingOccurred());
    object.addProperty("targetClassDefined", proof.targetClassDefined());
    addString(object, "definedClassName", proof.definedClassName());
    object.addProperty(
        "definedBySteelHookRuntimeClassLoader", proof.definedBySteelHookRuntimeClassLoader());
    object.addProperty("classInitialized", proof.classInitialized());
    object.addProperty("minecraftMainInvoked", proof.minecraftMainInvoked());
    object.addProperty("serverLaunchOccurred", proof.serverLaunchOccurred());
    object.addProperty("hookInstallationOccurred", proof.hookInstallationOccurred());
    object.addProperty("runtimeDispatchOccurred", proof.runtimeDispatchOccurred());
    object.addProperty("dispatcherInvocationObserved", proof.dispatcherInvocationObserved());
    object.addProperty("dispatcherInvocationCountBefore", proof.dispatcherInvocationCountBefore());
    object.addProperty("dispatcherInvocationCountAfter", proof.dispatcherInvocationCountAfter());
    object.addProperty("stackMapTableRewriteSupported", proof.stackMapTableRewriteSupported());
    object.addProperty("stackMapTableRewriteApplied", proof.stackMapTableRewriteApplied());
    object.addProperty(
        "methodEntryTransformationOccurred", proof.methodEntryTransformationOccurred());
    object.addProperty(
        "methodExitTransformationOccurred", proof.methodExitTransformationOccurred());
    object.addProperty("bytecodeModified", proof.bytecodeModified());
    object.addProperty("transformedClassBytesProduced", proof.transformedClassBytesProduced());
    addString(object, "originalClassSha256", proof.originalClassSha256());
    addString(object, "transformedClassSha256", proof.transformedClassSha256());
    addString(object, "originalCodeSha256", proof.originalCodeSha256());
    addString(object, "transformedCodeSha256", proof.transformedCodeSha256());
    addInteger(object, "originalCodeLength", proof.originalCodeLength());
    addInteger(object, "transformedCodeLength", proof.transformedCodeLength());
    addInteger(object, "constantPoolCountBefore", proof.constantPoolCountBefore());
    addInteger(object, "constantPoolCountAfter", proof.constantPoolCountAfter());
    addString(object, "failureReason", proof.failureReason());
    return object;
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
