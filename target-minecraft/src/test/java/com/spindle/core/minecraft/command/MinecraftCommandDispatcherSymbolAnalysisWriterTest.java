package com.spindle.core.minecraft.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftCommandDispatcherSymbolAnalysisWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftCommandDispatcherSymbolAnalysisWriter writer =
      new MinecraftCommandDispatcherSymbolAnalysisWriter();

  @Test
  void writerSerializesSchemaAndMilestone() throws Exception {
    MinecraftCommandDispatcherSymbolAnalysis analysis = stableAnalysis();
    Path first = tempDirectory.resolve("a/minecraft-command-dispatcher-symbol-analysis.json");
    Path second = tempDirectory.resolve("b/minecraft-command-dispatcher-symbol-analysis.json");

    writer.write(first, analysis);
    writer.write(second, analysis);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"schema\": 1"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-14\""));
    assertTrue(firstJson.contains("\"sourceCommandRegistrationAnalysisMilestone\": \"Target-13\""));
  }

  @Test
  void writerSerializesStableSelectedCandidate() {
    String json = writer.toJson(stableAnalysis()).toString();

    assertTrue(json.contains("\"selectionStatus\":\"STABLE_TARGET_SELECTED\""));
    assertTrue(
        json.contains(
            "\"selectedCandidateId\":\"target-14.minecraft.commands.dispatcher.candidate.001\""));
    assertTrue(json.contains("\"selected\":true"));
  }

  @Test
  void writerSerializesNoCandidateState() {
    String json = writer.toJson(noCandidateAnalysis()).toString();

    assertTrue(json.contains("\"selectionStatus\":\"NO_CANDIDATES\""));
    assertTrue(json.contains("\"minimalCommandRegistrationProofEligible\":false"));
    assertTrue(json.contains("\"selectedCandidateCount\":0"));
  }

  @Test
  void writerSerializesAmbiguousCandidateState() {
    String json = writer.toJson(ambiguousAnalysis()).toString();

    assertTrue(json.contains("\"selectionStatus\":\"AMBIGUOUS_CANDIDATES\""));
    assertTrue(json.contains("\"selectableCandidateCount\":2"));
    assertTrue(json.contains("\"selectedCandidateId\":null"));
  }

  @Test
  void writerSerializesNullsDeterministically() {
    String json = writer.toJson(noCandidateAnalysis()).toString();

    assertTrue(json.contains("\"selectedCandidateId\":null"));
    assertTrue(json.contains("\"gateFailureReason\":null"));
    assertTrue(json.contains("\"memberName\":null"));
    assertTrue(json.contains("\"descriptor\":null"));
  }

  @Test
  void writerDoesNotSerializePathsByteArraysOrCommandTreeContents() {
    String json = writer.toJson(stableAnalysis()).toString();

    assertFalse(json.contains("C:/"));
    assertFalse(json.contains("\\\\"));
    assertFalse(json.contains("bytes"));
    assertFalse(json.contains("bytecode"));
    assertFalse(json.contains("literal_command_tree"));
  }

  private MinecraftCommandDispatcherSymbolAnalysis stableAnalysis() {
    return new MinecraftCommandDispatcherSymbolAnalysis(
        1,
        "Target-14",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.command_registration",
        "Target-13",
        "minecraft.commands.dispatcher.discovery",
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
        null,
        "Lcom/mojang/brigadier/CommandDispatcher;",
        1,
        1,
        0,
        0,
        1,
        1,
        MinecraftCommandDispatcherSymbolSelectionStatus.STABLE_TARGET_SELECTED,
        true,
        "target-14.minecraft.commands.dispatcher.candidate.001",
        List.of(
            new MinecraftCommandDispatcherSymbolCandidate(
                "target-14.minecraft.commands.dispatcher.candidate.001",
                MinecraftCommandDispatcherSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE,
                "net/minecraft/commands/Commands",
                "buildDispatcher",
                "(Lcom/mojang/brigadier/CommandDispatcher;)V",
                true,
                List.of("PUBLIC", "STATIC"),
                true,
                true,
                null,
                null)));
  }

  private MinecraftCommandDispatcherSymbolAnalysis noCandidateAnalysis() {
    return new MinecraftCommandDispatcherSymbolAnalysis(
        1,
        "Target-14",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.command_registration",
        "Target-13",
        "minecraft.commands.dispatcher.discovery",
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
        null,
        "Lcom/mojang/brigadier/CommandDispatcher;",
        1,
        0,
        0,
        1,
        0,
        0,
        MinecraftCommandDispatcherSymbolSelectionStatus.NO_CANDIDATES,
        false,
        null,
        List.of(
            new MinecraftCommandDispatcherSymbolCandidate(
                "target-14.minecraft.commands.dispatcher.candidate.001",
                MinecraftCommandDispatcherSymbolCandidateKind.BRIGADIER_LIBRARY_CLASS,
                "com/mojang/brigadier/CommandDispatcher",
                null,
                null,
                false,
                List.of(),
                false,
                false,
                null,
                "Brigadier library class presence is metadata only.")));
  }

  private MinecraftCommandDispatcherSymbolAnalysis ambiguousAnalysis() {
    return new MinecraftCommandDispatcherSymbolAnalysis(
        1,
        "Target-14",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.command_registration",
        "Target-13",
        "minecraft.commands.dispatcher.discovery",
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
        null,
        "Lcom/mojang/brigadier/CommandDispatcher;",
        2,
        1,
        1,
        0,
        2,
        0,
        MinecraftCommandDispatcherSymbolSelectionStatus.AMBIGUOUS_CANDIDATES,
        false,
        null,
        List.of(
            new MinecraftCommandDispatcherSymbolCandidate(
                "target-14.minecraft.commands.dispatcher.candidate.001",
                MinecraftCommandDispatcherSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE,
                "net/minecraft/commands/Commands",
                "dispatcher",
                "Lcom/mojang/brigadier/CommandDispatcher;",
                false,
                List.of("PUBLIC"),
                true,
                false,
                null,
                null),
            new MinecraftCommandDispatcherSymbolCandidate(
                "target-14.minecraft.commands.dispatcher.candidate.002",
                MinecraftCommandDispatcherSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE,
                "net/minecraft/commands/Commands",
                "create",
                "(Lcom/mojang/brigadier/CommandDispatcher;)V",
                false,
                List.of("PUBLIC"),
                true,
                false,
                null,
                null)));
  }
}
