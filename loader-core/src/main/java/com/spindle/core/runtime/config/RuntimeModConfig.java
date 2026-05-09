package com.spindle.core.runtime.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.spindle.api.config.ModConfig;
import com.spindle.core.runtime.capability.RuntimeCapabilityCatalog;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class RuntimeModConfig implements ModConfig {
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
  private final String modId;
  private final boolean writeGranted;
  private final boolean runtimeWrites;
  private final Path configPath;
  private final Map<String, RuntimeConfigEntryPlan> entriesByKey;
  private final Map<String, String> valuesByKey;
  private final Map<String, JsonElement> unknownValues;

  RuntimeModConfig(
      String modId,
      boolean writeGranted,
      boolean runtimeWrites,
      Path configPath,
      Map<String, RuntimeConfigEntryPlan> entriesByKey,
      Map<String, String> valuesByKey,
      Map<String, JsonElement> unknownValues) {
    this.modId = modId;
    this.writeGranted = writeGranted;
    this.runtimeWrites = runtimeWrites;
    this.configPath = configPath;
    this.entriesByKey = new LinkedHashMap<>(entriesByKey);
    this.valuesByKey = new LinkedHashMap<>(valuesByKey);
    this.unknownValues = new LinkedHashMap<>(unknownValues);
  }

  @Override
  public boolean getBoolean(String key) {
    return Boolean.parseBoolean(requireTypedKey(key, "boolean"));
  }

  @Override
  public int getInteger(String key) {
    return Integer.parseInt(requireTypedKey(key, "integer"));
  }

  @Override
  public double getNumber(String key) {
    return Double.parseDouble(requireTypedKey(key, "number"));
  }

  @Override
  public String getString(String key) {
    return requireTypedKey(key, "string");
  }

  @Override
  public boolean has(String key) {
    return entriesByKey.containsKey(key);
  }

  @Override
  public Set<String> keys() {
    return Set.copyOf(entriesByKey.keySet());
  }

  @Override
  public boolean writable() {
    return writeGranted && runtimeWrites;
  }

  @Override
  public void setBoolean(String key, boolean value) {
    setValue(key, "boolean", Boolean.toString(value));
  }

  @Override
  public void setInteger(String key, int value) {
    setValue(key, "integer", Integer.toString(value));
  }

  @Override
  public void setNumber(String key, double value) {
    setValue(key, "number", new BigDecimal(Double.toString(value)).stripTrailingZeros().toPlainString());
  }

  @Override
  public void setString(String key, String value) {
    setValue(key, "string", value);
  }

  private String requireTypedKey(String key, String expectedType) {
    RuntimeConfigEntryPlan entry = requireDeclaredKey(key);
    if (!expectedType.equals(entry.type())) {
      throw new IllegalStateException(
          "Mod `"
              + modId
              + "` declared config key `"
              + key
              + "` as "
              + entry.type()
              + " but requested "
              + expectedType
              + ".");
    }
    return valuesByKey.get(key);
  }

  private void setValue(String key, String expectedType, String value) {
    requireWritable(key);
    RuntimeConfigEntryPlan entry = requireDeclaredKey(key);
    if (!expectedType.equals(entry.type())) {
      throw new IllegalStateException(
          "Mod `"
              + modId
              + "` declared config key `"
              + key
              + "` as "
              + entry.type()
              + " but requested "
              + expectedType
              + ".");
    }
    validate(entry, value);
    valuesByKey.put(key, value);
    persist();
  }

  private RuntimeConfigEntryPlan requireDeclaredKey(String key) {
    RuntimeConfigEntryPlan entry = entriesByKey.get(key);
    if (entry != null) {
      return entry;
    }
    throw new IllegalStateException(
        "Mod `"
            + modId
            + "` cannot access config key `"
            + key
            + "` because it was not declared in config.entries.");
  }

  private void requireWritable(String key) {
    if (writeGranted) {
      if (!runtimeWrites) {
        throw new IllegalStateException(
            "Mod `"
                + modId
                + "` cannot write config key `"
                + key
                + "` because config.runtimeWrites is false.");
      }
      return;
    }
    throw new IllegalStateException(
        "Mod `"
            + modId
            + "` cannot write config key `"
            + key
            + "` because capability `"
            + RuntimeCapabilityCatalog.CONFIG_WRITE
            + "` was not granted.");
  }

  private void validate(RuntimeConfigEntryPlan entry, String value) {
    switch (entry.type()) {
      case "boolean" -> {
        if (!"true".equals(value) && !"false".equals(value)) {
          throw new IllegalStateException("Config key `" + entry.key() + "` requires a boolean value.");
        }
      }
      case "integer" -> validateNumber(entry, new BigDecimal(value), true);
      case "number" -> validateNumber(entry, new BigDecimal(value), false);
      case "string" -> {
        if (!entry.allowed().isEmpty() && !entry.allowed().contains(value)) {
          throw new IllegalStateException(
              "Config key `"
                  + entry.key()
                  + "` must be one of "
                  + entry.allowed()
                  + ".");
        }
      }
      default -> throw new IllegalArgumentException("Unsupported config type " + entry.type());
    }
  }

  private void validateNumber(RuntimeConfigEntryPlan entry, BigDecimal value, boolean integer) {
    if (integer) {
      value.toBigIntegerExact();
    }
    if (entry.min() != null && value.compareTo(new BigDecimal(entry.min())) < 0) {
      throw new IllegalStateException(
          "Config key `" + entry.key() + "` must be >= " + entry.min() + ".");
    }
    if (entry.max() != null && value.compareTo(new BigDecimal(entry.max())) > 0) {
      throw new IllegalStateException(
          "Config key `" + entry.key() + "` must be <= " + entry.max() + ".");
    }
  }

  private void persist() {
    JsonObject root = new JsonObject();
    entriesByKey.values().stream()
        .sorted(java.util.Comparator.comparing(RuntimeConfigEntryPlan::key))
        .forEach(entry -> root.add(entry.key(), typedValue(valuesByKey.get(entry.key()), entry.type())));
    unknownValues.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> root.add(entry.getKey(), entry.getValue()));
    try {
      Files.createDirectories(configPath.getParent());
      try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
        gson.toJson(root, writer);
      }
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Failed to persist config file `" + configPath.toString().replace('\\', '/') + "`.",
          exception);
    }
  }

  private JsonElement typedValue(String value, String type) {
    return switch (type) {
      case "boolean" -> gson.toJsonTree(Boolean.parseBoolean(value));
      case "integer" -> gson.toJsonTree(Integer.parseInt(value));
      case "number" -> gson.toJsonTree(new BigDecimal(value));
      case "string" -> gson.toJsonTree(value);
      default -> throw new IllegalArgumentException("Unsupported config type " + type);
    };
  }
}
