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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Runtime1CompiledRuntimeContractTest {
  private static final Pattern ISO_TIMESTAMP_PATTERN =
      Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");

  @TempDir Path tempDirectory;

  @Test
  void compiledProfileIsWrittenDuringValidateOnlyRun() throws Exception {
    createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod");

    executeValidateOnly();

    Path compiledProfilePath = tempDirectory.resolve("spindle.profile.json");
    assertTrue(Files.exists(compiledProfilePath));
    assertTrue(Files.exists(tempDirectory.resolve("spindle.lifecycle-report.json")));
    assertTrue(Files.exists(tempDirectory.resolve("spindle.quality-report.json")));
    assertTrue(Files.exists(tempDirectory.resolve("spindle.security-report.json")));

    String diagnostics = Files.readString(tempDirectory.resolve("diagnostics/startup-trace.json"));
    assertTrue(diagnostics.contains("\"name\": \"runtime.compiled_profile.write\""));
    assertTrue(diagnostics.contains("\"name\": \"runtime.compiled_profile.cache\""));
  }

  @Test
  void compiledProfileUsesSchemaVersionFiveAndRuntimeContractSections() throws Exception {
    createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod");

    executeValidateOnly();

    JsonObject profile = readCompiledProfile();
    assertEquals(5, profile.get("schemaVersion").getAsInt());
    assertEquals("compiled-modpack", profile.get("profileKind").getAsString());
    assertEquals("spindle", profile.getAsJsonObject("loader").get("id").getAsString());
    assertEquals("sample", profile.getAsJsonObject("game").get("id").getAsString());
    assertEquals("universal", profile.getAsJsonObject("game").get("side").getAsString());
    assertEquals("verify-or-write", profile.getAsJsonObject("lockfile").get("mode").getAsString());
    assertEquals("wrote", profile.getAsJsonObject("lockfile").get("action").getAsString());
    assertEquals("miss", profile.getAsJsonObject("cache").get("status").getAsString());
    assertEquals(1, profile.getAsJsonObject("permissions").get("catalogVersion").getAsInt());
    assertEquals(
        "spindle-api-only", profile.getAsJsonObject("permissions").get("scope").getAsString());
    assertEquals(
        List.of(1),
        profile.getAsJsonObject("metadata").getAsJsonArray("schemaVersions").asList().stream()
            .map(element -> element.getAsInt())
            .toList());
    assertEquals(
        List.of("BOOTSTRAP", "CONFIGURE", "PRE_SERVER_MAIN"),
        toStringList(profile.getAsJsonObject("lifecycle").getAsJsonArray("phaseOrder")));
    assertEquals(
        "config/samplemod",
        profile
            .getAsJsonObject("contexts")
            .getAsJsonArray("mods")
            .get(0)
            .getAsJsonObject()
            .get("configDirectory")
            .getAsString());
    assertEquals(100, profile.getAsJsonObject("quality").get("score").getAsInt());

    String fingerprint = profile.get("fingerprint").getAsString();
    String inputFingerprint = profile.get("inputFingerprint").getAsString();
    String runtimePolicyFingerprint = profile.get("runtimePolicyFingerprint").getAsString();
    assertNotNull(fingerprint);
    assertTrue(fingerprint.matches("[0-9a-f]{64}"));
    assertTrue(inputFingerprint.matches("[0-9a-f]{64}"));
    assertTrue(runtimePolicyFingerprint.matches("[0-9a-f]{64}"));
    assertEquals(
        profile.getAsJsonObject("lockfile").get("path").getAsString(), "spindle.lock.json");

    JsonObject lifecycleReport =
        JsonParser.parseString(
                Files.readString(
                    tempDirectory.resolve("spindle.lifecycle-report.json"), StandardCharsets.UTF_8))
            .getAsJsonObject();
    assertEquals("planned", lifecycleReport.get("state").getAsString());

    JsonObject qualityReport =
        JsonParser.parseString(
                Files.readString(
                    tempDirectory.resolve("spindle.quality-report.json"), StandardCharsets.UTF_8))
            .getAsJsonObject();
    assertEquals("early-deterministic-signal", qualityReport.get("scoreKind").getAsString());
  }

  @Test
  void compiledProfileReusesCacheOnEquivalentSecondRun() throws Exception {
    createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod");

    executeValidateOnly();
    JsonObject first = readCompiledProfile();

    executeValidateOnly();
    JsonObject second = readCompiledProfile();

    assertEquals(first.get("fingerprint").getAsString(), second.get("fingerprint").getAsString());
    assertEquals(
        first.get("inputFingerprint").getAsString(), second.get("inputFingerprint").getAsString());
    assertEquals("miss", first.getAsJsonObject("cache").get("status").getAsString());
    assertEquals("hit", second.getAsJsonObject("cache").get("status").getAsString());
    assertEquals("cache hit", second.getAsJsonObject("cache").get("reason").getAsString());
  }

  @Test
  void unreadableCachedProfileRebuildsWithSpecificMissReason() throws Exception {
    createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod");

    executeValidateOnly();
    JsonObject profile = readCompiledProfile();
    Files.writeString(cachedProfilePath(profile), "{ nope", StandardCharsets.UTF_8);

    executeValidateOnly();

    assertEquals(
        "unreadable profile",
        readCompiledProfile().getAsJsonObject("cache").get("reason").getAsString());
  }

  @Test
  void cachedProfileSchemaMismatchRebuilds() throws Exception {
    assertCacheMissReasonAfterMutation(
        root -> root.addProperty("schemaVersion", 1), "schema mismatch");
  }

  @Test
  void cachedProfileKindMismatchRebuilds() throws Exception {
    assertCacheMissReasonAfterMutation(
        root -> root.addProperty("profileKind", "wrong-kind"), "profile kind mismatch");
  }

  @Test
  void cachedProfileLoaderMismatchRebuilds() throws Exception {
    assertCacheMissReasonAfterMutation(
        root -> root.getAsJsonObject("loader").addProperty("version", "0.0.0"),
        "loader mismatch");
  }

  @Test
  void cachedProfileGameMismatchRebuilds() throws Exception {
    assertCacheMissReasonAfterMutation(
        root -> root.getAsJsonObject("game").addProperty("version", "0.0.0"), "game mismatch");
  }

  @Test
  void cachedProfileInputFingerprintMismatchRebuilds() throws Exception {
    assertCacheMissReasonAfterMutation(
        root ->
            root.addProperty(
                "inputFingerprint",
                "0000000000000000000000000000000000000000000000000000000000000000"),
        "input fingerprint mismatch");
  }

  @Test
  void cachedProfileFingerprintMismatchRebuilds() throws Exception {
    assertCacheMissReasonAfterMutation(
        root ->
            root.addProperty(
                "fingerprint",
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
        "profile fingerprint mismatch");
  }

  @Test
  void cachedProfileRuntimePolicyFingerprintMismatchRebuilds() throws Exception {
    assertCacheMissReasonAfterMutation(
        root ->
            root.addProperty(
                "runtimePolicyFingerprint",
                "1111111111111111111111111111111111111111111111111111111111111111"),
        "runtime policy fingerprint mismatch");
  }

  @Test
  void compiledProfileDistinguishesLockfileModeAndAction() throws Exception {
    createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod");

    executeValidateOnly();
    JsonObject first = readCompiledProfile();

    deleteProfileCacheDirectory(first);
    executeValidateOnly();
    JsonObject second = readCompiledProfile();

    assertEquals("verify-or-write", first.getAsJsonObject("lockfile").get("mode").getAsString());
    assertEquals("wrote", first.getAsJsonObject("lockfile").get("action").getAsString());
    assertEquals("verify-or-write", second.getAsJsonObject("lockfile").get("mode").getAsString());
    assertEquals("verified", second.getAsJsonObject("lockfile").get("action").getAsString());
  }

  @Test
  void compiledProfileDoesNotContainTimestampsOrUnsafeAbsolutePaths() throws Exception {
    createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod");

    executeValidateOnly();

    String profile = Files.readString(tempDirectory.resolve("spindle.profile.json"));
    String normalizedTempPath =
        tempDirectory.toAbsolutePath().normalize().toString().replace('\\', '/');
    assertFalse(profile.toLowerCase().contains("timestamp"));
    assertFalse(ISO_TIMESTAMP_PATTERN.matcher(profile).find());
    assertFalse(profile.contains(normalizedTempPath));
  }

  @Test
  void compiledProfileReflectsResolvedModOrderAndClasspathOwnership() throws Exception {
    createModJar(
        tempDirectory.resolve("mods/beta.jar"),
        metadataJson(
            "beta",
            "com.example.BetaEntrypoint",
            Map.of("alpha", ">=1.0.0", "java", ">=25", "loader", ">=0.1.0", "minecraft", ">=26.1.2"),
            Map.of()),
        Map.of("com/example/BetaEntrypoint.class", new byte[] {2}));
    createModJar(
        tempDirectory.resolve("mods/alpha.jar"),
        metadataJson(
            "alpha",
            "com.example.AlphaEntrypoint",
            Map.of("java", ">=25", "loader", ">=0.1.0", "minecraft", ">=26.1.2"),
            Map.of()),
        Map.of("com/example/AlphaEntrypoint.class", new byte[] {1}));

    executeValidateOnly();

    JsonObject profile = readCompiledProfile();
    JsonArray resolvedOrder = profile.getAsJsonArray("resolvedOrder");
    assertEquals(List.of("alpha", "beta"), toStringList(resolvedOrder));

    JsonArray classpath = profile.getAsJsonArray("classpath");
    assertEquals(2, classpath.size());
    assertEquals("mods/alpha.jar", classpath.get(0).getAsJsonObject().get("path").getAsString());
    assertEquals("alpha", classpath.get(0).getAsJsonObject().get("owner").getAsString());
    assertEquals("mods/beta.jar", classpath.get(1).getAsJsonObject().get("path").getAsString());
    assertEquals("beta", classpath.get(1).getAsJsonObject().get("owner").getAsString());
  }

  @Test
  void compiledProfileGenerationDoesNotTriggerSecondPlanningPass() throws Exception {
    createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod");

    executeValidateOnly();

    String diagnostics = Files.readString(tempDirectory.resolve("diagnostics/startup-trace.json"));
    assertEquals(1, countOccurrences(diagnostics, "\"name\": \"mod.discovery\""));
    assertEquals(1, countOccurrences(diagnostics, "\"name\": \"metadata.parse\""));
    assertEquals(1, countOccurrences(diagnostics, "\"name\": \"dependency.resolution\""));
    assertEquals(1, countOccurrences(diagnostics, "\"name\": \"runtime.compiled_profile.write\""));
  }

  @Test
  void validateOnlyBehaviorRemainsUnchangedWhenCompiledProfileIsWritten() throws Exception {
    createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod");

    String output = executeValidateOnly();
    String diagnostics = Files.readString(tempDirectory.resolve("diagnostics/startup-trace.json"));

    assertTrue(output.contains("[spindle] validation complete"));
    assertFalse(diagnostics.contains("\"name\": \"lifecycle.execute\""));
    assertFalse(diagnostics.contains("\"name\": \"game.launch\""));
    assertTrue(Files.exists(tempDirectory.resolve("spindle.profile.json")));
    assertTrue(Files.exists(tempDirectory.resolve("spindle.lifecycle-report.json")));
    assertTrue(Files.exists(tempDirectory.resolve("spindle.quality-report.json")));
    assertTrue(Files.exists(tempDirectory.resolve("spindle.security-report.json")));
  }

  private JsonObject readCompiledProfile() throws IOException {
    return JsonParser.parseString(
            Files.readString(tempDirectory.resolve("spindle.profile.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private void assertCacheMissReasonAfterMutation(
      Consumer<JsonObject> mutation, String expectedReason) throws Exception {
    createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod");

    executeValidateOnly();
    JsonObject profile = readCompiledProfile();
    Path cachePath = cachedProfilePath(profile);
    JsonObject cachedProfile =
        JsonParser.parseString(Files.readString(cachePath, StandardCharsets.UTF_8)).getAsJsonObject();
    mutation.accept(cachedProfile);
    Files.writeString(cachePath, cachedProfile.toString(), StandardCharsets.UTF_8);

    executeValidateOnly();

    JsonObject rebuilt = readCompiledProfile();
    assertEquals("miss", rebuilt.getAsJsonObject("cache").get("status").getAsString());
    assertEquals(expectedReason, rebuilt.getAsJsonObject("cache").get("reason").getAsString());
  }

  private Path cachedProfilePath(JsonObject profile) {
    return tempDirectory
        .resolve(".spindle")
        .resolve("profile-cache")
        .resolve(profile.get("inputFingerprint").getAsString())
        .resolve("spindle.profile.json");
  }

  private void deleteProfileCacheDirectory(JsonObject profile) throws IOException {
    try (var paths = Files.walk(cachedProfilePath(profile).getParent().getParent())) {
      paths.sorted(java.util.Comparator.reverseOrder()).forEach(this::deleteUnchecked);
    }
  }

  private void deleteUnchecked(Path path) {
    try {
      Files.delete(path);
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  private String executeValidateOnly() throws Exception {
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
                          true,
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

  private List<String> toStringList(JsonArray array) {
    return array.asList().stream().map(element -> element.getAsString()).toList();
  }

  private int countOccurrences(String text, String token) {
    int count = 0;
    int index = 0;
    while ((index = text.indexOf(token, index)) >= 0) {
      count++;
      index += token.length();
    }
    return count;
  }

  private void createStandardModJar(Path jarPath, String modId) throws IOException {
    createModJar(
        jarPath,
        metadataJson(
            modId,
            "com.example.Entrypoint",
            Map.of("java", ">=25", "loader", ">=0.1.0", "minecraft", ">=26.1.2"),
            Map.of()),
        Map.of("com/example/Entrypoint.class", new byte[] {1}));
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

  private String metadataJson(
      String modId,
      String entrypointClassName,
      Map<String, String> depends,
      Map<String, String> breaks) {
    return """
        {
          "schema": 1,
          "id": "%s",
          "version": "1.0.0",
          "side": "universal",
          "entrypoints": {
            "main": [
              "%s"
            ]
          },
          "depends": %s,
          "breaks": %s
        }
        """
        .formatted(
            modId,
            entrypointClassName,
            toJsonObject(depends),
            toJsonObject(breaks));
  }

  private String toJsonObject(Map<String, String> values) {
    StringBuilder builder = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<String, String> entry :
        values.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
      if (!first) {
        builder.append(", ");
      }
      builder.append("\"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\"");
      first = false;
    }
    builder.append("}");
    return builder.toString();
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }

  public static final class ValidationGameMain {
    public static void main(String[] args) {}
  }
}
