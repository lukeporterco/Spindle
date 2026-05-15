package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftClassLoadingAudit;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook02GatedRuntimeTransformationResultWriterTest {
  @TempDir Path tempDir;

  @Test
  void writerCreatesDeterministicJsonWithoutSerializingRawBytes() throws Exception {
    SteelHook02GatedRuntimeTransformationResult result =
        new SteelHook02GatedRuntimeTransformationResult(
            1,
            "Target-26",
            "minecraft",
            "0.2",
            "Target-7",
            "Target-23",
            "Target-24",
            "Target-25",
            true,
            true,
            true,
            true,
            true,
            MinecraftBootstrapHookTransformationMode
                .STEELHOOK_0_2_GATED_RUNTIME_METHOD_ENTRY_TRANSFORM,
            "net.minecraft.server.Main",
            "net/minecraft/server/Main.class",
            "net.minecraft.server.Main",
            true,
            true,
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
            false,
            false,
            true,
            true,
            true,
            SteelHook02GatedRuntimeTransformationStatus.TRANSFORMED_AND_DEFINED,
            SteelHook02GatedRuntimeTransformationNextDirection
                .MOVE_TO_TARGET_27_STEELHOOK_0_2_COMPLETION,
            null,
            "orig-class",
            "transformed-class",
            "orig-code",
            "transformed-code",
            1,
            4,
            10,
            16,
            15,
            "b8 00 0f",
            new MinecraftClassLoadingAudit.Summary(
                Map.of("minecraft-runtime-steelhook-0-2", 1),
                Map.of("minecraft-runtime-steelhook-0-2", 1),
                List.of()),
            new SteelHook02GatedRuntimeTransformationGate(
                true, null, true, true, true, true, true, true, true, true, true, true, true, false,
                false),
            List.of(
                new SteelHook02GatedRuntimeTransformationFinding(
                    "finding-1",
                    SteelHook02GatedRuntimeTransformationFindingStatus.PASS,
                    "Defined target class through the runtime classloader.")));

    Path outputOne = tempDir.resolve("one/result.json");
    Path outputTwo = tempDir.resolve("two/result.json");
    SteelHook02GatedRuntimeTransformationResultWriter writer =
        new SteelHook02GatedRuntimeTransformationResultWriter();
    writer.write(outputOne, result);
    writer.write(outputTwo, result);

    String first = Files.readString(outputOne, StandardCharsets.UTF_8);
    String second = Files.readString(outputTwo, StandardCharsets.UTF_8);

    assertEquals(first, second);
    assertTrue(first.contains("\"milestoneName\": \"Target-26\""));
    assertTrue(first.contains("\"steelHookVersion\": \"0.2\""));
    assertTrue(first.contains("\"status\": \"TRANSFORMED_AND_DEFINED\""));
    assertTrue(first.contains("\"failureReason\": null"));
    assertTrue(first.contains("\"insertedInstructionHex\": \"b8 00 0f\""));
    assertTrue(first.contains("\"classLoadingAuditSummary\""));
    assertFalse(first.contains("\"transformedClassBytes\""));
  }
}
