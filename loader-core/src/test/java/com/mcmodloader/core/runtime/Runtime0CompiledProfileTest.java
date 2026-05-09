package com.mcmodloader.core.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mcmodloader.core.LoaderMain;
import com.mcmodloader.core.app.LoaderApplication;
import com.mcmodloader.core.cli.LaunchArguments;
import com.mcmodloader.core.diagnostics.JsonDiagnosticSink;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Runtime0CompiledProfileTest {
  private static final Pattern ISO_TIMESTAMP_PATTERN =
      Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");

  @TempDir Path tempDirectory;

  @Test
  void compiledProfileIsWrittenDuringValidateOnlyRun() throws Exception {
    createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod");

    executeValidateOnly();

    Path compiledProfilePath = tempDirectory.resolve("mcml.compiled-profile.json");
    assertTrue(Files.exists(compiledProfilePath));

    String diagnostics = Files.readString(tempDirectory.resolve("diagnostics/startup-trace.json"));
    assertTrue(diagnostics.contains("\"name\": \"runtime.compiled_profile.write\""));
  }

  @Test
  void compiledProfileUsesSchemaVersionOneAndStableFingerprintShape() throws Exception {
    createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod");

    executeValidateOnly();

    JsonObject profile = readCompiledProfile();
    assertEquals(1, profile.get("schemaVersion").getAsInt());
    assertEquals("compiled-modpack", profile.get("profileKind").getAsString());
    assertEquals("mc-modloader", profile.getAsJsonObject("loader").get("id").getAsString());
    assertEquals("sample", profile.getAsJsonObject("game").get("id").getAsString());
    assertEquals("universal", profile.getAsJsonObject("game").get("side").getAsString());
    assertEquals("verify-or-write", profile.getAsJsonObject("lockfile").get("mode").getAsString());

    String fingerprint = profile.get("fingerprint").getAsString();
    assertNotNull(fingerprint);
    assertTrue(fingerprint.matches("[0-9a-f]{64}"));
    assertEquals(
        profile.getAsJsonObject("lockfile").get("path").getAsString(), "loader.lock.json");
  }

  @Test
  void compiledProfileIsDeterministicAcrossEquivalentRuns() throws Exception {
    createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod");

    executeValidateOnly();
    String first = Files.readString(tempDirectory.resolve("mcml.compiled-profile.json"));

    executeValidateOnly();
    String second = Files.readString(tempDirectory.resolve("mcml.compiled-profile.json"));

    assertEquals(first, second);
  }

  @Test
  void compiledProfileDoesNotContainTimestampsOrUnsafeAbsolutePaths() throws Exception {
    createStandardModJar(tempDirectory.resolve("mods/sample-mod.jar"), "samplemod");

    executeValidateOnly();

    String profile = Files.readString(tempDirectory.resolve("mcml.compiled-profile.json"));
    String normalizedTempPath = tempDirectory.toAbsolutePath().normalize().toString().replace('\\', '/');
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

    assertTrue(output.contains("[loader] validation complete"));
    assertFalse(diagnostics.contains("\"name\": \"entrypoint.invoke\""));
    assertFalse(diagnostics.contains("\"name\": \"game.launch\""));
    assertTrue(Files.exists(tempDirectory.resolve("mcml.compiled-profile.json")));
  }

  private JsonObject readCompiledProfile() throws IOException {
    return JsonParser.parseString(
            Files.readString(tempDirectory.resolve("mcml.compiled-profile.json"), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  private String executeValidateOnly() throws Exception {
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
