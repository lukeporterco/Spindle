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

class MinecraftResourceReloadSymbolAnalysisCliTest {
  @TempDir Path tempDirectory;

  @Test
  void minecraftResourceReloadSymbolAnalysisWritesRequiredUpstreamChain() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createHookServerJar(tempDirectory.resolve("hook-server.jar")));

    execute(
        tempDirectory,
        serverArgs(
            minecraftDir, "--minecraft-resource-reload-symbol-analysis", "--minecraft-offline"));

    assertTrue(Files.exists(tempDirectory.resolve("minecraft-server-runtime-plan.json")));
    assertTrue(Files.exists(tempDirectory.resolve("minecraft-artifact-interpretation.json")));
    assertTrue(Files.exists(tempDirectory.resolve("minecraft-hook-contracts.json")));
    assertTrue(Files.exists(tempDirectory.resolve("minecraft-server-lifecycle-bindings.json")));
    assertTrue(
        Files.exists(tempDirectory.resolve("minecraft-server-lifecycle-dispatch-plan.json")));
    assertTrue(Files.exists(tempDirectory.resolve("minecraft-resource-reload-analysis.json")));
    assertTrue(
        Files.exists(tempDirectory.resolve("minecraft-resource-reload-symbol-analysis.json")));
  }

  @Test
  void minecraftResourceReloadSymbolAnalysisDoesNotWriteCommandReportsOrHookReports()
      throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createHookServerJar(tempDirectory.resolve("hook-server.jar")));

    execute(
        tempDirectory,
        serverArgs(
            minecraftDir, "--minecraft-resource-reload-symbol-analysis", "--minecraft-offline"));

    assertFalse(
        Files.exists(tempDirectory.resolve("minecraft-command-registration-analysis.json")));
    assertFalse(
        Files.exists(tempDirectory.resolve("minecraft-command-dispatcher-symbol-analysis.json")));
    assertFalse(
        Files.exists(tempDirectory.resolve("minecraft-command-dispatcher-binding-analysis.json")));
    assertFalse(Files.exists(tempDirectory.resolve("minecraft-hook-placement-plan.json")));
    assertFalse(Files.exists(tempDirectory.resolve("minecraft-hook-bytecode-analysis.json")));
    assertFalse(Files.exists(tempDirectory.resolve("minecraft-hook-patch-plan.json")));
    assertFalse(Files.exists(tempDirectory.resolve("minecraft-hook-installation-plan.json")));
    assertFalse(
        Files.exists(tempDirectory.resolve("minecraft-hook-bootstrap-transformation-result.json")));
    assertFalse(Files.exists(tempDirectory.resolve("minecraft-steelhook-0.1-report.json")));
    assertFalse(Files.exists(tempDirectory.resolve("minecraft-server-bootstrap-result.json")));
    assertFalse(Files.exists(tempDirectory.resolve("minecraft-mod-execution-result.json")));
  }

  @Test
  void minecraftExplainResourceReloadSymbolAnalysisPrintsAnalysisOnlyExplanationAndStatusLine()
      throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createHookServerJar(tempDirectory.resolve("hook-server.jar")));

    String stdout =
        execute(
            tempDirectory,
            serverArgs(
                minecraftDir,
                "--minecraft-explain-resource-reload-symbol-analysis",
                "--minecraft-offline"));

    assertTrue(stdout.contains("Target-17 resource/reload symbol analysis is analysis-only."));
    assertTrue(
        stdout.contains(
            "It scans Target-1 interpreted metadata for resource/reload-like class, method, and field symbols."));
    assertTrue(
        stdout.contains(
            "It does not inspect bytecode instructions, mappings, decompiled source, live classes, resources, datapacks, generated JSON, registries, or command trees."));
    assertTrue(
        stdout.contains(
            "It does not perform reload handling, resource access, datapack access, data generation, registry mutation, public API exposure, runtime dispatch, hook installation, or transformation."));
    assertTrue(
        stdout.contains("No resource/reload symbol candidates were discovered.")
            || stdout.contains(
                "Selectable resource/reload symbol candidates were discovered for future binding strategy analysis.")
            || stdout.contains("Only rejected resource/reload symbol candidates were discovered.")
            || stdout.contains("Resource/reload symbol discovery gate failed:"));
  }

  @Test
  void minecraftResourceReloadSymbolAnalysisRejectsClientSide() {
    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                execute(
                    tempDirectory,
                    new String[] {
                      "--game-main",
                      "unused.for.minecraft.Target17Client",
                      "--game-provider",
                      "minecraft",
                      "--minecraft-version",
                      "26.1.2",
                      "--minecraft-side",
                      "client",
                      "--minecraft-dry-run",
                      "--minecraft-resource-reload-symbol-analysis"
                    }));

    assertTrue(exception.getMessage().contains("server-side"));
    assertFalse(
        Files.exists(tempDirectory.resolve("minecraft-resource-reload-symbol-analysis.json")));
  }

  @Test
  void preflightMegaMilestoneReportListIncludesResourceReloadSymbolAnalysis() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createHookServerJar(tempDirectory.resolve("hook-server.jar")));

    execute(
        tempDirectory,
        serverArgs(
            minecraftDir,
            "--minecraft-resource-reload-symbol-analysis",
            "--minecraft-preflight",
            "--minecraft-offline"));

    String preflight =
        Files.readString(tempDirectory.resolve("spindle.preflight.json"), StandardCharsets.UTF_8);
    assertTrue(preflight.contains("minecraft-resource-reload-symbol-analysis.json"));
  }

  private String[] serverArgs(Path minecraftDir, String... extraArgs) {
    java.util.List<String> args = new java.util.ArrayList<>();
    args.add("--game-main");
    args.add("unused.for.minecraft.Target17");
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
        MinecraftResourceReloadSymbolAnalysisCliTest.class
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
