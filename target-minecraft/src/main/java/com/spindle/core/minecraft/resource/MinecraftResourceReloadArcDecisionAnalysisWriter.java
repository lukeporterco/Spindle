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

public final class MinecraftResourceReloadArcDecisionAnalysisWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftResourceReloadArcDecisionAnalysis analysis)
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
          "Failed to write Minecraft resource/reload arc decision analysis " + outputPath,
          exception);
    }
  }

  JsonObject toJson(MinecraftResourceReloadArcDecisionAnalysis analysis) {
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
        "sourceResourceReloadSymbolAnalysisMilestone",
        analysis.sourceResourceReloadSymbolAnalysisMilestone());
    addString(
        root,
        "sourceResourceReloadBindingAnalysisMilestone",
        analysis.sourceResourceReloadBindingAnalysisMilestone());
    addString(
        root,
        "sourceResourceVisibilityGenerationAnalysisMilestone",
        analysis.sourceResourceVisibilityGenerationAnalysisMilestone());
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
    root.addProperty("registryBootstrapOccurred", analysis.registryBootstrapOccurred());
    root.addProperty("registryMutationOccurred", analysis.registryMutationOccurred());
    root.addProperty("publicApiExposed", analysis.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", analysis.javaModExecutionSandboxed());
    root.addProperty("sourceResourceReloadGatePassed", analysis.sourceResourceReloadGatePassed());
    root.addProperty("sourceSymbolGatePassed", analysis.sourceSymbolGatePassed());
    addString(root, "sourceSymbolDiscoveryStatus", analysis.sourceSymbolDiscoveryStatus());
    root.addProperty("sourceBindingGatePassed", analysis.sourceBindingGatePassed());
    addString(root, "sourceBindingStatus", analysis.sourceBindingStatus());
    root.addProperty(
        "sourceBindingReloadProofRecommended", analysis.sourceBindingReloadProofRecommended());
    root.addProperty(
        "sourceBindingCurrentSteelHookMethodEntryCompatible",
        analysis.sourceBindingCurrentSteelHookMethodEntryCompatible());
    root.addProperty(
        "sourceVisibilityGenerationGatePassed", analysis.sourceVisibilityGenerationGatePassed());
    addString(
        root, "sourceVisibilityGenerationStatus", analysis.sourceVisibilityGenerationStatus());
    root.addProperty(
        "sourceRuntimeVisibilitySeparatedFromOfflineGeneration",
        analysis.sourceRuntimeVisibilitySeparatedFromOfflineGeneration());
    root.addProperty(
        "sourceDataGenerationRequiresOfflineDesign",
        analysis.sourceDataGenerationRequiresOfflineDesign());
    root.addProperty(
        "sourceRuntimeReloadRequiresFutureBindingDecision",
        analysis.sourceRuntimeReloadRequiresFutureBindingDecision());
    root.addProperty("gatePassed", analysis.gatePassed());
    addString(root, "gateFailureReason", analysis.gateFailureReason());
    root.addProperty("decisionStatus", analysis.decisionStatus().name());
    root.addProperty("nextDirection", analysis.nextDirection().name());
    root.addProperty("resourceReloadArcCompleteForNow", analysis.resourceReloadArcCompleteForNow());
    root.addProperty(
        "resourceReloadImplementationReady", analysis.resourceReloadImplementationReady());
    root.addProperty("resourceReloadProofRecommended", analysis.resourceReloadProofRecommended());
    root.addProperty(
        "currentSteelHookMethodEntryCompatible", analysis.currentSteelHookMethodEntryCompatible());
    root.addProperty(
        "steelHookPrimitiveDesignRecommendedNow",
        analysis.steelHookPrimitiveDesignRecommendedNow());
    root.addProperty(
        "continueResourceReloadAnalysisRecommended",
        analysis.continueResourceReloadAnalysisRecommended());
    root.addProperty("registryBootstrapRecommended", analysis.registryBootstrapRecommended());
    addString(root, "recommendedNextConceptId", analysis.recommendedNextConceptId());
    addString(root, "recommendedNextMilestoneName", analysis.recommendedNextMilestoneName());
    addString(root, "recommendedNextPassTitle", analysis.recommendedNextPassTitle());
    addString(root, "nextRecommendedAction", analysis.nextRecommendedAction());
    root.add("findings", findings(analysis.findings()));
    return root;
  }

  private JsonArray findings(List<MinecraftResourceReloadArcDecisionFinding> findings) {
    JsonArray array = new JsonArray();
    for (MinecraftResourceReloadArcDecisionFinding finding : findings) {
      JsonObject object = new JsonObject();
      addString(object, "id", finding.id());
      addString(object, "sourceMilestoneName", finding.sourceMilestoneName());
      addString(object, "summary", finding.summary());
      object.addProperty("implementationReady", finding.implementationReady());
      object.addProperty(
          "recommendedForImmediateImplementation", finding.recommendedForImmediateImplementation());
      object.addProperty("requiresFutureWork", finding.requiresFutureWork());
      addString(object, "notes", finding.notes());
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
