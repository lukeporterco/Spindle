package com.spindle.core.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.discovery.ModCandidate;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public final class ModMetadataParser {
  private static final Pattern MOD_ID_PATTERN = Pattern.compile("[a-z][a-z0-9_-]{1,63}");
  private static final Set<String> VALID_SIDES = Set.of("universal", "client", "server");

  public ModMetadata parse(ModCandidate candidate) throws LoaderException {
    Path jarPath = candidate.jarPath();
    try (JarFile jarFile = new JarFile(jarPath.toFile())) {
      ZipEntry entry = jarFile.getEntry("loader.mod.json");
      if (entry == null || entry.isDirectory()) {
        throw new LoaderException(
            "Missing loader.mod.json in " + candidate.normalizedRelativePath());
      }
      try (Reader reader =
          new java.io.InputStreamReader(
              jarFile.getInputStream(entry), java.nio.charset.StandardCharsets.UTF_8)) {
        return parse(reader, candidate.normalizedRelativePath());
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to read metadata from " + candidate.normalizedRelativePath(), exception);
    }
  }

  public ModMetadata parse(String json, String sourceName) throws LoaderException {
    return parse(new StringReader(json), sourceName);
  }

  public ModMetadata parse(Reader reader, String sourceName) throws LoaderException {
    JsonObject jsonObject;
    try {
      jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
    } catch (IllegalStateException | JsonParseException exception) {
      throw new LoaderException("Invalid metadata JSON in " + sourceName, exception);
    }

    int schema = requiredInt(jsonObject, "schema", sourceName);
    if (schema != 1) {
      throw new LoaderException("Unsupported metadata schema in " + sourceName + ": " + schema);
    }

    String id = requiredString(jsonObject, "id", sourceName).trim();
    if (!MOD_ID_PATTERN.matcher(id).matches()) {
      throw new LoaderException("Invalid mod id in " + sourceName + ": " + id);
    }

    String version = requiredString(jsonObject, "version", sourceName).trim();
    if (version.isEmpty()) {
      throw new LoaderException("Mod version must be non-empty in " + sourceName);
    }

    String side = requiredString(jsonObject, "side", sourceName).trim();
    if (!VALID_SIDES.contains(side)) {
      throw new LoaderException("Invalid mod side in " + sourceName + ": " + side);
    }
    Map<String, List<String>> entrypoints = parseEntrypoints(jsonObject, sourceName);
    Map<String, String> depends = parseDepends(jsonObject, sourceName);
    Map<String, String> breaks = parseStringMap(jsonObject, "breaks", sourceName);
    return new ModMetadata(schema, id, version, side, entrypoints, depends, breaks);
  }

  private Map<String, List<String>> parseEntrypoints(JsonObject jsonObject, String sourceName)
      throws LoaderException {
    JsonObject entrypointsObject = requiredObject(jsonObject, "entrypoints", sourceName);
    Map<String, List<String>> entrypoints = new LinkedHashMap<>();
    for (Map.Entry<String, JsonElement> entry :
        entrypointsObject.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
      if (!entry.getValue().isJsonArray()) {
        throw new LoaderException(
            "entrypoints." + entry.getKey() + " must be an array in " + sourceName);
      }
      JsonArray array = entry.getValue().getAsJsonArray();
      if (array.size() == 0) {
        throw new LoaderException(
            "entrypoints." + entry.getKey() + " must contain at least one class in " + sourceName);
      }
      List<String> entrypointClasses = new ArrayList<>();
      for (JsonElement element : array) {
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
          throw new LoaderException(
              "entrypoints." + entry.getKey() + " must contain class names in " + sourceName);
        }
        String className = element.getAsString().trim();
        if (className.isEmpty()) {
          throw new LoaderException("Entrypoint class name must be non-empty in " + sourceName);
        }
        entrypointClasses.add(className);
      }
      entrypoints.put(entry.getKey(), List.copyOf(entrypointClasses));
    }
    if (entrypoints.isEmpty()) {
      throw new LoaderException(
          "entrypoints must declare at least one entrypoint group in " + sourceName);
    }
    return Map.copyOf(entrypoints);
  }

  private Map<String, String> parseDepends(JsonObject jsonObject, String sourceName)
      throws LoaderException {
    return parseStringMap(jsonObject, "depends", sourceName);
  }

  private Map<String, String> parseStringMap(
      JsonObject jsonObject, String fieldName, String sourceName) throws LoaderException {
    JsonObject dependsObject;
    if (!jsonObject.has(fieldName) || jsonObject.get(fieldName).isJsonNull()) {
      dependsObject = new JsonObject();
    } else if (jsonObject.get(fieldName).isJsonObject()) {
      dependsObject = jsonObject.getAsJsonObject(fieldName);
    } else {
      throw new LoaderException(fieldName + " must be an object in " + sourceName);
    }

    Map<String, String> depends = new TreeMap<>();
    for (Map.Entry<String, JsonElement> entry : dependsObject.entrySet()) {
      JsonElement element = entry.getValue();
      if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
        throw new LoaderException(
            fieldName + " version must be a string for " + entry.getKey() + " in " + sourceName);
      }
      String requirement = element.getAsString().trim();
      if (requirement.isEmpty()) {
        throw new LoaderException(
            fieldName + " version must be non-empty for " + entry.getKey() + " in " + sourceName);
      }
      depends.put(entry.getKey(), requirement);
    }
    return Map.copyOf(depends);
  }

  private JsonObject requiredObject(JsonObject jsonObject, String key, String sourceName)
      throws LoaderException {
    JsonElement element = jsonObject.get(key);
    if (element == null || !element.isJsonObject()) {
      throw new LoaderException("Missing object field " + key + " in " + sourceName);
    }
    return element.getAsJsonObject();
  }

  private JsonArray requiredArray(JsonObject jsonObject, String key, String sourceName)
      throws LoaderException {
    JsonElement element = jsonObject.get(key);
    if (element == null || !element.isJsonArray()) {
      throw new LoaderException("Missing array field " + key + " in " + sourceName);
    }
    return element.getAsJsonArray();
  }

  private int requiredInt(JsonObject jsonObject, String key, String sourceName)
      throws LoaderException {
    JsonElement element = jsonObject.get(key);
    if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
      throw new LoaderException("Missing integer field " + key + " in " + sourceName);
    }

    try {
      return new BigDecimal(element.getAsString()).intValueExact();
    } catch (NumberFormatException | ArithmeticException exception) {
      throw new LoaderException("Missing integer field " + key + " in " + sourceName);
    }
  }

  private String requiredString(JsonObject jsonObject, String key, String sourceName)
      throws LoaderException {
    JsonElement element = jsonObject.get(key);
    if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
      throw new LoaderException("Missing string field " + key + " in " + sourceName);
    }
    return Objects.requireNonNull(element.getAsString());
  }
}
