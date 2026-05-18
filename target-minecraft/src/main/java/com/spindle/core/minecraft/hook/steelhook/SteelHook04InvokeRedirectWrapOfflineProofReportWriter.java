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

public final class SteelHook04InvokeRedirectWrapOfflineProofReportWriter {
  public static final String REPORT_FILE_NAME =
      SteelHook04InvokeRedirectWrapOfflineProofRunner.REPORT_FILE_NAME;

  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook04InvokeRedirectWrapOfflineProofReport report)
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
          "Failed to write Target-34 invoke redirect/wrap offline proof report " + outputPath,
          exception);
    }
  }

  JsonObject toJson(SteelHook04InvokeRedirectWrapOfflineProofReport report) {
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
        "sourceInvokeRedirectCandidatePresent", report.sourceInvokeRedirectCandidatePresent());
    root.addProperty(
        "sourceInvokeRedirectCandidateInternalOnly",
        report.sourceInvokeRedirectCandidateInternalOnly());
    root.addProperty(
        "sourceInvokeRedirectCandidatePublicApiExposed",
        report.sourceInvokeRedirectCandidatePublicApiExposed());
    root.addProperty(
        "sourceInvokeRedirectCandidateRuntimeReady",
        report.sourceInvokeRedirectCandidateRuntimeReady());
    root.addProperty(
        "sourceInvokeRedirectCandidateGatedRuntimeReady",
        report.sourceInvokeRedirectCandidateGatedRuntimeReady());
    root.addProperty(
        "sourceInvokeRedirectCandidateImplementedInTarget32",
        report.sourceInvokeRedirectCandidateImplementedInTarget32());
    root.addProperty("sourceInvokeWrapCandidatePresent", report.sourceInvokeWrapCandidatePresent());
    root.addProperty(
        "sourceInvokeWrapCandidateInternalOnly", report.sourceInvokeWrapCandidateInternalOnly());
    root.addProperty(
        "sourceInvokeWrapCandidatePublicApiExposed",
        report.sourceInvokeWrapCandidatePublicApiExposed());
    root.addProperty(
        "sourceInvokeWrapCandidateRuntimeReady", report.sourceInvokeWrapCandidateRuntimeReady());
    root.addProperty(
        "sourceInvokeWrapCandidateGatedRuntimeReady",
        report.sourceInvokeWrapCandidateGatedRuntimeReady());
    root.addProperty(
        "sourceInvokeWrapCandidateImplementedInTarget32",
        report.sourceInvokeWrapCandidateImplementedInTarget32());
    root.addProperty(
        "sourceTarget32RuntimeSideEffectsSafe", report.sourceTarget32RuntimeSideEffectsSafe());
    addString(root, "sourceTarget33Milestone", report.sourceTarget33Milestone());
    addString(root, "sourceTarget33ProofStatus", report.sourceTarget33ProofStatus());
    root.addProperty("sourceTarget33ProofReady", report.sourceTarget33ProofReady());
    addString(
        root,
        "sourceTarget33PrimitiveKind",
        report.sourceTarget33PrimitiveKind() == null
            ? null
            : report.sourceTarget33PrimitiveKind().id());
    root.addProperty(
        "sourceTarget33SuccessfulProofCaseCount", report.sourceTarget33SuccessfulProofCaseCount());
    root.addProperty(
        "sourceTarget33RuntimeSideEffectsSafe", report.sourceTarget33RuntimeSideEffectsSafe());
    root.addProperty("proofReady", report.proofReady());
    addString(root, "proofStatus", report.proofStatus().name());
    addString(root, "proofStatusId", report.proofStatus().id());
    addString(root, "nextDirection", report.nextDirection().name());
    addString(root, "nextDirectionId", report.nextDirection().id());
    addString(root, "nextRecommendedAction", report.nextRecommendedAction());
    root.add("approvedPrimitiveKinds", primitiveKinds(report.approvedPrimitiveKinds()));
    addString(root, "approvedFixtureShape", report.approvedFixtureShape().id());
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

  private JsonArray primitiveKinds(List<SteelHook04PrimitiveKind> primitiveKinds) {
    JsonArray array = new JsonArray();
    for (SteelHook04PrimitiveKind primitiveKind : primitiveKinds) {
      array.add(primitiveKind.id());
    }
    return array;
  }

  private JsonArray proofCases(List<SteelHook04InvokeCallsiteProofCase> proofCases) {
    JsonArray array = new JsonArray();
    for (SteelHook04InvokeCallsiteProofCase proofCase : proofCases) {
      JsonObject object = new JsonObject();
      addString(object, "id", proofCase.id());
      addString(object, "label", proofCase.label());
      addString(object, "primitiveKind", proofCase.primitiveKind().id());
      addString(object, "rewriteMode", proofCase.rewriteMode().id());
      addString(object, "fixtureShape", proofCase.fixtureShape().id());
      addString(object, "targetOwnerInternalName", proofCase.targetOwnerInternalName());
      addString(object, "targetMethodName", proofCase.targetMethodName());
      addString(object, "targetDescriptor", proofCase.targetDescriptor());
      addString(
          object, "expectedInvokeOwnerInternalName", proofCase.expectedInvokeOwnerInternalName());
      addString(object, "expectedInvokeName", proofCase.expectedInvokeName());
      addString(object, "expectedInvokeDescriptor", proofCase.expectedInvokeDescriptor());
      addString(object, "expectedInvokeOpcode", proofCase.expectedInvokeOpcode().id());
      addString(
          object,
          "replacementInvokeOwnerInternalName",
          proofCase.replacementInvokeOwnerInternalName());
      addString(object, "replacementInvokeName", proofCase.replacementInvokeName());
      addString(object, "replacementInvokeDescriptor", proofCase.replacementInvokeDescriptor());
      addString(object, "replacementInvokeOpcode", proofCase.replacementInvokeOpcode().id());
      addString(object, "matchedInvokeOpcode", proofCase.matchedInvokeOpcode());
      object.addProperty("matchedCallsiteCount", proofCase.matchedCallsiteCount());
      addString(object, "originalClassSha256", proofCase.originalClassSha256());
      addString(object, "transformedClassSha256", proofCase.transformedClassSha256());
      addString(object, "originalCodeSha256", proofCase.originalCodeSha256());
      addString(object, "transformedCodeSha256", proofCase.transformedCodeSha256());
      object.addProperty("originalCodeLength", proofCase.originalCodeLength());
      object.addProperty("transformedCodeLength", proofCase.transformedCodeLength());
      object.addProperty("bytecodeModified", proofCase.bytecodeModified());
      object.addProperty(
          "transformedClassBytesProduced", proofCase.transformedClassBytesProduced());
      addString(object, "replacementSummary", proofCase.replacementSummary());
      addString(
          object, "wrappedDelegateOwnerInternalName", proofCase.wrappedDelegateOwnerInternalName());
      addString(object, "wrappedDelegateName", proofCase.wrappedDelegateName());
      addString(object, "wrappedDelegateDescriptor", proofCase.wrappedDelegateDescriptor());
      addString(
          object,
          "wrappedDelegateOpcode",
          proofCase.wrappedDelegateOpcode() == null
              ? null
              : proofCase.wrappedDelegateOpcode().id());
      addString(object, "wrapperOwnerInternalName", proofCase.wrapperOwnerInternalName());
      addString(object, "wrapperName", proofCase.wrapperName());
      addString(object, "wrapperDescriptor", proofCase.wrapperDescriptor());
      addString(
          object,
          "wrapperOpcode",
          proofCase.wrapperOpcode() == null ? null : proofCase.wrapperOpcode().id());
      array.add(object);
    }
    return array;
  }

  private JsonArray findings(List<SteelHook04InvokeCallsiteFinding> findings) {
    JsonArray array = new JsonArray();
    for (SteelHook04InvokeCallsiteFinding finding : findings) {
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
