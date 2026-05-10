package com.spindle.core.runtime.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class RuntimeConfigMaterializer {
  private static final BigInteger INT32_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
  private static final BigInteger INT32_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  public RuntimeConfigContract materialize(Path workingDirectory, RuntimeConfigContract contract)
      throws LoaderException {
    List<RuntimeConfigModPlan> mods = new ArrayList<>();
    for (RuntimeConfigModPlan mod : contract.mods()) {
      mods.add(materializeMod(workingDirectory, mod));
    }
    return new RuntimeConfigContract(
        contract.contractVersion(), contract.scope(), contract.format(), mods, null);
  }

  private RuntimeConfigModPlan materializeMod(Path workingDirectory, RuntimeConfigModPlan mod)
      throws LoaderException {
    if (RuntimeConfigStates.MOD_NONE.equals(mod.state())) {
      return mod;
    }
    if (RuntimeConfigStates.MOD_STORAGE_NOT_GRANTED.equals(mod.state())) {
      return new RuntimeConfigModPlan(
          mod.modId(),
          mod.path(),
          mod.runtimeWrites(),
          RuntimeConfigStates.MOD_STORAGE_NOT_GRANTED,
          mod.entries(),
          List.of(),
          RuntimeConfigSummary.modSummary(mod.entries(), 0, 1, 0),
          "config.storage_not_granted");
    }

    Path configPath = workingDirectory.resolve(mod.path()).normalize();
    if (!Files.exists(configPath)) {
      writeConfigFile(configPath, mod.entries(), Map.of());
      List<RuntimeConfigEntryPlan> entries =
          mod.entries().stream()
              .map(
                  entry ->
                      new RuntimeConfigEntryPlan(
                          entry.key(),
                          entry.type(),
                          entry.defaultValue(),
                          entry.defaultValue(),
                          RuntimeConfigStates.ENTRY_DEFAULTED,
                          "Config file was missing, so Spindle wrote the declared default value.",
                          entry.min(),
                          entry.max(),
                          entry.allowed(),
                          "config.missing_file_defaulted"))
              .toList();
      return new RuntimeConfigModPlan(
          mod.modId(),
          mod.path(),
          mod.runtimeWrites(),
          RuntimeConfigStates.MOD_DEFAULTED,
          entries,
          List.of(),
          RuntimeConfigSummary.modSummary(entries, 0, 0, 1),
          null);
    }

    JsonObject root;
    try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
      JsonElement parsed = JsonParser.parseReader(reader);
      if (!parsed.isJsonObject()) {
        return invalidMod(mod, "config.invalid_json", "Config file must contain a flat JSON object.");
      }
      root = parsed.getAsJsonObject();
    } catch (RuntimeException | IOException exception) {
      return invalidMod(mod, "config.invalid_json", "Config file is not valid JSON.");
    }

    Map<String, JsonElement> unknownKeyValues = new TreeMap<>();
    for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
      unknownKeyValues.put(entry.getKey(), entry.getValue().deepCopy());
    }

    List<RuntimeConfigEntryPlan> entries = new ArrayList<>();
    boolean shouldWrite = false;
    int warningCount = 0;
    int fatalCount = 0;
    String fatalFindingCode = null;
    for (RuntimeConfigEntryPlan entry : mod.entries()) {
      unknownKeyValues.remove(entry.key());
      JsonElement value = root.get(entry.key());
      if (value == null || value.isJsonNull()) {
        entries.add(
            new RuntimeConfigEntryPlan(
                entry.key(),
                entry.type(),
                entry.defaultValue(),
                entry.defaultValue(),
                RuntimeConfigStates.ENTRY_MISSING_DEFAULTED,
                "Config key was missing, so Spindle added the declared default value.",
                entry.min(),
                entry.max(),
                entry.allowed(),
                "config.missing_key_defaulted"));
        shouldWrite = true;
        warningCount++;
        continue;
      }
      ValidationResult validation = validateValue(entry, value);
      entries.add(
          new RuntimeConfigEntryPlan(
              entry.key(),
              entry.type(),
              entry.defaultValue(),
              validation.value(),
              validation.state(),
              validation.reason(),
              entry.min(),
              entry.max(),
              entry.allowed(),
              validation.findingCode()));
      if (validation.fatal()) {
        fatalCount++;
        fatalFindingCode = validation.findingCode();
      }
    }

    List<String> unknownKeys = List.copyOf(unknownKeyValues.keySet());
    if (!unknownKeys.isEmpty()) {
      warningCount++;
    }
    if (fatalCount > 0) {
      return new RuntimeConfigModPlan(
          mod.modId(),
          mod.path(),
          mod.runtimeWrites(),
          RuntimeConfigStates.MOD_INVALID,
          entries,
          unknownKeys,
          RuntimeConfigSummary.modSummary(entries, unknownKeys.size(), fatalCount, warningCount),
          fatalFindingCode);
    }

    if (shouldWrite) {
      writeConfigFile(configPath, entries, unknownKeyValues);
    }
    return new RuntimeConfigModPlan(
        mod.modId(),
        mod.path(),
        mod.runtimeWrites(),
        warningCount > 0 ? RuntimeConfigStates.MOD_DEFAULTED : RuntimeConfigStates.MOD_VALID,
        entries,
        unknownKeys,
        RuntimeConfigSummary.modSummary(entries, unknownKeys.size(), 0, warningCount),
        null);
  }

  private RuntimeConfigModPlan invalidMod(
      RuntimeConfigModPlan mod, String findingCode, String reason) {
    List<RuntimeConfigEntryPlan> entries =
        mod.entries().stream()
            .map(
                entry ->
                    new RuntimeConfigEntryPlan(
                        entry.key(),
                        entry.type(),
                        entry.defaultValue(),
                        null,
                        RuntimeConfigStates.ENTRY_VALID,
                        reason,
                        entry.min(),
                        entry.max(),
                        entry.allowed(),
                        findingCode))
            .toList();
    return new RuntimeConfigModPlan(
        mod.modId(),
        mod.path(),
        mod.runtimeWrites(),
        RuntimeConfigStates.MOD_INVALID,
        entries,
        List.of(),
        RuntimeConfigSummary.modSummary(entries, 0, 1, 0),
        findingCode);
  }

  private void writeConfigFile(
      Path configPath,
      List<RuntimeConfigEntryPlan> entries,
      Map<String, JsonElement> unknownKeyValues)
      throws LoaderException {
    JsonObject object = new JsonObject();
    entries.stream()
        .sorted(java.util.Comparator.comparing(RuntimeConfigEntryPlan::key))
        .forEach(entry -> object.add(entry.key(), typedValue(entry.value(), entry.type())));
    unknownKeyValues.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> object.add(entry.getKey(), entry.getValue()));
    try {
      Files.createDirectories(configPath.getParent());
      try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
        gson.toJson(object, writer);
      }
    } catch (IOException exception) {
      throw new LoaderException("Failed to write config file `" + configPath.toString().replace('\\', '/') + "`.", exception);
    }
  }

  private ValidationResult validateValue(RuntimeConfigEntryPlan entry, JsonElement value) {
    try {
      return switch (entry.type()) {
        case "boolean" -> {
          if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            yield invalidType();
          }
          yield new ValidationResult(
              Boolean.toString(value.getAsBoolean()),
              RuntimeConfigStates.ENTRY_VALID,
              "Config value is valid.",
              null,
              false);
        }
        case "integer" -> validateNumeric(entry, value, true);
        case "number" -> validateNumeric(entry, value, false);
        case "string" -> validateString(entry, value);
        default -> throw new IllegalArgumentException(entry.type());
      };
    } catch (RuntimeException exception) {
      return invalidType();
    }
  }

  private ValidationResult validateNumeric(
      RuntimeConfigEntryPlan entry, JsonElement value, boolean integer) {
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
      return invalidType();
    }
    try {
      BigDecimal parsed = new BigDecimal(value.getAsString());
      if (integer) {
        BigInteger integerValue = parsed.toBigIntegerExact();
        if (integerValue.compareTo(INT32_MIN) < 0 || integerValue.compareTo(INT32_MAX) > 0) {
          return invalidType();
        }
        parsed = new BigDecimal(integerValue);
      }
      if (entry.min() != null && parsed.compareTo(new BigDecimal(entry.min())) < 0) {
        return new ValidationResult(
            canonicalNumber(parsed),
            RuntimeConfigStates.ENTRY_INVALID_RANGE,
            "Config value is below the declared minimum.",
            "config.invalid_range",
            true);
      }
      if (entry.max() != null && parsed.compareTo(new BigDecimal(entry.max())) > 0) {
        return new ValidationResult(
            canonicalNumber(parsed),
            RuntimeConfigStates.ENTRY_INVALID_RANGE,
            "Config value is above the declared maximum.",
            "config.invalid_range",
            true);
      }
      return new ValidationResult(
          integer ? parsed.toBigIntegerExact().toString() : canonicalNumber(parsed),
          RuntimeConfigStates.ENTRY_VALID,
          "Config value is valid.",
          null,
          false);
    } catch (ArithmeticException exception) {
      return invalidType();
    }
  }

  private ValidationResult validateString(RuntimeConfigEntryPlan entry, JsonElement value) {
    if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
      return invalidType();
    }
    String stringValue = value.getAsString();
    if (!entry.allowed().isEmpty() && !entry.allowed().contains(stringValue)) {
      return new ValidationResult(
          stringValue,
          RuntimeConfigStates.ENTRY_INVALID_OPTION,
          "Config value is not in the declared allowed list.",
          "config.invalid_option",
          true);
    }
    return new ValidationResult(
        stringValue, RuntimeConfigStates.ENTRY_VALID, "Config value is valid.", null, false);
  }

  private ValidationResult invalidType() {
    return new ValidationResult(
        null,
        RuntimeConfigStates.ENTRY_INVALID_TYPE,
        "Config value does not match the declared type.",
        "config.invalid_type",
        true);
  }

  private JsonElement typedValue(String value, String type) {
    return switch (type) {
      case "boolean" -> gson.toJsonTree(Boolean.parseBoolean(value));
      case "integer" -> gson.toJsonTree(parseInt32(value));
      case "number" -> gson.toJsonTree(new BigDecimal(value));
      case "string" -> gson.toJsonTree(value);
      default -> throw new IllegalArgumentException("Unsupported config type " + type);
    };
  }

  private int parseInt32(String value) {
    try {
      BigInteger integerValue = new BigDecimal(value).toBigIntegerExact();
      if (integerValue.compareTo(INT32_MIN) < 0 || integerValue.compareTo(INT32_MAX) > 0) {
        throw new IllegalArgumentException("Integer value is outside signed 32-bit range.");
      }
      return integerValue.intValueExact();
    } catch (ArithmeticException | NumberFormatException exception) {
      throw new IllegalArgumentException("Invalid integer config value `" + value + "`.", exception);
    }
  }

  private String canonicalNumber(BigDecimal value) {
    return value.stripTrailingZeros().toPlainString();
  }

  private record ValidationResult(
      String value, String state, String reason, String findingCode, boolean fatal) {}
}
