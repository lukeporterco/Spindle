package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SteelHook03FramedMethodFoundationRunnerTest {
  private final SteelHook03FramedMethodFoundationRunner runner =
      new SteelHook03FramedMethodFoundationRunner();

  @Test
  void validTarget27CompletionPlusFramedFixtureWritesFoundationReadyReport() {
    SteelHook03FramedMethodFoundationReport report =
        runner.run(
            SteelHook03TestFixtures.passedCompletionReport(),
            SteelHook03TestFixtures.framedFixtureClassBytes());

    assertEquals(SteelHook03FramedMethodFoundationStatus.FOUNDATION_READY, report.status());
    assertTrue(report.framedMethodFoundationReady());
    assertTrue(report.stackMapTableRewriteApplied());
    assertTrue(report.stackMapTableFrameShiftApplied());
    assertEquals(1, report.stackMapTableEntryCountBefore());
    assertEquals(1, report.stackMapTableEntryCountAfter());
    assertEquals(5, report.firstFrameOffsetDeltaBefore());
    assertEquals(8, report.firstFrameOffsetDeltaAfter());
    assertFalse(report.runtimeClassLoadingPathEnabled());
    assertFalse(report.classLoadingOccurred());
    assertFalse(report.serverLaunchOccurred());
    assertFalse(report.hookInstallationOccurred());
    assertFalse(report.runtimeDispatchOccurred());
    assertFalse(report.javaModExecutionSandboxed());
    assertNull(report.failureReason());
  }

  @Test
  void missingTarget27ReportBlocks() {
    SteelHook03FramedMethodFoundationReport report =
        runner.run(null, SteelHook03TestFixtures.framedFixtureClassBytes());

    assertEquals(SteelHook03FramedMethodFoundationStatus.BLOCKED, report.status());
    assertFalse(report.framedMethodFoundationReady());
  }

  @Test
  void failedTarget27ReportBlocks() {
    SteelHook03FramedMethodFoundationReport report =
        runner.run(
            SteelHook03TestFixtures.failedCompletionReport(),
            SteelHook03TestFixtures.framedFixtureClassBytes());

    assertEquals(SteelHook03FramedMethodFoundationStatus.BLOCKED, report.status());
  }

  @Test
  void nonCompleteTarget27HandoffBlocks() {
    SteelHook03FramedMethodFoundationReport report =
        runner.run(
            SteelHook03TestFixtures.blockedCompletionReport(),
            SteelHook03TestFixtures.framedFixtureClassBytes());

    assertEquals(SteelHook03FramedMethodFoundationStatus.BLOCKED, report.status());
  }
}
