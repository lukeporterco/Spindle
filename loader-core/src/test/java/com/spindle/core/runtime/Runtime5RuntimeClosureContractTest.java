package com.spindle.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spindle.core.app.LoaderApplication;
import com.spindle.core.cli.LaunchArguments;
import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.security.SecurityRuleId;
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

class Runtime5RuntimeClosureContractTest {
  @TempDir Path tempDirectory;

  @Test
  void compiledProfileIncludesRuntimeFiveClosureContract() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/runtime5.jar"),
        "runtime5mod",
        List.of(),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)));

    execute(true);

    JsonObject profile = readCompiledProfile();
    assertEquals(6, profile.get("schemaVersion").getAsInt());
    JsonObject runtimeClosure = profile.getAsJsonObject("runtimeClosure");
    assertNotNull(runtimeClosure);
    assertEquals(2, runtimeClosure.get("contractVersion").getAsInt());
    assertEquals("runtime-arc-closed", runtimeClosure.get("arcStatus").getAsString());
    assertEquals("spindle-runtime-core", runtimeClosure.get("scope").getAsString());
    assertEquals(
        "minecraft-as-target-not-foundation", runtimeClosure.get("targetModel").getAsString());
    assertEquals(
        "in-process-unrestricted-java",
        runtimeClosure.get("runtimeExecutionIsolationMode").getAsString());
    assertFalse(runtimeClosure.get("sandboxed").getAsBoolean());
    assertEquals("not-sandboxed", runtimeClosure.get("sandboxClaim").getAsString());
  }

  @Test
  void runtimeClosureListsImplementedUnavailableAndVisibilityOnlySurfaces() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/runtime5.jar"),
        "runtime5mod",
        List.of(),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)));

    execute(true);

    Map<String, JsonObject> surfaces =
        objectsById(readCompiledProfile().getAsJsonObject("runtimeClosure").getAsJsonArray("surfaces"));
    assertSurfaceState(surfaces, "compiled-profile", "implemented");
    assertSurfaceState(surfaces, "lifecycle-contract", "implemented");
    assertSurfaceState(surfaces, "mod-context", "implemented");
    assertSurfaceState(surfaces, "storage-directories", "implemented");
    assertSurfaceState(surfaces, "capability-grants", "implemented");
    assertSurfaceState(surfaces, "config-schema-runtime", "implemented");
    assertSurfaceState(surfaces, "deterministic-service-registry", "implemented");
    assertSurfaceState(surfaces, "resource.declare", "unavailable");
    assertSurfaceState(surfaces, "resource.overlay", "unavailable");
    assertSurfaceState(surfaces, "network.outbound", "visibility-only");
    assertSurfaceState(surfaces, "process.spawn", "visibility-only");
    assertSurfaceState(surfaces, "native.load", "visibility-only");
    assertSurfaceState(surfaces, "reflection.deep", "visibility-only");
    assertSurfaceState(surfaces, "unsafe.access", "visibility-only");
  }

  @Test
  void runtimeClosureRecordsGateOrderBeforeClassloading() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/runtime5.jar"),
        "runtime5mod",
        List.of(),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)));

    execute(true);

    JsonArray gates = readCompiledProfile().getAsJsonObject("runtimeClosure").getAsJsonArray("gates");
    assertEquals(
        List.of(
            "security-gate",
            "runtime-config-contract-gate",
            "runtime-service-contract-gate",
            "mod-classloader-create",
            "lifecycle-execute",
            "game-launch"),
        ids(gates));
    assertTrue(gates.get(0).getAsJsonObject().get("beforeClassloading").getAsBoolean());
    assertTrue(gates.get(1).getAsJsonObject().get("beforeClassloading").getAsBoolean());
    assertTrue(gates.get(2).getAsJsonObject().get("beforeClassloading").getAsBoolean());
    assertFalse(gates.get(3).getAsJsonObject().get("beforeClassloading").getAsBoolean());
    assertTrue(
        gates.get(3)
            .getAsJsonObject()
            .get("note")
            .getAsString()
            .contains("classloading boundary"));
  }

  @Test
  void runtimeClosureRecordsLoaderApiBoundaryWithoutSealingApi() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/runtime5.jar"),
        "runtime5mod",
        List.of(),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)));

    execute(true);

    JsonObject boundary =
        readCompiledProfile().getAsJsonObject("runtimeClosure").getAsJsonObject("loaderApiBoundary");
    assertEquals("runtime-api-stabilized", boundary.get("status").getAsString());
    assertEquals("Minecraft Target Arc", boundary.get("nextArc").getAsString());
    assertEquals(
        List.of(
            "com.spindle.api.LoaderApi",
            "com.spindle.api.ModContext",
            "com.spindle.api.ModInitializer",
            "com.spindle.api.config.ModConfig",
            "com.spindle.api.exception.CapabilityDeniedException",
            "com.spindle.api.exception.ConfigAccessException",
            "com.spindle.api.exception.ServiceAccessException",
            "com.spindle.api.exception.SpindleApiException",
            "com.spindle.api.lifecycle.LifecyclePhase",
            "com.spindle.api.service.ServiceRegistry"),
        toStringList(boundary.getAsJsonArray("stableCandidates")));
    assertEquals(
        List.of(
            "com.spindle.api.minecraft.MinecraftServerModContext",
            "com.spindle.api.minecraft.MinecraftServerModInitializer"),
        toStringList(boundary.getAsJsonArray("deferredReview")));
  }

  @Test
  void resourceCapabilitiesRemainUnavailable() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/runtime5.jar"),
        "runtime5mod",
        List.of("resource.declare", "resource.overlay"),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)));

    execute(true);

    JsonObject profile = readCompiledProfile();
    JsonObject modPermissions =
        profile.getAsJsonObject("permissions").getAsJsonArray("mods").get(0).getAsJsonObject();
    Map<String, JsonObject> profileGrants = grantsByCapability(modPermissions.getAsJsonArray("grants"));
    assertEquals("unavailable", profileGrants.get("resource.declare").get("state").getAsString());
    assertEquals("unavailable", profileGrants.get("resource.overlay").get("state").getAsString());
    assertEquals(2, profile.getAsJsonObject("permissions").get("catalogVersion").getAsInt());

    JsonObject report = readSecurityReport();
    JsonObject reportPermissions =
        report.getAsJsonObject("capabilityGrants").getAsJsonArray("mods").get(0).getAsJsonObject();
    Map<String, JsonObject> reportGrants = grantsByCapability(reportPermissions.getAsJsonArray("grants"));
    assertEquals("unavailable", reportGrants.get("resource.declare").get("state").getAsString());
    assertEquals("unavailable", reportGrants.get("resource.overlay").get("state").getAsString());
    assertTrue(ruleIds(report).contains(SecurityRuleId.SEC_PERM_001.id()));
    assertEquals(0, report.get("fatalCount").getAsInt());
  }

  @Test
  void runtimeFiveSchemaInvalidatesSchemaFiveCache() throws Exception {
    createSchemaTwoModJar(
        tempDirectory.resolve("mods/runtime5.jar"),
        "runtime5mod",
        List.of(),
        Map.of(
            resourceName(SecurityReportAwareLifecycle.class),
            readClassBytes(SecurityReportAwareLifecycle.class)));

    execute(true);

    JsonObject cachedProfile =
        JsonParser.parseString(Files.readString(cachedProfilePath(), StandardCharsets.UTF_8))
            .getAsJsonObject();
    cachedProfile.addProperty("schemaVersion", 5);
    cachedProfile.remove("runtimeClosure");
    Files.writeString(cachedProfilePath(), cachedProfile.toString(), StandardCharsets.UTF_8);

    execute(true);

    assertEquals(
        "schema mismatch",
        readCompiledProfile().getAsJsonObject("cache").get("reason").getAsString());
  }

  private void createSchemaTwoModJar(
      Path jarPath, String modId, List<String> permissions, Map<String, byte[]> entries)
      throws IOException {
    createModJar(jarPath, schemaTwoMetadata(modId, permissions), entries);
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

  private String schemaTwoMetadata(String modId, List<String> permissions) {
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
            "BOOTSTRAP": [
              "%s::bootstrap"
            ]
          },
          "permissions": %s,
          "storage": {
            "config": false,
            "data": false,
            "cache": false,
            "generated": false
          },
          "services": {
            "provides": [],
            "consumes": []
          }
        }
        """
        .formatted(modId, SecurityReportAwareLifecycle.class.getName(), toJsonArray(permissions));
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

  private Map<String, JsonObject> objectsById(JsonArray array) {
    java.util.LinkedHashMap<String, JsonObject> values = new java.util.LinkedHashMap<>();
    for (var element : array) {
      JsonObject object = element.getAsJsonObject();
      values.put(object.get("id").getAsString(), object);
    }
    return Map.copyOf(values);
  }

  private Map<String, JsonObject> grantsByCapability(JsonArray grants) {
    java.util.LinkedHashMap<String, JsonObject> values = new java.util.LinkedHashMap<>();
    for (var element : grants) {
      JsonObject grant = element.getAsJsonObject();
      values.put(grant.get("capability").getAsString(), grant);
    }
    return Map.copyOf(values);
  }

  private void assertSurfaceState(Map<String, JsonObject> surfaces, String id, String state) {
    assertEquals(state, surfaces.get(id).get("state").getAsString());
  }

  private List<String> ids(JsonArray array) {
    List<String> values = new ArrayList<>();
    for (var element : array) {
      values.add(element.getAsJsonObject().get("id").getAsString());
    }
    return values;
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
