package com.spindle.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spindle.core.app.LoaderApplication;
import com.spindle.core.cli.LaunchArguments;
import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.security.SecurityRuleId;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.ConfigReaderLifecycle;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.ConfigWriterLifecycle;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.SecurityReportAwareLifecycle;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.UndeclaredConfigReaderLifecycle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Runtime4ConfigSchemaContractTest {
  @TempDir Path tempDirectory;

  @Test
  void compiledProfileIncludesRuntimeFourConfigContract() throws Exception {
    createModJar(
        tempDirectory.resolve("mods/configmod.jar"),
        "configmod",
        Map.of("BOOTSTRAP", List.of(SecurityReportAwareLifecycle.class.getName() + "::bootstrap")),
        List.of("config.read", "storage.config"),
        true,
        false,
        false,
        false,
        false,
        defaultConfigEntries(),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)));

    execute(true);

    JsonObject profile = readCompiledProfile();
    assertEquals(5, profile.get("schemaVersion").getAsInt());
    JsonObject config = profile.getAsJsonObject("config");
    assertEquals(1, config.get("contractVersion").getAsInt());
    assertEquals("defaulted", config.getAsJsonArray("mods").get(0).getAsJsonObject().get("state").getAsString());
    assertEquals(
        "true",
        config.getAsJsonArray("mods").get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject().get("value").getAsString());
    JsonObject modPermissions = permissionMod(profile.getAsJsonObject("permissions"), "configmod");
    assertEquals(
        "granted",
        grantsByCapability(modPermissions.getAsJsonArray("grants")).get("config.read").get("state").getAsString());
    assertFalse(ruleIds(readSecurityReport()).contains(SecurityRuleId.SEC_PERM_001.id()));
  }

  @Test
  void configReaderLifecycleReceivesDeclaredValues() throws Exception {
    createModJar(
        tempDirectory.resolve("mods/configreader.jar"),
        "configreader",
        Map.of("BOOTSTRAP", List.of(ConfigReaderLifecycle.class.getName() + "::bootstrap")),
        List.of("config.read", "storage.config", "storage.generated"),
        true,
        false,
        false,
        true,
        false,
        defaultConfigEntries(),
        Map.of(resourceName(ConfigReaderLifecycle.class), readClassBytes(ConfigReaderLifecycle.class)));

    execute(false);

    assertEquals(
        "true|8|1.0|balanced",
        Files.readString(tempDirectory.resolve("generated/configreader/config.marker"), StandardCharsets.UTF_8).trim());
  }

  @Test
  void missingConfigFileAndMissingKeysAreDefaulted() throws Exception {
    createModJar(
        tempDirectory.resolve("mods/defaulted.jar"),
        "defaulted",
        Map.of("BOOTSTRAP", List.of(SecurityReportAwareLifecycle.class.getName() + "::bootstrap")),
        List.of("config.read", "storage.config"),
        true,
        false,
        false,
        false,
        false,
        defaultConfigEntries(),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)));

    execute(true);
    Path configPath = tempDirectory.resolve("config/defaulted/config.json");
    assertTrue(Files.exists(configPath));
    JsonObject defaults = JsonParser.parseString(Files.readString(configPath, StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals(true, defaults.get("enabled").getAsBoolean());
    assertTrue(findingCodes(readQualityReport(), "warningFindings").contains("config.missing_file_defaulted"));

    Files.writeString(
        configPath,
        """
        {
          "enabled": false,
          "mode": "balanced",
          "extra": "kept"
        }
        """,
        StandardCharsets.UTF_8);

    execute(true);

    JsonObject rewritten = JsonParser.parseString(Files.readString(configPath, StandardCharsets.UTF_8)).getAsJsonObject();
    assertEquals(false, rewritten.get("enabled").getAsBoolean());
    assertEquals(8, rewritten.get("maxcount").getAsInt());
    assertEquals("kept", rewritten.get("extra").getAsString());
    List<String> warnings = findingCodes(readQualityReport(), "warningFindings");
    assertTrue(warnings.contains("config.missing_key_defaulted"));
    assertTrue(warnings.contains("config.unknown_key"));
    assertTrue(findingCodes(readQualityReport(), "fatalFindings").isEmpty());
  }

  @Test
  void invalidConfigBlocksExecutionAfterValidation() throws Exception {
    createModJar(
        tempDirectory.resolve("mods/invalidconfig.jar"),
        "invalidconfig",
        Map.of("BOOTSTRAP", List.of(ConfigReaderLifecycle.class.getName() + "::bootstrap")),
        List.of("config.read", "storage.config", "storage.generated"),
        true,
        false,
        false,
        true,
        false,
        defaultConfigEntries(),
        Map.of(resourceName(ConfigReaderLifecycle.class), readClassBytes(ConfigReaderLifecycle.class)));
    Path configPath = tempDirectory.resolve("config/invalidconfig/config.json");
    Files.createDirectories(configPath.getParent());
    Files.writeString(configPath, "{\"maxcount\":\"bad\"}", StandardCharsets.UTF_8);

    execute(true);

    assertTrue(Files.exists(tempDirectory.resolve("spindle.profile.json")));
    assertTrue(Files.exists(tempDirectory.resolve("spindle.quality-report.json")));
    assertTrue(Files.exists(tempDirectory.resolve("spindle.lifecycle-report.json")));
    assertTrue(Files.exists(tempDirectory.resolve("spindle.security-report.json")));
    assertTrue(findingCodes(readQualityReport(), "fatalFindings").contains("config.invalid_type"));

    LoaderException exception = assertThrows(LoaderException.class, () -> execute(false));
    assertTrue(exception.getMessage().contains("Runtime config contract has fatal findings"));
    assertFalse(Files.exists(tempDirectory.resolve("generated/invalidconfig/config.marker")));
  }

  @Test
  void runtimeConfigWriteRequiresConfigWriteGrant() throws Exception {
    createModJar(
        tempDirectory.resolve("mods/readonly.jar"),
        "readonly",
        Map.of("BOOTSTRAP", List.of(ConfigWriterLifecycle.class.getName() + "::bootstrap")),
        List.of("config.write", "storage.config"),
        true,
        false,
        false,
        false,
        false,
        defaultConfigEntries(),
        Map.of(resourceName(ConfigWriterLifecycle.class), readClassBytes(ConfigWriterLifecycle.class)));

    LoaderException readonly = assertThrows(LoaderException.class, () -> execute(false));
    assertTrue(readonly.getMessage().contains("Lifecycle handler failed for mod `readonly`"));
    assertTrue(
        readonly.getCause().getCause().getMessage().contains("capability `config.write` was not granted"));

    resetWorkspace();
    createModJar(
        tempDirectory.resolve("mods/readonly.jar"),
        "readonly",
        Map.of("BOOTSTRAP", List.of(ConfigWriterLifecycle.class.getName() + "::bootstrap")),
        List.of("config.write", "storage.config"),
        true,
        false,
        false,
        false,
        true,
        defaultConfigEntries(),
        Map.of(resourceName(ConfigWriterLifecycle.class), readClassBytes(ConfigWriterLifecycle.class)));

    execute(false);

    JsonObject configFile =
        JsonParser.parseString(
                Files.readString(tempDirectory.resolve("config/readonly/config.json"), StandardCharsets.UTF_8))
            .getAsJsonObject();
    assertEquals("fast", configFile.get("mode").getAsString());
  }

  @Test
  void requestedConfigWriteDeniedWhenReadonly() throws Exception {
    createModJar(
        tempDirectory.resolve("mods/readonly.jar"),
        "readonly",
        Map.of("BOOTSTRAP", List.of(SecurityReportAwareLifecycle.class.getName() + "::bootstrap")),
        List.of("config.write", "storage.config"),
        true,
        false,
        false,
        false,
        false,
        defaultConfigEntries(),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)));

    execute(true);

    JsonObject modPermissions = permissionMod(readCompiledProfile().getAsJsonObject("permissions"), "readonly");
    assertEquals(
        "denied",
        grantsByCapability(modPermissions.getAsJsonArray("grants")).get("config.write").get("state").getAsString());
    JsonObject report = readSecurityReport();
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_PERM_001.id()));
    assertEquals(0, report.get("fatalCount").getAsInt());
  }

  @Test
  void undeclaredConfigLookupFailsClearly() throws Exception {
    createModJar(
        tempDirectory.resolve("mods/undeclared.jar"),
        "undeclared",
        Map.of("BOOTSTRAP", List.of(UndeclaredConfigReaderLifecycle.class.getName() + "::bootstrap")),
        List.of("config.read", "storage.config"),
        true,
        false,
        false,
        false,
        false,
        List.of(configEntry("mode", "string", "\"balanced\"", null, null, "[\"balanced\"]")),
        Map.of(
            resourceName(UndeclaredConfigReaderLifecycle.class),
            readClassBytes(UndeclaredConfigReaderLifecycle.class)));

    LoaderException exception = assertThrows(LoaderException.class, () -> execute(false));
    assertTrue(exception.getMessage().contains("Lifecycle handler failed for mod `undeclared`"));
    assertTrue(
        exception
            .getCause()
            .getCause()
            .getMessage()
            .contains("it was not declared in config.entries"));
  }

  @Test
  void schemaFourCacheInvalidatesCleanlyAgainstSchemaFiveReader() throws Exception {
    createModJar(
        tempDirectory.resolve("mods/cachemod.jar"),
        "cachemod",
        Map.of("BOOTSTRAP", List.of(SecurityReportAwareLifecycle.class.getName() + "::bootstrap")),
        List.of("config.read", "storage.config"),
        true,
        false,
        false,
        false,
        false,
        defaultConfigEntries(),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)));

    execute(true);

    JsonObject cachedProfile =
        JsonParser.parseString(Files.readString(cachedProfilePath(), StandardCharsets.UTF_8)).getAsJsonObject();
    cachedProfile.addProperty("schemaVersion", 4);
    cachedProfile.remove("config");
    Files.writeString(cachedProfilePath(), cachedProfile.toString(), StandardCharsets.UTF_8);

    execute(true);

    assertEquals("schema mismatch", readCompiledProfile().getAsJsonObject("cache").get("reason").getAsString());
  }

  private void createModJar(
      Path jarPath,
      String modId,
      Map<String, List<String>> lifecycle,
      List<String> permissions,
      boolean storageConfig,
      boolean storageData,
      boolean storageCache,
      boolean storageGenerated,
      boolean runtimeWrites,
      List<String> configEntries,
      Map<String, byte[]> entries)
      throws IOException {
    Files.createDirectories(jarPath.getParent());
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
      jarOutputStream.putNextEntry(new JarEntry("loader.mod.json"));
      jarOutputStream.write(
          metadataJson(
                  modId,
                  lifecycle,
                  permissions,
                  storageConfig,
                  storageData,
                  storageCache,
                  storageGenerated,
                  runtimeWrites,
                  configEntries)
              .getBytes(StandardCharsets.UTF_8));
      jarOutputStream.closeEntry();
      for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
        jarOutputStream.putNextEntry(new JarEntry(entry.getKey()));
        jarOutputStream.write(entry.getValue());
        jarOutputStream.closeEntry();
      }
    }
  }

  private String metadataJson(
      String modId,
      Map<String, List<String>> lifecycle,
      List<String> permissions,
      boolean storageConfig,
      boolean storageData,
      boolean storageCache,
      boolean storageGenerated,
      boolean runtimeWrites,
      List<String> configEntries) {
    return """
        {
          "schema": 2,
          "id": "%s",
          "version": "1.0.0",
          "side": "universal",
          "depends": {
            "loader": ">=0.1.0",
            "java": ">=25",
            "minecraft": ">=26.1.2"
          },
          "breaks": {},
          "lifecycle": %s,
          "permissions": %s,
          "storage": {
            "config": %s,
            "data": %s,
            "cache": %s,
            "generated": %s
          },
          "config": {
            "runtimeWrites": %s,
            "entries": %s
          },
          "services": {
            "provides": [],
            "consumes": []
          }
        }
        """
        .formatted(
            modId,
            lifecycleJson(lifecycle),
            toJsonArray(permissions),
            Boolean.toString(storageConfig),
            Boolean.toString(storageData),
            Boolean.toString(storageCache),
            Boolean.toString(storageGenerated),
            Boolean.toString(runtimeWrites),
            configEntries.stream().collect(java.util.stream.Collectors.joining(", ", "[", "]")));
  }

  private List<String> defaultConfigEntries() {
    return List.of(
        configEntry("enabled", "boolean", "true", null, null, null),
        configEntry("maxcount", "integer", "8", "0", "64", null),
        configEntry("mode", "string", "\"balanced\"", null, null, "[\"balanced\", \"fast\"]"),
        configEntry("scale", "number", "1.0", "0.1", "10.0", null));
  }

  private String configEntry(
      String key, String type, String defaultValue, String min, String max, String allowed) {
    StringBuilder builder = new StringBuilder();
    builder.append("{\"key\":\"").append(key).append("\",\"type\":\"").append(type).append("\",\"default\":").append(defaultValue);
    if (min != null) {
      builder.append(",\"min\":").append(min);
    }
    if (max != null) {
      builder.append(",\"max\":").append(max);
    }
    if (allowed != null) {
      builder.append(",\"allowed\":").append(allowed);
    }
    builder.append("}");
    return builder.toString();
  }

  private String lifecycleJson(Map<String, List<String>> lifecycle) {
    StringBuilder builder = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<String, List<String>> entry : lifecycle.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
      if (!first) {
        builder.append(", ");
      }
      builder.append("\"").append(entry.getKey()).append("\": ").append(toJsonArray(entry.getValue()));
      first = false;
    }
    builder.append("}");
    return builder.toString();
  }

  private JsonObject readCompiledProfile() throws IOException {
    return JsonParser.parseString(
            Files.readString(tempDirectory.resolve("spindle.profile.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private JsonObject readQualityReport() throws IOException {
    return JsonParser.parseString(
            Files.readString(tempDirectory.resolve("spindle.quality-report.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private JsonObject readSecurityReport() throws IOException {
    return JsonParser.parseString(
            Files.readString(tempDirectory.resolve("spindle.security-report.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private Path cachedProfilePath() throws IOException {
    JsonObject profile = readCompiledProfile();
    return tempDirectory
        .resolve(".spindle")
        .resolve("profile-cache")
        .resolve(profile.get("inputFingerprint").getAsString())
        .resolve("spindle.profile.json");
  }

  private JsonObject permissionMod(JsonObject permissions, String modId) {
    for (var element : permissions.getAsJsonArray("mods")) {
      JsonObject mod = element.getAsJsonObject();
      if (modId.equals(mod.get("modId").getAsString())) {
        return mod;
      }
    }
    throw new IllegalArgumentException("Missing permission mod " + modId);
  }

  private Map<String, JsonObject> grantsByCapability(JsonArray grants) {
    Map<String, JsonObject> values = new LinkedHashMap<>();
    for (var element : grants) {
      JsonObject grant = element.getAsJsonObject();
      values.put(grant.get("capability").getAsString(), grant);
    }
    return Map.copyOf(values);
  }

  private List<String> findingCodes(JsonObject report, String fieldName) {
    List<String> values = new ArrayList<>();
    for (var element : report.getAsJsonArray(fieldName)) {
      values.add(element.getAsJsonObject().get("code").getAsString());
    }
    return values;
  }

  private List<String> ruleIds(JsonObject report) {
    List<String> values = new ArrayList<>();
    for (var element : report.getAsJsonArray("findings")) {
      values.add(element.getAsJsonObject().get("ruleId").getAsString());
    }
    return values;
  }

  private void resetWorkspace() throws IOException {
    try (var paths = Files.list(tempDirectory.resolve("mods"))) {
      for (Path path : paths.toList()) {
        Files.deleteIfExists(path);
      }
    }
    Files.deleteIfExists(tempDirectory.resolve("spindle.lock.json"));
    Path cacheDirectory = tempDirectory.resolve(".spindle");
    if (Files.exists(cacheDirectory)) {
      try (var paths = Files.walk(cacheDirectory)) {
        paths.sorted(java.util.Comparator.reverseOrder()).forEach(this::deleteUnchecked);
      }
    }
  }

  private void deleteUnchecked(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  private String toJsonArray(List<String> values) {
    return values.stream()
        .map(value -> "\"" + value + "\"")
        .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
  }

  private String execute(boolean validateOnly) throws Exception {
    JsonDiagnosticSink sink =
        new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json"));
    try {
      return captureStdout(
          () ->
              new LoaderApplication()
                  .run(
                      tempDirectory,
                      new LaunchArguments(
                          ValidationGameMain.class.getName(),
                          "sample",
                          List.of(),
                          validateOnly,
                          false,
                          false,
                          false,
                          null,
                          null,
                          null,
                          false),
                      sink));
    } finally {
      sink.write();
    }
  }

  private String captureStdout(ThrowingRunnable runnable) throws Exception {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (PrintStream replacement = new PrintStream(outputStream, true, StandardCharsets.UTF_8)) {
      System.setOut(replacement);
      runnable.run();
    } finally {
      System.setOut(originalOut);
    }
    return outputStream.toString(StandardCharsets.UTF_8);
  }

  private byte[] readClassBytes(Class<?> type) throws IOException {
    try (var inputStream = type.getClassLoader().getResourceAsStream(resourceName(type))) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + resourceName(type));
      }
      return inputStream.readAllBytes();
    }
  }

  private String resourceName(Class<?> type) {
    return type.getName().replace('.', '/') + ".class";
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }

  public static final class ValidationGameMain {
    public static void main(String[] args) {}
  }
}
