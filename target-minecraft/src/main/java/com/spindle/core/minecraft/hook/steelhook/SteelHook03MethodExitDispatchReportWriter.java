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

public final class SteelHook03MethodExitDispatchReportWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook03MethodExitDispatchReport report)
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
          "Failed to write SteelHook 0.3 method-exit dispatch report " + outputPath.getFileName(),
          exception);
    }
  }

  JsonObject toJson(SteelHook03MethodExitDispatchReport report) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", report.schema());
    addString(root, "milestoneName", report.milestoneName());
    addString(root, "target", report.target());
    addString(root, "steelHookVersion", report.steelHookVersion());
    addString(
        root, "primitiveKind", report.primitiveKind() == null ? null : report.primitiveKind().id());
    addString(root, "sourceTarget28Milestone", report.sourceTarget28Milestone());
    addString(root, "sourceTarget28Status", report.sourceTarget28Status());
    root.addProperty(
        "sourceTarget28FramedMethodFoundationReady",
        report.sourceTarget28FramedMethodFoundationReady());
    addString(root, "sourceTarget28NextDirection", report.sourceTarget28NextDirection());
    root.addProperty("methodExitDispatchReady", report.methodExitDispatchReady());
    addString(root, "status", report.status() == null ? null : report.status().id());
    addString(
        root, "nextDirection", report.nextDirection() == null ? null : report.nextDirection().id());
    addString(root, "targetOwnerInternalName", report.targetOwnerInternalName());
    addString(root, "targetMethodName", report.targetMethodName());
    addString(root, "targetDescriptor", report.targetDescriptor());
    addString(root, "dispatcherOwnerInternalName", report.dispatcherOwnerInternalName());
    addString(root, "dispatcherMethodName", report.dispatcherMethodName());
    addString(root, "dispatcherDescriptor", report.dispatcherDescriptor());
    addString(root, "opcodeMnemonic", report.opcodeMnemonic());
    addString(root, "opcodeHex", report.opcodeHex());
    root.addProperty("insertedInstructionLength", report.insertedInstructionLength());
    JsonArray supportedReturnOpcodes = new JsonArray();
    for (String opcode : report.supportedReturnOpcodes()) {
      supportedReturnOpcodes.add(opcode);
    }
    root.add("supportedReturnOpcodes", supportedReturnOpcodes);
    addInteger(root, "normalReturnOpcodeCount", report.normalReturnOpcodeCount());
    addInteger(root, "insertionCount", report.insertionCount());
    root.add("insertionOffsetsOriginal", integers(report.insertionOffsetsOriginal()));
    root.add("insertionOffsetsTransformed", integers(report.insertionOffsetsTransformed()));
    addInteger(root, "originalCodeLength", report.originalCodeLength());
    addInteger(root, "transformedCodeLength", report.transformedCodeLength());
    addInteger(root, "constantPoolCountBefore", report.constantPoolCountBefore());
    addInteger(root, "constantPoolCountAfter", report.constantPoolCountAfter());
    root.addProperty("methodExitTransformationOccurred", report.methodExitTransformationOccurred());
    root.addProperty("bytecodeModified", report.bytecodeModified());
    root.addProperty("transformedClassBytesProduced", report.transformedClassBytesProduced());
    root.addProperty("stackMapTablePresent", report.stackMapTablePresent());
    root.addProperty("stackMapTableRewriteSupported", report.stackMapTableRewriteSupported());
    root.addProperty("stackMapTableRewriteApplied", report.stackMapTableRewriteApplied());
    root.addProperty("exceptionTablePresent", report.exceptionTablePresent());
    root.addProperty("branchRewriteRequired", report.branchRewriteRequired());
    root.addProperty("switchRewriteRequired", report.switchRewriteRequired());
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
    for (SteelHook03MethodExitDispatchFinding finding : report.findings()) {
      JsonObject object = new JsonObject();
      addString(object, "id", finding.id());
      object.addProperty("blocking", finding.blocking());
      addString(object, "summary", finding.summary());
      findings.add(object);
    }
    root.add("findings", findings);
    return root;
  }

  private JsonArray integers(java.util.List<Integer> values) {
    JsonArray array = new JsonArray();
    for (Integer value : values) {
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
