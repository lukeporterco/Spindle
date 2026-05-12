package com.spindle.core.minecraft.hook.install;

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

public final class MinecraftHookInstallationPlanWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftHookInstallationPlan plan) throws LoaderException {
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(toJson(plan), writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write Minecraft hook installation plan " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftHookInstallationPlan plan) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", plan.schema());
    addString(root, "milestoneName", plan.milestoneName());
    addString(root, "target", plan.target());
    addString(root, "minecraftVersion", plan.minecraftVersion());
    addString(root, "side", plan.side());
    addString(root, "catalogId", plan.catalogId());
    root.addProperty("sourceContractValidationPassed", plan.sourceContractValidationPassed());
    root.addProperty("sourceContractErrorCount", plan.sourceContractErrorCount());
    addString(root, "minecraftMainClass", plan.minecraftMainClass());
    root.addProperty("gatePassed", plan.gatePassed());
    addString(root, "gateFailureReason", plan.gateFailureReason());
    root.addProperty("installationPlanned", plan.installationPlanned());
    addString(
        root,
        "installationMode",
        plan.installationMode() == null ? null : plan.installationMode().id());
    root.addProperty("plannedHookCount", plan.plannedHookCount());
    root.add("plannedHooks", plannedHooks(plan.plannedHooks()));
    root.addProperty("injectionOccurred", plan.injectionOccurred());
    root.addProperty("transformationOccurred", plan.transformationOccurred());
    root.addProperty("patchingOccurred", plan.patchingOccurred());
    root.addProperty("bytecodeModified", plan.bytecodeModified());
    root.addProperty("javaAgentUsed", plan.javaAgentUsed());
    root.addProperty("mixinUsed", plan.mixinUsed());
    root.addProperty("remappingOccurred", plan.remappingOccurred());
    root.addProperty("publicApiExposed", plan.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", plan.javaModExecutionSandboxed());
    return root;
  }

  private JsonArray plannedHooks(java.util.List<MinecraftPlannedHookInstallation> plannedHooks) {
    JsonArray array = new JsonArray();
    for (MinecraftPlannedHookInstallation plannedHook : plannedHooks) {
      JsonObject object = new JsonObject();
      addString(object, "id", plannedHook.id());
      addString(object, "sourceContractId", plannedHook.sourceContractId());
      addString(object, "catalogId", plannedHook.catalogId());
      addString(object, "kind", plannedHook.kind());
      addString(object, "ownerInternalName", plannedHook.ownerInternalName());
      addString(object, "memberName", plannedHook.memberName());
      addString(object, "descriptor", plannedHook.descriptor());
      object.addProperty("required", plannedHook.required());
      addString(object, "mode", plannedHook.mode() == null ? null : plannedHook.mode().id());
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
