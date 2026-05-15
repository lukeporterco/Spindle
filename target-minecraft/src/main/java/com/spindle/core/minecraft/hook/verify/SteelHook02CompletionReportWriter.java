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

public final class SteelHook02CompletionReportWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook02CompletionReport report) throws LoaderException {
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
          "Failed to write SteelHook 0.2 completion report " + outputPath.getFileName(), exception);
    }
  }

  JsonObject toJson(SteelHook02CompletionReport report) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", report.schema());
    addString(root, "milestoneName", report.milestoneName());
    addString(root, "target", report.target());
    addString(root, "steelHookVersion", report.steelHookVersion());
    addString(root, "status", report.status() == null ? null : report.status().id());
    addString(
        root, "handoffStatus", report.handoffStatus() == null ? null : report.handoffStatus().id());
    addString(
        root, "nextDirection", report.nextDirection() == null ? null : report.nextDirection().id());
    root.addProperty("reportChainVerified", report.reportChainVerified());
    root.addProperty("completionReady", report.completionReady());
    root.addProperty("stageFailureCount", report.stageFailureCount());
    root.addProperty("safetyInvariantFailureCount", report.safetyInvariantFailureCount());
    root.addProperty("capabilityBoundaryCount", report.capabilityBoundaryCount());
    addString(root, "targetBinaryName", report.targetBinaryName());
    addString(root, "targetClassEntryName", report.targetClassEntryName());
    addString(root, "primitiveKind", report.primitiveKind());
    root.addProperty("runtimeClassLoadingPathVerified", report.runtimeClassLoadingPathVerified());
    root.addProperty("runtimeClassDefinedVerified", report.runtimeClassDefinedVerified());
    root.addProperty("minecraftMainNotInvokedVerified", report.minecraftMainNotInvokedVerified());
    root.addProperty(
        "minecraftServerNotLaunchedVerified", report.minecraftServerNotLaunchedVerified());
    root.addProperty(
        "hookInstallationNotOccurredVerified", report.hookInstallationNotOccurredVerified());
    root.addProperty(
        "runtimeDispatchNotObservedVerified", report.runtimeDispatchNotObservedVerified());
    root.addProperty("publicApiNotExposedVerified", report.publicApiNotExposedVerified());
    root.addProperty(
        "javaModExecutionSandboxingNotClaimedVerified",
        report.javaModExecutionSandboxingNotClaimedVerified());
    root.addProperty(
        "unsupportedCapabilitiesRemainBlockedVerified",
        report.unsupportedCapabilitiesRemainBlockedVerified());
    root.add("stageVerifications", stageVerifications(report.stageVerifications()));
    root.add("safetyInvariants", safetyInvariants(report.safetyInvariants()));
    root.add("capabilityBoundaries", capabilityBoundaries(report.capabilityBoundaries()));
    addString(root, "failureSummary", report.failureSummary());
    return root;
  }

  private JsonArray stageVerifications(List<SteelHookStageVerification> stageVerifications) {
    JsonArray array = new JsonArray();
    for (SteelHookStageVerification stage : stageVerifications) {
      JsonObject object = new JsonObject();
      addString(object, "stageId", stage.stageId());
      addString(object, "milestoneName", stage.milestoneName());
      addString(object, "summary", stage.summary());
      object.addProperty("passed", stage.passed());
      addString(object, "failureReason", stage.failureReason());
      array.add(object);
    }
    return array;
  }

  private JsonArray safetyInvariants(List<SteelHookSafetyInvariant> safetyInvariants) {
    JsonArray array = new JsonArray();
    for (SteelHookSafetyInvariant invariant : safetyInvariants) {
      JsonObject object = new JsonObject();
      addString(object, "id", invariant.id());
      addString(object, "expectedValue", invariant.expectedValue());
      addString(object, "actualValue", invariant.actualValue());
      object.addProperty("passed", invariant.passed());
      addString(object, "failureReason", invariant.failureReason());
      array.add(object);
    }
    return array;
  }

  private JsonArray capabilityBoundaries(List<SteelHookCapabilityBoundary> capabilityBoundaries) {
    JsonArray array = new JsonArray();
    for (SteelHookCapabilityBoundary boundary : capabilityBoundaries) {
      JsonObject object = new JsonObject();
      addString(object, "id", boundary.id());
      addString(object, "status", boundary.status());
      addString(object, "summary", boundary.summary());
      array.add(object);
    }
    return array;
  }

  private void addString(JsonObject object, String name, String value) {
    if (value == null) {
      object.add(name, JsonNull.INSTANCE);
    } else {
      object.addProperty(name, value);
    }
  }
}
