package com.spindle.core.minecraft.interpret;

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

public final class MinecraftArtifactInterpretationWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftArtifactInterpretation interpretation)
      throws LoaderException {
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(toJson(interpretation), writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write Minecraft artifact interpretation report " + outputPath, exception);
    }
  }

  JsonObject toJson(MinecraftArtifactInterpretation interpretation) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", interpretation.schema());
    root.addProperty("milestoneName", interpretation.milestoneName());
    root.addProperty("target", interpretation.target());
    addString(root, "minecraftVersion", interpretation.minecraftVersion());
    root.addProperty("side", interpretation.side());
    root.addProperty("analysisOnly", interpretation.analysisOnly());
    root.addProperty("classLoadingOccurred", interpretation.classLoadingOccurred());
    root.addProperty("injectionOccurred", interpretation.injectionOccurred());
    root.addProperty("transformationOccurred", interpretation.transformationOccurred());
    root.addProperty("patchingOccurred", interpretation.patchingOccurred());
    root.addProperty("hookInstallationOccurred", interpretation.hookInstallationOccurred());
    root.addProperty("interpretedAtRuntimePhase", interpretation.interpretedAtRuntimePhase());
    root.add("jars", jars(interpretation.jars()));
    root.addProperty("packageCount", interpretation.packageCount());
    root.addProperty("classCount", interpretation.classCount());
    root.addProperty("fieldCount", interpretation.fieldCount());
    root.addProperty("methodCount", interpretation.methodCount());
    root.addProperty("constructorCount", interpretation.constructorCount());
    root.add("packages", strings(interpretation.packages()));
    root.add("warnings", strings(interpretation.warnings()));
    return root;
  }

  private JsonArray jars(java.util.List<MinecraftInterpretedJar> jars) {
    JsonArray array = new JsonArray();
    for (MinecraftInterpretedJar jar : jars) {
      JsonObject object = new JsonObject();
      object.addProperty("path", jar.path());
      addString(object, "ownership", jar.ownership());
      addString(object, "origin", jar.origin());
      addString(object, "sha256", jar.sha256());
      object.addProperty("classCount", jar.classCount());
      object.addProperty("fieldCount", jar.fieldCount());
      object.addProperty("methodCount", jar.methodCount());
      object.addProperty("constructorCount", jar.constructorCount());
      object.add("packages", strings(jar.packages()));
      object.add("classes", classes(jar.classes()));
      array.add(object);
    }
    return array;
  }

  private JsonArray classes(java.util.List<MinecraftInterpretedClass> classes) {
    JsonArray array = new JsonArray();
    for (MinecraftInterpretedClass interpretedClass : classes) {
      JsonObject object = new JsonObject();
      object.addProperty("binaryName", interpretedClass.binaryName());
      object.addProperty("internalName", interpretedClass.internalName());
      object.addProperty("packageName", interpretedClass.packageName());
      addString(object, "superName", interpretedClass.superName());
      object.add("interfaces", strings(interpretedClass.interfaces()));
      object.addProperty("access", interpretedClass.access());
      object.add("accessFlags", strings(interpretedClass.accessFlags()));
      object.add("fields", fields(interpretedClass.fields()));
      object.add("methods", methods(interpretedClass.methods()));
      array.add(object);
    }
    return array;
  }

  private JsonArray fields(java.util.List<MinecraftInterpretedField> fields) {
    JsonArray array = new JsonArray();
    for (MinecraftInterpretedField field : fields) {
      JsonObject object = new JsonObject();
      object.addProperty("name", field.name());
      object.addProperty("descriptor", field.descriptor());
      object.addProperty("access", field.access());
      object.add("accessFlags", strings(field.accessFlags()));
      array.add(object);
    }
    return array;
  }

  private JsonArray methods(java.util.List<MinecraftInterpretedMethod> methods) {
    JsonArray array = new JsonArray();
    for (MinecraftInterpretedMethod method : methods) {
      JsonObject object = new JsonObject();
      object.addProperty("name", method.name());
      object.addProperty("descriptor", method.descriptor());
      object.addProperty("access", method.access());
      object.add("accessFlags", strings(method.accessFlags()));
      object.addProperty("constructor", method.constructor());
      object.addProperty("staticMethod", method.staticMethod());
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
