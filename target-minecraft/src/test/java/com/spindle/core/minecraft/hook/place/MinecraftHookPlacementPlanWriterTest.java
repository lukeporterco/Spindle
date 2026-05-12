package com.spindle.core.minecraft.hook.place;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftHookPlacementPlanWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftHookPlacementPlanWriter writer = new MinecraftHookPlacementPlanWriter();

  @Test
  void writesDeterministicJson() throws Exception {
    MinecraftHookPlacementPlan plan =
        new MinecraftHookPlacementPlan(
            1,
            "Target-5",
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
            1,
            List.of(
                new MinecraftPlannedHookPlacement(
                    "target-5.minecraft.server.main.method-entry-placement",
                    "minecraft.26_1_2.server.main.entrypoint",
                    "minecraft-26.1.2-server-known-symbols",
                    MinecraftHookPlacementKind.METHOD_ENTRY,
                    "net/minecraft/server/Main",
                    "main",
                    "([Ljava/lang/String;)V",
                    0,
                    MinecraftHookPlacementMode.METHOD_ENTRY_ANALYSIS_ONLY,
                    true,
                    new MinecraftMethodCodeSummary(2, 1, 17, "abc123", 0, 1, true, false, 0))),
            true,
            true,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false);

    Path first = tempDirectory.resolve("one/minecraft-hook-placement-plan.json");
    Path second = tempDirectory.resolve("two/minecraft-hook-placement-plan.json");

    writer.write(first, plan);
    writer.write(second, plan);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"schema\": 1"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-5\""));
    assertTrue(firstJson.contains("\"placementPlanned\": true"));
    assertTrue(firstJson.contains("\"bytecodeOffset\": 0"));
    assertTrue(firstJson.contains("\"codeInspectionOccurred\": true"));
    assertTrue(firstJson.contains("\"instructionInspectionOccurred\": false"));
  }
}
