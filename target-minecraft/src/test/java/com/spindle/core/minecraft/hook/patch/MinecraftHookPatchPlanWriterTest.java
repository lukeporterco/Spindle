package com.spindle.core.minecraft.hook.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftHookPatchPlanWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftHookPatchPlanWriter writer = new MinecraftHookPatchPlanWriter();

  @Test
  void writesDeterministicJson() throws Exception {
    MinecraftHookPatchPlan plan =
        new MinecraftHookPatchPlan(
            1,
            "Target-7",
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
            true,
            1,
            MinecraftHookPatchEligibility.FIXTURE_ONLY_FUTURE_TRANSFORM,
            "target-5.minecraft.server.main.method-entry-placement",
            1,
            "Target-6",
            "net/minecraft/server/Main",
            "main",
            "([Ljava/lang/String;)V",
            31,
            34,
            3,
            "abc123",
            0,
            true,
            0,
            "b8 ?? ??",
            List.of(
                new MinecraftPatchConstantPoolRequirement(
                    "Utf8", "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher")),
            true,
            true,
            false,
            false,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            false,
            true,
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
            new MinecraftPatchOffsetAdjustmentSummary(
                "branch-targets", 3, 1, List.of(1), List.of(12), true, "branch rule"),
            new MinecraftPatchOffsetAdjustmentSummary(
                "switch-targets", 3, 3, List.of(8), List.of(20, 24, 28), true, "switch rule"),
            new MinecraftPatchExceptionTableImpact(
                true, 1, 3, true, 3, List.of("entry[0].startPc 0->3")),
            new MinecraftPatchStackMapImpact(true, 2, true, "rewrite required"),
            new MinecraftPatchNestedAttributeImpact(
                4,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                List.of(
                    "LineNumberTable",
                    "LocalVariableTable",
                    "LocalVariableTypeTable",
                    "StackMapTable")),
            List.of(
                new MinecraftPlannedHookPatch(
                    "target-7.minecraft.server.main.method-entry-dispatch-patch",
                    "target-5.minecraft.server.main.method-entry-placement",
                    "minecraft.26_1_2.server.main.entrypoint",
                    "Target-6",
                    "minecraft-26.1.2-server-known-symbols",
                    MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH,
                    MinecraftHookPatchMode.DRY_RUN_STATIC_DISPATCH_INVOKESTATIC,
                    MinecraftHookPatchEligibility.FIXTURE_ONLY_FUTURE_TRANSFORM,
                    "net/minecraft/server/Main",
                    "main",
                    "([Ljava/lang/String;)V",
                    0,
                    true,
                    new MinecraftPatchCodeInsertion(
                        "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
                        "beforeMinecraftServerMain",
                        "()V",
                        "invokestatic",
                        "b8",
                        3,
                        0,
                        0,
                        "b8 ?? ??"),
                    List.of(
                        new MinecraftPatchConstantPoolRequirement(
                            "Utf8", "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher")),
                    true,
                    true,
                    false,
                    false,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    new MinecraftPatchOffsetAdjustmentSummary(
                        "branch-targets", 3, 1, List.of(1), List.of(12), true, "branch rule"),
                    new MinecraftPatchOffsetAdjustmentSummary(
                        "switch-targets",
                        3,
                        3,
                        List.of(8),
                        List.of(20, 24, 28),
                        true,
                        "switch rule"),
                    new MinecraftPatchExceptionTableImpact(
                        true, 1, 3, true, 3, List.of("entry[0].startPc 0->3")),
                    new MinecraftPatchStackMapImpact(true, 2, true, "rewrite required"),
                    new MinecraftPatchNestedAttributeImpact(
                        4,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        List.of(
                            "LineNumberTable",
                            "LocalVariableTable",
                            "LocalVariableTypeTable",
                            "StackMapTable")),
                    true,
                    false)));

    Path first = tempDirectory.resolve("one/minecraft-hook-patch-plan.json");
    Path second = tempDirectory.resolve("two/minecraft-hook-patch-plan.json");

    writer.write(first, plan);
    writer.write(second, plan);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-7\""));
    assertTrue(firstJson.contains("\"patchPlanningOccurred\": true"));
    assertTrue(firstJson.contains("\"insertedInstructionHex\": \"b8 ?? ??\""));
    assertTrue(firstJson.contains("\"patchEligibility\": \"fixture-only-future-transform\""));
    assertTrue(firstJson.contains("\"plannedOpcode\": \"invokestatic\""));
  }
}
