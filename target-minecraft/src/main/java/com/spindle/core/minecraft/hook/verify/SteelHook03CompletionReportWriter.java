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

public final class SteelHook03CompletionReportWriter {
  private static final Set<String> RAW_BYTE_KEYS =
      Set.of(
          "classBytes",
          "rawClassBytes",
          "originalClassBytes",
          "transformedClassBytes",
          "stackMapTableBytes",
          "rawStackMapTableBytes",
          "bytecodeBytes");

  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook03CompletionReport report) throws LoaderException {
    JsonObject root = toJson(report);
    String rawByteKey = firstRawByteKey(root);
    if (rawByteKey != null) {
      throw new LoaderException(
          "SteelHook 0.3 completion report must not serialize raw byte payload key " + rawByteKey);
    }
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
          "Failed to write SteelHook 0.3 completion report " + outputPath.getFileName(), exception);
    }
  }

  JsonObject toJson(SteelHook03CompletionReport report) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", report.schema());
    addString(root, "milestoneName", report.milestoneName());
    addString(root, "target", report.target());
    addString(root, "steelHookVersion", report.steelHookVersion());
    root.addProperty("completionReady", report.completionReady());
    addString(root, "status", report.status() == null ? null : report.status().id());
    addString(
        root, "handoffStatus", report.handoffStatus() == null ? null : report.handoffStatus().id());
    addString(root, "sourceSteelHook02Milestone", report.sourceSteelHook02Milestone());
    addString(root, "sourceSteelHook02Status", report.sourceSteelHook02Status());
    root.addProperty("sourceSteelHook02CompletionReady", report.sourceSteelHook02CompletionReady());
    addString(root, "sourceSteelHook02HandoffStatus", report.sourceSteelHook02HandoffStatus());
    addString(root, "sourceTarget28Milestone", report.sourceTarget28Milestone());
    addString(root, "sourceTarget28Status", report.sourceTarget28Status());
    root.addProperty(
        "sourceTarget28FramedMethodFoundationReady",
        report.sourceTarget28FramedMethodFoundationReady());
    addString(root, "sourceTarget29Milestone", report.sourceTarget29Milestone());
    addString(root, "sourceTarget29Status", report.sourceTarget29Status());
    root.addProperty(
        "sourceTarget29MethodExitDispatchReady", report.sourceTarget29MethodExitDispatchReady());
    addString(root, "sourceTarget30Milestone", report.sourceTarget30Milestone());
    addString(root, "sourceTarget30Status", report.sourceTarget30Status());
    root.addProperty(
        "sourceTarget30GatedRuntimeProofReady", report.sourceTarget30GatedRuntimeProofReady());
    root.add("completedCapabilities", stringArray(report.completedCapabilities()));
    root.add("unsupportedCapabilities", stringArray(report.unsupportedCapabilities()));
    root.add("stageVerifications", stageVerifications(report.stageVerifications()));
    root.add("safetyInvariants", safetyInvariants(report.safetyInvariants()));
    root.add("forbiddenReportChecks", findings(report.forbiddenReportChecks()));
    root.addProperty("runtimeClassLoaderProofCount", report.runtimeClassLoaderProofCount());
    root.addProperty("runtimeClassLoaderSuccessCount", report.runtimeClassLoaderSuccessCount());
    root.addProperty("entryPrimitiveVerified", report.entryPrimitiveVerified());
    root.addProperty("exitPrimitiveVerified", report.exitPrimitiveVerified());
    root.addProperty("runtimeClassLoadingPathEnabled", report.runtimeClassLoadingPathEnabled());
    root.addProperty("classLoadingOccurred", report.classLoadingOccurred());
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
    root.add("findings", findings(report.findings()));
    return root;
  }

  private JsonArray stringArray(List<String> values) {
    JsonArray array = new JsonArray();
    for (String value : values) {
      array.add(value);
    }
    return array;
  }

  private JsonArray stageVerifications(List<SteelHook03CompletionStageVerification> values) {
    JsonArray array = new JsonArray();
    for (SteelHook03CompletionStageVerification value : values) {
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

  private JsonArray safetyInvariants(List<SteelHook03CompletionSafetyInvariant> values) {
    JsonArray array = new JsonArray();
    for (SteelHook03CompletionSafetyInvariant value : values) {
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

  private JsonArray findings(List<SteelHook03CompletionFinding> values) {
    JsonArray array = new JsonArray();
    for (SteelHook03CompletionFinding value : values) {
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
