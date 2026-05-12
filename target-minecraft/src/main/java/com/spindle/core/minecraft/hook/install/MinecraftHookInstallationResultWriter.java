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

public final class MinecraftHookInstallationResultWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftHookInstallationResult result)
      throws LoaderException {
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(toJson(result), writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write Minecraft hook installation result " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftHookInstallationResult result) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", result.schema());
    addString(root, "milestoneName", result.milestoneName());
    addString(root, "target", result.target());
    addString(root, "minecraftVersion", result.minecraftVersion());
    addString(root, "side", result.side());
    addString(root, "minecraftMainClass", result.minecraftMainClass());
    addString(
        root,
        "installationMode",
        result.installationMode() == null ? null : result.installationMode().id());
    root.addProperty("hookInstallationOccurred", result.hookInstallationOccurred());
    root.addProperty("hookInvocationOccurred", result.hookInvocationOccurred());
    root.addProperty("minecraftMainClassLoaded", result.minecraftMainClassLoaded());
    root.addProperty("minecraftMainInvoked", result.minecraftMainInvoked());
    root.addProperty("installedHookCount", result.installedHookCount());
    root.addProperty("invokedHookCount", result.invokedHookCount());
    root.addProperty("failedHookCount", result.failedHookCount());
    addString(root, "status", result.status() == null ? null : result.status().name());
    root.addProperty("injectionOccurred", result.injectionOccurred());
    root.addProperty("transformationOccurred", result.transformationOccurred());
    root.addProperty("patchingOccurred", result.patchingOccurred());
    root.addProperty("bytecodeModified", result.bytecodeModified());
    root.addProperty("javaAgentUsed", result.javaAgentUsed());
    root.addProperty("mixinUsed", result.mixinUsed());
    root.addProperty("remappingOccurred", result.remappingOccurred());
    root.addProperty("publicApiExposed", result.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", result.javaModExecutionSandboxed());
    root.add("installedHooks", installedHooks(result.installedHooks()));
    addString(root, "failureCategory", result.failureCategory());
    addString(root, "failureMessage", result.failureMessage());
    root.add("failureDetails", strings(result.failureDetails()));
    return root;
  }

  private JsonArray installedHooks(
      java.util.List<MinecraftInstalledHookInvocation> installedHooks) {
    JsonArray array = new JsonArray();
    for (MinecraftInstalledHookInvocation hook : installedHooks) {
      JsonObject object = new JsonObject();
      addString(object, "id", hook.id());
      addString(object, "sourceContractId", hook.sourceContractId());
      addString(object, "ownerInternalName", hook.ownerInternalName());
      addString(object, "memberName", hook.memberName());
      addString(object, "descriptor", hook.descriptor());
      addString(object, "mode", hook.mode() == null ? null : hook.mode().id());
      object.addProperty("installed", hook.installed());
      object.addProperty("invoked", hook.invoked());
      addString(object, "failureMessage", hook.failureMessage());
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
