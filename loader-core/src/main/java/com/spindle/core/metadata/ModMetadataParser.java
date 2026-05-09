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
import java.util.HashSet;
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
  private static final Pattern JAVA_CLASS_NAME_PATTERN =
      Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)+");
  private static final Pattern SERVICE_ID_PATTERN =
      Pattern.compile("[a-z][a-z0-9_-]{1,63}:[a-z][a-z0-9_.-]{1,127}");
  private static final Pattern CONFIG_KEY_PATTERN = Pattern.compile("[a-z][a-z0-9_-]{0,63}");
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
    ModMetadata.Services services =
        schema == 2 ? parseServices(jsonObject, id, sourceName) : ModMetadata.Services.empty();
    ModMetadata.Config config =
        schema == 2 ? parseConfig(jsonObject, id, sourceName) : ModMetadata.Config.empty();
    if (schema == 1 && jsonObject.has("lifecycle")) {
      throw new LoaderException(
          "Mod `"
              + id
              + "` uses metadata schema `1`, which does not support the `lifecycle` field in "
              + sourceName
              + ". Use `entrypoints.main` or upgrade to schema `2`.");
    }
    if (schema == 1 && jsonObject.has("services")) {
      throw new LoaderException(
          "Mod `"
              + id
              + "` uses metadata schema `1`, which does not support the `services` field in "
              + sourceName
              + ". Upgrade to schema `2`.");
    }
    if (schema == 1 && jsonObject.has("config")) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + id
                  + "` uses metadata schema `1`, which does not support the `config` field in "
                  + sourceName
                  + ". Upgrade to schema `2`."));
    }
    if (schema == 2 && entrypoints.isEmpty() && lifecycle.isEmpty() && services.isEmpty()) {
      throw new LoaderException(
          "Mod `"
              + id
              + "` uses metadata schema `2` but declares neither `lifecycle` handlers, compatibility `entrypoints`, nor `services` in "
              + sourceName
              + ".");
    }
    Map<String, String> depends = parseDepends(jsonObject, sourceName);
    Map<String, String> breaks = parseStringMap(jsonObject, "breaks", sourceName);
    return new ModMetadata(
        schema,
        id,
        version,
        side,
        entrypoints,
        depends,
        breaks,
        lifecycle,
        permissions,
        storage,
        services,
        config);
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

  private ModMetadata.Services parseServices(JsonObject jsonObject, String modId, String sourceName)
      throws LoaderException {
    if (!jsonObject.has("services") || jsonObject.get("services").isJsonNull()) {
      return ModMetadata.Services.empty();
    }
    if (!jsonObject.get("services").isJsonObject()) {
      throw new LoaderException(
          metadataError(
              "Mod `" + modId + "` must declare `services` as an object in " + sourceName + "."));
    }
    JsonObject servicesObject = jsonObject.getAsJsonObject("services");
    return new ModMetadata.Services(
        parseProviders(servicesObject, modId, sourceName),
        parseConsumers(servicesObject, modId, sourceName));
  }

  private ModMetadata.Config parseConfig(JsonObject jsonObject, String modId, String sourceName)
      throws LoaderException {
    if (!jsonObject.has("config") || jsonObject.get("config").isJsonNull()) {
      return ModMetadata.Config.empty();
    }
    if (!jsonObject.get("config").isJsonObject()) {
      throw new LoaderException(
          metadataError(
              "Mod `" + modId + "` must declare `config` as an object in " + sourceName + "."));
    }
    JsonObject configObject = jsonObject.getAsJsonObject("config");
    boolean runtimeWrites =
        optionalBoolean(configObject, "runtimeWrites", modId, "config.runtimeWrites", sourceName);
    JsonElement entriesElement = configObject.get("entries");
    if (entriesElement == null || entriesElement.isJsonNull()) {
      return new ModMetadata.Config(runtimeWrites, List.of());
    }
    if (!entriesElement.isJsonArray()) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` must declare `config.entries` as an array in "
                  + sourceName
                  + "."));
    }
    List<ModMetadata.ConfigEntry> entries = new ArrayList<>();
    Set<String> seenKeys = new HashSet<>();
    for (JsonElement entryElement : entriesElement.getAsJsonArray()) {
      if (!entryElement.isJsonObject()) {
        throw new LoaderException(
            metadataError(
                "Mod `"
                    + modId
                    + "` must declare each `config.entries` entry as an object in "
                    + sourceName
                    + "."));
      }
      JsonObject entryObject = entryElement.getAsJsonObject();
      String key = requiredString(entryObject, "key", sourceName).trim();
      if (!CONFIG_KEY_PATTERN.matcher(key).matches()) {
        throw new LoaderException(
            metadataError(
                "Mod `"
                    + modId
                    + "` declares invalid config key `"
                    + key
                    + "` in `config.entries` in "
                    + sourceName
                    + ". Expected `[a-z][a-z0-9_-]{0,63}`."));
      }
      if (!seenKeys.add(key)) {
        throw new LoaderException(
            metadataError(
                "Mod `"
                    + modId
                    + "` declares duplicate config key `"
                    + key
                    + "` in `config.entries` in "
                    + sourceName
                    + "."));
      }
      entries.add(parseConfigEntry(entryObject, modId, key, sourceName));
    }
    return new ModMetadata.Config(runtimeWrites, entries);
  }

  private ModMetadata.ConfigEntry parseConfigEntry(
      JsonObject entryObject, String modId, String key, String sourceName) throws LoaderException {
    String type = requiredString(entryObject, "type", sourceName).trim();
    if (!Set.of("boolean", "integer", "number", "string").contains(type)) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` declares unsupported config type `"
                  + type
                  + "` for key `"
                  + key
                  + "` in "
                  + sourceName
                  + "."));
    }
    JsonElement defaultElement = entryObject.get("default");
    if (defaultElement == null || defaultElement.isJsonNull()) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` must declare `default` for config key `"
                  + key
                  + "` in "
                  + sourceName
                  + "."));
    }
    String defaultValue = parseConfigValue(defaultElement, type, modId, key, "default", sourceName);
    String min = optionalNumericConfigField(entryObject, "min", type, modId, key, sourceName);
    String max = optionalNumericConfigField(entryObject, "max", type, modId, key, sourceName);
    List<String> allowed =
        optionalAllowedConfigField(entryObject, type, modId, key, defaultValue, sourceName);
    return new ModMetadata.ConfigEntry(key, type, defaultValue, min, max, allowed);
  }

  private String parseConfigValue(
      JsonElement element,
      String type,
      String modId,
      String key,
      String fieldName,
      String sourceName)
      throws LoaderException {
    if (!element.isJsonPrimitive()) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` must declare `config.entries."
                  + fieldName
                  + "` for key `"
                  + key
                  + "` as a primitive "
                  + type
                  + " value in "
                  + sourceName
                  + "."));
    }
    try {
      return switch (type) {
        case "boolean" -> {
          if (!element.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException();
          }
          yield Boolean.toString(element.getAsBoolean());
        }
        case "integer" -> canonicalInteger(element.getAsString());
        case "number" -> canonicalNumber(element.getAsString());
        case "string" -> {
          if (!element.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException();
          }
          yield element.getAsString();
        }
        default -> throw new IllegalArgumentException(type);
      };
    } catch (RuntimeException exception) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` declares config key `"
                  + key
                  + "` with invalid "
                  + fieldName
                  + " for type `"
                  + type
                  + "` in "
                  + sourceName
                  + "."));
    }
  }

  private String optionalNumericConfigField(
      JsonObject entryObject,
      String fieldName,
      String type,
      String modId,
      String key,
      String sourceName)
      throws LoaderException {
    JsonElement element = entryObject.get(fieldName);
    if (element == null || element.isJsonNull()) {
      return null;
    }
    if (!"integer".equals(type) && !"number".equals(type)) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` declares `"
                  + fieldName
                  + "` for config key `"
                  + key
                  + "`, but only integer or number entries may use `"
                  + fieldName
                  + "` in "
                  + sourceName
                  + "."));
    }
    return parseConfigValue(element, type, modId, key, fieldName, sourceName);
  }

  private List<String> optionalAllowedConfigField(
      JsonObject entryObject,
      String type,
      String modId,
      String key,
      String defaultValue,
      String sourceName)
      throws LoaderException {
    JsonElement element = entryObject.get("allowed");
    if (element == null || element.isJsonNull()) {
      return List.of();
    }
    if (!"string".equals(type)) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` declares `allowed` for config key `"
                  + key
                  + "`, but only string entries may use `allowed` in "
                  + sourceName
                  + "."));
    }
    if (!element.isJsonArray()) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` must declare `allowed` as an array for config key `"
                  + key
                  + "` in "
                  + sourceName
                  + "."));
    }
    List<String> allowed = new ArrayList<>();
    for (JsonElement optionElement : element.getAsJsonArray()) {
      if (!optionElement.isJsonPrimitive() || !optionElement.getAsJsonPrimitive().isString()) {
        throw new LoaderException(
            metadataError(
                "Mod `"
                    + modId
                    + "` must declare string `allowed` values for config key `"
                    + key
                    + "` in "
                    + sourceName
                    + "."));
      }
      allowed.add(optionElement.getAsString());
    }
    if (!allowed.contains(defaultValue)) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` declares default string `"
                  + defaultValue
                  + "` for config key `"
                  + key
                  + "`, but it is not included in `allowed` in "
                  + sourceName
                  + "."));
    }
    return allowed;
  }

  private String canonicalInteger(String value) {
    return new BigDecimal(value).toBigIntegerExact().toString();
  }

  private String canonicalNumber(String value) {
    return new BigDecimal(value).stripTrailingZeros().toPlainString();
  }

  private List<ModMetadata.ServiceProvider> parseProviders(
      JsonObject servicesObject, String modId, String sourceName) throws LoaderException {
    JsonElement element = servicesObject.get("provides");
    if (element == null || element.isJsonNull()) {
      return List.of();
    }
    if (!element.isJsonArray()) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` must declare `services.provides` as an array in "
                  + sourceName
                  + "."));
    }
    List<ModMetadata.ServiceProvider> providers = new ArrayList<>();
    for (JsonElement providerElement : element.getAsJsonArray()) {
      if (!providerElement.isJsonObject()) {
        throw new LoaderException(
            metadataError(
                "Mod `"
                    + modId
                    + "` must declare each `services.provides` entry as an object in "
                    + sourceName
                    + "."));
      }
      JsonObject providerObject = providerElement.getAsJsonObject();
      providers.add(
          new ModMetadata.ServiceProvider(
              requiredServiceId(providerObject, "id", modId, "services.provides", sourceName),
              requiredJavaClassName(providerObject, "type", modId, "services.provides", sourceName),
              requiredJavaClassName(
                  providerObject, "implementation", modId, "services.provides", sourceName)));
    }
    return providers.stream()
        .sorted(
            java.util.Comparator.comparing(ModMetadata.ServiceProvider::id)
                .thenComparing(ModMetadata.ServiceProvider::type)
                .thenComparing(ModMetadata.ServiceProvider::implementation))
        .toList();
  }

  private List<ModMetadata.ServiceConsumer> parseConsumers(
      JsonObject servicesObject, String modId, String sourceName) throws LoaderException {
    JsonElement element = servicesObject.get("consumes");
    if (element == null || element.isJsonNull()) {
      return List.of();
    }
    if (!element.isJsonArray()) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` must declare `services.consumes` as an array in "
                  + sourceName
                  + "."));
    }
    List<ModMetadata.ServiceConsumer> consumers = new ArrayList<>();
    Set<String> seenServiceIds = new HashSet<>();
    for (JsonElement consumerElement : element.getAsJsonArray()) {
      if (!consumerElement.isJsonObject()) {
        throw new LoaderException(
            metadataError(
                "Mod `"
                    + modId
                    + "` must declare each `services.consumes` entry as an object in "
                    + sourceName
                    + "."));
      }
      JsonObject consumerObject = consumerElement.getAsJsonObject();
      String serviceId =
          requiredServiceId(consumerObject, "id", modId, "services.consumes", sourceName);
      if (!seenServiceIds.add(serviceId)) {
        throw new LoaderException(
            metadataError(
                "Mod `"
                    + modId
                    + "` declares duplicate consumed service id `"
                    + serviceId
                    + "` in `services.consumes` in "
                    + sourceName
                    + ". Declare each consumed service id at most once per mod."));
      }
      consumers.add(
          new ModMetadata.ServiceConsumer(
              serviceId,
              requiredJavaClassName(consumerObject, "type", modId, "services.consumes", sourceName),
              optionalBooleanRequired(consumerObject, modId, sourceName)));
    }
    return consumers.stream()
        .sorted(
            java.util.Comparator.comparing(ModMetadata.ServiceConsumer::id)
                .thenComparing(ModMetadata.ServiceConsumer::type)
                .thenComparing(ModMetadata.ServiceConsumer::required))
        .toList();
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

  private boolean optionalBooleanRequired(JsonObject jsonObject, String modId, String sourceName)
      throws LoaderException {
    if (!jsonObject.has("required") || jsonObject.get("required").isJsonNull()) {
      return true;
    }
    return optionalBoolean(jsonObject, "required", modId, "services.consumes.required", sourceName);
  }

  private String requiredServiceId(
      JsonObject jsonObject, String key, String modId, String field, String sourceName)
      throws LoaderException {
    String value = requiredString(jsonObject, key, sourceName).trim();
    if (!SERVICE_ID_PATTERN.matcher(value).matches()) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` declares invalid service id `"
                  + value
                  + "` in `"
                  + field
                  + "` in "
                  + sourceName
                  + "."));
    }
    return value;
  }

  private String requiredJavaClassName(
      JsonObject jsonObject, String key, String modId, String field, String sourceName)
      throws LoaderException {
    String value = requiredString(jsonObject, key, sourceName).trim();
    if (!JAVA_CLASS_NAME_PATTERN.matcher(value).matches()) {
      throw new LoaderException(
          metadataError(
              "Mod `"
                  + modId
                  + "` declares invalid Java class name `"
                  + value
                  + "` in `"
                  + field
                  + "` in "
                  + sourceName
                  + "."));
    }
    return value;
  }

  private String metadataError(String message) {
    return "[" + SecurityRuleId.SEC_METADATA_001.id() + "] " + message;
  }

  private String lifecycleError(String message) {
    return "[" + SecurityRuleId.SEC_LIFECYCLE_001.id() + "] " + message;
  }
}
