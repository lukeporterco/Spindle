package com.spindle.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
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

class MinecraftSteelHookCompletionCliTest {
  @TempDir Path tempDirectory;

  @Test
  void steelHookCompletionCheckWritesTargetTenReportChain() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createHookServerJar(tempDirectory.resolve("hook-server.jar")));
    Path markerPath = tempDirectory.resolve("steelhook.marker");

    String stdout =
        execute(
            tempDirectory,
            serverArgs(
                minecraftDir,
                "--minecraft-steelhook-0-1-check",
                "--minecraft-explain-steelhook-0-1-check",
                "--minecraft-server-arg",
                markerPath.toString()));

    assertTrue(Files.exists(tempDirectory.resolve("minecraft-hook-contracts.json")));
    assertTrue(Files.exists(tempDirectory.resolve("minecraft-hook-placement-plan.json")));
    assertTrue(Files.exists(tempDirectory.resolve("minecraft-hook-bytecode-analysis.json")));
    assertTrue(Files.exists(tempDirectory.resolve("minecraft-hook-patch-plan.json")));
    assertTrue(
        Files.exists(tempDirectory.resolve("minecraft-hook-bootstrap-transformation-result.json")));
    assertTrue(Files.exists(tempDirectory.resolve("minecraft-server-bootstrap-result.json")));
    assertTrue(Files.exists(tempDirectory.resolve("minecraft-mod-execution-result.json")));
    assertTrue(Files.exists(tempDirectory.resolve("minecraft-steelhook-0.1-report.json")));
    assertFalse(Files.exists(tempDirectory.resolve("minecraft-hook-installation-result.json")));
    assertTrue(Files.exists(markerPath));
    assertTrue(stdout.contains("SteelHook 0.1 check: passed"));

    String report =
        Files.readString(
            tempDirectory.resolve("minecraft-steelhook-0.1-report.json"), StandardCharsets.UTF_8);
    assertTrue(report.contains("\"milestoneName\": \"Target-10\""));
    assertTrue(report.contains("\"steelHookVersion\": \"0.1\""));
    assertTrue(report.contains("\"status\": \"passed\""));
    assertTrue(report.contains("\"target-9-bootstrap-transform\""));
  }

  @Test
  void steelHookCompletionCheckCannotBeCombinedWithInstallHooks() {
    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                execute(
                    tempDirectory,
                    new String[] {
                      "--game-main",
                      "unused.for.minecraft.Target10",
                      "--game-provider",
                      "minecraft",
                      "--minecraft-version",
                      "26.1.2",
                      "--minecraft-side",
                      "server",
                      "--minecraft-dry-run",
                      "--minecraft-steelhook-0-1-check",
                      "--minecraft-install-hooks"
                    }));

    assertTrue(exception.getMessage().contains("--minecraft-install-hooks"));
    assertFalse(Files.exists(tempDirectory.resolve("minecraft-steelhook-0.1-report.json")));
  }

  @Test
  void plainBootstrapWithoutTargetTenFlagDoesNotWriteCompletionReport() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createHookServerJar(tempDirectory.resolve("plain-server.jar")));

    execute(
        tempDirectory,
        serverArgs(
            minecraftDir,
            "--minecraft-bootstrap-transform-hooks",
            "--minecraft-bootstrap-fake-server",
            "--minecraft-server-arg",
            tempDirectory.resolve("plain.marker").toString()));

    assertFalse(Files.exists(tempDirectory.resolve("minecraft-steelhook-0.1-report.json")));
  }

  private String[] serverArgs(Path minecraftDir, String... extraArgs) {
    java.util.List<String> args = new java.util.ArrayList<>();
    args.add("--game-main");
    args.add("unused.for.minecraft.Target10");
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
        MinecraftSteelHookCompletionCliTest.class
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
