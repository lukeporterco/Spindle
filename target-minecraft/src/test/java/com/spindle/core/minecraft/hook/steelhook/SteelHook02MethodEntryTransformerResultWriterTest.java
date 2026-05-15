package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook02MethodEntryTransformerResultWriterTest {
  @TempDir Path tempDir;

  @Test
  void writerCreatesDeterministicJsonWithoutSerializingRawBytes() throws Exception {
    SteelHook02MethodEntryTransformerResult result =
        new SteelHook02MethodEntryTransformerResult(
            1,
            "Target-25",
            "minecraft",
            "0.2",
            "Target-7",
            "Target-23",
            "Target-24",
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
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            SteelHook02MethodEntryTransformerStatus.TRANSFORMED,
            SteelHook02MethodEntryTransformerNextDirection
                .MOVE_TO_TARGET_26_GATED_REAL_RUNTIME_TRANSFORMATION,
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
            new SteelHook02MethodEntryTransformerGate(
                true, null, true, true, true, true, true, true, true, true, false, false),
            null,
            null,
            null,
            null,
            new SteelHook02TargetClassBytes(
                "net/minecraft/server/Main.class",
                "/tmp/main.jar",
                "server-jar",
                "class-sha",
                new byte[] {1, 2, 3},
                true,
                true,
                null),
            List.of(
                new SteelHook02MethodEntryTransformerFinding(
                    "finding-1",
                    "Target-24 handoff preserved",
                    SteelHook02MethodEntryTransformerFindingStatus.PASS,
                    true,
                    "Descriptor handoff is valid.",
                    "No drift observed.")));

    Path outputOne = tempDir.resolve("one/result.json");
    Path outputTwo = tempDir.resolve("two/result.json");
    SteelHook02MethodEntryTransformerResultWriter writer =
        new SteelHook02MethodEntryTransformerResultWriter();
    writer.write(outputOne, result);
    writer.write(outputTwo, result);

    String first = Files.readString(outputOne, StandardCharsets.UTF_8);
    String second = Files.readString(outputTwo, StandardCharsets.UTF_8);

    assertEquals(first, second);
    assertTrue(first.contains("\"milestoneName\": \"Target-25\""));
    assertTrue(first.contains("\"steelHookVersion\": \"0.2\""));
    assertTrue(first.contains("\"status\": \"TRANSFORMED\""));
    assertTrue(first.contains("\"failureReason\": null"));
    assertTrue(first.contains("\"insertedInstructionHex\": \"b8 00 0f\""));
    assertFalse(first.contains("\"classBytes\""));
  }
}
