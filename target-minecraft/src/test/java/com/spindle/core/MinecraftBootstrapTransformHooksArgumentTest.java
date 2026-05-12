package com.spindle.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.MinecraftPlanFingerprint;
import com.spindle.core.minecraft.bootstrap.MinecraftBootstrapArguments;
import com.spindle.core.minecraft.bootstrap.MinecraftBootstrapPlanVerifier;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftBootstrapTransformHooksArgumentTest {
  @TempDir Path tempDirectory;

  @Test
  void parseSupportsTransformHooksPatchPlanAndExpectedFingerprint() throws Exception {
    MinecraftBootstrapArguments arguments =
        MinecraftBootstrapArguments.parse(
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
              "--transform-hooks",
              "--hook-patch-plan",
              "minecraft-hook-patch-plan.json",
              "--expected-hook-patch-plan-fingerprint",
              "abc123"
            });

    assertTrue(arguments.transformHooks());
    assertEquals("abc123", arguments.expectedHookPatchPlanFingerprint());
    assertEquals(
        tempDirectory.resolve("minecraft-hook-patch-plan.json").toAbsolutePath().normalize(),
        arguments.hookPatchPlanPath());
  }

  @Test
  void parseRequiresHookPatchPlanWhenTransformHooksEnabled() {
    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                MinecraftBootstrapArguments.parse(
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
                      "--transform-hooks"
                    }));

    assertTrue(exception.getMessage().contains("--hook-patch-plan"));
  }

  @Test
  void verifierAcceptsValidTargetSevenPatchPlanForBootstrapTransformHooks() throws Exception {
    prepareGeneratedPlans();
    MinecraftPlanFingerprint hookPatchFingerprint =
        MinecraftPlanFingerprint.fromFile(
            "hook-patch-plan", tempDirectory.resolve("minecraft-hook-patch-plan.json"));

    MinecraftBootstrapPlanVerifier.VerifiedPlans verifiedPlans =
        new MinecraftBootstrapPlanVerifier()
            .verify(bootstrapArguments(hookPatchFingerprint.sha256()));

    assertEquals(hookPatchFingerprint.sha256(), verifiedPlans.hookPatchPlanFingerprint().sha256());
    assertTrue(verifiedPlans.hookPatchPlan().has("plannedPatches"));
  }

  @Test
  void verifierRejectsHookPatchPlanFingerprintMismatch() throws Exception {
    prepareGeneratedPlans();

    MinecraftBootstrapPlanVerifier.PlanDriftException exception =
        assertThrows(
            MinecraftBootstrapPlanVerifier.PlanDriftException.class,
            () -> new MinecraftBootstrapPlanVerifier().verify(bootstrapArguments("not-the-sha")));

    assertTrue(exception.failure().details().contains("hook patch plan fingerprint mismatch"));
  }

  @Test
  void verifierRejectsNonFakeServerExecutionPolicy() throws Exception {
    prepareGeneratedPlans();
    Path executionPlanPath = tempDirectory.resolve("minecraft-mod-execution-plan.json");
    String original = Files.readString(executionPlanPath, StandardCharsets.UTF_8);
    Files.writeString(
        executionPlanPath,
        original.replace("\"bootstrapFakeServer\": true", "\"bootstrapFakeServer\": false"),
        StandardCharsets.UTF_8);
    String hookPatchFingerprint =
        MinecraftPlanFingerprint.fromFile(
                "hook-patch-plan", tempDirectory.resolve("minecraft-hook-patch-plan.json"))
            .sha256();

    MinecraftBootstrapPlanVerifier.PlanDriftException exception =
        assertThrows(
            MinecraftBootstrapPlanVerifier.PlanDriftException.class,
            () ->
                new MinecraftBootstrapPlanVerifier()
                    .verify(bootstrapArguments(hookPatchFingerprint)));

    assertTrue(
        exception
            .failure()
            .details()
            .contains("hook patch plan requires bootstrap fake server execution policy"));
  }

  private MinecraftBootstrapArguments bootstrapArguments(String hookPatchFingerprint)
      throws LoaderException {
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
          "--verify-plan-fingerprints",
          "--transform-hooks",
          "--hook-patch-plan",
          "minecraft-hook-patch-plan.json",
          "--expected-runtime-fingerprint",
          MinecraftPlanFingerprint.fromFile(
                  "runtime-plan", tempDirectory.resolve("minecraft-server-runtime-plan.json"))
              .sha256(),
          "--expected-boundary-fingerprint",
          MinecraftPlanFingerprint.fromFile(
                  "boundary-plan", tempDirectory.resolve("minecraft-runtime-boundary.json"))
              .sha256(),
          "--expected-integration-fingerprint",
          MinecraftPlanFingerprint.fromFile(
                  "integration-plan", tempDirectory.resolve("minecraft-mod-integration-plan.json"))
              .sha256(),
          "--expected-execution-fingerprint",
          MinecraftPlanFingerprint.fromFile(
                  "execution-plan", tempDirectory.resolve("minecraft-mod-execution-plan.json"))
              .sha256(),
          "--expected-hook-patch-plan-fingerprint",
          hookPatchFingerprint
        });
  }

  private void prepareGeneratedPlans() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createHookServerJar(tempDirectory.resolve("hook-server.jar")));
    execute(
        tempDirectory,
        new String[] {
          "--game-main",
          "unused.for.minecraft.Target9Args",
          "--game-provider",
          "minecraft",
          "--minecraft-version",
          "26.1.2",
          "--minecraft-dir",
          minecraftDir.toString(),
          "--minecraft-side",
          "server",
          "--minecraft-dry-run",
          "--minecraft-verify-files",
          "--minecraft-deny-loader-internals",
          "--minecraft-verify-plan-fingerprints",
          "--minecraft-bootstrap-transform-hooks",
          "--minecraft-bootstrap-fake-server"
        });
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

  private Path createHookServerJar(Path jarPath) throws IOException {
    Files.createDirectories(jarPath.getParent());
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "net.minecraft.server.Main");
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jar = new JarOutputStream(outputStream, manifest)) {
      jar.putNextEntry(new JarEntry("net/minecraft/server/Main.class"));
      jar.write(readResourceBytes("net/minecraft/server/Main.class"));
      jar.closeEntry();
    }
    return jarPath;
  }

  private byte[] readResourceBytes(String resourceName) throws IOException {
    try (var inputStream =
        MinecraftBootstrapTransformHooksArgumentTest.class
            .getClassLoader()
            .getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + resourceName);
      }
      return inputStream.readAllBytes();
    }
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
