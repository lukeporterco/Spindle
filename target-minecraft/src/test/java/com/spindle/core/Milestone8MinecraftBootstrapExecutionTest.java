package com.spindle.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.bootstrap.MinecraftBootstrapArguments;
import com.spindle.core.minecraft.bootstrap.MinecraftBootstrapExitCode;
import com.spindle.core.minecraft.bootstrap.MinecraftBootstrapResult;
import com.spindle.core.minecraft.bootstrap.MinecraftBootstrapRunner;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Milestone8MinecraftBootstrapExecutionTest {
  private static final String RECORDING_ENTRYPOINT =
      "com.spindle.fixture.bootstrapmod.RecordingMinecraftServerEntrypoint";
  private static final String FAILING_ENTRYPOINT =
      "com.spindle.fixture.bootstrapmod.FailingMinecraftServerEntrypoint";
  private static final String CANARY_ENTRYPOINT =
      "com.spindle.fixture.bootstrapmod.CanaryMinecraftServerEntrypoint";

  @TempDir Path tempDirectory;

  @Test
  void executionPlanIsDeterministicAcrossRepeatedRuns() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createFixtureServerJar(tempDirectory.resolve("fake-server.jar")));
    createMinecraftServerModJar(
        tempDirectory.resolve("mods/accepted.jar"),
        "accepted",
        RECORDING_ENTRYPOINT,
        readClassBytes(RECORDING_ENTRYPOINT));

    execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-execution-plan"));
    String first =
        Files.readString(
            tempDirectory.resolve("minecraft-mod-execution-plan.json"), StandardCharsets.UTF_8);

    execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-execution-plan"));
    String second =
        Files.readString(
            tempDirectory.resolve("minecraft-mod-execution-plan.json"), StandardCharsets.UTF_8);

    assertEquals(first, second);
    assertTrue(first.contains("\"milestoneName\": \"Milestone 8\""));
    assertTrue(first.contains("\"minecraftModExecutionAllowed\": true"));
  }

  @Test
  void classloaderGraphReportIsDeterministicAcrossRepeatedRuns() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createFixtureServerJar(tempDirectory.resolve("fake-server.jar")));
    createMinecraftServerModJar(
        tempDirectory.resolve("mods/accepted.jar"),
        "accepted",
        RECORDING_ENTRYPOINT,
        readClassBytes(RECORDING_ENTRYPOINT));

    execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-bootstrap-classloader-graph"));
    String first =
        Files.readString(
            tempDirectory.resolve("minecraft-bootstrap-classloader-graph.json"),
            StandardCharsets.UTF_8);

    execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-bootstrap-classloader-graph"));
    String second =
        Files.readString(
            tempDirectory.resolve("minecraft-bootstrap-classloader-graph.json"),
            StandardCharsets.UTF_8);

    assertEquals(first, second);
    assertTrue(first.contains("\"modJarsOnMinecraftRuntimeClasspath\": false"));
  }

  @Test
  void canaryModProvesValidationDoesNotLoadClasses() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createFixtureServerJar(tempDirectory.resolve("fake-server.jar")));
    createMinecraftServerModJar(
        tempDirectory.resolve("mods/canary.jar"),
        "canary",
        CANARY_ENTRYPOINT,
        readClassBytes(CANARY_ENTRYPOINT));

    execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-preflight"));
    String preflight =
        Files.readString(tempDirectory.resolve("spindle.preflight.json"), StandardCharsets.UTF_8);
    assertTrue(preflight.contains("\"succeeded\": true"));
    assertFalse(preflight.contains("canary should never be loaded"));
    assertFalse(Files.exists(tempDirectory.resolve("mod-data/canary")));
  }

  @Test
  void bootstrapExecutionLoadsAcceptedModEntrypointAndInvokesFakeServerMainAfterward()
      throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createFixtureServerJar(tempDirectory.resolve("fake-server.jar")));
    createMinecraftServerModJar(
        tempDirectory.resolve("mods/accepted.jar"),
        "accepted",
        RECORDING_ENTRYPOINT,
        readClassBytes(RECORDING_ENTRYPOINT));

    execute(
        tempDirectory,
        serverArgs(
            minecraftDir,
            "--minecraft-bootstrap-server",
            "--minecraft-bootstrap-fake-server",
            "--minecraft-server-arg",
            "--bootstrap-marker",
            "--minecraft-server-arg",
            "fake-server-main.marker"));

    String executionResult =
        Files.readString(
            tempDirectory.resolve("minecraft-mod-execution-result.json"), StandardCharsets.UTF_8);
    String bootstrapResult =
        Files.readString(
            tempDirectory.resolve("minecraft-server-bootstrap-result.json"),
            StandardCharsets.UTF_8);
    String entrypointMarker =
        Files.readString(
            tempDirectory.resolve("mod-data/accepted/entrypoint.marker"), StandardCharsets.UTF_8);
    assertTrue(Files.exists(tempDirectory.resolve("fake-server-main.marker")));
    assertTrue(executionResult.contains("\"minecraftMainInvoked\": true"));
    assertTrue(bootstrapResult.contains("\"exitCode\": 0"));
    assertTrue(entrypointMarker.contains("loaderInternalsDenied=true"));
    assertTrue(
        entrypointMarker.contains(
            "apiVisible=com.spindle.api.minecraft.MinecraftServerModContext"));
  }

  @Test
  void malformedEntrypointIsRejectedBeforeClassLoading() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createFixtureServerJar(tempDirectory.resolve("fake-server.jar")));
    createModJar(
        tempDirectory.resolve("mods/malformed.jar"),
        metadataJson(
            "malformed",
            "minecraftServer",
            "not a class",
            Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"),
            "server"),
        Map.of());

    execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-execution-plan"));

    String plan =
        Files.readString(
            tempDirectory.resolve("minecraft-mod-execution-plan.json"), StandardCharsets.UTF_8);
    assertTrue(plan.contains("malformed"));
    assertTrue(plan.contains("malformed minecraftServer entrypoint declaration"));
    assertFalse(Files.exists(tempDirectory.resolve("mod-data/malformed")));
  }

  @Test
  void protectedPackageDefinitionIsRejected() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createFixtureServerJar(tempDirectory.resolve("fake-server.jar")));
    createModJar(
        tempDirectory.resolve("mods/protected.jar"),
        metadataJson(
            "protectedmod",
            "minecraftServer",
            "com.spindle.core.evil.BadEntrypoint",
            Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"),
            "server"),
        Map.of("com/spindle/core/evil/BadEntrypoint.class", classFileHeader(69)));

    execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-execution-plan"));

    String plan =
        Files.readString(
            tempDirectory.resolve("minecraft-mod-execution-plan.json"), StandardCharsets.UTF_8);
    String integrationPlan =
        Files.readString(
            tempDirectory.resolve("minecraft-mod-integration-plan.json"), StandardCharsets.UTF_8);
    assertTrue(plan.contains("protectedmod"));
    assertTrue(integrationPlan.contains("\"type\": \"mod-protected-package\""));
  }

  @Test
  void pathTraversalJarEntryIsRejected() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createFixtureServerJar(tempDirectory.resolve("fake-server.jar")));
    createModJar(
        tempDirectory.resolve("mods/traversal.jar"),
        metadataJson(
            "traversal",
            "minecraftServer",
            RECORDING_ENTRYPOINT,
            Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"),
            "server"),
        Map.of(
            resourceName(RECORDING_ENTRYPOINT),
            readClassBytes(RECORDING_ENTRYPOINT),
            "../escape.txt",
            "escape".getBytes(StandardCharsets.UTF_8)));

    execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-execution-plan"));

    String plan =
        Files.readString(
            tempDirectory.resolve("minecraft-mod-execution-plan.json"), StandardCharsets.UTF_8);
    String integrationPlan =
        Files.readString(
            tempDirectory.resolve("minecraft-mod-integration-plan.json"), StandardCharsets.UTF_8);
    assertTrue(plan.contains("traversal"));
    assertTrue(integrationPlan.contains("\"type\": \"mod-suspicious-path\""));
  }

  @Test
  void modJarHashDriftFailsBeforeClassLoading() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createFixtureServerJar(tempDirectory.resolve("fake-server.jar")));
    Path modJar = tempDirectory.resolve("mods/accepted.jar");
    createMinecraftServerModJar(
        modJar, "accepted", RECORDING_ENTRYPOINT, readClassBytes(RECORDING_ENTRYPOINT));

    execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-execution-plan"));
    createMinecraftServerModJar(
        modJar, "accepted", FAILING_ENTRYPOINT, readClassBytes(FAILING_ENTRYPOINT));

    MinecraftBootstrapResult result = new MinecraftBootstrapRunner().run(bootstrapArguments());
    assertEquals(MinecraftBootstrapExitCode.PLAN_DRIFT.code(), result.exitCode());
    assertTrue(Files.exists(tempDirectory.resolve("minecraft-plan-drift-report.json")));
    assertFalse(Files.exists(tempDirectory.resolve("mod-data/accepted/entrypoint.marker")));
  }

  @Test
  void runtimePlanDriftFailsBeforeClassLoading() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createFixtureServerJar(tempDirectory.resolve("fake-server.jar")));
    createMinecraftServerModJar(
        tempDirectory.resolve("mods/accepted.jar"),
        "accepted",
        RECORDING_ENTRYPOINT,
        readClassBytes(RECORDING_ENTRYPOINT));

    execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-execution-plan"));
    Files.writeString(
        minecraftDir.resolve("versions/26.1.2/26.1.2-server.jar"),
        "tampered",
        StandardCharsets.UTF_8);

    MinecraftBootstrapResult result = new MinecraftBootstrapRunner().run(bootstrapArguments());
    assertEquals(MinecraftBootstrapExitCode.PLAN_DRIFT.code(), result.exitCode());
    assertTrue(
        Files.readString(
                tempDirectory.resolve("minecraft-plan-drift-report.json"), StandardCharsets.UTF_8)
            .contains("runtime server jar hash drift"));
  }

  @Test
  void executionPlanDriftFailsBeforeClassLoading() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createFixtureServerJar(tempDirectory.resolve("fake-server.jar")));
    createMinecraftServerModJar(
        tempDirectory.resolve("mods/accepted.jar"),
        "accepted",
        RECORDING_ENTRYPOINT,
        readClassBytes(RECORDING_ENTRYPOINT));

    execute(tempDirectory, serverArgs(minecraftDir, "--minecraft-execution-plan"));
    Path executionPlanPath = tempDirectory.resolve("minecraft-mod-execution-plan.json");
    String original = Files.readString(executionPlanPath, StandardCharsets.UTF_8);
    String tampered = original.replaceFirst("\"modId\":\\s*\"accepted\"", "\"modId\": \"bogus\"");
    Files.writeString(executionPlanPath, tampered, StandardCharsets.UTF_8);

    MinecraftBootstrapResult result = new MinecraftBootstrapRunner().run(bootstrapArguments());
    assertEquals(MinecraftBootstrapExitCode.PLAN_DRIFT.code(), result.exitCode());
    assertTrue(
        Files.readString(
                tempDirectory.resolve("minecraft-plan-drift-report.json"), StandardCharsets.UTF_8)
            .contains("execution plan includes mod not accepted"));
  }

  @Test
  void entrypointFailureIsCapturedInResultReport() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createFixtureServerJar(tempDirectory.resolve("fake-server.jar")));
    createMinecraftServerModJar(
        tempDirectory.resolve("mods/failing.jar"),
        "failing",
        FAILING_ENTRYPOINT,
        readClassBytes(FAILING_ENTRYPOINT));

    assertThrows(
        LoaderException.class,
        () ->
            execute(
                tempDirectory,
                serverArgs(
                    minecraftDir,
                    "--minecraft-bootstrap-server",
                    "--minecraft-bootstrap-fake-server",
                    "--minecraft-server-arg",
                    "--bootstrap-marker",
                    "--minecraft-server-arg",
                    "fake-server-main.marker")));

    String executionResult =
        Files.readString(
            tempDirectory.resolve("minecraft-mod-execution-result.json"), StandardCharsets.UTF_8);
    assertTrue(executionResult.contains("entrypoint failed intentionally"));
    assertTrue(executionResult.contains("\"minecraftMainInvoked\": false"));
    assertFalse(Files.exists(tempDirectory.resolve("fake-server-main.marker")));
  }

  @Test
  void strictExecutionFailsOnWarningsPromotedToFatal() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createFixtureServerJar(tempDirectory.resolve("fake-server.jar")));
    createModJar(
        tempDirectory.resolve("mods/collision.jar"),
        metadataJson(
            "collision",
            "minecraftServer",
            "com.spindle.sampleserverfixture.CollisionEntrypoint",
            Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"),
            "server"),
        Map.of("com/spindle/sampleserverfixture/CollisionEntrypoint.class", classFileHeader(69)));

    assertThrows(
        LoaderException.class,
        () ->
            execute(
                tempDirectory,
                serverArgs(
                    minecraftDir, "--minecraft-execution-plan", "--minecraft-strict-execution")));
  }

  private MinecraftBootstrapArguments bootstrapArguments() throws LoaderException {
    return MinecraftBootstrapArguments.parse(
        new String[] {
          "--working-directory",
          tempDirectory.toString(),
          "--runtime-plan",
          "minecraft-server-runtime-plan.json",
          "--boundary-plan",
          "minecraft-runtime-boundary.json",
          "--integration-plan",
          "minecraft-mod-integration-plan.json",
          "--execution-plan",
          "minecraft-mod-execution-plan.json",
          "--verify-plan-fingerprints"
        });
  }

  private String[] serverArgs(Path minecraftDir, String... extraArgs) {
    java.util.List<String> args = new java.util.ArrayList<>();
    args.add("--game-main");
    args.add("unused.for.minecraft.Milestone8");
    args.add("--game-provider");
    args.add("minecraft");
    args.add("--minecraft-version");
    args.add("26.1.2");
    args.add("--minecraft-dir");
    args.add(minecraftDir.toString());
    args.add("--minecraft-side");
    args.add("server");
    args.add("--minecraft-dry-run");
    args.add("--minecraft-verify-files");
    args.add("--minecraft-deny-loader-internals");
    args.add("--minecraft-verify-plan-fingerprints");
    for (String extraArg : extraArgs) {
      args.add(extraArg);
    }
    return args.toArray(String[]::new);
  }

  private Path createFixtureMinecraftDirectory(Path minecraftDir, Path serverJar)
      throws IOException {
    Path versionDirectory = minecraftDir.resolve("versions/26.1.2");
    Files.createDirectories(versionDirectory);
    Files.writeString(
        versionDirectory.resolve("26.1.2.json"), versionJson(), StandardCharsets.UTF_8);
    Files.copy(serverJar, versionDirectory.resolve("26.1.2-server.jar"));
    return minecraftDir;
  }

  private Path createFixtureServerJar(Path jarPath) throws IOException {
    Files.createDirectories(jarPath.getParent());
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    manifest
        .getMainAttributes()
        .put(Attributes.Name.MAIN_CLASS, "com.spindle.sampleserverfixture.FakeMinecraftServerMain");
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jar = new JarOutputStream(outputStream, manifest)) {
      put(
          jar,
          "com/spindle/sampleserverfixture/FakeMinecraftServerMain.class",
          readClassBytes("com/spindle/sampleserverfixture/FakeMinecraftServerMain"));
    }
    return jarPath;
  }

  private void createMinecraftServerModJar(
      Path jarPath, String modId, String entrypointClassName, byte[] entrypointBytes)
      throws IOException {
    createModJar(
        jarPath,
        metadataJson(
            modId,
            "minecraftServer",
            entrypointClassName,
            Map.of("loader", ">=0.1.0", "java", ">=25", "minecraft", ">=26.1.2"),
            "server"),
        Map.of(resourceName(entrypointClassName), entrypointBytes));
  }

  private void createModJar(Path jarPath, String metadataJson, Map<String, byte[]> entries)
      throws IOException {
    Files.createDirectories(jarPath.getParent());
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jar = new JarOutputStream(outputStream)) {
      put(jar, "loader.mod.json", metadataJson.getBytes(StandardCharsets.UTF_8));
      for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
        put(jar, entry.getKey(), entry.getValue());
      }
    }
  }

  private String metadataJson(
      String modId,
      String entrypointKey,
      String entrypointClassName,
      Map<String, String> depends,
      String side) {
    return """
            {
              "schema": 1,
              "id": "%s",
              "version": "1.0.0",
              "side": "%s",
              "entrypoints": {
                "%s": [
                  "%s"
                ]
              },
              "depends": %s,
              "breaks": {}
            }
            """
        .formatted(modId, side, entrypointKey, entrypointClassName, toJsonObject(depends));
  }

  private String toJsonObject(Map<String, String> values) {
    StringBuilder builder = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<String, String> entry :
        values.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
      if (!first) {
        builder.append(", ");
      }
      builder
          .append("\"")
          .append(entry.getKey())
          .append("\": \"")
          .append(entry.getValue())
          .append("\"");
      first = false;
    }
    builder.append("}");
    return builder.toString();
  }

  private void put(JarOutputStream jar, String name, byte[] bytes) throws IOException {
    jar.putNextEntry(new JarEntry(name));
    jar.write(bytes);
    jar.closeEntry();
  }

  private byte[] classFileHeader(int majorVersion) {
    return new byte[] {
      (byte) 0xCA,
      (byte) 0xFE,
      (byte) 0xBA,
      (byte) 0xBE,
      0,
      0,
      (byte) ((majorVersion >> 8) & 0xFF),
      (byte) (majorVersion & 0xFF)
    };
  }

  private byte[] readClassBytes(String binaryName) throws IOException {
    try (var inputStream =
        getClass().getClassLoader().getResourceAsStream(resourceName(binaryName))) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + binaryName);
      }
      return inputStream.readAllBytes();
    }
  }

  private String resourceName(String binaryName) {
    return binaryName.replace('.', '/') + ".class";
  }

  private String execute(Path workingDirectory, String[] args) throws Exception {
    JsonDiagnosticSink sink =
        new JsonDiagnosticSink(workingDirectory.resolve("diagnostics/startup-trace.json"));
    try {
      return captureStdout(() -> LoaderMain.execute(workingDirectory, args, sink));
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

  private String versionJson() {
    return """
            {
              "id": "26.1.2",
              "type": "release",
              "downloads": {
                "server": {
                  "url": "https://example.invalid/server.jar",
                  "sha1": "server-sha1",
                  "size": 111
                }
              },
              "libraries": [],
              "arguments": {
                "game": [],
                "jvm": []
              }
            }
            """;
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
