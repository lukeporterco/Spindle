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

public final class SteelHook04GatedRuntimeProofReportWriter {
  public static final String REPORT_FILE_NAME = SteelHook04GatedRuntimeProofRunner.REPORT_FILE_NAME;

  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook04GatedRuntimeProofReport report)
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
          "Failed to write Target-35 gated runtime proof report " + outputPath, exception);
    }
  }

  JsonObject toJson(SteelHook04GatedRuntimeProofReport report) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", report.schema());
    addString(root, "reportFileName", REPORT_FILE_NAME);
    addString(root, "milestoneName", report.milestoneName());
    addString(root, "target", report.target());
    addString(root, "steelHookVersion", report.steelHookVersion());
    addString(root, "sourceTarget32Milestone", report.sourceTarget32Milestone());
    addString(root, "sourceTarget32BoundaryStatus", report.sourceTarget32BoundaryStatus());
    root.addProperty("sourceTarget32GatePassed", report.sourceTarget32GatePassed());
    root.addProperty(
        "sourceTarget32ApprovedPrimitiveCount", report.sourceTarget32ApprovedPrimitiveCount());
    addString(root, "sourceTarget33Milestone", report.sourceTarget33Milestone());
    addString(root, "sourceTarget33ProofStatus", report.sourceTarget33ProofStatus());
    root.addProperty("sourceTarget33ProofReady", report.sourceTarget33ProofReady());
    root.addProperty(
        "sourceTarget33SuccessfulProofCaseCount", report.sourceTarget33SuccessfulProofCaseCount());
    addString(root, "sourceTarget34Milestone", report.sourceTarget34Milestone());
    addString(root, "sourceTarget34ProofStatus", report.sourceTarget34ProofStatus());
    root.addProperty("sourceTarget34ProofReady", report.sourceTarget34ProofReady());
    root.addProperty(
        "sourceTarget34SuccessfulProofCaseCount", report.sourceTarget34SuccessfulProofCaseCount());
    root.addProperty("gatedRuntimeProofReady", report.gatedRuntimeProofReady());
    addString(root, "status", report.status() == null ? null : report.status().id());
    addString(
        root, "nextDirection", report.nextDirection() == null ? null : report.nextDirection().id());
    addString(root, "nextRecommendedAction", report.nextRecommendedAction());
    root.add("approvedPrimitiveKinds", primitiveKinds(report.approvedPrimitiveKinds()));
    root.addProperty("runtimeClassLoaderProofCount", report.runtimeClassLoaderProofCount());
    root.addProperty("runtimeClassLoaderSuccessCount", report.runtimeClassLoaderSuccessCount());
    root.add("returnValueInterceptProof", primitiveProof(report.returnValueInterceptProof()));
    root.add("invokeRedirectProof", primitiveProof(report.invokeRedirectProof()));
    root.add("invokeWrapProof", primitiveProof(report.invokeWrapProof()));
    root.addProperty(
        "unsupportedPrimitivePlanRejectedBeforeClassDefinition",
        report.unsupportedPrimitivePlanRejectedBeforeClassDefinition());
    root.addProperty(
        "unsupportedPrimitivePlanClassDefinitionAttempted",
        report.unsupportedPrimitivePlanClassDefinitionAttempted());
    root.addProperty("runtimeClassLoadingPathEnabled", report.runtimeClassLoadingPathEnabled());
    root.addProperty("classLoadingOccurred", report.classLoadingOccurred());
    root.addProperty("targetClassDefinitionOccurred", report.targetClassDefinitionOccurred());
    root.add("targetClassesDefined", strings(report.targetClassesDefined()));
    root.addProperty("classInitialized", report.classInitialized());
    root.addProperty("targetMethodInvoked", report.targetMethodInvoked());
    root.addProperty("wrapperExecuted", report.wrapperExecuted());
    root.addProperty("serverLaunchOccurred", report.serverLaunchOccurred());
    root.addProperty("minecraftMainInvoked", report.minecraftMainInvoked());
    root.addProperty("hookInstallationOccurred", report.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", report.runtimeDispatchOccurred());
    root.addProperty("publicApiExposed", report.publicApiExposed());
    root.addProperty("javaAgentUsed", report.javaAgentUsed());
    root.addProperty("mixinUsed", report.mixinUsed());
    root.addProperty("javaModExecutionSandboxed", report.javaModExecutionSandboxed());
    addString(root, "failureReason", report.failureReason());
    root.add("findings", findings(report.findings()));
    return root;
  }

  private JsonObject primitiveProof(SteelHook04RuntimePrimitiveProof proof) {
    if (proof == null) {
      return null;
    }
    JsonObject object = new JsonObject();
    addString(
        object, "primitiveKind", proof.primitiveKind() == null ? null : proof.primitiveKind().id());
    addString(object, "sourceMilestone", proof.sourceMilestone());
    addString(object, "sourceReportId", proof.sourceReportId());
    addString(object, "status", proof.status() == null ? null : proof.status().id());
    addString(object, "targetBinaryName", proof.targetBinaryName());
    addString(object, "targetInternalName", proof.targetInternalName());
    addString(object, "targetClassEntryName", proof.targetClassEntryName());
    addString(object, "targetMethodName", proof.targetMethodName());
    addString(object, "targetDescriptor", proof.targetDescriptor());
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
    object.addProperty("targetMethodInvoked", proof.targetMethodInvoked());
    object.addProperty("wrapperExecuted", proof.wrapperExecuted());
    object.addProperty("minecraftMainInvoked", proof.minecraftMainInvoked());
    object.addProperty("serverLaunchOccurred", proof.serverLaunchOccurred());
    object.addProperty("hookInstallationOccurred", proof.hookInstallationOccurred());
    object.addProperty("runtimeDispatchOccurred", proof.runtimeDispatchOccurred());
    object.addProperty("dispatcherInvocationObserved", proof.dispatcherInvocationObserved());
    object.addProperty("dispatcherInvocationCountBefore", proof.dispatcherInvocationCountBefore());
    object.addProperty("dispatcherInvocationCountAfter", proof.dispatcherInvocationCountAfter());
    object.addProperty("bytecodeModified", proof.bytecodeModified());
    object.addProperty("transformedClassBytesProduced", proof.transformedClassBytesProduced());
    addString(object, "originalClassSha256", proof.originalClassSha256());
    addString(object, "transformedClassSha256", proof.transformedClassSha256());
    addString(object, "originalCodeSha256", proof.originalCodeSha256());
    addString(object, "transformedCodeSha256", proof.transformedCodeSha256());
    addInteger(object, "originalCodeLength", proof.originalCodeLength());
    addInteger(object, "transformedCodeLength", proof.transformedCodeLength());
    addString(object, "matchedOpcode", proof.matchedOpcode());
    addInteger(object, "matchedCallsiteCount", proof.matchedCallsiteCount());
    addString(object, "wrappedDelegateOwnerInternalName", proof.wrappedDelegateOwnerInternalName());
    addString(object, "wrappedDelegateName", proof.wrappedDelegateName());
    addString(object, "wrappedDelegateDescriptor", proof.wrappedDelegateDescriptor());
    addString(object, "wrapperOwnerInternalName", proof.wrapperOwnerInternalName());
    addString(object, "wrapperName", proof.wrapperName());
    addString(object, "wrapperDescriptor", proof.wrapperDescriptor());
    addString(object, "failureReason", proof.failureReason());
    return object;
  }

  private JsonArray primitiveKinds(List<SteelHook04PrimitiveKind> primitiveKinds) {
    JsonArray array = new JsonArray();
    for (SteelHook04PrimitiveKind primitiveKind : primitiveKinds) {
      array.add(primitiveKind.id());
    }
    return array;
  }

  private JsonArray strings(List<String> values) {
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

  private JsonArray findings(List<SteelHook04GatedRuntimeProofFinding> findings) {
    JsonArray array = new JsonArray();
    for (SteelHook04GatedRuntimeProofFinding finding : findings) {
      JsonObject object = new JsonObject();
      addString(object, "id", finding.id());
      addString(object, "checkName", finding.checkName());
      addString(object, "status", finding.status().id());
      object.addProperty("blocking", finding.blocking());
      addString(object, "summary", finding.summary());
      addString(object, "details", finding.details());
      array.add(object);
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
