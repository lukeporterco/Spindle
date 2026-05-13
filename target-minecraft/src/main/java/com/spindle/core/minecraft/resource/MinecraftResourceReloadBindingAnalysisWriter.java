package com.spindle.core.minecraft.resource;

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

public final class MinecraftResourceReloadBindingAnalysisWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftResourceReloadBindingAnalysis analysis)
      throws LoaderException {
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(toJson(analysis), writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write Minecraft resource reload binding analysis " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftResourceReloadBindingAnalysis analysis) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", analysis.schema());
    addString(root, "milestoneName", analysis.milestoneName());
    addString(root, "target", analysis.target());
    addString(root, "minecraftVersion", analysis.minecraftVersion());
    addString(root, "side", analysis.side().id());
    addString(root, "conceptId", analysis.conceptId());
    addString(
        root,
        "sourceResourceReloadSymbolAnalysisMilestone",
        analysis.sourceResourceReloadSymbolAnalysisMilestone());
    addString(root, "resourceBoundaryId", analysis.resourceBoundaryId());
    root.addProperty("analysisOnly", analysis.analysisOnly());
    root.addProperty("classLoadingOccurred", analysis.classLoadingOccurred());
    root.addProperty("injectionOccurred", analysis.injectionOccurred());
    root.addProperty("transformationOccurred", analysis.transformationOccurred());
    root.addProperty("patchingOccurred", analysis.patchingOccurred());
    root.addProperty("hookInstallationOccurred", analysis.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", analysis.runtimeDispatchOccurred());
    root.addProperty("resourceReloadOccurred", analysis.resourceReloadOccurred());
    root.addProperty("resourceAccessOccurred", analysis.resourceAccessOccurred());
    root.addProperty("datapackAccessOccurred", analysis.datapackAccessOccurred());
    root.addProperty("dataGenerationOccurred", analysis.dataGenerationOccurred());
    root.addProperty("registryMutationOccurred", analysis.registryMutationOccurred());
    root.addProperty("publicApiExposed", analysis.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", analysis.javaModExecutionSandboxed());
    root.addProperty("sourceSymbolGatePassed", analysis.sourceSymbolGatePassed());
    addString(root, "sourceDiscoveryStatus", analysis.sourceDiscoveryStatus());
    root.addProperty(
        "sourceBindingStrategyAnalysisEligible", analysis.sourceBindingStrategyAnalysisEligible());
    root.addProperty("gatePassed", analysis.gatePassed());
    addString(root, "gateFailureReason", analysis.gateFailureReason());
    root.addProperty("bindingStatus", analysis.bindingStatus().name());
    root.addProperty("sourceCandidateCount", analysis.sourceCandidateCount());
    root.addProperty("analyzedCandidateCount", analysis.analyzedCandidateCount());
    root.addProperty("selectableCandidateCount", analysis.selectableCandidateCount());
    root.addProperty("rejectedCandidateCount", analysis.rejectedCandidateCount());
    root.addProperty("classReferenceOnlyCount", analysis.classReferenceOnlyCount());
    root.addProperty(
        "methodBoundaryAnalysisRequiredCount", analysis.methodBoundaryAnalysisRequiredCount());
    root.addProperty("fieldAccessRequiredCount", analysis.fieldAccessRequiredCount());
    root.addProperty("receiverCaptureRequiredCount", analysis.receiverCaptureRequiredCount());
    root.addProperty(
        "futureSteelHookPrimitiveRequiredCount", analysis.futureSteelHookPrimitiveRequiredCount());
    root.addProperty("reloadProofRecommended", analysis.reloadProofRecommended());
    root.addProperty(
        "currentSteelHookMethodEntryCompatible", analysis.currentSteelHookMethodEntryCompatible());
    addString(root, "nextRecommendedAction", analysis.nextRecommendedAction());
    root.add("candidates", candidates(analysis.candidates()));
    return root;
  }

  private JsonArray candidates(List<MinecraftResourceReloadBindingCandidate> candidates) {
    JsonArray array = new JsonArray();
    for (MinecraftResourceReloadBindingCandidate candidate : candidates) {
      JsonObject object = new JsonObject();
      addString(object, "sourceCandidateId", candidate.sourceCandidateId());
      addString(object, "sourceCandidateKind", candidate.sourceCandidateKind());
      addString(object, "boundaryId", candidate.boundaryId());
      addString(object, "ownerInternalName", candidate.ownerInternalName());
      addString(object, "memberName", candidate.memberName());
      addString(object, "descriptor", candidate.descriptor());
      object.addProperty("staticMember", candidate.staticMember());
      object.add("matchedTokens", strings(candidate.matchedTokens()));
      object.addProperty("selectable", candidate.selectable());
      addString(object, "sourceRejectionReason", candidate.sourceRejectionReason());
      object.addProperty("accessStrategy", candidate.accessStrategy().name());
      object.addProperty("requiresSymbolNarrowing", candidate.requiresSymbolNarrowing());
      object.addProperty(
          "requiresMethodBoundaryAnalysis", candidate.requiresMethodBoundaryAnalysis());
      object.addProperty("requiresReceiverCapture", candidate.requiresReceiverCapture());
      object.addProperty("requiresFieldAccess", candidate.requiresFieldAccess());
      object.addProperty(
          "requiresRuntimeResourceAccess", candidate.requiresRuntimeResourceAccess());
      object.addProperty("requiresReloadTimingDecision", candidate.requiresReloadTimingDecision());
      object.addProperty(
          "requiresReloadApplySemanticsDecision", candidate.requiresReloadApplySemanticsDecision());
      object.addProperty(
          "requiresFutureSteelHookPrimitive", candidate.requiresFutureSteelHookPrimitive());
      object.addProperty(
          "currentSteelHookMethodEntryCompatible",
          candidate.currentSteelHookMethodEntryCompatible());
      object.addProperty("reloadProofRecommended", candidate.reloadProofRecommended());
      addString(object, "notes", candidate.notes());
      array.add(object);
    }
    return array;
  }

  private JsonArray strings(List<String> values) {
    JsonArray array = new JsonArray();
    for (String value : values) {
      array.add(value);
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
