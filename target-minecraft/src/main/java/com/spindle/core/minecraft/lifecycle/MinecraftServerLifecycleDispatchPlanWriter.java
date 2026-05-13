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

public final class MinecraftServerLifecycleDispatchPlanWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftServerLifecycleDispatchPlan plan)
      throws LoaderException {
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
          "Failed to write Minecraft server lifecycle dispatch plan " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftServerLifecycleDispatchPlan plan) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", plan.schema());
    addString(root, "milestoneName", plan.milestoneName());
    addString(root, "target", plan.target());
    addString(root, "minecraftVersion", plan.minecraftVersion());
    addString(root, "side", plan.side().id());
    addString(root, "conceptId", plan.conceptId());
    addString(root, "sourceBindingReportMilestone", plan.sourceBindingReportMilestone());
    root.addProperty("analysisOnly", plan.analysisOnly());
    root.addProperty("classLoadingOccurred", plan.classLoadingOccurred());
    root.addProperty("injectionOccurred", plan.injectionOccurred());
    root.addProperty("transformationOccurred", plan.transformationOccurred());
    root.addProperty("patchingOccurred", plan.patchingOccurred());
    root.addProperty("hookInstallationOccurred", plan.hookInstallationOccurred());
    root.addProperty("runtimeDispatchOccurred", plan.runtimeDispatchOccurred());
    root.addProperty("publicApiExposed", plan.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", plan.javaModExecutionSandboxed());
    root.addProperty("sourceBindingGatePassed", plan.sourceBindingGatePassed());
    root.addProperty("gatePassed", plan.gatePassed());
    addString(root, "gateFailureReason", plan.gateFailureReason());
    root.addProperty("lifecyclePhaseCount", plan.lifecyclePhaseCount());
    root.addProperty("dispatchCount", plan.dispatchCount());
    root.addProperty("plannedDispatchCount", plan.plannedDispatchCount());
    root.addProperty("blockedDispatchCount", plan.blockedDispatchCount());
    root.addProperty("unsupportedDispatchCount", plan.unsupportedDispatchCount());
    root.add("dispatches", dispatches(plan.dispatches()));
    return root;
  }

  private JsonArray dispatches(java.util.List<MinecraftPlannedServerLifecycleDispatch> dispatches) {
    JsonArray array = new JsonArray();
    for (MinecraftPlannedServerLifecycleDispatch dispatch : dispatches) {
      JsonObject object = new JsonObject();
      addString(object, "id", dispatch.id());
      addString(object, "phaseId", dispatch.phaseId());
      addString(object, "displayName", dispatch.displayName());
      addString(object, "sourceBindingId", dispatch.sourceBindingId());
      addString(object, "sourceContractId", dispatch.sourceContractId());
      object.addProperty("status", dispatch.status().name());
      object.addProperty("mode", dispatch.mode().name());
      addString(object, "dispatchTiming", dispatch.dispatchTiming());
      addString(object, "dispatcherOwnerInternalName", dispatch.dispatcherOwnerInternalName());
      addString(object, "dispatcherMethodName", dispatch.dispatcherMethodName());
      addString(object, "dispatcherDescriptor", dispatch.dispatcherDescriptor());
      object.addProperty("cancellable", dispatch.cancellable());
      object.addProperty("allowsResultReplacement", dispatch.allowsResultReplacement());
      object.addProperty("publicListenerRegistration", dispatch.publicListenerRegistration());
      object.addProperty("modCallbackExecution", dispatch.modCallbackExecution());
      object.addProperty("runtimeDispatcherImplemented", dispatch.runtimeDispatcherImplemented());
      object.addProperty("symbolicOnly", dispatch.symbolicOnly());
      addString(object, "notes", dispatch.notes());
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
