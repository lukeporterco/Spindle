package com.spindle.core.minecraft.hook;

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

public final class MinecraftHookContractReportWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftHookContractReport report) throws LoaderException {
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
          "Failed to write Minecraft hook contract report " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftHookContractReport report) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", report.schema());
    root.addProperty("milestoneName", report.milestoneName());
    root.addProperty("target", report.target());
    addString(root, "minecraftVersion", report.minecraftVersion());
    root.addProperty("side", report.side());
    addString(root, "catalogId", report.catalogId());
    addString(root, "catalogDescription", report.catalogDescription());
    addString(root, "catalogMinecraftVersion", report.catalogMinecraftVersion());
    addString(root, "catalogSide", report.catalogSide());
    root.addProperty("analysisOnly", report.analysisOnly());
    root.addProperty("classLoadingOccurred", report.classLoadingOccurred());
    root.addProperty("injectionOccurred", report.injectionOccurred());
    root.addProperty("transformationOccurred", report.transformationOccurred());
    root.addProperty("patchingOccurred", report.patchingOccurred());
    root.addProperty("hookInstallationOccurred", report.hookInstallationOccurred());
    root.addProperty("artifactInterpretationSchema", report.artifactInterpretationSchema());
    addString(root, "artifactInterpretationMilestone", report.artifactInterpretationMilestone());
    root.addProperty("contractCount", report.contractCount());
    root.addProperty("validContractCount", report.validContractCount());
    root.addProperty("invalidContractCount", report.invalidContractCount());
    root.addProperty("requiredContractCount", report.requiredContractCount());
    root.addProperty("optionalContractCount", report.optionalContractCount());
    root.addProperty("warningCount", report.warningCount());
    root.addProperty("errorCount", report.errorCount());
    root.addProperty("validationPassed", report.validationPassed());
    root.add("contracts", contracts(report.contracts()));
    root.add("diagnostics", diagnostics(report.diagnostics()));
    return root;
  }

  private JsonArray contracts(java.util.List<MinecraftHookContractResult> contracts) {
    JsonArray array = new JsonArray();
    for (MinecraftHookContractResult contract : contracts) {
      JsonObject object = new JsonObject();
      addString(object, "id", contract.id());
      addString(object, "description", contract.description());
      addString(object, "side", contract.side());
      addString(object, "kind", contract.kind());
      addString(object, "ownerInternalName", contract.ownerInternalName());
      addString(object, "memberName", contract.memberName());
      addString(object, "descriptor", contract.descriptor());
      addString(object, "requirement", contract.requirement());
      addString(object, "status", contract.status());
      object.addProperty("valid", contract.valid());
      object.addProperty("required", contract.required());
      object.addProperty("optional", contract.optional());
      object.add("diagnosticIds", strings(contract.diagnosticIds()));
      addString(object, "matchedClass", contract.matchedClass());
      addString(object, "matchedMember", contract.matchedMember());
      array.add(object);
    }
    return array;
  }

  private JsonArray diagnostics(java.util.List<MinecraftHookContractDiagnostic> diagnostics) {
    JsonArray array = new JsonArray();
    for (MinecraftHookContractDiagnostic diagnostic : diagnostics) {
      JsonObject object = new JsonObject();
      addString(object, "id", diagnostic.id());
      object.addProperty("severity", diagnostic.severity().name());
      addString(object, "status", diagnostic.status());
      addString(object, "contractId", diagnostic.contractId());
      addString(object, "code", diagnostic.code());
      addString(object, "message", diagnostic.message());
      addString(object, "ownerInternalName", diagnostic.ownerInternalName());
      addString(object, "memberName", diagnostic.memberName());
      addString(object, "descriptor", diagnostic.descriptor());
      array.add(object);
    }
    return array;
  }

  private JsonArray strings(java.util.List<String> values) {
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
