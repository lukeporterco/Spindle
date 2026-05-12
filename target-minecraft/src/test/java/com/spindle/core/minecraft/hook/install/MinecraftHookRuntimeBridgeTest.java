package com.spindle.core.minecraft.hook.install;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftModExecutionPlan;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftHookRuntimeBridgeTest {
  @TempDir Path tempDirectory;

  private final MinecraftHookRuntimeBridge bridge = new MinecraftHookRuntimeBridge();

  @Test
  void validBridgeInvokesMinecraftMain() throws Exception {
    Path markerPath = tempDirectory.resolve("main.marker");
    MinecraftHookInstallationResult result =
        bridge.installAndInvoke(
            validPlan(),
            executionPlan("net.minecraft.server.Main", List.of(markerPath.toString())),
            getClass().getClassLoader());

    assertTrue(Files.exists(markerPath));
    assertEquals("hook-installed-main", Files.readString(markerPath, StandardCharsets.UTF_8));
    assertEquals(MinecraftHookInstallationStatus.SUCCESS, result.status());
    assertTrue(result.hookInstallationOccurred());
    assertTrue(result.hookInvocationOccurred());
    assertTrue(result.minecraftMainClassLoaded());
    assertTrue(result.minecraftMainInvoked());
    assertEquals(1, result.installedHookCount());
    assertEquals(1, result.invokedHookCount());
    assertEquals(0, result.failedHookCount());
  }

  @Test
  void failedGateFailsBeforeMinecraftMainClassLoading() {
    Path markerPath = tempDirectory.resolve("gate.marker");
    MinecraftHookRuntimeBridge.HookInstallationException exception =
        assertThrows(
            MinecraftHookRuntimeBridge.HookInstallationException.class,
            () ->
                bridge.installAndInvoke(
                    new MinecraftHookInstallationPlan(
                        1,
                        "Target-4",
                        "minecraft",
                        "26.1.2",
                        "server",
                        "minecraft-26.1.2-server-known-symbols",
                        true,
                        0,
                        "net.minecraft.server.Main",
                        false,
                        "gate failed",
                        false,
                        MinecraftHookInstallationMode.LAUNCH_BOUNDARY_MAIN_WRAPPER,
                        0,
                        List.of(),
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false),
                    executionPlan("net.minecraft.server.Main", List.of(markerPath.toString())),
                    getClass().getClassLoader()));

    assertFalse(Files.exists(markerPath));
    assertFalse(exception.result().minecraftMainClassLoaded());
    assertFalse(exception.result().minecraftMainInvoked());
  }

  @Test
  void mainClassMismatchFailsBeforeMinecraftMainClassLoading() {
    Path markerPath = tempDirectory.resolve("mismatch.marker");
    MinecraftHookRuntimeBridge.HookInstallationException exception =
        assertThrows(
            MinecraftHookRuntimeBridge.HookInstallationException.class,
            () ->
                bridge.installAndInvoke(
                    validPlan(),
                    executionPlan("com.example.NotMinecraftMain", List.of(markerPath.toString())),
                    getClass().getClassLoader()));

    assertFalse(Files.exists(markerPath));
    assertFalse(exception.result().minecraftMainClassLoaded());
    assertTrue(exception.result().failureMessage().contains("main class mismatch"));
  }

  private MinecraftHookInstallationPlan validPlan() {
    return new MinecraftHookInstallationPlan(
        1,
        "Target-4",
        "minecraft",
        "26.1.2",
        "server",
        "minecraft-26.1.2-server-known-symbols",
        true,
        0,
        "net.minecraft.server.Main",
        true,
        null,
        true,
        MinecraftHookInstallationMode.LAUNCH_BOUNDARY_MAIN_WRAPPER,
        1,
        List.of(
            new MinecraftPlannedHookInstallation(
                "target-4.minecraft.server.main.launch-boundary",
                "minecraft.26_1_2.server.main.entrypoint",
                "minecraft-26.1.2-server-known-symbols",
                "LAUNCH_BOUNDARY_MAIN",
                "net/minecraft/server/Main",
                "main",
                "([Ljava/lang/String;)V",
                true,
                MinecraftHookInstallationMode.LAUNCH_BOUNDARY_MAIN_WRAPPER)),
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false);
  }

  private MinecraftModExecutionPlan executionPlan(String mainClass, List<String> mainArgs) {
    return new MinecraftModExecutionPlan(
        1,
        "Milestone 8",
        "26.1.2",
        "25",
        "server",
        null,
        null,
        null,
        List.of(),
        List.of(),
        List.of(),
        null,
        mainClass,
        mainArgs,
        List.of(),
        null,
        null);
  }
}
