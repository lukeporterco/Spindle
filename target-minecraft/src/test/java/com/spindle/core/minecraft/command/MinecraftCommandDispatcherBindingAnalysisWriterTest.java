package com.spindle.core.minecraft.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftCommandDispatcherBindingAnalysisWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftCommandDispatcherBindingAnalysisWriter writer =
      new MinecraftCommandDispatcherBindingAnalysisWriter();

  @Test
  void writerSerializesSchemaAndMilestoneDeterministicallyAcrossTwoPaths() throws Exception {
    MinecraftCommandDispatcherBindingAnalysis analysis = selectedMethodAnalysis();
    Path first = tempDirectory.resolve("a/minecraft-command-dispatcher-binding-analysis.json");
    Path second = tempDirectory.resolve("b/minecraft-command-dispatcher-binding-analysis.json");

    writer.write(first, analysis);
    writer.write(second, analysis);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"schema\": 1"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-15\""));
    assertTrue(
        firstJson.contains("\"sourceCommandDispatcherSymbolAnalysisMilestone\": \"Target-14\""));
  }

  @Test
  void writerSerializesSelectedMethodStrategy() {
    String json = writer.toJson(selectedMethodAnalysis()).toString();

    assertTrue(json.contains("\"bindingStatus\":\"SELECTED_SYMBOL_ANALYZED\""));
    assertTrue(json.contains("\"accessStrategy\":\"METHOD_DESCRIPTOR_REFERENCE_ONLY\""));
    assertTrue(json.contains("\"selectedCandidateKind\":\"METHOD_DESCRIPTOR_REFERENCE\""));
  }

  @Test
  void writerSerializesSelectedFieldStrategy() {
    String json = writer.toJson(selectedFieldAnalysis()).toString();

    assertTrue(json.contains("\"accessStrategy\":\"INSTANCE_FIELD_OWNER_CAPTURE_REQUIRED\""));
    assertTrue(json.contains("\"requiresFieldAccess\":true"));
    assertTrue(json.contains("\"selectedCandidateKind\":\"FIELD_DESCRIPTOR_REFERENCE\""));
  }

  @Test
  void writerSerializesBlockedNoAndAmbiguousStates() {
    String blocked = writer.toJson(blockedAnalysis()).toString();
    String noTarget = writer.toJson(noTargetAnalysis()).toString();
    String ambiguous = writer.toJson(ambiguousAnalysis()).toString();

    assertTrue(blocked.contains("\"bindingStatus\":\"UPSTREAM_GATE_BLOCKED\""));
    assertTrue(noTarget.contains("\"bindingStatus\":\"NO_SYMBOL_TARGET\""));
    assertTrue(ambiguous.contains("\"bindingStatus\":\"AMBIGUOUS_SYMBOL_TARGETS\""));
  }

  @Test
  void writerSerializesNulls() {
    String json = writer.toJson(noTargetAnalysis()).toString();

    assertTrue(json.contains("\"selectedCandidateId\":null"));
    assertTrue(json.contains("\"selectedCandidateKind\":null"));
    assertTrue(json.contains("\"ownerInternalName\":null"));
    assertTrue(json.contains("\"notes\":null"));
  }

  @Test
  void writerDoesNotSerializeByteArraysPathsCommandTreeContentsOrBrigadierNodes() {
    String json = writer.toJson(selectedMethodAnalysis()).toString();

    assertFalse(json.contains("C:/"));
    assertFalse(json.contains("\\\\"));
    assertFalse(json.contains("bytecode"));
    assertFalse(json.contains("CommandNode"));
    assertFalse(json.contains("classBytes"));
    assertFalse(json.contains("command_tree"));
  }

  private MinecraftCommandDispatcherBindingAnalysis selectedMethodAnalysis() {
    return new MinecraftCommandDispatcherBindingAnalysis(
        1,
        "Target-15",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.command_registration",
        "Target-14",
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
        "STABLE_TARGET_SELECTED",
        true,
        true,
        null,
        MinecraftCommandDispatcherBindingStatus.SELECTED_SYMBOL_ANALYZED,
        MinecraftCommandDispatcherAccessStrategy.METHOD_DESCRIPTOR_REFERENCE_ONLY,
        "target-14.minecraft.commands.dispatcher.candidate.001",
        "METHOD_DESCRIPTOR_REFERENCE",
        "net/minecraft/commands/Commands",
        "buildDispatcher",
        "(Lcom/mojang/brigadier/CommandDispatcher;)V",
        true,
        true,
        false,
        false,
        true,
        false,
        false,
        "Plan a future value-capturing command dispatcher primitive before command registration.",
        "A selected method descriptor reference identifies a possible method boundary but does not provide dispatcher value access. SteelHook 0.1 method-entry dispatch cannot pass or capture dispatcher values.");
  }

  private MinecraftCommandDispatcherBindingAnalysis selectedFieldAnalysis() {
    return new MinecraftCommandDispatcherBindingAnalysis(
        1,
        "Target-15",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.command_registration",
        "Target-14",
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
        "STABLE_TARGET_SELECTED",
        true,
        true,
        null,
        MinecraftCommandDispatcherBindingStatus.SELECTED_SYMBOL_ANALYZED,
        MinecraftCommandDispatcherAccessStrategy.INSTANCE_FIELD_OWNER_CAPTURE_REQUIRED,
        "target-14.minecraft.commands.dispatcher.candidate.001",
        "FIELD_DESCRIPTOR_REFERENCE",
        "net/minecraft/server/CommandsHolder",
        "dispatcher",
        "Lcom/mojang/brigadier/CommandDispatcher;",
        false,
        true,
        true,
        true,
        true,
        false,
        false,
        "Plan a future owner-instance capture plus controlled field access primitive before command registration.",
        null);
  }

  private MinecraftCommandDispatcherBindingAnalysis blockedAnalysis() {
    return new MinecraftCommandDispatcherBindingAnalysis(
        1,
        "Target-15",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.command_registration",
        "Target-14",
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
        false,
        "UPSTREAM_GATE_BLOCKED",
        false,
        false,
        "Target-14 requires an available Target-13 command lifecycle anchor.",
        MinecraftCommandDispatcherBindingStatus.UPSTREAM_GATE_BLOCKED,
        MinecraftCommandDispatcherAccessStrategy.NONE,
        null,
        null,
        null,
        null,
        null,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        "Restore the Target-13 and Target-14 upstream gates before analyzing command dispatcher binding strategy.",
        null);
  }

  private MinecraftCommandDispatcherBindingAnalysis noTargetAnalysis() {
    return new MinecraftCommandDispatcherBindingAnalysis(
        1,
        "Target-15",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.command_registration",
        "Target-14",
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
        "NO_CANDIDATES",
        false,
        true,
        null,
        MinecraftCommandDispatcherBindingStatus.NO_SYMBOL_TARGET,
        MinecraftCommandDispatcherAccessStrategy.NONE,
        null,
        null,
        null,
        null,
        null,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        "Do not implement command registration yet; no selectable Minecraft command dispatcher symbol target is known.",
        null);
  }

  private MinecraftCommandDispatcherBindingAnalysis ambiguousAnalysis() {
    return new MinecraftCommandDispatcherBindingAnalysis(
        1,
        "Target-15",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.command_registration",
        "Target-14",
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
        "AMBIGUOUS_CANDIDATES",
        false,
        true,
        null,
        MinecraftCommandDispatcherBindingStatus.AMBIGUOUS_SYMBOL_TARGETS,
        MinecraftCommandDispatcherAccessStrategy.NONE,
        null,
        null,
        null,
        null,
        null,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        "Do not implement command registration yet; narrow dispatcher discovery before planning access or mutation.",
        null);
  }
}
