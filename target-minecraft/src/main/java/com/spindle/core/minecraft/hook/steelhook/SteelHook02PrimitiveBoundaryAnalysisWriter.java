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

public final class SteelHook02PrimitiveBoundaryAnalysisWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, SteelHook02PrimitiveBoundaryAnalysis analysis)
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
          "Failed to write SteelHook 0.2 primitive boundary analysis " + outputPath, exception);
    }
  }

  JsonObject toJson(SteelHook02PrimitiveBoundaryAnalysis analysis) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", analysis.schema());
    addString(root, "milestoneName", analysis.milestoneName());
    addString(root, "target", analysis.target());
    addString(root, "steelHookVersion", analysis.steelHookVersion());
    addString(root, "minecraftVersion", analysis.minecraftVersion());
    addString(root, "side", analysis.side() == null ? null : analysis.side().id());
    addString(root, "sourcePatchPlanMilestone", analysis.sourcePatchPlanMilestone());
    addString(
        root, "sourceSteelHookCompletionMilestone", analysis.sourceSteelHookCompletionMilestone());
    addString(
        root, "sourceRegistryHardeningMilestone", analysis.sourceRegistryHardeningMilestone());
    root.addProperty("analysisOnly", analysis.analysisOnly());
    root.addProperty("classLoadingOccurred", analysis.classLoadingOccurred());
    root.addProperty("injectionOccurred", analysis.injectionOccurred());
    root.addProperty("transformationOccurred", analysis.transformationOccurred());
    root.addProperty("patchingOccurred", analysis.patchingOccurred());
    root.addProperty("hookInstallationOccurred", analysis.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", analysis.runtimeDispatchOccurred());
    root.addProperty("publicApiExposed", analysis.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", analysis.javaModExecutionSandboxed());
    root.addProperty("supportedPrimitiveCount", analysis.supportedPrimitiveCount());
    root.addProperty("approvedCandidateCount", analysis.approvedCandidateCount());
    root.addProperty("deferredCandidateCount", analysis.deferredCandidateCount());
    root.addProperty("rejectedCandidateCount", analysis.rejectedCandidateCount());
    root.addProperty("gatePassed", analysis.gatePassed());
    addString(root, "gateFailureReason", analysis.gateFailureReason());
    addString(root, "boundaryStatus", analysis.boundaryStatus().name());
    addString(root, "nextDirection", analysis.nextDirection().name());
    addString(root, "nextRecommendedAction", analysis.nextRecommendedAction());
    root.add("candidates", candidates(analysis.candidates()));
    root.add("findings", findings(analysis.findings()));
    return root;
  }

  private JsonArray candidates(List<SteelHook02PrimitiveCandidate> candidates) {
    JsonArray array = new JsonArray();
    for (SteelHook02PrimitiveCandidate candidate : candidates) {
      JsonObject object = new JsonObject();
      addString(object, "id", candidate.id());
      addString(object, "primitiveKind", candidate.primitiveKind().name());
      addString(object, "candidateStatus", candidate.candidateStatus().name());
      addString(object, "sourcePatchId", candidate.sourcePatchId());
      addString(object, "ownerInternalName", candidate.ownerInternalName());
      addString(object, "memberName", candidate.memberName());
      addString(object, "descriptor", candidate.descriptor());
      object.addProperty("insertionOffset", candidate.insertionOffset());
      addString(object, "dispatcherOwnerInternalName", candidate.dispatcherOwnerInternalName());
      addString(object, "dispatcherMethodName", candidate.dispatcherMethodName());
      addString(object, "dispatcherDescriptor", candidate.dispatcherDescriptor());
      object.addProperty("fixtureTransformReady", candidate.fixtureTransformReady());
      object.addProperty(
          "minecraftRuntimeTransformReady", candidate.minecraftRuntimeTransformReady());
      object.addProperty(
          "eligibleForTarget24ContractGeneralization",
          candidate.eligibleForTarget24ContractGeneralization());
      object.addProperty(
          "eligibleForTarget25TransformerExtraction",
          candidate.eligibleForTarget25TransformerExtraction());
      object.addProperty(
          "eligibleForTarget26RuntimeTransformation",
          candidate.eligibleForTarget26RuntimeTransformation());
      object.add("notes", notes(candidate.notes()));
      array.add(object);
    }
    return array;
  }

  private JsonArray findings(List<SteelHook02PrimitiveFinding> findings) {
    JsonArray array = new JsonArray();
    for (SteelHook02PrimitiveFinding finding : findings) {
      JsonObject object = new JsonObject();
      addString(object, "id", finding.id());
      addString(object, "checkName", finding.checkName());
      addString(object, "status", finding.status().name());
      object.addProperty("blocking", finding.blocking());
      addString(object, "summary", finding.summary());
      addString(object, "notes", finding.notes());
      array.add(object);
    }
    return array;
  }

  private JsonArray notes(List<String> notes) {
    JsonArray array = new JsonArray();
    for (String note : notes) {
      if (note == null) {
        array.add(JsonNull.INSTANCE);
      } else {
        array.add(note);
      }
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
