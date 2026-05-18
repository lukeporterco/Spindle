package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook03GatedRuntimeProofRunnerTest {
  @TempDir Path tempDir;

  private final SteelHook03GatedRuntimeProofRunner runner =
      new SteelHook03GatedRuntimeProofRunner();

  @Test
  void validTarget29HandoffProducesGatedRuntimeProofReadyReport() {
    SteelHookDispatcher.resetForBootstrap();

    SteelHook03RuntimeProofReport report =
        runner.run(
            SteelHook03TestFixtures.runtimePlan(tempDir.resolve("unused.jar")),
            SteelHook03TestFixtures.passedTarget28Report(),
            SteelHook03TestFixtures.passedTarget29Report(),
            tempDir);

    assertEquals(SteelHook03RuntimeProofStatus.GATED_RUNTIME_PROOF_READY, report.status());
    assertTrue(report.gatedRuntimeProofReady());
    assertEquals(2, report.runtimeClassLoaderProofCount());
    assertEquals(2, report.runtimeClassLoaderSuccessCount());
    assertEquals(
        SteelHook03RuntimeProofNextDirection.MOVE_TO_TARGET_31_STEELHOOK_0_3_COMPLETION,
        report.nextDirection());
    assertEquals(
        SteelHook03RuntimeProofStatus.GATED_RUNTIME_PROOF_READY,
        report.entryPrimitiveProof().status());
    assertEquals(
        SteelHook03RuntimeProofStatus.GATED_RUNTIME_PROOF_READY,
        report.exitPrimitiveProof().status());
    assertTrue(report.entryPrimitiveProof().targetClassDefined());
    assertTrue(report.exitPrimitiveProof().targetClassDefined());
    assertTrue(report.entryPrimitiveProof().definedBySteelHookRuntimeClassLoader());
    assertTrue(report.exitPrimitiveProof().definedBySteelHookRuntimeClassLoader());
    assertFalse(report.entryPrimitiveProof().classInitialized());
    assertFalse(report.exitPrimitiveProof().classInitialized());
    assertFalse(report.entryPrimitiveProof().minecraftMainInvoked());
    assertFalse(report.exitPrimitiveProof().minecraftMainInvoked());
    assertFalse(report.entryPrimitiveProof().serverLaunchOccurred());
    assertFalse(report.exitPrimitiveProof().serverLaunchOccurred());
    assertFalse(report.entryPrimitiveProof().hookInstallationOccurred());
    assertFalse(report.exitPrimitiveProof().hookInstallationOccurred());
    assertFalse(report.entryPrimitiveProof().runtimeDispatchOccurred());
    assertFalse(report.exitPrimitiveProof().runtimeDispatchOccurred());
    assertEquals(0, report.entryPrimitiveProof().dispatcherInvocationCountBefore());
    assertEquals(0, report.entryPrimitiveProof().dispatcherInvocationCountAfter());
    assertEquals(0, report.exitPrimitiveProof().dispatcherInvocationCountBefore());
    assertEquals(0, report.exitPrimitiveProof().dispatcherInvocationCountAfter());
    assertTrue(report.entryPrimitiveProof().stackMapTableRewriteSupported());
    assertTrue(report.entryPrimitiveProof().stackMapTableRewriteApplied());
    assertFalse(report.exitPrimitiveProof().stackMapTableRewriteSupported());
    assertFalse(report.exitPrimitiveProof().stackMapTableRewriteApplied());
    assertTrue(report.entryPrimitiveProof().methodEntryTransformationOccurred());
    assertFalse(report.entryPrimitiveProof().methodExitTransformationOccurred());
    assertFalse(report.exitPrimitiveProof().methodEntryTransformationOccurred());
    assertTrue(report.exitPrimitiveProof().methodExitTransformationOccurred());
    assertFalse(report.beforeDispatcherInvocationObserved());
    assertFalse(report.afterDispatcherInvocationObserved());
    assertFalse(report.runtimeDispatchOccurred());
    assertEquals(
        "minecraft-runtime-steelhook-0-3-entry", report.entryPrimitiveProof().runtimeLoaderId());
    assertEquals(
        "minecraft-runtime-steelhook-0-3-exit", report.exitPrimitiveProof().runtimeLoaderId());
    assertEquals(0, SteelHookDispatcher.beforeMinecraftServerMainInvocationCount());
    assertEquals(0, SteelHookDispatcher.afterMinecraftServerMainInvocationCount());
  }

  @Test
  void missingTarget29ReportBlocks() {
    SteelHook03RuntimeProofReport report =
        runner.run(
            SteelHook03TestFixtures.runtimePlan(tempDir.resolve("unused.jar")),
            SteelHook03TestFixtures.passedTarget28Report(),
            null,
            tempDir);

    assertEquals(SteelHook03RuntimeProofStatus.BLOCKED, report.status());
    assertFalse(report.gatedRuntimeProofReady());
  }

  @Test
  void failedTarget29StatusBlocks() {
    SteelHook03RuntimeProofReport report =
        runner.run(
            SteelHook03TestFixtures.runtimePlan(tempDir.resolve("unused.jar")),
            SteelHook03TestFixtures.passedTarget28Report(),
            SteelHook03TestFixtures.failedTarget29Report(),
            tempDir);

    assertEquals(SteelHook03RuntimeProofStatus.BLOCKED, report.status());
  }

  @Test
  void target29NextDirectionMismatchBlocks() {
    SteelHook03RuntimeProofReport report =
        runner.run(
            SteelHook03TestFixtures.runtimePlan(tempDir.resolve("unused.jar")),
            SteelHook03TestFixtures.passedTarget28Report(),
            SteelHook03TestFixtures.nextDirectionMismatchTarget29Report(),
            tempDir);

    assertEquals(SteelHook03RuntimeProofStatus.BLOCKED, report.status());
  }

  @Test
  void missingRuntimeJarOrUnreadableRuntimePathFailsDeterministically() {
    java.nio.file.Path blockedPath = tempDir.resolve("blocked-path");
    try {
      java.nio.file.Files.writeString(blockedPath, "not-a-directory");
    } catch (java.io.IOException exception) {
      throw new IllegalStateException(exception);
    }
    SteelHook03RuntimeProofReport report =
        runner.run(
            SteelHook03TestFixtures.runtimePlan(tempDir.resolve("unused.jar")),
            SteelHook03TestFixtures.passedTarget28Report(),
            SteelHook03TestFixtures.passedTarget29Report(),
            blockedPath);

    assertEquals(SteelHook03RuntimeProofStatus.FAILED, report.status());
    assertFalse(report.gatedRuntimeProofReady());
  }
}
