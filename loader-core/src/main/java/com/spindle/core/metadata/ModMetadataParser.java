package com.spindle.core.metadata;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.spindle.api.lifecycle.LifecyclePhase;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.discovery.ModCandidate;
import com.spindle.core.security.SecurityRuleId;
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
  private static final Pattern LIFECYCLE_DECLARATION_PATTERN =
      Pattern.compile(
          "[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)+::[A-Za-z_$][A-Za-z0-9_$]*");
  private static final Set<String> VALID_SIDES = Set.of("universal", "client", "server");
  private static final Set<String> VALID_LIFECYCLE_PHASES =
      Set.of(
          LifecyclePhase.BOOTSTRAP.name(),
          LifecyclePhase.CONFIGURE.name(),
          LifecyclePhase.PRE_SERVER_MAIN.name());

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
      throw new LoaderException(metadataError("Invalid metadata JSON in " + sourceName), exception);
    }

    int schema = requiredInt(jsonObject, "schema", sourceName);
    if (schema != 1 && schema != 2) {
      throw new LoaderException(
          metadataError(
              "Unsupported metadata schema `"
                  + schema
                  + "` in "
                  + sourceName
                  + ". Spindle supports schema `1` and schema `2`."));
    }

    String id = requiredString(jsonObject, "id", sourceName).trim();
    if (!MOD_ID_PATTERN.matcher(id).matches()) {
      throw new LoaderException(metadataError("Invalid mod id in " + sourceName + ": " + id));
    }

    String version = requiredString(jsonObject, "version", sourceName).trim();
    if (version.isEmpty()) {
      throw new LoaderException("Mod version must be non-empty in " + sourceName);
    }

    String side = requiredString(jsonObject, "side", sourceName).trim();
    if (!VALID_SIDES.contains(side)) {
      throw new LoaderException(metadataError("Invalid mod side in " + sourceName + ": " + side));
    }
    Map<String, List<String>> entrypoints = parseEntrypoints(jsonObject, sourceName, schema == 1);
    Map<String, List<String>> lifecycle =
        schema == 2 ? parseLifecycle(jsonObject, id, sourceName) : Map.of();
    List<String> permissions =
        schema == 2 ? parsePermissions(jsonObject, id, sourceName) : List.of();
    ModMetadata.Storage storage =
        schema == 2 ? parseStorage(jsonObject, id, sourceName) : ModMetadata.Storage.disabled();
    if (schema == 1 && jsonObject.has("lifecycle")) {
      throw new LoaderException(
          "Mod `"
              + id
              + "` uses metadata schema `1`, which does not support the `lifecycle` field in "
              + sourceName
              + ". Use `entrypoints.main` or upgrade to schema `2`.");
    }
    if (schema == 2 && entrypoints.isEmpty() && lifecycle.isEmpty()) {
      throw new LoaderException(
          "Mod `"
              + id
              + "` uses metadata schema `2` but declares neither `lifecycle` handlers nor compatibility `entrypoints` in "
              + sourceName
              + ".");
    }
    Map<String, String> depends = parseDepends(jsonObject, sourceName);
    Map<String, String> breaks = parseStringMap(jsonObject, "breaks", sourceName);
    return new ModMetadata(
        schema, id, version, side, entrypoints, depends, breaks, lifecycle, permissions, storage);
  }

  private Map<String, List<String>> parseEntrypoints(
      JsonObject jsonObject, String sourceName, boolean required) throws LoaderException {
    JsonObject entrypointsObject;
    if (!jsonObject.has("entrypoints") || jsonObject.get("entrypoints").isJsonNull()) {
      if (required) {
        throw new LoaderException("Missing object field entrypoints in " + sourceName);
      }
      return Map.of();
    }
    if (!jsonObject.get("entrypoints").isJsonObject()) {
      throw new LoaderException("entrypoints must be an object in " + sourceName);
    }
    entrypointsObject = jsonObject.getAsJsonObject("entrypoints");
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
    if (required && entrypoints.isEmpty()) {
      throw new LoaderException(
          "entrypoints must declare at least one entrypoint group in " + sourceName);
    }
    return Map.copyOf(entrypoints);
  }

  private Map<String, List<String>> parseLifecycle(
      JsonObject jsonObject, String modId, String sourceName) throws LoaderException {
    if (!jsonObject.has("lifecycle") || jsonObject.get("lifecycle").isJsonNull()) {
      return Map.of();
    }
    if (!jsonObject.get("lifecycle").isJsonObject()) {
      throw new LoaderException(
          lifecycleError(
              "Mod `" + modId + "` must declare `lifecycle` as an object in " + sourceName + "."));
    }

    JsonObject lifecycleObject = jsonObject.getAsJsonObject("lifecycle");
    Map<String, List<String>> lifecycle = new TreeMap<>();
    for (Map.Entry<String, JsonElement> entry :
        lifecycleObject.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
      String phase = entry.getKey().trim();
      if (!VALID_LIFECYCLE_PHASES.contains(phase)) {
        throw new LoaderException(
            lifecycleError(
                "Mod `"
                    + modId
                    + "` declares unsupported lifecycle phase `"
                    + phase
                    + "` in "
                    + sourceName
                    + ". Expected one of "
                    + String.join(", ", VALID_LIFECYCLE_PHASES)));
      }
      if (!entry.getValue().isJsonArray()) {
        throw new LoaderException(
            lifecycleError(
                "Mod `"
                    + modId
                    + "` must declare `lifecycle."
                    + phase
                    + "` as an array in "
                    + sourceName
                    + "."));
      }
      JsonArray array = entry.getValue().getAsJsonArray();
      if (array.size() == 0) {
        throw new LoaderException(
            lifecycleError(
                "Mod `"
                    + modId
                    + "` declares empty `lifecycle."
                    + phase
                    + "` in "
                    + sourceName
                    + "; declare at least one handler."));
      }
      List<String> declarations = new ArrayList<>();
      for (JsonElement element : array) {
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
          throw new LoaderException(
              lifecycleError(
                  "Mod `"
                      + modId
                      + "` must declare string handler values in `lifecycle."
                      + phase
                      + "` in "
                      + sourceName
                      + "."));
        }
        String declaration = element.getAsString().trim();
        if (!LIFECYCLE_DECLARATION_PATTERN.matcher(declaration).matches()) {
          throw new LoaderException(
              lifecycleError(
                  "Mod `"
                      + modId
                      + "` declares lifecycle handler `"
                      + declaration
                      + "` for phase `"
                      + phase
                      + "`, but expected `ClassName::methodName` in "
                      + sourceName));
        }
        declarations.add(declaration);
      }
      lifecycle.put(phase, List.copyOf(declarations));
    }
    return Map.copyOf(lifecycle);
  }

  private List<String> parsePermissions(JsonObject jsonObject, String modId, String sourceName)
      throws LoaderException {
    if (!jsonObject.has("permissions") || jsonObject.get("permissions").isJsonNull()) {
      return List.of();
    }
    if (!jsonObject.get("permissions").isJsonArray()) {
      throw new LoaderException(metadataError("permissions must be an array in " + sourceName));
    }
    List<String> permissions = new ArrayList<>();
    for (JsonElement element : jsonObject.getAsJsonArray("permissions")) {
      if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
        throw new LoaderException(
            metadataError(
                "permissions must contain strings for mod `" + modId + "` in " + sourceName));
      }
      String permission = element.getAsString().trim();
      if (permission.isEmpty()) {
        throw new LoaderException(
            metadataError(
                "permissions must not contain empty values for mod `"
                    + modId
                    + "` in "
                    + sourceName));
      }
      permissions.add(permission);
    }
    return permissions.stream().sorted().toList();
  }

  private ModMetadata.Storage parseStorage(JsonObject jsonObject, String modId, String sourceName)
      throws LoaderException {
    if (!jsonObject.has("storage") || jsonObject.get("storage").isJsonNull()) {
      return ModMetadata.Storage.disabled();
    }
    if (!jsonObject.get("storage").isJsonObject()) {
      throw new LoaderException(
          metadataError(
              "Mod `" + modId + "` must declare `storage` as an object in " + sourceName + "."));
    }
    JsonObject storageObject = jsonObject.getAsJsonObject("storage");
    return new ModMetadata.Storage(
        optionalBoolean(storageObject, "config", modId, "storage.config", sourceName),
        optionalBoolean(storageObject, "data", modId, "storage.data", sourceName),
        optionalBoolean(storageObject, "cache", modId, "storage.cache", sourceName),
        optionalBoolean(storageObject, "generated", modId, "storage.generated", sourceName));
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

  private boolean optionalBoolean(
      JsonObject jsonObject, String key, String modId, String displayField, String sourceName)
      throws LoaderException {
    if (!jsonObject.has(key) || jsonObject.get(key).isJsonNull()) {
      return false;
    }
    JsonElement element = jsonObject.get(key);
    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` must declare `"
                  + displayField
                  + "` as a boolean in "
                  + sourceName
                  + "."));
    }
    return element.getAsBoolean();
  }

  private String metadataError(String message) {
    return "[" + SecurityRuleId.SEC_METADATA_001.id() + "] " + message;
  }

  private String lifecycleError(String message) {
    return "[" + SecurityRuleId.SEC_LIFECYCLE_001.id() + "] " + message;
  }
}
