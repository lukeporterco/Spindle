package com.spindle.core.minecraft.command;

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

public final class MinecraftCommandDispatcherSymbolAnalysisWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftCommandDispatcherSymbolAnalysis analysis)
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
          "Failed to write Minecraft command dispatcher symbol analysis " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftCommandDispatcherSymbolAnalysis analysis) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", analysis.schema());
    addString(root, "milestoneName", analysis.milestoneName());
    addString(root, "target", analysis.target());
    addString(root, "minecraftVersion", analysis.minecraftVersion());
    addString(root, "side", analysis.side().id());
    addString(root, "conceptId", analysis.conceptId());
    addString(
        root,
        "sourceCommandRegistrationAnalysisMilestone",
        analysis.sourceCommandRegistrationAnalysisMilestone());
    addString(root, "commandBoundaryId", analysis.commandBoundaryId());
    root.addProperty("analysisOnly", analysis.analysisOnly());
    root.addProperty("classLoadingOccurred", analysis.classLoadingOccurred());
    root.addProperty("injectionOccurred", analysis.injectionOccurred());
    root.addProperty("transformationOccurred", analysis.transformationOccurred());
    root.addProperty("patchingOccurred", analysis.patchingOccurred());
    root.addProperty("hookInstallationOccurred", analysis.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", analysis.runtimeDispatchOccurred());
    root.addProperty("commandRegistrationOccurred", analysis.commandRegistrationOccurred());
    root.addProperty("commandExecutionOccurred", analysis.commandExecutionOccurred());
    root.addProperty("commandDispatcherAccessOccurred", analysis.commandDispatcherAccessOccurred());
    root.addProperty("publicApiExposed", analysis.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", analysis.javaModExecutionSandboxed());
    root.addProperty(
        "sourceCommandRegistrationGatePassed", analysis.sourceCommandRegistrationGatePassed());
    root.addProperty("lifecycleAnchorAvailable", analysis.lifecycleAnchorAvailable());
    root.addProperty("gatePassed", analysis.gatePassed());
    addString(root, "gateFailureReason", analysis.gateFailureReason());
    addString(root, "commandDispatcherDescriptor", analysis.commandDispatcherDescriptor());
    root.addProperty("candidateCount", analysis.candidateCount());
    root.addProperty("methodCandidateCount", analysis.methodCandidateCount());
    root.addProperty("fieldCandidateCount", analysis.fieldCandidateCount());
    root.addProperty("libraryClassCandidateCount", analysis.libraryClassCandidateCount());
    root.addProperty("selectableCandidateCount", analysis.selectableCandidateCount());
    root.addProperty("selectedCandidateCount", analysis.selectedCandidateCount());
    root.addProperty("selectionStatus", analysis.selectionStatus().name());
    root.addProperty(
        "minimalCommandRegistrationProofEligible",
        analysis.minimalCommandRegistrationProofEligible());
    addString(root, "selectedCandidateId", analysis.selectedCandidateId());
    root.add("candidates", candidates(analysis.candidates()));
    return root;
  }

  private JsonArray candidates(List<MinecraftCommandDispatcherSymbolCandidate> candidates) {
    JsonArray array = new JsonArray();
    for (MinecraftCommandDispatcherSymbolCandidate candidate : candidates) {
      JsonObject object = new JsonObject();
      addString(object, "id", candidate.id());
      object.addProperty("kind", candidate.kind().name());
      addString(object, "ownerInternalName", candidate.ownerInternalName());
      addString(object, "memberName", candidate.memberName());
      addString(object, "descriptor", candidate.descriptor());
      object.addProperty("staticMember", candidate.staticMember());
      JsonArray accessFlags = new JsonArray();
      for (String accessFlag : candidate.accessFlags()) {
        accessFlags.add(accessFlag);
      }
      object.add("accessFlags", accessFlags);
      object.addProperty("selectable", candidate.selectable());
      object.addProperty("selected", candidate.selected());
      addString(object, "rejectionReason", candidate.rejectionReason());
      addString(object, "notes", candidate.notes());
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
