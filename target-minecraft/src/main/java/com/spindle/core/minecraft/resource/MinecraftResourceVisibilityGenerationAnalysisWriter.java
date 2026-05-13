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

public final class MinecraftResourceVisibilityGenerationAnalysisWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftResourceVisibilityGenerationAnalysis analysis)
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
          "Failed to write Minecraft resource visibility generation analysis " + outputPath,
          exception);
    }
  }

  JsonObject toJson(MinecraftResourceVisibilityGenerationAnalysis analysis) {
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
    addString(
        root,
        "sourceResourceReloadBindingAnalysisMilestone",
        analysis.sourceResourceReloadBindingAnalysisMilestone());
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
    root.addProperty("generatedFileWriteOccurred", analysis.generatedFileWriteOccurred());
    root.addProperty("registryMutationOccurred", analysis.registryMutationOccurred());
    root.addProperty("publicApiExposed", analysis.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", analysis.javaModExecutionSandboxed());
    root.addProperty("sourceResourceReloadGatePassed", analysis.sourceResourceReloadGatePassed());
    root.addProperty("sourceBindingGatePassed", analysis.sourceBindingGatePassed());
    addString(root, "sourceBindingStatus", analysis.sourceBindingStatus());
    root.addProperty("sourceReloadProofRecommended", analysis.sourceReloadProofRecommended());
    root.addProperty(
        "sourceCurrentSteelHookMethodEntryCompatible",
        analysis.sourceCurrentSteelHookMethodEntryCompatible());
    root.addProperty("gatePassed", analysis.gatePassed());
    addString(root, "gateFailureReason", analysis.gateFailureReason());
    root.addProperty("separationStatus", analysis.separationStatus().name());
    root.addProperty("runtimeFacingSurfaceCount", analysis.runtimeFacingSurfaceCount());
    root.addProperty("offlineGenerationSurfaceCount", analysis.offlineGenerationSurfaceCount());
    root.addProperty("runtimeReloadTimingSurfaceCount", analysis.runtimeReloadTimingSurfaceCount());
    root.addProperty(
        "runtimeResourceVisibilitySurfaceCount", analysis.runtimeResourceVisibilitySurfaceCount());
    root.addProperty(
        "offlineDataGenerationSurfaceCount", analysis.offlineDataGenerationSurfaceCount());
    root.addProperty("implementationReadySurfaceCount", analysis.implementationReadySurfaceCount());
    root.addProperty(
        "futureSteelHookPrimitiveRequiredSurfaceCount",
        analysis.futureSteelHookPrimitiveRequiredSurfaceCount());
    root.addProperty(
        "runtimeVisibilitySeparatedFromOfflineGeneration",
        analysis.runtimeVisibilitySeparatedFromOfflineGeneration());
    root.addProperty(
        "dataGenerationRequiresOfflineDesign", analysis.dataGenerationRequiresOfflineDesign());
    root.addProperty(
        "runtimeReloadRequiresFutureBindingDecision",
        analysis.runtimeReloadRequiresFutureBindingDecision());
    root.addProperty("reloadProofRecommended", analysis.reloadProofRecommended());
    root.addProperty(
        "currentSteelHookMethodEntryCompatible", analysis.currentSteelHookMethodEntryCompatible());
    addString(root, "nextRecommendedAction", analysis.nextRecommendedAction());
    root.add("surfaces", surfaces(analysis.surfaces()));
    return root;
  }

  private JsonArray surfaces(List<MinecraftResourceVisibilityGenerationSurface> surfaces) {
    JsonArray array = new JsonArray();
    for (MinecraftResourceVisibilityGenerationSurface surface : surfaces) {
      JsonObject object = new JsonObject();
      addString(object, "boundaryId", surface.boundaryId());
      addString(object, "displayName", surface.displayName());
      object.addProperty("order", surface.order());
      object.addProperty("lane", surface.lane().name());
      object.addProperty("runtimeFacing", surface.runtimeFacing());
      object.addProperty("offlineGenerationFacing", surface.offlineGenerationFacing());
      object.addProperty("availableInTarget19", surface.availableInTarget19());
      object.addProperty("requiresRuntimeReloadTiming", surface.requiresRuntimeReloadTiming());
      object.addProperty(
          "requiresRuntimeResourceVisibilityDesign",
          surface.requiresRuntimeResourceVisibilityDesign());
      object.addProperty(
          "requiresOfflineGenerationDesign", surface.requiresOfflineGenerationDesign());
      object.addProperty("requiresBindingRequirements", surface.requiresBindingRequirements());
      object.addProperty(
          "requiresFutureSteelHookPrimitive", surface.requiresFutureSteelHookPrimitive());
      object.addProperty("implementationReady", surface.implementationReady());
      object.addProperty("sourceBoundaryStatus", surface.sourceBoundaryStatus().name());
      addString(object, "notes", surface.notes());
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
