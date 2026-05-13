package com.spindle.core.minecraft.lifecycle;

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

public final class MinecraftServerLifecycleBindingReportWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftServerLifecycleBindingReport report)
      throws LoaderException {
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(toJson(report), writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write Minecraft server lifecycle binding report " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftServerLifecycleBindingReport report) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", report.schema());
    addString(root, "milestoneName", report.milestoneName());
    addString(root, "target", report.target());
    addString(root, "minecraftVersion", report.minecraftVersion());
    addString(root, "side", report.side());
    addString(root, "conceptId", report.conceptId());
    root.addProperty("conceptOrder", report.conceptOrder());
    addString(root, "conceptDisplayName", report.conceptDisplayName());
    root.addProperty("analysisOnly", report.analysisOnly());
    root.addProperty("classLoadingOccurred", report.classLoadingOccurred());
    root.addProperty("injectionOccurred", report.injectionOccurred());
    root.addProperty("transformationOccurred", report.transformationOccurred());
    root.addProperty("patchingOccurred", report.patchingOccurred());
    root.addProperty("hookInstallationOccurred", report.hookInstallationOccurred());
    root.addProperty("publicApiExposed", report.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", report.javaModExecutionSandboxed());
    addString(root, "contractCatalogId", report.contractCatalogId());
    root.addProperty("sourceContractValidationPassed", report.sourceContractValidationPassed());
    root.addProperty("gatePassed", report.gatePassed());
    addString(root, "gateFailureReason", report.gateFailureReason());
    root.addProperty("lifecyclePhaseCount", report.lifecyclePhaseCount());
    root.addProperty("boundPhaseCount", report.boundPhaseCount());
    root.addProperty("unboundPhaseCount", report.unboundPhaseCount());
    root.addProperty("bindingCount", report.bindingCount());
    root.add("bindings", bindings(report.bindings()));
    return root;
  }

  private JsonArray bindings(java.util.List<MinecraftServerLifecycleBinding> bindings) {
    JsonArray array = new JsonArray();
    for (MinecraftServerLifecycleBinding binding : bindings) {
      JsonObject object = new JsonObject();
      addString(object, "id", binding.id());
      addString(object, "phaseId", binding.phaseId());
      addString(object, "displayName", binding.displayName());
      object.addProperty("status", binding.status().name());
      object.addProperty("supportedInThisPass", binding.supportedInThisPass());
      addString(object, "boundContractId", binding.boundContractId());
      addString(object, "ownerInternalName", binding.ownerInternalName());
      addString(object, "memberName", binding.memberName());
      addString(object, "descriptor", binding.descriptor());
      addString(object, "bindingKind", binding.bindingKind());
      addString(object, "notes", binding.notes());
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
