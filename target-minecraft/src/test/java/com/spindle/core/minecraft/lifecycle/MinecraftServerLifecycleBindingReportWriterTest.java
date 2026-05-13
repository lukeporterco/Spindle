package com.spindle.core.minecraft.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftServerLifecycleBindingReportWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftServerLifecycleBindingReportWriter writer =
      new MinecraftServerLifecycleBindingReportWriter();

  @Test
  void writerSerializesSchemaAndMilestone() throws Exception {
    MinecraftServerLifecycleBindingReport report = report();
    Path first = tempDirectory.resolve("a/minecraft-server-lifecycle-bindings.json");
    Path second = tempDirectory.resolve("b/minecraft-server-lifecycle-bindings.json");

    writer.write(first, report);
    writer.write(second, report);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"schema\": 1"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-11\""));
    assertTrue(firstJson.contains("\"conceptId\": \"minecraft.concept.server_lifecycle\""));
  }

  @Test
  void writerSerializesBoundStartingPhase() {
    String json = writer.toJson(report()).toString();

    assertTrue(json.contains("\"id\":\"target-11.minecraft.server.lifecycle.starting\""));
    assertTrue(json.contains("\"status\":\"BOUND\""));
    assertTrue(json.contains("\"boundContractId\":\"minecraft.26_1_2.server.main.entrypoint\""));
    assertTrue(json.contains("\"bindingKind\":\"known-main-entrypoint-analysis\""));
  }

  @Test
  void writerSerializesDeclaredUnboundFuturePhases() {
    String json = writer.toJson(report()).toString();

    assertTrue(json.contains("\"phaseId\":\"minecraft.server.lifecycle.started\""));
    assertTrue(json.contains("\"phaseId\":\"minecraft.server.lifecycle.reload_requested\""));
    assertTrue(json.contains("\"status\":\"DECLARED_UNBOUND\""));
    assertTrue(json.contains("\"supportedInThisPass\":false"));
  }

  @Test
  void writerSerializesNullsDeterministically() {
    String json = writer.toJson(report()).toString();

    assertTrue(json.contains("\"gateFailureReason\":null"));
    assertTrue(json.contains("\"boundContractId\":null"));
    assertTrue(json.contains("\"ownerInternalName\":null"));
    assertTrue(json.contains("\"bindingKind\":null"));
  }

  @Test
  void writerDoesNotSerializePathsOrByteArrays() {
    String json = writer.toJson(report()).toString();

    assertFalse(json.contains("C:/"));
    assertFalse(json.contains("\\\\"));
    assertFalse(json.contains("bytecode"));
    assertFalse(json.contains("bytes"));
  }

  private MinecraftServerLifecycleBindingReport report() {
    return new MinecraftServerLifecycleBindingReport(
        1,
        "Target-11",
        "minecraft",
        "26.1.2",
        "server",
        "minecraft.concept.server_lifecycle",
        1,
        "Server Lifecycle",
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        "minecraft-26.1.2-server-known-symbols",
        true,
        true,
        null,
        6,
        1,
        5,
        6,
        List.of(
            new MinecraftServerLifecycleBinding(
                "target-11.minecraft.server.lifecycle.starting",
                "minecraft.server.lifecycle.starting",
                "Starting",
                MinecraftServerLifecycleBindingStatus.BOUND,
                true,
                "minecraft.26_1_2.server.main.entrypoint",
                "net/minecraft/server/Main",
                "main",
                "([Ljava/lang/String;)V",
                "known-main-entrypoint-analysis",
                "Bound."),
            new MinecraftServerLifecycleBinding(
                "target-11.minecraft.server.lifecycle.started",
                "minecraft.server.lifecycle.started",
                "Started",
                MinecraftServerLifecycleBindingStatus.DECLARED_UNBOUND,
                false,
                null,
                null,
                null,
                null,
                null,
                "Future."),
            new MinecraftServerLifecycleBinding(
                "target-11.minecraft.server.lifecycle.stopping",
                "minecraft.server.lifecycle.stopping",
                "Stopping",
                MinecraftServerLifecycleBindingStatus.DECLARED_UNBOUND,
                false,
                null,
                null,
                null,
                null,
                null,
                "Future."),
            new MinecraftServerLifecycleBinding(
                "target-11.minecraft.server.lifecycle.stopped",
                "minecraft.server.lifecycle.stopped",
                "Stopped",
                MinecraftServerLifecycleBindingStatus.DECLARED_UNBOUND,
                false,
                null,
                null,
                null,
                null,
                null,
                "Future."),
            new MinecraftServerLifecycleBinding(
                "target-11.minecraft.server.lifecycle.crashed",
                "minecraft.server.lifecycle.crashed",
                "Crashed",
                MinecraftServerLifecycleBindingStatus.DECLARED_UNBOUND,
                false,
                null,
                null,
                null,
                null,
                null,
                "Future."),
            new MinecraftServerLifecycleBinding(
                "target-11.minecraft.server.lifecycle.reload_requested",
                "minecraft.server.lifecycle.reload_requested",
                "Reload Requested",
                MinecraftServerLifecycleBindingStatus.DECLARED_UNBOUND,
                false,
                null,
                null,
                null,
                null,
                null,
                "Future.")));
  }
}
