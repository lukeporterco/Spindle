package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SteelHook03MethodExitDispatchRunnerTest {
  private final SteelHook03MethodExitDispatchRunner runner =
      new SteelHook03MethodExitDispatchRunner();

  @Test
  void validTarget28HandoffPlusMethodExitFixtureWritesMethodExitDispatchReadyReport() {
    SteelHook03MethodExitDispatchReport report =
        runner.run(
            SteelHook03TestFixtures.passedTarget28Report(),
            SteelHook03TestFixtures.methodExitFixtureClassBytes());

    assertEquals(SteelHook03MethodExitDispatchStatus.METHOD_EXIT_DISPATCH_READY, report.status());
    assertEquals(SteelHook03PrimitiveKind.METHOD_EXIT_STATIC_DISPATCH, report.primitiveKind());
    assertTrue(report.methodExitDispatchReady());
    assertEquals(1, report.insertionCount());
    assertEquals(1, report.normalReturnOpcodeCount());
    assertEquals(3, report.insertedInstructionLength());
    assertFalse(report.runtimeClassLoadingPathEnabled());
    assertFalse(report.classLoadingOccurred());
    assertFalse(report.serverLaunchOccurred());
    assertFalse(report.hookInstallationOccurred());
    assertFalse(report.runtimeDispatchOccurred());
    assertFalse(report.publicApiExposed());
    assertFalse(report.javaAgentUsed());
    assertFalse(report.mixinUsed());
    assertFalse(report.javaModExecutionSandboxed());
    assertNull(report.failureReason());
  }

  @Test
  void missingTarget28ReportBlocks() {
    SteelHook03MethodExitDispatchReport report =
        runner.run(null, SteelHook03TestFixtures.methodExitFixtureClassBytes());

    assertEquals(SteelHook03MethodExitDispatchStatus.BLOCKED, report.status());
    assertFalse(report.methodExitDispatchReady());
  }

  @Test
  void failedTarget28StatusBlocks() {
    SteelHook03MethodExitDispatchReport report =
        runner.run(
            SteelHook03TestFixtures.failedTarget28Report(),
            SteelHook03TestFixtures.methodExitFixtureClassBytes());

    assertEquals(SteelHook03MethodExitDispatchStatus.BLOCKED, report.status());
  }

  @Test
  void target28NextDirectionMismatchBlocks() {
    SteelHook03MethodExitDispatchReport report =
        runner.run(
            SteelHook03TestFixtures.nextDirectionMismatchTarget28Report(),
            SteelHook03TestFixtures.methodExitFixtureClassBytes());

    assertEquals(SteelHook03MethodExitDispatchStatus.BLOCKED, report.status());
  }
}
