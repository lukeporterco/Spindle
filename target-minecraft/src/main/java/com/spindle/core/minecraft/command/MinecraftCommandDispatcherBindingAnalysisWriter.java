package com.spindle.core.minecraft.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftCommandDispatcherBindingAnalysisWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftCommandDispatcherBindingAnalysis analysis)
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
          "Failed to write Minecraft command dispatcher binding analysis " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftCommandDispatcherBindingAnalysis analysis) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", analysis.schema());
    addString(root, "milestoneName", analysis.milestoneName());
    addString(root, "target", analysis.target());
    addString(root, "minecraftVersion", analysis.minecraftVersion());
    addString(root, "side", analysis.side().id());
    addString(root, "conceptId", analysis.conceptId());
    addString(
        root,
        "sourceCommandDispatcherSymbolAnalysisMilestone",
        analysis.sourceCommandDispatcherSymbolAnalysisMilestone());
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
    root.addProperty("sourceSymbolGatePassed", analysis.sourceSymbolGatePassed());
    addString(root, "sourceSelectionStatus", analysis.sourceSelectionStatus());
    root.addProperty(
        "sourceMinimalCommandRegistrationProofEligible",
        analysis.sourceMinimalCommandRegistrationProofEligible());
    root.addProperty("gatePassed", analysis.gatePassed());
    addString(root, "gateFailureReason", analysis.gateFailureReason());
    root.addProperty("bindingStatus", analysis.bindingStatus().name());
    root.addProperty("accessStrategy", analysis.accessStrategy().name());
    addString(root, "selectedCandidateId", analysis.selectedCandidateId());
    addString(root, "selectedCandidateKind", analysis.selectedCandidateKind());
    addString(root, "ownerInternalName", analysis.ownerInternalName());
    addString(root, "memberName", analysis.memberName());
    addString(root, "descriptor", analysis.descriptor());
    root.addProperty("staticMember", analysis.staticMember());
    root.addProperty("requiresDispatcherValueCapture", analysis.requiresDispatcherValueCapture());
    root.addProperty("requiresOwnerInstanceCapture", analysis.requiresOwnerInstanceCapture());
    root.addProperty("requiresFieldAccess", analysis.requiresFieldAccess());
    root.addProperty(
        "requiresFutureSteelHookPrimitive", analysis.requiresFutureSteelHookPrimitive());
    root.addProperty(
        "currentSteelHookMethodEntryCompatible", analysis.currentSteelHookMethodEntryCompatible());
    root.addProperty(
        "minimalCommandRegistrationProofRecommended",
        analysis.minimalCommandRegistrationProofRecommended());
    addString(root, "nextRecommendedAction", analysis.nextRecommendedAction());
    addString(root, "notes", analysis.notes());
    return root;
  }

  private void addString(JsonObject object, String name, String value) {
    if (value == null) {
      object.add(name, JsonNull.INSTANCE);
    } else {
      object.addProperty(name, value);
    }
  }
}
