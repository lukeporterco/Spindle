package com.spindle.core.minecraft.hook.install;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftHookInstallationPlanWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftHookInstallationPlanWriter writer =
      new MinecraftHookInstallationPlanWriter();

  @Test
  void writesDeterministicJsonForTargetFourPlan() throws Exception {
    MinecraftHookInstallationPlan plan =
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

    Path first = tempDirectory.resolve("one/minecraft-hook-installation-plan.json");
    Path second = tempDirectory.resolve("two/minecraft-hook-installation-plan.json");

    writer.write(first, plan);
    writer.write(second, plan);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"schema\": 1"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-4\""));
    assertTrue(firstJson.contains("\"bytecodeModified\": false"));
    assertTrue(firstJson.contains("\"javaAgentUsed\": false"));
    assertTrue(firstJson.contains("\"publicApiExposed\": false"));
    assertTrue(firstJson.contains("\"javaModExecutionSandboxed\": false"));
  }
}
