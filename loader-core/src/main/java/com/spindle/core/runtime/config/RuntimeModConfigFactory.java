package com.spindle.core.runtime.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spindle.api.config.ModConfig;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.runtime.CompiledModpackProfile;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RuntimeModConfigFactory {
  public Map<String, ModConfig> create(Path workingDirectory, CompiledModpackProfile profile)
      throws LoaderException {
    Map<String, ModConfig> configs = new LinkedHashMap<>();
    for (RuntimeConfigModPlan modPlan : profile.config().mods()) {
      if (modPlan.entries().isEmpty()) {
        continue;
      }
      configs.put(modPlan.modId(), createConfig(workingDirectory, profile, modPlan));
    }
    return Map.copyOf(configs);
  }

  private ModConfig createConfig(
      Path workingDirectory, CompiledModpackProfile profile, RuntimeConfigModPlan modPlan)
      throws LoaderException {
    Path configPath = workingDirectory.resolve(modPlan.path()).normalize();
    JsonObject fileValues;
    try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
      fileValues = JsonParser.parseReader(reader).getAsJsonObject();
    } catch (IOException | RuntimeException exception) {
      throw new LoaderException(
          "Failed to load validated config file `" + configPath.toString().replace('\\', '/') + "`.",
          exception);
    }
    Map<String, RuntimeConfigEntryPlan> declaredEntries = new LinkedHashMap<>();
    Map<String, String> values = new LinkedHashMap<>();
    for (RuntimeConfigEntryPlan entry : modPlan.entries()) {
      declaredEntries.put(entry.key(), entry);
      values.put(entry.key(), entry.value());
      fileValues.remove(entry.key());
    }
    Map<String, JsonElement> unknownValues = new LinkedHashMap<>();
    fileValues.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> unknownValues.put(entry.getKey(), entry.getValue().deepCopy()));
    return new RuntimeModConfig(
        modPlan.modId(),
        grantedWrites(profile, modPlan.modId()),
        modPlan.runtimeWrites(),
        configPath,
        declaredEntries,
        values,
        unknownValues);
  }

  private boolean grantedWrites(CompiledModpackProfile profile, String modId) {
    return profile.permissions().mods().stream()
        .filter(modPlan -> modId.equals(modPlan.modId()))
        .flatMap(modPlan -> modPlan.grants().stream())
        .anyMatch(grant -> "config.write".equals(grant.capability()) && "granted".equals(grant.state()));
  }
}
