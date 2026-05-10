package com.spindle.core.minecraft;

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

public final class MinecraftServerRuntimePlanWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftServerRuntimePlan plan) throws LoaderException {
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
          "Failed to write Minecraft server runtime plan " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftServerRuntimePlan plan) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", plan.schema());
    root.addProperty("milestoneName", plan.milestoneName());
    root.addProperty("projectJavaBaseline", plan.projectJavaBaseline());
    root.addProperty("projectTargetMinecraft", plan.projectTargetMinecraft());
    root.addProperty("resolvedMinecraftVersion", plan.resolvedMinecraftVersion());
    addString(root, "selectorUsed", plan.selectorUsed());
    addString(root, "selectorResolutionReason", plan.selectorResolutionReason());
    addString(root, "manifestSource", plan.manifestSource());
    addString(root, "versionJsonSource", plan.versionJsonSource());
    addString(root, "serverJarPath", plan.serverJarPath());
    addString(root, "serverJarSource", plan.serverJarSource());
    addString(root, "serverJarSha1", plan.serverJarSha1());
    addString(root, "serverJarSha256", plan.serverJarSha256());
    if (plan.serverJarSize() == null) {
      root.add("serverJarSize", JsonNull.INSTANCE);
    } else {
      root.addProperty("serverJarSize", plan.serverJarSize());
    }
    root.addProperty("launchMode", plan.launchMode());
    root.addProperty("launchModeReason", plan.launchModeReason());
    addString(root, "mainClass", plan.mainClass());
    root.add("classpathEntries", classpathEntries(plan));
    root.add("bundledRuntimeExtractionEntries", runtimeFiles(plan));
    root.add("jvmArgs", strings(plan.jvmArgs()));
    root.add("serverArgs", strings(plan.serverArgs()));
    root.addProperty("workingDirectory", plan.workingDirectory());
    root.addProperty("javaExecutable", plan.javaExecutable());
    root.add("commandPreview", strings(plan.commandPreview()));
    root.addProperty("cacheDirectory", plan.cacheDirectory());
    root.addProperty("runtimeCacheDirectory", plan.runtimeCacheDirectory());
    root.addProperty("offline", plan.offline());
    root.addProperty("strict", plan.strict());
    root.addProperty("networkRequestCount", plan.networkRequestCount());
    root.addProperty("generatedFromCacheOnly", plan.generatedFromCacheOnly());
    root.addProperty("replayableOffline", plan.replayableOffline());
    root.addProperty(
        "modJarsOnMinecraftRuntimeClasspath", plan.modJarsOnMinecraftRuntimeClasspath());
    root.addProperty("injectionOccurred", plan.injectionOccurred());
    root.addProperty("minecraftModClassesLoaded", plan.minecraftModClassesLoaded());
    root.addProperty(
        "minecraftModClassLoaderAttachedToMinecraft",
        plan.minecraftModClassLoaderAttachedToMinecraft());
    root.addProperty("minecraftEntrypointsInvoked", plan.minecraftEntrypointsInvoked());
    root.addProperty("transformationsOccurred", plan.transformationsOccurred());
    root.addProperty("remappingOccurred", plan.remappingOccurred());
    root.addProperty("mixinOccurred", plan.mixinOccurred());
    root.addProperty("patchingOccurred", plan.patchingOccurred());
    root.add("provenance", provenance(plan.provenance()));
    return root;
  }

  private JsonArray classpathEntries(MinecraftServerRuntimePlan plan) {
    JsonArray array = new JsonArray();
    for (MinecraftServerRuntimeClasspath.Entry entry : plan.classpathEntries()) {
      JsonObject object = new JsonObject();
      object.addProperty("path", entry.path());
      object.addProperty("ownership", entry.ownership());
      object.addProperty("origin", entry.origin());
      addString(object, "sha256", entry.sha256());
      array.add(object);
    }
    return array;
  }

  private JsonArray runtimeFiles(MinecraftServerRuntimePlan plan) {
    JsonArray array = new JsonArray();
    for (MinecraftRuntimeFile file : plan.bundledRuntimeFiles()) {
      JsonObject object = new JsonObject();
      object.addProperty("id", file.id());
      object.addProperty("path", file.relativeCachePath());
      object.addProperty("origin", file.origin());
      addString(object, "sha1", file.sha1());
      addString(object, "sha256", file.sha256());
      object.addProperty("size", file.size());
      object.addProperty("present", file.present());
      object.addProperty("verified", file.verified());
      object.addProperty("verificationStatus", file.verificationStatus());
      array.add(object);
    }
    return array;
  }

  private JsonObject provenance(MinecraftRuntimeProvenance provenance) {
    JsonObject object = new JsonObject();
    object.addProperty("schema", provenance.schema());
    object.addProperty("milestoneName", provenance.milestoneName());
    object.add("inputs", strings(provenance.inputs()));
    object.addProperty("cacheSource", provenance.cacheSource());
    object.addProperty("networkSource", provenance.networkSource());
    object.addProperty("offlineReplay", provenance.offlineReplay());
    object.addProperty("strict", provenance.strict());
    object.addProperty("commandLineMode", provenance.commandLineMode());
    object.addProperty("workingDirectory", provenance.workingDirectory());
    object.addProperty("outputDirectory", provenance.outputDirectory());
    object.add("reportDependencies", strings(provenance.reportDependencies()));
    return object;
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
