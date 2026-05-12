package com.spindle.core.minecraft.hook.install;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftModExecutionPlan;
import com.spindle.core.minecraft.hook.MinecraftHookContractReport;
import com.spindle.core.minecraft.hook.MinecraftHookContractResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftHookInstallationPlannerTest {
  private final MinecraftHookInstallationPlanner planner = new MinecraftHookInstallationPlanner();

  @Test
  void validTargetThreeReportPlansSingleHook() {
    MinecraftHookInstallationPlan plan =
        planner.plan(validContractReport(), executionPlan("net.minecraft.server.Main"));

    assertTrue(plan.gatePassed());
    assertTrue(plan.installationPlanned());
    assertEquals("Target-4", plan.milestoneName());
    assertEquals("launch-boundary-main-wrapper", plan.installationMode().id());
    assertEquals(1, plan.plannedHookCount());
    assertEquals(
        "target-4.minecraft.server.main.launch-boundary", plan.plannedHooks().getFirst().id());
  }

  @Test
  void failedContractValidationFailsGate() {
    MinecraftHookInstallationPlan plan =
        planner.plan(
            new MinecraftHookContractReport(
                2,
                "Target-3",
                "minecraft",
                "26.1.2",
                "server",
                "minecraft-26.1.2-server-known-symbols",
                "catalog",
                "26.1.2",
                "server",
                true,
                false,
                false,
                false,
                false,
                false,
                1,
                "Target-1",
                1,
                0,
                1,
                1,
                0,
                0,
                1,
                false,
                List.of(invalidEntrypointContract()),
                List.of()),
            executionPlan("net.minecraft.server.Main"));

    assertFalse(plan.gatePassed());
    assertFalse(plan.installationPlanned());
    assertTrue(plan.gateFailureReason().contains("validation failed"));
  }

  @Test
  void unsupportedCatalogFailsGate() {
    MinecraftHookInstallationPlan plan =
        planner.plan(
            new MinecraftHookContractReport(
                2,
                "Target-3",
                "minecraft",
                "26.1.2",
                "server",
                "unsupported-catalog",
                "catalog",
                "26.1.2",
                "server",
                true,
                false,
                false,
                false,
                false,
                false,
                1,
                "Target-1",
                1,
                1,
                0,
                1,
                0,
                0,
                0,
                true,
                List.of(validEntrypointContract()),
                List.of()),
            executionPlan("net.minecraft.server.Main"));

    assertFalse(plan.gatePassed());
    assertTrue(plan.gateFailureReason().contains("Unsupported hook contract catalog"));
  }

  @Test
  void executionMainClassMismatchFailsGate() {
    MinecraftHookInstallationPlan plan =
        planner.plan(validContractReport(), executionPlan("com.example.NotMinecraftMain"));

    assertFalse(plan.gatePassed());
    assertFalse(plan.installationPlanned());
    assertEquals(0, plan.plannedHookCount());
    assertTrue(plan.gateFailureReason().contains("main class"));
    assertNull(plan.plannedHooks().stream().findFirst().orElse(null));
  }

  private MinecraftHookContractReport validContractReport() {
    return new MinecraftHookContractReport(
        2,
        "Target-3",
        "minecraft",
        "26.1.2",
        "server",
        "minecraft-26.1.2-server-known-symbols",
        "catalog",
        "26.1.2",
        "server",
        true,
        false,
        false,
        false,
        false,
        false,
        1,
        "Target-1",
        1,
        1,
        0,
        1,
        0,
        0,
        0,
        true,
        List.of(validEntrypointContract()),
        List.of());
  }

  private MinecraftHookContractResult validEntrypointContract() {
    return new MinecraftHookContractResult(
        "minecraft.26_1_2.server.main.entrypoint",
        "entrypoint",
        "server",
        "METHOD",
        "net/minecraft/server/Main",
        "main",
        "([Ljava/lang/String;)V",
        "REQUIRED",
        "VALID",
        true,
        true,
        false,
        List.of(),
        "net/minecraft/server/Main",
        "main([Ljava/lang/String;)V");
  }

  private MinecraftHookContractResult invalidEntrypointContract() {
    return new MinecraftHookContractResult(
        "minecraft.26_1_2.server.main.entrypoint",
        "entrypoint",
        "server",
        "METHOD",
        "net/minecraft/server/Main",
        "main",
        "([Ljava/lang/String;)V",
        "REQUIRED",
        "MISSING_MEMBER",
        false,
        true,
        false,
        List.of("hook-contract-0001"),
        "net/minecraft/server/Main",
        null);
  }

  private MinecraftModExecutionPlan executionPlan(String mainClass) {
    return new MinecraftModExecutionPlan(
        1,
        "Milestone 8",
        "26.1.2",
        "25",
        "server",
        null,
        null,
        null,
        List.of(),
        List.of(),
        List.of(),
        null,
        mainClass,
        List.of(),
        List.of(),
        null,
        null);
  }
}
