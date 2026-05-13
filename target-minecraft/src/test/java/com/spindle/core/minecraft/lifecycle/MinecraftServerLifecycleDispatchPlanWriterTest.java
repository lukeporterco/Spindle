package com.spindle.core.minecraft.lifecycle;

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

class MinecraftServerLifecycleDispatchPlanWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftServerLifecycleDispatchPlanWriter writer =
      new MinecraftServerLifecycleDispatchPlanWriter();

  @Test
  void writerSerializesSchemaAndMilestone() throws Exception {
    MinecraftServerLifecycleDispatchPlan plan = plan();
    Path first = tempDirectory.resolve("a/minecraft-server-lifecycle-dispatch-plan.json");
    Path second = tempDirectory.resolve("b/minecraft-server-lifecycle-dispatch-plan.json");

    writer.write(first, plan);
    writer.write(second, plan);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"schema\": 1"));
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-12\""));
    assertTrue(firstJson.contains("\"sourceBindingReportMilestone\": \"Target-11\""));
  }

  @Test
  void writerSerializesSymbolicStartingDispatch() {
    String json = writer.toJson(plan()).toString();

    assertTrue(json.contains("\"id\":\"target-12.minecraft.server.lifecycle.starting.dispatch\""));
    assertTrue(json.contains("\"status\":\"PLANNED\""));
    assertTrue(json.contains("\"mode\":\"INTERNAL_STATIC_DISPATCH_SYMBOLIC\""));
    assertTrue(json.contains("\"dispatcherMethodName\":\"beforeMinecraftServerMain\""));
  }

  @Test
  void writerSerializesUnsupportedFutureDispatches() {
    String json = writer.toJson(plan()).toString();

    assertTrue(json.contains("\"phaseId\":\"minecraft.server.lifecycle.started\""));
    assertTrue(json.contains("\"phaseId\":\"minecraft.server.lifecycle.reload_requested\""));
    assertTrue(json.contains("\"status\":\"DECLARED_UNSUPPORTED\""));
    assertTrue(json.contains("\"mode\":\"NONE\""));
  }

  @Test
  void writerSerializesNullsDeterministically() {
    String json = writer.toJson(plan()).toString();

    assertTrue(json.contains("\"gateFailureReason\":null"));
    assertTrue(json.contains("\"sourceContractId\":null"));
    assertTrue(json.contains("\"dispatcherOwnerInternalName\":null"));
    assertTrue(json.contains("\"dispatchTiming\":null"));
  }

  @Test
  void writerDoesNotSerializePathsOrByteArrays() {
    String json = writer.toJson(plan()).toString();

    assertFalse(json.contains("C:/"));
    assertFalse(json.contains("\\\\"));
    assertFalse(json.contains("bytecode"));
    assertFalse(json.contains("bytes"));
  }

  private MinecraftServerLifecycleDispatchPlan plan() {
    return new MinecraftServerLifecycleDispatchPlan(
        1,
        "Target-12",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.server_lifecycle",
        "Target-11",
        true,
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
        6,
        6,
        1,
        0,
        5,
        List.of(
            new MinecraftPlannedServerLifecycleDispatch(
                "target-12.minecraft.server.lifecycle.starting.dispatch",
                "minecraft.server.lifecycle.starting",
                "Starting",
                "target-11.minecraft.server.lifecycle.starting",
                "minecraft.26_1_2.server.main.entrypoint",
                MinecraftServerLifecycleDispatchStatus.PLANNED,
                MinecraftServerLifecycleDispatchMode.INTERNAL_STATIC_DISPATCH_SYMBOLIC,
                "BEFORE_MINECRAFT_SERVER_MAIN",
                "com/spindle/core/minecraft/lifecycle/runtime/MinecraftServerLifecycleDispatcher",
                "beforeMinecraftServerMain",
                "()V",
                false,
                false,
                false,
                false,
                false,
                true,
                "Symbolic."),
            unsupported("started", "Started"),
            unsupported("stopping", "Stopping"),
            unsupported("stopped", "Stopped"),
            unsupported("crashed", "Crashed"),
            unsupported("reload_requested", "Reload Requested")));
  }

  private MinecraftPlannedServerLifecycleDispatch unsupported(
      String phaseSuffix, String displayName) {
    return new MinecraftPlannedServerLifecycleDispatch(
        "target-12.minecraft.server.lifecycle." + phaseSuffix + ".dispatch",
        "minecraft.server.lifecycle." + phaseSuffix,
        displayName,
        "target-11.minecraft.server.lifecycle." + phaseSuffix,
        null,
        MinecraftServerLifecycleDispatchStatus.DECLARED_UNSUPPORTED,
        MinecraftServerLifecycleDispatchMode.NONE,
        null,
        null,
        null,
        null,
        false,
        false,
        false,
        false,
        false,
        true,
        "Future.");
  }
}
