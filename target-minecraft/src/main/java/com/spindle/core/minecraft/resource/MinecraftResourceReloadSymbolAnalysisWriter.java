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

public final class MinecraftResourceReloadSymbolAnalysisWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftResourceReloadSymbolAnalysis analysis)
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
          "Failed to write Minecraft resource reload symbol analysis " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftResourceReloadSymbolAnalysis analysis) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", analysis.schema());
    addString(root, "milestoneName", analysis.milestoneName());
    addString(root, "target", analysis.target());
    addString(root, "minecraftVersion", analysis.minecraftVersion());
    addString(root, "side", analysis.side().id());
    addString(root, "conceptId", analysis.conceptId());
    addString(
        root,
        "sourceResourceReloadAnalysisMilestone",
        analysis.sourceResourceReloadAnalysisMilestone());
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
    root.addProperty("sourceResourceReloadGatePassed", analysis.sourceResourceReloadGatePassed());
    root.addProperty(
        "resourceLifecycleAnchorAvailable", analysis.resourceLifecycleAnchorAvailable());
    root.addProperty("reloadDiscoveryBoundaryDeclared", analysis.reloadDiscoveryBoundaryDeclared());
    root.addProperty("gatePassed", analysis.gatePassed());
    addString(root, "gateFailureReason", analysis.gateFailureReason());
    root.add("discoveryTokens", strings(analysis.discoveryTokens()));
    root.addProperty("candidateCount", analysis.candidateCount());
    root.addProperty("classNameCandidateCount", analysis.classNameCandidateCount());
    root.addProperty("fieldNameCandidateCount", analysis.fieldNameCandidateCount());
    root.addProperty("fieldDescriptorCandidateCount", analysis.fieldDescriptorCandidateCount());
    root.addProperty("methodNameCandidateCount", analysis.methodNameCandidateCount());
    root.addProperty("methodDescriptorCandidateCount", analysis.methodDescriptorCandidateCount());
    root.addProperty("selectableCandidateCount", analysis.selectableCandidateCount());
    root.addProperty("rejectedCandidateCount", analysis.rejectedCandidateCount());
    root.addProperty("discoveryStatus", analysis.discoveryStatus().name());
    root.addProperty("bindingStrategyAnalysisEligible", analysis.bindingStrategyAnalysisEligible());
    root.add("candidates", candidates(analysis.candidates()));
    return root;
  }

  private JsonArray candidates(List<MinecraftResourceReloadSymbolCandidate> candidates) {
    JsonArray array = new JsonArray();
    for (MinecraftResourceReloadSymbolCandidate candidate : candidates) {
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
