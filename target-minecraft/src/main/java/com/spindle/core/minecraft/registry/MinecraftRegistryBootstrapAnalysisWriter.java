package com.spindle.core.minecraft.registry;

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

public final class MinecraftRegistryBootstrapAnalysisWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftRegistryBootstrapAnalysis analysis)
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
          "Failed to write Minecraft registry bootstrap analysis " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftRegistryBootstrapAnalysis analysis) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", analysis.schema());
    addString(root, "milestoneName", analysis.milestoneName());
    addString(root, "target", analysis.target());
    addString(root, "minecraftVersion", analysis.minecraftVersion());
    addString(root, "side", analysis.side().id());
    addString(root, "conceptId", analysis.conceptId());
    addString(
        root,
        "sourceArtifactInterpretationMilestone",
        analysis.sourceArtifactInterpretationMilestone());
    addString(
        root,
        "sourceResourceReloadArcDecisionMilestone",
        analysis.sourceResourceReloadArcDecisionMilestone());
    root.addProperty("analysisOnly", analysis.analysisOnly());
    root.addProperty("classLoadingOccurred", analysis.classLoadingOccurred());
    root.addProperty("injectionOccurred", analysis.injectionOccurred());
    root.addProperty("transformationOccurred", analysis.transformationOccurred());
    root.addProperty("patchingOccurred", analysis.patchingOccurred());
    root.addProperty("hookInstallationOccurred", analysis.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", analysis.runtimeDispatchOccurred());
    root.addProperty("registryBootstrapOccurred", analysis.registryBootstrapOccurred());
    root.addProperty("registryMutationOccurred", analysis.registryMutationOccurred());
    root.addProperty("contentRegistrationOccurred", analysis.contentRegistrationOccurred());
    root.addProperty("resourceAccessOccurred", analysis.resourceAccessOccurred());
    root.addProperty("datapackAccessOccurred", analysis.datapackAccessOccurred());
    root.addProperty("dataGenerationOccurred", analysis.dataGenerationOccurred());
    root.addProperty("publicApiExposed", analysis.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", analysis.javaModExecutionSandboxed());
    root.addProperty(
        "sourceResourceReloadArcDecisionGatePassed",
        analysis.sourceResourceReloadArcDecisionGatePassed());
    root.addProperty(
        "sourceResourceReloadArcDecisionRegistryBootstrapRecommended",
        analysis.sourceResourceReloadArcDecisionRegistryBootstrapRecommended());
    addString(
        root,
        "sourceResourceReloadArcDecisionNextDirection",
        analysis.sourceResourceReloadArcDecisionNextDirection());
    root.addProperty("gatePassed", analysis.gatePassed());
    addString(root, "gateFailureReason", analysis.gateFailureReason());
    root.add("discoveryTokens", strings(analysis.discoveryTokens()));
    root.addProperty("boundaryCount", analysis.boundaryCount());
    root.addProperty("anchorBoundaryCount", analysis.anchorBoundaryCount());
    root.addProperty("metadataAnalyzedBoundaryCount", analysis.metadataAnalyzedBoundaryCount());
    root.addProperty("declaredUnboundBoundaryCount", analysis.declaredUnboundBoundaryCount());
    root.addProperty("blockedBoundaryCount", analysis.blockedBoundaryCount());
    root.addProperty("candidateCount", analysis.candidateCount());
    root.addProperty("classNameCandidateCount", analysis.classNameCandidateCount());
    root.addProperty("fieldNameCandidateCount", analysis.fieldNameCandidateCount());
    root.addProperty("fieldDescriptorCandidateCount", analysis.fieldDescriptorCandidateCount());
    root.addProperty("methodNameCandidateCount", analysis.methodNameCandidateCount());
    root.addProperty("methodDescriptorCandidateCount", analysis.methodDescriptorCandidateCount());
    root.addProperty("selectableCandidateCount", analysis.selectableCandidateCount());
    root.addProperty("rejectedCandidateCount", analysis.rejectedCandidateCount());
    root.addProperty("classReferenceOnlyCount", analysis.classReferenceOnlyCount());
    root.addProperty(
        "methodBoundaryAnalysisRequiredCount", analysis.methodBoundaryAnalysisRequiredCount());
    root.addProperty("fieldAccessRequiredCount", analysis.fieldAccessRequiredCount());
    root.addProperty("receiverCaptureRequiredCount", analysis.receiverCaptureRequiredCount());
    root.addProperty(
        "futureSteelHookPrimitiveRequiredCount", analysis.futureSteelHookPrimitiveRequiredCount());
    root.addProperty("discoveryStatus", analysis.discoveryStatus().name());
    root.addProperty("bindingStatus", analysis.bindingStatus().name());
    root.addProperty("registryProofRecommended", analysis.registryProofRecommended());
    root.addProperty(
        "currentSteelHookMethodEntryCompatible", analysis.currentSteelHookMethodEntryCompatible());
    root.addProperty(
        "steelHookPrimitiveDesignRecommended", analysis.steelHookPrimitiveDesignRecommended());
    addString(root, "nextRecommendedAction", analysis.nextRecommendedAction());
    root.add("boundaries", boundaries(analysis.boundaries()));
    root.add("candidates", candidates(analysis.candidates()));
    return root;
  }

  private JsonArray boundaries(List<MinecraftAnalyzedRegistryBoundary> boundaries) {
    JsonArray array = new JsonArray();
    for (MinecraftAnalyzedRegistryBoundary boundary : boundaries) {
      JsonObject object = new JsonObject();
      addString(object, "boundaryId", boundary.boundaryId());
      addString(object, "displayName", boundary.displayName());
      object.addProperty("order", boundary.order());
      object.addProperty("status", boundary.status().name());
      object.addProperty("representationKind", boundary.representationKind().name());
      addString(object, "notes", boundary.notes());
      array.add(object);
    }
    return array;
  }

  private JsonArray candidates(List<MinecraftRegistryCandidate> candidates) {
    JsonArray array = new JsonArray();
    for (MinecraftRegistryCandidate candidate : candidates) {
      JsonObject object = new JsonObject();
      addString(object, "id", candidate.id());
      object.addProperty("kind", candidate.kind().name());
      addString(object, "boundaryId", candidate.boundaryId());
      addString(object, "ownerInternalName", candidate.ownerInternalName());
      addString(object, "memberName", candidate.memberName());
      addString(object, "descriptor", candidate.descriptor());
      object.addProperty("staticMember", candidate.staticMember());
      object.add("accessFlags", strings(candidate.accessFlags()));
      object.add("matchedTokens", strings(candidate.matchedTokens()));
      object.addProperty("selectable", candidate.selectable());
      addString(object, "rejectionReason", candidate.rejectionReason());
      object.addProperty("accessStrategy", candidate.accessStrategy().name());
      object.addProperty("requiresSymbolNarrowing", candidate.requiresSymbolNarrowing());
      object.addProperty(
          "requiresMethodBoundaryAnalysis", candidate.requiresMethodBoundaryAnalysis());
      object.addProperty("requiresReceiverCapture", candidate.requiresReceiverCapture());
      object.addProperty("requiresFieldAccess", candidate.requiresFieldAccess());
      object.addProperty("requiresRegistryValueAccess", candidate.requiresRegistryValueAccess());
      object.addProperty(
          "requiresRegistrationTimingDecision", candidate.requiresRegistrationTimingDecision());
      object.addProperty(
          "requiresRegistrationApplySemanticsDecision",
          candidate.requiresRegistrationApplySemanticsDecision());
      object.addProperty(
          "requiresFutureSteelHookPrimitive", candidate.requiresFutureSteelHookPrimitive());
      object.addProperty(
          "currentSteelHookMethodEntryCompatible",
          candidate.currentSteelHookMethodEntryCompatible());
      object.addProperty("registryProofRecommended", candidate.registryProofRecommended());
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
