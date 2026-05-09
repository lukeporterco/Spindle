package com.spindle.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spindle.core.app.LoaderApplication;
import com.spindle.core.cli.LaunchArguments;
import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.security.SecurityRuleId;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.CapabilityAwareLifecycle;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.DeniedStorageLifecycle;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.SecurityReportAwareLifecycle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Runtime2CapabilityGrantContractTest {
  @TempDir Path tempDirectory;

  @Test
  void compiledProfileIncludesRuntimeTwoCapabilityPlan() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/runtime-two.jar"),
        "runtime2mod",
        Map.of("BOOTSTRAP", List.of(SecurityReportAwareLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)),
        List.of("storage.config", "service.provide", "network.outbound", "example.custom"),
        true,
        false,
        false,
        true);

    execute(true);

    JsonObject profile = readCompiledProfile();
    JsonObject permissions = profile.getAsJsonObject("permissions");
    assertEquals(5, profile.get("schemaVersion").getAsInt());
    assertEquals(1, permissions.get("catalogVersion").getAsInt());
    assertEquals("spindle-api-only", permissions.get("scope").getAsString());
    assertEquals(
        "in-process-unrestricted-java",
        permissions.get("runtimeExecutionIsolationMode").getAsString());
    assertFalse(permissions.get("sandboxed").getAsBoolean());

    JsonObject modPermissions = permissions.getAsJsonArray("mods").get(0).getAsJsonObject();
    assertEquals(
        List.of("example.custom", "network.outbound", "service.provide", "storage.config"),
        toStringList(modPermissions.getAsJsonArray("requested")));

    Map<String, JsonObject> grants = grantsByCapability(modPermissions.getAsJsonArray("grants"));
    assertGrant(
        grants.get("storage.config"),
        "granted",
        List.of("metadata.storage.config", "metadata.permissions"));
    assertGrant(
        grants.get("storage.generated"), "granted", List.of("metadata.storage.generated"));
    assertGrant(grants.get("service.provide"), "denied", List.of("metadata.permissions"));
    assertGrant(
        grants.get("network.outbound"), "visibility-only", List.of("metadata.permissions"));
    assertGrant(grants.get("example.custom"), "unknown", List.of("metadata.permissions"));

    assertSummary(modPermissions.getAsJsonObject("summary"), 2, 1, 0, 1, 1);
    assertSummary(permissions.getAsJsonObject("summary"), 2, 1, 0, 1, 1);
  }

  @Test
  void deniedStorageRequestProducesPermissionWarningWithoutBlockingExecution() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/denied.jar"),
        "deniedmod",
        Map.of("BOOTSTRAP", List.of(SecurityReportAwareLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)),
        List.of("storage.data"),
        false,
        false,
        false,
        false);

    execute(false);

    JsonObject report = readSecurityReport();
    assertEquals("validated", report.get("state").getAsString());
    assertEquals(0, report.get("fatalCount").getAsInt());
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_PERM_001.id()));

    JsonObject modPlan =
        report.getAsJsonObject("capabilityGrants").getAsJsonArray("mods").get(0).getAsJsonObject();
    assertEquals(
        "denied",
        grantsByCapability(modPlan.getAsJsonArray("grants"))
            .get("storage.data")
            .get("state")
            .getAsString());
  }

  @Test
  void grantedStorageRequestDoesNotProducePermissionWarning() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/granted.jar"),
        "grantedmod",
        Map.of("BOOTSTRAP", List.of(SecurityReportAwareLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)),
        List.of("storage.config"),
        true,
        false,
        false,
        false);

    execute(false);

    JsonObject report = readSecurityReport();
    assertFalse(ruleIds(report).contains(SecurityRuleId.SEC_PERM_001.id()));
    JsonObject modPlan =
        report.getAsJsonObject("capabilityGrants").getAsJsonArray("mods").get(0).getAsJsonObject();
    assertEquals(
        "granted",
        grantsByCapability(modPlan.getAsJsonArray("grants"))
            .get("storage.config")
            .get("state")
            .getAsString());
  }

  @Test
  void modContextExposesGrantedCapabilitiesDuringLifecycleExecution() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/capabilities.jar"),
        "capabilitymod",
        Map.of("BOOTSTRAP", List.of(CapabilityAwareLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(CapabilityAwareLifecycle.class),
            readClassBytes(CapabilityAwareLifecycle.class)),
        List.of("storage.generated"),
        false,
        false,
        false,
        true);

    execute(false);

    assertEquals(
        "true|true",
        Files.readString(
                tempDirectory.resolve("generated/capabilitymod/capabilities.marker"),
                StandardCharsets.UTF_8)
            .trim());
  }

  @Test
  void modContextDeniesDisabledStorageAccessDuringLifecycleExecution() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/disabled-storage.jar"),
        "disabledstorage",
        Map.of("BOOTSTRAP", List.of(DeniedStorageLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(DeniedStorageLifecycle.class),
            readClassBytes(DeniedStorageLifecycle.class)),
        List.of(),
        false,
        false,
        false,
        false);

    LoaderException exception = assertThrows(LoaderException.class, () -> execute(false));

    assertTrue(exception.getMessage().contains("Lifecycle handler failed for mod `disabledstorage`"));
    LoaderException lifecycleFailure = assertInstanceOf(LoaderException.class, exception.getCause());
    Throwable cause = assertInstanceOf(IllegalStateException.class, lifecycleFailure.getCause());
    assertTrue(cause.getMessage().contains("dataDirectory()"));
    assertTrue(cause.getMessage().contains("storage.data"));
    assertTrue(cause.getMessage().contains("Enable storage.data in loader.mod.json."));
    assertEquals("validated", readSecurityReport().get("state").getAsString());
  }

  @Test
  void schemaThreeCacheInvalidatesCleanlyAgainstSchemaFiveReader() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/cache-schema.jar"),
        "cachemod",
        Map.of("BOOTSTRAP", List.of(SecurityReportAwareLifecycle.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)),
        List.of("network.outbound"),
        false,
        false,
        false,
        false);

    execute(true);

    JsonObject cachedProfile =
        JsonParser.parseString(Files.readString(cachedProfilePath(), StandardCharsets.UTF_8))
            .getAsJsonObject();
    cachedProfile.addProperty("schemaVersion", 3);
    JsonObject oldPermissions = new JsonObject();
    JsonArray mods = new JsonArray();
    JsonObject mod = new JsonObject();
    mod.addProperty("modId", "cachemod");
    JsonArray requested = new JsonArray();
    requested.add("network.outbound");
    mod.add("requested", requested);
    mods.add(mod);
    oldPermissions.add("mods", mods);
    cachedProfile.add("permissions", oldPermissions);
    Files.writeString(cachedProfilePath(), cachedProfile.toString(), StandardCharsets.UTF_8);

    execute(true);

    assertEquals(
        "schema mismatch",
        readCompiledProfile().getAsJsonObject("cache").get("reason").getAsString());
  }

  private JsonObject readCompiledProfile() throws IOException {
    return JsonParser.parseString(
            Files.readString(tempDirectory.resolve("spindle.profile.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private JsonObject readSecurityReport() throws IOException {
    return JsonParser.parseString(
            Files.readString(
                tempDirectory.resolve("spindle.security-report.json"), StandardCharsets.UTF_8))
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

  private void createSchemaTwoModJar(
      Path jarPath,
      String modId,
      Map<String, List<String>> lifecycle,
      Map<String, byte[]> entries,
      List<String> permissions,
      boolean config,
      boolean data,
      boolean cache,
      boolean generated)
      throws IOException {
    createModJar(
        jarPath, schemaTwoMetadata(modId, lifecycle, permissions, config, data, cache, generated), entries);
  }

  private void createModJar(Path jarPath, String metadataJson, Map<String, byte[]> entries)
      throws IOException {
    Files.createDirectories(jarPath.getParent());
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
      jarOutputStream.putNextEntry(new JarEntry("loader.mod.json"));
      jarOutputStream.write(metadataJson.getBytes(StandardCharsets.UTF_8));
      jarOutputStream.closeEntry();
      for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
        jarOutputStream.putNextEntry(new JarEntry(entry.getKey()));
        jarOutputStream.write(entry.getValue());
        jarOutputStream.closeEntry();
      }
    }
  }

  private String schemaTwoMetadata(
      String modId,
      Map<String, List<String>> lifecycle,
      List<String> permissions,
      boolean config,
      boolean data,
      boolean cache,
      boolean generated) {
    StringBuilder lifecycleJson = new StringBuilder();
    boolean firstPhase = true;
    for (Map.Entry<String, List<String>> entry :
        lifecycle.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
      if (!firstPhase) {
        lifecycleJson.append(",\n");
      }
      lifecycleJson.append("    \"").append(entry.getKey()).append("\": [\n");
      for (int index = 0; index < entry.getValue().size(); index++) {
        lifecycleJson.append("      \"").append(entry.getValue().get(index)).append("\"");
        if (index + 1 < entry.getValue().size()) {
          lifecycleJson.append(",");
        }
        lifecycleJson.append("\n");
      }
      lifecycleJson.append("    ]");
      firstPhase = false;
    }
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
          "lifecycle": {
%s
          },
          "permissions": %s,
          "storage": {
            "config": %s,
            "data": %s,
            "cache": %s,
            "generated": %s
          }
        }
        """
        .formatted(
            modId,
            lifecycleJson,
            toJsonArray(permissions),
            Boolean.toString(config),
            Boolean.toString(data),
            Boolean.toString(cache),
            Boolean.toString(generated));
  }

  private Map<String, JsonObject> grantsByCapability(JsonArray grants) {
    java.util.LinkedHashMap<String, JsonObject> values = new java.util.LinkedHashMap<>();
    for (var element : grants) {
      JsonObject grant = element.getAsJsonObject();
      values.put(grant.get("capability").getAsString(), grant);
    }
    return Map.copyOf(values);
  }

  private void assertGrant(JsonObject grant, String state, List<String> sources) {
    assertEquals(state, grant.get("state").getAsString());
    assertEquals(sources, toStringList(grant.getAsJsonArray("sources")));
  }

  private void assertSummary(
      JsonObject summary, int granted, int denied, int unavailable, int unknown, int visibilityOnly) {
    assertEquals(granted, summary.get("granted").getAsInt());
    assertEquals(denied, summary.get("denied").getAsInt());
    assertEquals(unavailable, summary.get("unavailable").getAsInt());
    assertEquals(unknown, summary.get("unknown").getAsInt());
    assertEquals(visibilityOnly, summary.get("visibilityOnly").getAsInt());
  }

  private List<String> toStringList(JsonArray array) {
    List<String> values = new ArrayList<>();
    for (var element : array) {
      values.add(element.getAsString());
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
