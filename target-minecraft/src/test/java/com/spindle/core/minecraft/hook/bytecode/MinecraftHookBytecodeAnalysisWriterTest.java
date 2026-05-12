package com.spindle.core.minecraft.hook.bytecode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftHookBytecodeAnalysisWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftHookBytecodeAnalysisWriter writer =
      new MinecraftHookBytecodeAnalysisWriter();

  @Test
  void writesDeterministicJson() throws Exception {
    MinecraftHookBytecodeAnalysisReport report =
        new MinecraftHookBytecodeAnalysisReport(
            1,
            "Target-6",
            "minecraft",
            "26.1.2",
            "server",
            "minecraft-26.1.2-server-known-symbols",
            true,
            0,
            "net.minecraft.server.Main",
            "target-5.minecraft.server.main.method-entry-placement",
            "minecraft.26_1_2.server.main.entrypoint",
            "net/minecraft/server/Main",
            "main",
            "([Ljava/lang/String;)V",
            0,
            true,
            null,
            true,
            true,
            true,
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
            2,
            0,
            1,
            2,
            "abc123",
            false,
            null,
            1,
            0,
            1,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            true,
            List.of(
                new MinecraftDecodedInstruction(
                    0,
                    0,
                    "nop",
                    1,
                    MinecraftDecodedInstructionKind.SIMPLE,
                    "",
                    List.of(),
                    null,
                    List.of(),
                    null),
                new MinecraftDecodedInstruction(
                    1,
                    177,
                    "return",
                    1,
                    MinecraftDecodedInstructionKind.RETURN,
                    "",
                    List.of(),
                    null,
                    List.of(),
                    null)),
            List.of(),
            List.of(new MinecraftCodeNestedAttributeSummary("LineNumberTable", 8, null)));

    Path first = tempDirectory.resolve("one/minecraft-hook-bytecode-analysis.json");
    Path second = tempDirectory.resolve("two/minecraft-hook-bytecode-analysis.json");

    writer.write(first, report);
    writer.write(second, report);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-6\""));
    assertTrue(firstJson.contains("\"instructionCount\": 2"));
    assertTrue(firstJson.contains("\"decodedInstructions\""));
    assertTrue(firstJson.contains("\"methodEntryInstructionBoundary\": true"));
  }
}
