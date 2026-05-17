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

public final class SteelHook03FramedMethodFoundationReportWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook03FramedMethodFoundationReport report)
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
          "Failed to write SteelHook 0.3 framed method foundation report "
              + outputPath.getFileName(),
          exception);
    }
  }

  JsonObject toJson(SteelHook03FramedMethodFoundationReport report) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", report.schema());
    addString(root, "milestoneName", report.milestoneName());
    addString(root, "target", report.target());
    addString(root, "steelHookVersion", report.steelHookVersion());
    addString(root, "sourceSteelHook02Milestone", report.sourceSteelHook02Milestone());
    root.addProperty("sourceSteelHook02CompletionReady", report.sourceSteelHook02CompletionReady());
    addString(root, "sourceSteelHook02HandoffStatus", report.sourceSteelHook02HandoffStatus());
    root.addProperty("framedMethodFoundationReady", report.framedMethodFoundationReady());
    addString(root, "status", report.status() == null ? null : report.status().id());
    addString(
        root, "nextDirection", report.nextDirection() == null ? null : report.nextDirection().id());
    root.addProperty("stackMapTableRewriteSupported", report.stackMapTableRewriteSupported());
    root.addProperty("stackMapTableRewriteApplied", report.stackMapTableRewriteApplied());
    root.addProperty("stackMapTableFrameShiftApplied", report.stackMapTableFrameShiftApplied());
    addInteger(root, "stackMapTableEntryCountBefore", report.stackMapTableEntryCountBefore());
    addInteger(root, "stackMapTableEntryCountAfter", report.stackMapTableEntryCountAfter());
    addInteger(root, "firstFrameOffsetDeltaBefore", report.firstFrameOffsetDeltaBefore());
    addInteger(root, "firstFrameOffsetDeltaAfter", report.firstFrameOffsetDeltaAfter());
    root.addProperty("insertionOffset", report.insertionOffset());
    root.addProperty("insertedInstructionLength", report.insertedInstructionLength());
    root.addProperty(
        "methodEntryTransformationOccurred", report.methodEntryTransformationOccurred());
    root.addProperty("bytecodeModified", report.bytecodeModified());
    root.addProperty("transformedClassBytesProduced", report.transformedClassBytesProduced());
    root.addProperty("runtimeClassLoadingPathEnabled", report.runtimeClassLoadingPathEnabled());
    root.addProperty("classLoadingOccurred", report.classLoadingOccurred());
    root.addProperty("serverLaunchOccurred", report.serverLaunchOccurred());
    root.addProperty("hookInstallationOccurred", report.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", report.runtimeDispatchOccurred());
    root.addProperty("publicApiExposed", report.publicApiExposed());
    root.addProperty("javaAgentUsed", report.javaAgentUsed());
    root.addProperty("mixinUsed", report.mixinUsed());
    root.addProperty("javaModExecutionSandboxed", report.javaModExecutionSandboxed());
    addString(root, "failureReason", report.failureReason());
    JsonArray findings = new JsonArray();
    for (SteelHook03FramedMethodFoundationFinding finding : report.findings()) {
      JsonObject object = new JsonObject();
      addString(object, "id", finding.id());
      object.addProperty("blocking", finding.blocking());
      addString(object, "summary", finding.summary());
      findings.add(object);
    }
    root.add("findings", findings);
    return root;
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
