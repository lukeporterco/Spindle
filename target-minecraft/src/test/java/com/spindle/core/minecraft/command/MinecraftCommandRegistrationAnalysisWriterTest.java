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

class MinecraftCommandRegistrationAnalysisWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftCommandRegistrationAnalysisWriter writer =
      new MinecraftCommandRegistrationAnalysisWriter();

  @Test
  void writerSerializesSchemaAndMilestone() throws Exception {
    MinecraftCommandRegistrationAnalysis analysis = analysis();
    Path first = tempDirectory.resolve("a/minecraft-command-registration-analysis.json");
    Path second = tempDirectory.resolve("b/minecraft-command-registration-analysis.json");

    writer.write(first, analysis);
    writer.write(second, analysis);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"schema\": 1"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-13\""));
    assertTrue(firstJson.contains("\"sourceLifecycleDispatchPlanMilestone\": \"Target-12\""));
  }

  @Test
  void writerSerializesLifecycleAnchor() {
    String json = writer.toJson(analysis()).toString();

    assertTrue(json.contains("\"id\":\"target-13.minecraft.commands.lifecycle_anchor\""));
    assertTrue(json.contains("\"status\":\"ANCHOR_AVAILABLE\""));
    assertTrue(json.contains("\"representationKind\":\"UPSTREAM_LIFECYCLE_DISPATCH\""));
    assertTrue(
        json.contains(
            "\"upstreamDispatchId\":\"target-12.minecraft.server.lifecycle.starting.dispatch\""));
  }

  @Test
  void writerSerializesDeclaredUnboundCommandBoundaries() {
    String json = writer.toJson(analysis()).toString();

    assertTrue(json.contains("\"boundaryId\":\"minecraft.commands.dispatcher.discovery\""));
    assertTrue(json.contains("\"boundaryId\":\"minecraft.commands.reload.reapply\""));
    assertTrue(json.contains("\"status\":\"DECLARED_UNBOUND\""));
    assertTrue(
        json.contains(
            "\"notes\":\"No Minecraft command dispatcher symbol is known in this pass.\""));
  }

  @Test
  void writerSerializesNullsDeterministically() {
    String json = writer.toJson(analysis()).toString();

    assertTrue(json.contains("\"gateFailureReason\":null"));
    assertTrue(json.contains("\"ownerInternalName\":null"));
    assertTrue(json.contains("\"memberName\":null"));
    assertTrue(json.contains("\"descriptor\":null"));
  }

  @Test
  void writerDoesNotSerializePathsByteArraysOrCommandTreeContents() {
    String json = writer.toJson(analysis()).toString();

    assertFalse(json.contains("C:/"));
    assertFalse(json.contains("\\\\"));
    assertFalse(json.contains("bytes"));
    assertFalse(json.contains("bytecode"));
    assertFalse(json.contains("literal_command_tree"));
  }

  private MinecraftCommandRegistrationAnalysis analysis() {
    return new MinecraftCommandRegistrationAnalysis(
        1,
        "Target-13",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.command_registration",
        2,
        "Command Registration",
        "minecraft.concept.server_lifecycle",
        "Target-12",
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
        true,
        true,
        null,
        5,
        1,
        4,
        0,
        0,
        0,
        List.of(
            new MinecraftAnalyzedCommandRegistrationBoundary(
                "target-13.minecraft.commands.lifecycle_anchor",
                "minecraft.commands.lifecycle_anchor",
                "Lifecycle Anchor",
                MinecraftCommandRegistrationBoundaryStatus.ANCHOR_AVAILABLE,
                MinecraftCommandRegistrationRepresentationKind.UPSTREAM_LIFECYCLE_DISPATCH,
                "minecraft.concept.server_lifecycle",
                "target-12.minecraft.server.lifecycle.starting.dispatch",
                "minecraft.server.lifecycle.starting",
                false,
                null,
                null,
                null,
                false,
                false,
                false,
                true,
                "Anchored."),
            futureBoundary(
                "target-13.minecraft.commands.dispatcher.discovery",
                "minecraft.commands.dispatcher.discovery",
                "Dispatcher Discovery",
                MinecraftCommandRegistrationRepresentationKind.FUTURE_DISPATCHER_SYMBOL,
                "No Minecraft command dispatcher symbol is known in this pass."),
            futureBoundary(
                "target-13.minecraft.commands.registration.window",
                "minecraft.commands.registration.window",
                "Registration Window",
                MinecraftCommandRegistrationRepresentationKind.FUTURE_REGISTRATION_PHASE,
                "No Minecraft command dispatcher symbol is known in this pass, so command registration remains unbound."),
            futureBoundary(
                "target-13.minecraft.commands.registration.apply",
                "minecraft.commands.registration.apply",
                "Registration Apply",
                MinecraftCommandRegistrationRepresentationKind.FUTURE_REGISTRATION_PHASE,
                "No Minecraft command dispatcher symbol is known in this pass, so command registration remains unbound."),
            futureBoundary(
                "target-13.minecraft.commands.reload.reapply",
                "minecraft.commands.reload.reapply",
                "Reload Reapply",
                MinecraftCommandRegistrationRepresentationKind.FUTURE_RELOAD_PHASE,
                "No Minecraft command dispatcher symbol is known in this pass, so reload-safe command reapplication remains unbound.")));
  }

  private MinecraftAnalyzedCommandRegistrationBoundary futureBoundary(
      String id,
      String boundaryId,
      String displayName,
      MinecraftCommandRegistrationRepresentationKind representationKind,
      String notes) {
    return new MinecraftAnalyzedCommandRegistrationBoundary(
        id,
        boundaryId,
        displayName,
        MinecraftCommandRegistrationBoundaryStatus.DECLARED_UNBOUND,
        representationKind,
        "minecraft.concept.server_lifecycle",
        "target-12.minecraft.server.lifecycle.starting.dispatch",
        "minecraft.server.lifecycle.starting",
        false,
        null,
        null,
        null,
        true,
        false,
        false,
        true,
        notes);
  }
}
