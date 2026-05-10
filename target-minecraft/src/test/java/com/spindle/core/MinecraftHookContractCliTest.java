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

class MinecraftHookContractCliTest {
  @TempDir Path tempDirectory;

  @Test
  void minecraftHookContractsWritesTargetTwoReportWithoutLaunching() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            createFakeServerJar(tempDirectory.resolve("fake-server.jar")));

    execute(
        tempDirectory,
        new String[] {
          "--game-main",
          "unused.for.minecraft.Target2",
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
          "--minecraft-offline",
          "--minecraft-hook-contracts"
        });

    Path interpretationPath = tempDirectory.resolve("minecraft-artifact-interpretation.json");
    Path hookReportPath = tempDirectory.resolve("minecraft-hook-contracts.json");
    assertTrue(Files.exists(interpretationPath));
    assertTrue(Files.exists(hookReportPath));
    String hookReport = Files.readString(hookReportPath, StandardCharsets.UTF_8);
    assertTrue(hookReport.contains("\"milestoneName\": \"Target-2\""));
    assertTrue(hookReport.contains("\"analysisOnly\": true"));
    assertTrue(hookReport.contains("\"hookInstallationOccurred\": false"));
    assertTrue(hookReport.contains("\"contractCount\": 0"));
    assertTrue(hookReport.contains("target-2.no_contracts_declared"));
    assertFalse(Files.exists(tempDirectory.resolve("minecraft-server-launch-result.json")));
  }

  @Test
  void minecraftHookContractsRejectsClientSide() throws Exception {
    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                execute(
                    tempDirectory,
                    new String[] {
                      "--game-main",
                      "unused.for.minecraft.Target2Client",
                      "--game-provider",
                      "minecraft",
                      "--minecraft-version",
                      "26.1.2",
                      "--minecraft-side",
                      "client",
                      "--minecraft-dry-run",
                      "--minecraft-hook-contracts"
                    }));

    assertTrue(exception.getMessage().contains("server-side"));
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

  private Path createFakeServerJar(Path jarPath) throws IOException {
    Files.createDirectories(jarPath.getParent());
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    manifest
        .getMainAttributes()
        .put(Attributes.Name.MAIN_CLASS, "com.spindle.sampleserverfixture.FakeMinecraftServerMain");
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jar = new JarOutputStream(outputStream, manifest)) {
      jar.putNextEntry(
          new JarEntry("com/spindle/sampleserverfixture/FakeMinecraftServerMain.class"));
      jar.write(readResourceBytes("com/spindle/sampleserverfixture/FakeMinecraftServerMain.class"));
      jar.closeEntry();
    }
    return jarPath;
  }

  private byte[] readResourceBytes(String resourceName) throws IOException {
    try (var inputStream =
        MinecraftHookContractCliTest.class.getClassLoader().getResourceAsStream(resourceName)) {
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

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
