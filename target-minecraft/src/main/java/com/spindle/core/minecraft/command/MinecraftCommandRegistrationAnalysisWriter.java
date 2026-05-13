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

public final class MinecraftCommandRegistrationAnalysisWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftCommandRegistrationAnalysis analysis)
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
          "Failed to write Minecraft command registration analysis " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftCommandRegistrationAnalysis analysis) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", analysis.schema());
    addString(root, "milestoneName", analysis.milestoneName());
    addString(root, "target", analysis.target());
    addString(root, "minecraftVersion", analysis.minecraftVersion());
    addString(root, "side", analysis.side().id());
    addString(root, "conceptId", analysis.conceptId());
    root.addProperty("conceptOrder", analysis.conceptOrder());
    addString(root, "conceptDisplayName", analysis.conceptDisplayName());
    addString(root, "upstreamConceptId", analysis.upstreamConceptId());
    addString(
        root,
        "sourceLifecycleDispatchPlanMilestone",
        analysis.sourceLifecycleDispatchPlanMilestone());
    root.addProperty("analysisOnly", analysis.analysisOnly());
    root.addProperty("classLoadingOccurred", analysis.classLoadingOccurred());
    root.addProperty("injectionOccurred", analysis.injectionOccurred());
    root.addProperty("transformationOccurred", analysis.transformationOccurred());
    root.addProperty("patchingOccurred", analysis.patchingOccurred());
    root.addProperty("hookInstallationOccurred", analysis.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", analysis.runtimeDispatchOccurred());
    root.addProperty("commandRegistrationOccurred", analysis.commandRegistrationOccurred());
    root.addProperty("commandExecutionOccurred", analysis.commandExecutionOccurred());
    root.addProperty("publicApiExposed", analysis.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", analysis.javaModExecutionSandboxed());
    root.addProperty(
        "sourceLifecycleDispatchGatePassed", analysis.sourceLifecycleDispatchGatePassed());
    root.addProperty("gatePassed", analysis.gatePassed());
    addString(root, "gateFailureReason", analysis.gateFailureReason());
    root.addProperty("boundaryCount", analysis.boundaryCount());
    root.addProperty("anchoredBoundaryCount", analysis.anchoredBoundaryCount());
    root.addProperty("unboundBoundaryCount", analysis.unboundBoundaryCount());
    root.addProperty("blockedBoundaryCount", analysis.blockedBoundaryCount());
    root.addProperty("boundMinecraftSymbolCount", analysis.boundMinecraftSymbolCount());
    root.addProperty("implementedBoundaryCount", analysis.implementedBoundaryCount());
    root.add("boundaries", boundaries(analysis.boundaries()));
    return root;
  }

  private JsonArray boundaries(List<MinecraftAnalyzedCommandRegistrationBoundary> boundaries) {
    JsonArray array = new JsonArray();
    for (MinecraftAnalyzedCommandRegistrationBoundary boundary : boundaries) {
      JsonObject object = new JsonObject();
      addString(object, "id", boundary.id());
      addString(object, "boundaryId", boundary.boundaryId());
      addString(object, "displayName", boundary.displayName());
      object.addProperty("status", boundary.status().name());
      object.addProperty("representationKind", boundary.representationKind().name());
      addString(object, "upstreamConceptId", boundary.upstreamConceptId());
      addString(object, "upstreamDispatchId", boundary.upstreamDispatchId());
      addString(object, "sourceLifecyclePhaseId", boundary.sourceLifecyclePhaseId());
      object.addProperty("minecraftSymbolKnown", boundary.minecraftSymbolKnown());
      addString(object, "ownerInternalName", boundary.ownerInternalName());
      addString(object, "memberName", boundary.memberName());
      addString(object, "descriptor", boundary.descriptor());
      object.addProperty("requiresFutureMinecraftSymbol", boundary.requiresFutureMinecraftSymbol());
      object.addProperty(
          "requiresFutureSteelHookPrimitive", boundary.requiresFutureSteelHookPrimitive());
      object.addProperty("implementedInThisPass", boundary.implementedInThisPass());
      object.addProperty("analysisOnly", boundary.analysisOnly());
      addString(object, "notes", boundary.notes());
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
