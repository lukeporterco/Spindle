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

public final class MinecraftRegistryArcHardeningAnalysisWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftRegistryArcHardeningAnalysis analysis)
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
          "Failed to write Minecraft registry arc hardening analysis " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftRegistryArcHardeningAnalysis analysis) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", analysis.schema());
    addString(root, "milestoneName", analysis.milestoneName());
    addString(root, "target", analysis.target());
    addString(root, "minecraftVersion", analysis.minecraftVersion());
    addString(root, "side", analysis.side().id());
    addString(root, "conceptId", analysis.conceptId());
    addString(
        root,
        "sourceResourceReloadArcDecisionMilestone",
        analysis.sourceResourceReloadArcDecisionMilestone());
    addString(
        root,
        "sourceRegistryBootstrapAnalysisMilestone",
        analysis.sourceRegistryBootstrapAnalysisMilestone());
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
        analysis.sourceResourceReloadArcDecisionNextDirection().name());
    root.addProperty(
        "sourceRegistryBootstrapGatePassed", analysis.sourceRegistryBootstrapGatePassed());
    addString(
        root, "sourceRegistryDiscoveryStatus", analysis.sourceRegistryDiscoveryStatus().name());
    addString(root, "sourceRegistryBindingStatus", analysis.sourceRegistryBindingStatus().name());
    root.addProperty("sourceRegistryCandidateCount", analysis.sourceRegistryCandidateCount());
    root.addProperty(
        "sourceRegistrySelectableCandidateCount",
        analysis.sourceRegistrySelectableCandidateCount());
    root.addProperty(
        "sourceRegistryRejectedCandidateCount", analysis.sourceRegistryRejectedCandidateCount());
    root.addProperty(
        "sourceRegistryFutureSteelHookPrimitiveRequiredCount",
        analysis.sourceRegistryFutureSteelHookPrimitiveRequiredCount());
    root.addProperty("gatePassed", analysis.gatePassed());
    addString(root, "gateFailureReason", analysis.gateFailureReason());
    addString(root, "hardeningStatus", analysis.hardeningStatus().name());
    addString(root, "nextDirection", analysis.nextDirection().name());
    root.addProperty("registryArcCompleteForNow", analysis.registryArcCompleteForNow());
    root.addProperty("registryImplementationReady", analysis.registryImplementationReady());
    root.addProperty("registryProofRecommended", analysis.registryProofRecommended());
    root.addProperty(
        "currentSteelHookMethodEntryCompatible", analysis.currentSteelHookMethodEntryCompatible());
    root.addProperty(
        "steelHook02PrimitiveDesignRecommended", analysis.steelHook02PrimitiveDesignRecommended());
    root.addProperty(
        "continueRegistryAnalysisRecommended", analysis.continueRegistryAnalysisRecommended());
    root.addProperty("blockingFindingCount", analysis.blockingFindingCount());
    root.addProperty("warningFindingCount", analysis.warningFindingCount());
    root.addProperty("passingFindingCount", analysis.passingFindingCount());
    addString(root, "nextRecommendedAction", analysis.nextRecommendedAction());
    root.add("findings", findings(analysis.findings()));
    return root;
  }

  private JsonArray findings(List<MinecraftRegistryArcHardeningFinding> findings) {
    JsonArray array = new JsonArray();
    for (MinecraftRegistryArcHardeningFinding finding : findings) {
      JsonObject object = new JsonObject();
      addString(object, "id", finding.id());
      addString(object, "sourceMilestoneName", finding.sourceMilestoneName());
      addString(object, "checkName", finding.checkName());
      addString(object, "status", finding.status().name());
      object.addProperty("blocking", finding.blocking());
      addString(object, "summary", finding.summary());
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
