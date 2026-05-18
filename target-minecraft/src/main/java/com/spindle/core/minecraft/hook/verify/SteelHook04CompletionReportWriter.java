package com.spindle.core.minecraft.hook.verify;

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
import java.util.Set;

public final class SteelHook04CompletionReportWriter {
  private static final Set<String> RAW_BYTE_KEYS =
      Set.of(
          "classBytes",
          "rawClassBytes",
          "originalClassBytes",
          "transformedClassBytes",
          "methodCodeBytes",
          "rawMethodCode",
          "bytecodeBytes",
          "stackMapTableBytes",
          "rawStackMapTableBytes",
          "codeBytes",
          "bytes",
          "payload");

  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook04CompletionReport report) throws LoaderException {
    JsonObject root = toJson(report);
    validateNoRawByteKeys(root);
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(root, writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write SteelHook 0.4 completion report " + outputPath.getFileName(), exception);
    }
  }

  JsonObject toJson(SteelHook04CompletionReport report) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", report.schema());
    addString(root, "milestoneName", report.milestoneName());
    addString(root, "target", report.target());
    addString(root, "steelHookVersion", report.steelHookVersion());
    root.addProperty("completionReady", report.completionReady());
    addString(root, "status", report.status() == null ? null : report.status().id());
    addString(
        root, "handoffStatus", report.handoffStatus() == null ? null : report.handoffStatus().id());
    addString(
        root, "nextDirection", report.nextDirection() == null ? null : report.nextDirection().id());
    addString(root, "nextRecommendedAction", report.nextRecommendedAction());
    addString(root, "sourceTarget32Milestone", report.sourceTarget32Milestone());
    addString(root, "sourceTarget32BoundaryStatus", report.sourceTarget32BoundaryStatus());
    root.addProperty("sourceTarget32GatePassed", report.sourceTarget32GatePassed());
    root.addProperty(
        "sourceTarget32ApprovedPrimitiveCount", report.sourceTarget32ApprovedPrimitiveCount());
    addString(root, "sourceTarget33Milestone", report.sourceTarget33Milestone());
    addString(root, "sourceTarget33ProofStatus", report.sourceTarget33ProofStatus());
    root.addProperty("sourceTarget33ProofReady", report.sourceTarget33ProofReady());
    addString(root, "sourceTarget33PrimitiveKind", report.sourceTarget33PrimitiveKind());
    root.addProperty(
        "sourceTarget33SuccessfulProofCaseCount", report.sourceTarget33SuccessfulProofCaseCount());
    addString(root, "sourceTarget34Milestone", report.sourceTarget34Milestone());
    addString(root, "sourceTarget34ProofStatus", report.sourceTarget34ProofStatus());
    root.addProperty("sourceTarget34ProofReady", report.sourceTarget34ProofReady());
    root.addProperty(
        "sourceTarget34SuccessfulProofCaseCount", report.sourceTarget34SuccessfulProofCaseCount());
    addString(root, "sourceTarget35Milestone", report.sourceTarget35Milestone());
    addString(root, "sourceTarget35Status", report.sourceTarget35Status());
    root.addProperty(
        "sourceTarget35GatedRuntimeProofReady", report.sourceTarget35GatedRuntimeProofReady());
    root.addProperty(
        "sourceTarget35RuntimeClassLoaderProofCount",
        report.sourceTarget35RuntimeClassLoaderProofCount());
    root.addProperty(
        "sourceTarget35RuntimeClassLoaderSuccessCount",
        report.sourceTarget35RuntimeClassLoaderSuccessCount());
    root.add("completedPrimitiveKinds", stringArray(report.completedPrimitiveKinds()));
    root.add("completedCapabilities", stringArray(report.completedCapabilities()));
    root.add("unsupportedCapabilities", stringArray(report.unsupportedCapabilities()));
    root.add("stageVerifications", stageVerifications(report.stageVerifications()));
    root.add("safetyInvariants", safetyInvariants(report.safetyInvariants()));
    root.add("forbiddenReportChecks", findings(report.forbiddenReportChecks()));
    root.addProperty("returnValueInterceptVerified", report.returnValueInterceptVerified());
    root.addProperty("invokeRedirectVerified", report.invokeRedirectVerified());
    root.addProperty("invokeWrapVerified", report.invokeWrapVerified());
    root.addProperty("offlineProofChainVerified", report.offlineProofChainVerified());
    root.addProperty("gatedRuntimeProofVerified", report.gatedRuntimeProofVerified());
    root.addProperty(
        "unsupportedPrimitiveRejectionVerified", report.unsupportedPrimitiveRejectionVerified());
    root.addProperty("rawBytePayloadsAbsent", report.rawBytePayloadsAbsent());
    root.addProperty(
        "unsupportedPrimitiveLeakageAbsent", report.unsupportedPrimitiveLeakageAbsent());
    root.addProperty("runtimeClassLoadingPathEnabled", report.runtimeClassLoadingPathEnabled());
    root.addProperty("classLoadingOccurred", report.classLoadingOccurred());
    root.addProperty("targetClassDefinitionOccurred", report.targetClassDefinitionOccurred());
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

  void validateNoRawByteKeys(JsonObject root) throws LoaderException {
    String rawByteKey = firstRawByteKey(root);
    if (rawByteKey != null) {
      throw new LoaderException(
          "SteelHook 0.4 completion report must not serialize raw byte payload key " + rawByteKey);
    }
  }

  private JsonArray stringArray(List<String> values) {
    JsonArray array = new JsonArray();
    for (String value : values) {
      array.add(value);
    }
    return array;
  }

  private JsonArray stageVerifications(List<SteelHook04CompletionStageVerification> values) {
    JsonArray array = new JsonArray();
    for (SteelHook04CompletionStageVerification value : values) {
      JsonObject object = new JsonObject();
      addString(object, "stageId", value.stageId());
      addString(object, "milestoneName", value.milestoneName());
      addString(object, "summary", value.summary());
      object.addProperty("passed", value.passed());
      addString(object, "failureReason", value.failureReason());
      array.add(object);
    }
    return array;
  }

  private JsonArray safetyInvariants(List<SteelHook04CompletionSafetyInvariant> values) {
    JsonArray array = new JsonArray();
    for (SteelHook04CompletionSafetyInvariant value : values) {
      JsonObject object = new JsonObject();
      addString(object, "id", value.id());
      addString(object, "expectedValue", value.expectedValue());
      addString(object, "actualValue", value.actualValue());
      object.addProperty("passed", value.passed());
      addString(object, "failureReason", value.failureReason());
      array.add(object);
    }
    return array;
  }

  private JsonArray findings(List<SteelHook04CompletionFinding> values) {
    JsonArray array = new JsonArray();
    for (SteelHook04CompletionFinding value : values) {
      JsonObject object = new JsonObject();
      addString(object, "id", value.id());
      object.addProperty("fatal", value.fatal());
      addString(object, "message", value.message());
      array.add(object);
    }
    return array;
  }

  private String firstRawByteKey(JsonObject object) {
    for (String key : object.keySet()) {
      if (RAW_BYTE_KEYS.contains(key)) {
        return key;
      }
      if (object.get(key).isJsonObject()) {
        String nested = firstRawByteKey(object.getAsJsonObject(key));
        if (nested != null) {
          return nested;
        }
      }
      if (object.get(key).isJsonArray()) {
        for (int index = 0; index < object.getAsJsonArray(key).size(); index++) {
          if (object.getAsJsonArray(key).get(index).isJsonObject()) {
            String nested =
                firstRawByteKey(object.getAsJsonArray(key).get(index).getAsJsonObject());
            if (nested != null) {
              return nested;
            }
          }
        }
      }
    }
    return null;
  }

  private void addString(JsonObject object, String name, String value) {
    if (value == null) {
      object.add(name, JsonNull.INSTANCE);
    } else {
      object.addProperty(name, value);
    }
  }
}
