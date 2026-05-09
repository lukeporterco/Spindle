package com.spindle.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spindle.core.app.LoaderApplication;
import com.spindle.core.cli.LaunchArguments;
import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.security.SecurityRuleId;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.AlphaLifecycle;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.BetaLifecycle;
import com.spindle.fixture.runtime.RuntimeLifecycleFixtures.InvalidLifecycleHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Runtime1LifecycleKernelTest {
  @TempDir Path tempDirectory;

  @Test
  void schemaTwoLifecycleHandlersExecuteInDeterministicPhaseAndModOrder() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/beta.jar"),
        "beta",
        Map.of(
            "BOOTSTRAP", List.of(BetaLifecycle.class.getName() + "::bootstrap"),
            "CONFIGURE", List.of(BetaLifecycle.class.getName() + "::configure")),
        Map.of(
            resourceName(BetaLifecycle.class), readClassBytes(BetaLifecycle.class)),
        true);
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/alpha.jar"),
        "alpha",
        Map.of(
            "BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap"),
            "PRE_SERVER_MAIN", List.of(AlphaLifecycle.class.getName() + "::preServerMain")),
        Map.of(
            resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true);

    execute(false);

    List<String> lifecycleLog =
        Files.readAllLines(tempDirectory.resolve("lifecycle.log"), StandardCharsets.UTF_8);
    assertEquals(
        List.of(
            "alpha|BOOTSTRAP",
            "beta|BOOTSTRAP",
            "beta|CONFIGURE",
            "alpha|PRE_SERVER_MAIN"),
        lifecycleLog);
    assertTrue(Files.exists(tempDirectory.resolve("generated/alpha/alpha.marker")));
    assertTrue(Files.exists(tempDirectory.resolve("generated/beta/beta.marker")));

    JsonObject report =
        JsonParser.parseString(
                Files.readString(
                    tempDirectory.resolve("spindle.lifecycle-report.json"), StandardCharsets.UTF_8))
            .getAsJsonObject();
    assertEquals("executed", report.get("state").getAsString());
    assertEquals(4, report.getAsJsonArray("plannedHandlers").size());
    assertEquals(4, report.getAsJsonArray("attemptedHandlers").size());
    assertEquals(4, report.getAsJsonArray("successfulHandlers").size());
    assertEquals(0, report.getAsJsonArray("failedHandlers").size());
    assertTrue(report.get("runtimePolicyFingerprint").getAsString().matches("[0-9a-f]{64}"));
    assertEquals("miss", readCompiledProfile().getAsJsonObject("cache").get("status").getAsString());
  }

  @Test
  void invalidLifecycleMethodSignatureFailsBeforeHandlerExecution() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/invalid.jar"),
        "invalidmod",
        Map.of("BOOTSTRAP", List.of(InvalidLifecycleHandler.class.getName() + "::bootstrap")),
        Map.of(
            resourceName(InvalidLifecycleHandler.class),
            readClassBytes(InvalidLifecycleHandler.class)),
        true);

    LoaderException exception = assertThrows(LoaderException.class, () -> execute(false));

    assertTrue(
        exception
            .getMessage()
            .contains(SecurityRuleId.SEC_LIFECYCLE_002.id()));
    assertTrue(
        exception
            .getMessage()
            .contains("public static void bootstrap(com.spindle.api.ModContext)"));
    assertFalse(Files.exists(tempDirectory.resolve("lifecycle.log")));
    JsonObject report =
        JsonParser.parseString(
                Files.readString(
                    tempDirectory.resolve("spindle.lifecycle-report.json"), StandardCharsets.UTF_8))
            .getAsJsonObject();
    assertEquals("planned", report.get("state").getAsString());
    assertEquals(1, report.getAsJsonArray("plannedHandlers").size());
    assertEquals(0, report.getAsJsonArray("attemptedHandlers").size());
  }

  @Test
  void protectedPackageDefinitionsProduceBlockedSecurityReportDuringValidateOnly() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/protected.jar"),
        "protectedmod",
        Map.of("BOOTSTRAP", List.of("com.spindle.core.Hijack::bootstrap")),
        Map.of("com/spindle/core/Hijack.class", new byte[] {1, 2, 3}),
        true);

    executeValidateOnly();

    assertTrue(Files.exists(tempDirectory.resolve("spindle.profile.json")));
    JsonObject securityReport =
        JsonParser.parseString(
                Files.readString(
                    tempDirectory.resolve("spindle.security-report.json"),
                    StandardCharsets.UTF_8))
            .getAsJsonObject();
    assertEquals("blocked", securityReport.get("state").getAsString());
    assertEquals(1, securityReport.get("fatalCount").getAsInt());
    assertEquals(
        SecurityRuleId.SEC_PACKAGE_001.id(),
        securityReport.getAsJsonArray("findings").get(0).getAsJsonObject().get("ruleId").getAsString());
  }

  @Test
  void profileCacheInvalidatesWhenModJarHashChanges() throws Exception {
    Path jarPath = tempDirectory.resolve("mods/samplemod.jar");
    createSchemaOneModJar(
        jarPath,
        "samplemod",
        Map.of("com/example/Entrypoint.class", new byte[] {1}));

    executeValidateOnly();
    JsonObject first = readCompiledProfile();

    createSchemaOneModJar(
        jarPath,
        "samplemod",
        Map.of("com/example/Entrypoint.class", new byte[] {2}));
    Files.delete(tempDirectory.resolve("spindle.lock.json"));
    executeValidateOnly();
    JsonObject second = readCompiledProfile();

    assertEquals("miss", first.getAsJsonObject("cache").get("status").getAsString());
    assertEquals("miss", second.getAsJsonObject("cache").get("status").getAsString());
    assertFalse(
        first.get("inputFingerprint").getAsString().equals(second.get("inputFingerprint").getAsString()));
  }

  @Test
  void requestedPermissionsAreRecordedInProfileWithoutRuntimeGranting() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/permitted.jar"),
        "permittedmod",
        Map.of("BOOTSTRAP", List.of(AlphaLifecycle.class.getName() + "::bootstrap")),
        Map.of(resourceName(AlphaLifecycle.class), readClassBytes(AlphaLifecycle.class)),
        true,
        List.of("filesystem.write", "network.outbound"));

    executeValidateOnly();

    JsonObject profile = readCompiledProfile();
    JsonObject permissions =
        profile.getAsJsonObject("permissions").getAsJsonArray("mods").get(0).getAsJsonObject();
    assertEquals("permittedmod", permissions.get("modId").getAsString());
    assertEquals(
        List.of("filesystem.write", "network.outbound"),
        permissions.getAsJsonArray("requested").asList().stream()
            .map(element -> element.getAsString())
            .toList());
    JsonObject qualityReport =
        JsonParser.parseString(
                Files.readString(
                    tempDirectory.resolve("spindle.quality-report.json"), StandardCharsets.UTF_8))
            .getAsJsonObject();
    assertEquals("early-deterministic-signal", qualityReport.get("scoreKind").getAsString());
    assertEquals("miss", profile.getAsJsonObject("cache").get("status").getAsString());
  }

  private JsonObject readCompiledProfile() throws IOException {
    return JsonParser.parseString(
            Files.readString(tempDirectory.resolve("spindle.profile.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private void createSchemaOneModJar(Path jarPath, String modId, Map<String, byte[]> entries)
      throws IOException {
    createModJar(
        jarPath,
        """
        {
          "schema": 1,
          "id": "%s",
          "version": "1.0.0",
          "side": "universal",
          "entrypoints": {
            "main": [
              "com.example.Entrypoint"
            ]
          },
          "depends": {
            "loader": ">=0.1.0",
            "java": ">=25",
            "minecraft": ">=26.1.2"
          }
        }
        """
            .formatted(modId),
        entries);
  }

  private void createSchemaTwoModJar(
      Path jarPath,
      String modId,
      Map<String, List<String>> lifecycle,
      Map<String, byte[]> entries,
      boolean storageEnabled)
      throws IOException {
    createSchemaTwoModJar(jarPath, modId, lifecycle, entries, storageEnabled, List.of());
  }

  private void createSchemaTwoModJar(
      Path jarPath,
      String modId,
      Map<String, List<String>> lifecycle,
      Map<String, byte[]> entries,
      boolean storageEnabled,
      List<String> permissions)
      throws IOException {
    createModJar(jarPath, schemaTwoMetadata(modId, lifecycle, storageEnabled, permissions), entries);
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
      boolean storageEnabled,
      List<String> permissions) {
    StringBuilder lifecycleJson = new StringBuilder();
    boolean firstPhase = true;
    for (Map.Entry<String, List<String>> entry :
        lifecycle.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
      if (!firstPhase) {
        lifecycleJson.append(",\n");
      }
      lifecycleJson
          .append("    \"")
          .append(entry.getKey())
          .append("\": [\n");
      for (int index = 0; index < entry.getValue().size(); index++) {
        lifecycleJson
            .append("      \"")
            .append(entry.getValue().get(index))
            .append("\"");
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
            Boolean.toString(storageEnabled),
            Boolean.toString(storageEnabled),
            Boolean.toString(storageEnabled),
            Boolean.toString(storageEnabled));
  }

  private String toJsonArray(List<String> values) {
    return values.stream().map(value -> "\"" + value + "\"").collect(java.util.stream.Collectors.joining(", ", "[", "]"));
  }

  private String execute(boolean validateOnly) throws Exception {
    JsonDiagnosticSink sink = new JsonDiagnosticSink(tempDirectory.resolve("diagnostics/startup-trace.json"));
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

  private String executeValidateOnly() throws Exception {
    return execute(true);
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
