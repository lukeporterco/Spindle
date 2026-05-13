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

public final class MinecraftResourceReloadAnalysisWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftResourceReloadAnalysis analysis)
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
          "Failed to write Minecraft resource reload analysis " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftResourceReloadAnalysis analysis) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", analysis.schema());
    addString(root, "milestoneName", analysis.milestoneName());
    addString(root, "target", analysis.target());
    addString(root, "minecraftVersion", analysis.minecraftVersion());
    addString(root, "side", analysis.side().id());
    addString(root, "conceptId", analysis.conceptId());
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
    addString(
        root,
        "sourceServerLifecycleDispatchPlanMilestone",
        analysis.sourceServerLifecycleDispatchPlanMilestone());
    root.addProperty("sourceLifecycleGatePassed", analysis.sourceLifecycleGatePassed());
    root.addProperty(
        "sourceLifecycleStartingDispatchAvailable",
        analysis.sourceLifecycleStartingDispatchAvailable());
    root.addProperty("gatePassed", analysis.gatePassed());
    addString(root, "gateFailureReason", analysis.gateFailureReason());
    root.addProperty("availableBoundaryCount", analysis.availableBoundaryCount());
    root.addProperty("declaredUnboundBoundaryCount", analysis.declaredUnboundBoundaryCount());
    root.addProperty("upstreamBlockedBoundaryCount", analysis.upstreamBlockedBoundaryCount());
    root.add("boundaries", boundaries(analysis.boundaries()));
    return root;
  }

  private JsonArray boundaries(List<MinecraftAnalyzedResourceReloadBoundary> boundaries) {
    JsonArray array = new JsonArray();
    for (MinecraftAnalyzedResourceReloadBoundary boundary : boundaries) {
      JsonObject object = new JsonObject();
      addString(object, "boundaryId", boundary.boundaryId());
      addString(object, "displayName", boundary.displayName());
      object.addProperty("order", boundary.order());
      object.addProperty("status", boundary.status().name());
      object.addProperty("representationKind", boundary.representationKind().name());
      object.addProperty("available", boundary.available());
      addString(object, "sourceLifecyclePhaseId", boundary.sourceLifecyclePhaseId());
      addString(object, "sourceLifecycleDispatchId", boundary.sourceLifecycleDispatchId());
      object.addProperty("requiresSymbolDiscovery", boundary.requiresSymbolDiscovery());
      object.addProperty(
          "requiresBindingStrategyAnalysis", boundary.requiresBindingStrategyAnalysis());
      object.addProperty("requiresRuntimeResourceAccess", boundary.requiresRuntimeResourceAccess());
      object.addProperty(
          "requiresOfflineGenerationDesign", boundary.requiresOfflineGenerationDesign());
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
