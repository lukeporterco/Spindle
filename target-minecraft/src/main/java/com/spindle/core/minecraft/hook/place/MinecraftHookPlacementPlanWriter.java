package com.spindle.core.minecraft.hook.place;

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

public final class MinecraftHookPlacementPlanWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftHookPlacementPlan plan) throws LoaderException {
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
          "Failed to write Minecraft hook placement plan " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftHookPlacementPlan plan) {
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
    root.addProperty("placementPlanned", plan.placementPlanned());
    root.addProperty("plannedPlacementCount", plan.plannedPlacementCount());
    root.add("plannedPlacements", plannedPlacements(plan.plannedPlacements()));
    root.addProperty("codeInspectionOccurred", plan.codeInspectionOccurred());
    root.addProperty("codeAttributeParsed", plan.codeAttributeParsed());
    root.addProperty("injectionOccurred", plan.injectionOccurred());
    root.addProperty("transformationOccurred", plan.transformationOccurred());
    root.addProperty("patchingOccurred", plan.patchingOccurred());
    root.addProperty("bytecodeModified", plan.bytecodeModified());
    root.addProperty("javaAgentUsed", plan.javaAgentUsed());
    root.addProperty("mixinUsed", plan.mixinUsed());
    root.addProperty("remappingOccurred", plan.remappingOccurred());
    root.addProperty("publicApiExposed", plan.publicApiExposed());
    root.addProperty("javaModExecutionSandboxed", plan.javaModExecutionSandboxed());
    root.addProperty("instructionInspectionOccurred", plan.instructionInspectionOccurred());
    root.addProperty("callsiteInspectionOccurred", plan.callsiteInspectionOccurred());
    return root;
  }

  private JsonArray plannedPlacements(
      java.util.List<MinecraftPlannedHookPlacement> plannedPlacements) {
    JsonArray array = new JsonArray();
    for (MinecraftPlannedHookPlacement plannedPlacement : plannedPlacements) {
      JsonObject object = new JsonObject();
      addString(object, "id", plannedPlacement.id());
      addString(object, "sourceContractId", plannedPlacement.sourceContractId());
      addString(object, "catalogId", plannedPlacement.catalogId());
      addString(
          object, "kind", plannedPlacement.kind() == null ? null : plannedPlacement.kind().name());
      addString(object, "ownerInternalName", plannedPlacement.ownerInternalName());
      addString(object, "memberName", plannedPlacement.memberName());
      addString(object, "descriptor", plannedPlacement.descriptor());
      object.addProperty("bytecodeOffset", plannedPlacement.bytecodeOffset());
      addString(
          object, "mode", plannedPlacement.mode() == null ? null : plannedPlacement.mode().id());
      object.addProperty("required", plannedPlacement.required());
      object.add("methodCodeSummary", methodCodeSummary(plannedPlacement.methodCodeSummary()));
      array.add(object);
    }
    return array;
  }

  private JsonObject methodCodeSummary(MinecraftMethodCodeSummary summary) {
    JsonObject object = new JsonObject();
    if (summary == null) {
      return object;
    }
    addInteger(object, "maxStack", summary.maxStack());
    addInteger(object, "maxLocals", summary.maxLocals());
    addInteger(object, "codeLength", summary.codeLength());
    addString(object, "codeSha256", summary.codeSha256());
    addInteger(object, "exceptionTableCount", summary.exceptionTableCount());
    addInteger(object, "nestedCodeAttributeCount", summary.nestedCodeAttributeCount());
    object.addProperty("hasCodeAttribute", summary.hasCodeAttribute());
    object.addProperty("abstractOrNative", summary.abstractOrNative());
    addInteger(object, "methodEntryOffset", summary.methodEntryOffset());
    return object;
  }

  private void addInteger(JsonObject object, String name, Integer value) {
    if (value == null) {
      object.add(name, JsonNull.INSTANCE);
    } else {
      object.addProperty(name, value);
    }
  }

  private void addString(JsonObject object, String name, String value) {
    if (value == null) {
      object.add(name, JsonNull.INSTANCE);
    } else {
      object.addProperty(name, value);
    }
  }
}
