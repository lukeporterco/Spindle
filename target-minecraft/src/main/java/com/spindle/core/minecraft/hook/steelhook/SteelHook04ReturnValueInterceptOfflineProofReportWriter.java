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

public final class SteelHook04ReturnValueInterceptOfflineProofReportWriter {
  public static final String REPORT_FILE_NAME =
      SteelHook04ReturnValueInterceptOfflineProofRunner.REPORT_FILE_NAME;

  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook04ReturnValueInterceptOfflineProofReport report)
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
          "Failed to write Target-33 return-value intercept offline proof report " + outputPath,
          exception);
    }
  }

  JsonObject toJson(SteelHook04ReturnValueInterceptOfflineProofReport report) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", report.schema());
    addString(root, "milestoneName", report.milestoneName());
    addString(root, "target", report.target());
    addString(root, "steelHookVersion", report.steelHookVersion());
    addString(root, "sourceTarget32Milestone", report.sourceTarget32Milestone());
    addString(root, "sourceTarget32BoundaryStatus", report.sourceTarget32BoundaryStatus());
    root.addProperty("sourceTarget32GatePassed", report.sourceTarget32GatePassed());
    root.addProperty(
        "sourceTarget32ApprovedPrimitiveCount", report.sourceTarget32ApprovedPrimitiveCount());
    root.addProperty(
        "sourceReturnValueInterceptCandidatePresent",
        report.sourceReturnValueInterceptCandidatePresent());
    root.addProperty(
        "sourceReturnValueInterceptCandidateInternalOnly",
        report.sourceReturnValueInterceptCandidateInternalOnly());
    root.addProperty(
        "sourceReturnValueInterceptCandidatePublicApiExposed",
        report.sourceReturnValueInterceptCandidatePublicApiExposed());
    root.addProperty(
        "sourceReturnValueInterceptCandidateRuntimeReady",
        report.sourceReturnValueInterceptCandidateRuntimeReady());
    root.addProperty(
        "sourceReturnValueInterceptCandidateGatedRuntimeReady",
        report.sourceReturnValueInterceptCandidateGatedRuntimeReady());
    root.addProperty(
        "sourceReturnValueInterceptCandidateImplementedInTarget32",
        report.sourceReturnValueInterceptCandidateImplementedInTarget32());
    root.addProperty("sourceRuntimeSideEffectsSafe", report.sourceRuntimeSideEffectsSafe());
    root.addProperty("proofReady", report.proofReady());
    addString(root, "proofStatus", report.proofStatus().name());
    addString(root, "proofStatusId", report.proofStatus().id());
    addString(root, "nextDirection", report.nextDirection().name());
    addString(root, "nextDirectionId", report.nextDirection().id());
    addString(root, "nextRecommendedAction", report.nextRecommendedAction());
    addString(root, "primitiveKind", report.primitiveKind().id());
    root.add("approvedFixtureShapes", fixtureShapes(report.approvedFixtureShapes()));
    root.addProperty("unsupportedFixtureShapesRejected", report.unsupportedFixtureShapesRejected());
    root.addProperty("successfulProofCaseCount", report.successfulProofCaseCount());
    root.addProperty("rejectionProofCaseCount", report.rejectionProofCaseCount());
    root.add("proofCases", proofCases(report.proofCases()));
    root.add("findings", findings(report.findings()));
    root.addProperty("offlineOnly", report.offlineOnly());
    root.addProperty("runtimeClassLoadingPathEnabled", report.runtimeClassLoadingPathEnabled());
    root.addProperty("classLoadingOccurred", report.classLoadingOccurred());
    root.addProperty("serverLaunchOccurred", report.serverLaunchOccurred());
    root.addProperty("minecraftMainInvoked", report.minecraftMainInvoked());
    root.addProperty("hookInstallationOccurred", report.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", report.runtimeDispatchOccurred());
    root.addProperty("publicApiExposed", report.publicApiExposed());
    root.addProperty("javaAgentUsed", report.javaAgentUsed());
    root.addProperty("mixinUsed", report.mixinUsed());
    root.addProperty("javaModExecutionSandboxed", report.javaModExecutionSandboxed());
    return root;
  }

  private JsonArray fixtureShapes(List<SteelHook04FixtureShape> fixtureShapes) {
    JsonArray array = new JsonArray();
    for (SteelHook04FixtureShape fixtureShape : fixtureShapes) {
      array.add(fixtureShape.id());
    }
    return array;
  }

  private JsonArray proofCases(List<SteelHook04ReturnValueInterceptProofCase> proofCases) {
    JsonArray array = new JsonArray();
    for (SteelHook04ReturnValueInterceptProofCase proofCase : proofCases) {
      JsonObject object = new JsonObject();
      addString(object, "id", proofCase.id());
      addString(object, "label", proofCase.label());
      addString(object, "mode", proofCase.mode().id());
      addString(object, "fixtureShape", proofCase.fixtureShape().id());
      addString(object, "interceptKind", proofCase.interceptKind().id());
      addString(object, "targetOwnerInternalName", proofCase.targetOwnerInternalName());
      addString(object, "targetMethodName", proofCase.targetMethodName());
      addString(object, "targetDescriptor", proofCase.targetDescriptor());
      addString(object, "returnOpcode", proofCase.returnOpcode());
      addString(object, "producerOpcode", proofCase.producerOpcode());
      object.addProperty("matchCount", proofCase.matchCount());
      object.addProperty("bytecodeModified", proofCase.bytecodeModified());
      object.addProperty(
          "transformedClassBytesProduced", proofCase.transformedClassBytesProduced());
      addString(object, "originalClassSha256", proofCase.originalClassSha256());
      addString(object, "transformedClassSha256", proofCase.transformedClassSha256());
      addString(object, "originalCodeSha256", proofCase.originalCodeSha256());
      addString(object, "transformedCodeSha256", proofCase.transformedCodeSha256());
      object.addProperty("originalCodeLength", proofCase.originalCodeLength());
      object.addProperty("transformedCodeLength", proofCase.transformedCodeLength());
      addString(object, "replacementSummary", proofCase.replacementSummary());
      array.add(object);
    }
    return array;
  }

  private JsonArray findings(List<SteelHook04ReturnValueInterceptFinding> findings) {
    JsonArray array = new JsonArray();
    for (SteelHook04ReturnValueInterceptFinding finding : findings) {
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

  private void addString(JsonObject object, String name, String value) {
    if (value == null) {
      object.add(name, JsonNull.INSTANCE);
    } else {
      object.addProperty(name, value);
    }
  }
}
