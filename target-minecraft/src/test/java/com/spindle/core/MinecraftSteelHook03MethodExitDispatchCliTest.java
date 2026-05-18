package com.spindle.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher;
import com.spindle.core.minecraft.hook.steelhook.SteelHook02TestFixtures;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftSteelHook03MethodExitDispatchCliTest {
  @TempDir Path tempDirectory;

  @Test
  void steelHook03MethodExitStaticDispatchWritesChainAndTarget29Report() throws Exception {
    Path minecraftDir =
        createFixtureMinecraftDirectory(
            tempDirectory.resolve("minecraft"),
            SteelHook02TestFixtures.createRuntimeJar(
                tempDirectory.resolve("hook-server.jar"),
                SteelHook02TestFixtures.readResourceBytes("net/minecraft/server/Main.class")));
    SteelHookDispatcher.resetForBootstrap();

    String stdout =
        execute(
            tempDirectory,
            serverArgs(
                minecraftDir,
                "--minecraft-steelhook-0-3-method-exit-static-dispatch",
                "--minecraft-explain-steelhook-0-3-method-exit-static-dispatch",
                "--minecraft-offline"));

    assertTrue(Files.exists(tempDirectory.resolve("minecraft-steelhook-0-2-report.json")));
    assertTrue(
        Files.exists(
            tempDirectory.resolve("minecraft-steelhook-0-3-framed-method-foundation.json")));
    assertTrue(
        Files.exists(
            tempDirectory.resolve("minecraft-steelhook-0-3-method-exit-static-dispatch.json")));
    assertFalse(Files.exists(tempDirectory.resolve("minecraft-hook-installation-result.json")));
    assertFalse(Files.exists(tempDirectory.resolve("minecraft-server-bootstrap-result.json")));
    String report =
        Files.readString(
            tempDirectory.resolve("minecraft-steelhook-0-3-method-exit-static-dispatch.json"),
            StandardCharsets.UTF_8);
    assertTrue(report.contains("\"milestoneName\": \"Target-29\""));
    assertTrue(report.contains("\"primitiveKind\": \"METHOD_EXIT_STATIC_DISPATCH\""));
    assertTrue(report.contains("\"status\": \"method-exit-dispatch-ready\""));
    assertTrue(stdout.contains("Target-29 proves METHOD_EXIT_STATIC_DISPATCH offline."));
    assertTrue(stdout.contains("immediately before supported normal return opcodes"));
    assertTrue(stdout.contains("Runtime classloading remains disabled."));
    assertTrue(stdout.contains("Minecraft main was not invoked."));
    assertTrue(stdout.contains("The method-exit dispatcher was not executed."));
    assertTrue(stdout.contains("Java mod execution is not sandboxed."));
    assertEquals(0, SteelHookDispatcher.beforeMinecraftServerMainInvocationCount());
    assertEquals(0, SteelHookDispatcher.afterMinecraftServerMainInvocationCount());
  }

  @Test
  void target29CannotBeCombinedWithInstallHooks() {
    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                execute(
                    tempDirectory,
                    new String[] {
                      "--game-main",
                      "unused.for.minecraft.Target29",
                      "--game-provider",
                      "minecraft",
                      "--minecraft-version",
                      "26.1.2",
                      "--minecraft-side",
                      "server",
                      "--minecraft-dry-run",
                      "--minecraft-steelhook-0-3-method-exit-static-dispatch",
                      "--minecraft-install-hooks"
                    }));

    assertTrue(exception.getMessage().contains("--minecraft-install-hooks"));
  }

  @Test
  void target29CannotBeCombinedWithBootstrapServer() {
    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                execute(
                    tempDirectory,
                    new String[] {
                      "--game-main",
                      "unused.for.minecraft.Target29",
                      "--game-provider",
                      "minecraft",
                      "--minecraft-version",
                      "26.1.2",
                      "--minecraft-side",
                      "server",
                      "--minecraft-dry-run",
                      "--minecraft-steelhook-0-3-method-exit-static-dispatch",
                      "--minecraft-bootstrap-server"
                    }));

    assertTrue(exception.getMessage().contains("--minecraft-bootstrap-server"));
  }

  private String[] serverArgs(Path minecraftDir, String... extraArgs) {
    java.util.List<String> args = new java.util.ArrayList<>();
    args.add("--game-main");
    args.add("unused.for.minecraft.Target29");
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
    PrintStream original = System.out;
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (PrintStream capture = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
      System.setOut(capture);
      runnable.run();
    } finally {
      System.setOut(original);
    }
    return bytes.toString(StandardCharsets.UTF_8);
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
