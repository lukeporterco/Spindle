package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class SteelHook04ReturnValueInterceptOfflineProofRunnerTest {
  private final SteelHook04ReturnValueInterceptOfflineProofRunner runner =
      new SteelHook04ReturnValueInterceptOfflineProofRunner();

  @Test
  void validTarget32BoundaryProducesProofReadyTrue() {
    SteelHook04ReturnValueInterceptOfflineProofReport report =
        runner.run(SteelHook04TestFixtures.passedTarget32Report());

    assertTrue(report.proofReady());
    assertEquals(
        SteelHook04ReturnValueInterceptOfflineProofStatus.PROOF_READY, report.proofStatus());
  }

  @Test
  void nullTarget32SourceBlocksTarget33() {
    SteelHook04ReturnValueInterceptOfflineProofReport report = runner.run(null);

    assertFalse(report.proofReady());
    assertEquals(SteelHook04ReturnValueInterceptOfflineProofStatus.BLOCKED, report.proofStatus());
  }

  @Test
  void target32SourceGateFalseBlocksTarget33() {
    SteelHook04PrimitiveBoundaryReport source =
        copy(SteelHook04TestFixtures.passedTarget32Report(), false);

    SteelHook04ReturnValueInterceptOfflineProofReport report = runner.run(source);

    assertFalse(report.proofReady());
    assertEquals(SteelHook04ReturnValueInterceptOfflineProofStatus.BLOCKED, report.proofStatus());
  }

  @Test
  void target32BoundaryStatusOtherThanBoundaryReadyBlocksTarget33() {
    SteelHook04PrimitiveBoundaryReport source =
        new SteelHook04PrimitiveBoundaryReport(
            1,
            "Target-32",
            "minecraft",
            "0.4",
            "Target-31",
            "passed",
            true,
            "steelhook-0-3-complete",
            true,
            null,
            SteelHook04PrimitiveBoundaryStatus.SOURCE_GATE_BLOCKED,
            SteelHook04PrimitiveBoundaryNextDirection.RESTORE_TARGET_31_STEELHOOK_0_3_COMPLETION,
            "blocked",
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
            3,
            SteelHook04TestFixtures.passedTarget32Report().candidates(),
            SteelHook04TestFixtures.passedTarget32Report().allowedFixtureShapes(),
            SteelHook04TestFixtures.passedTarget32Report().unsupportedFixtureShapes(),
            SteelHook04TestFixtures.passedTarget32Report().rejectionTaxonomy(),
            SteelHook04TestFixtures.passedTarget32Report().evidenceRequirements(),
            List.of());

    SteelHook04ReturnValueInterceptOfflineProofReport report = runner.run(source);

    assertFalse(report.proofReady());
  }

  @Test
  void missingReturnValueInterceptCandidateBlocksTarget33() {
    SteelHook04PrimitiveBoundaryReport passed = SteelHook04TestFixtures.passedTarget32Report();
    SteelHook04PrimitiveBoundaryReport source =
        new SteelHook04PrimitiveBoundaryReport(
            passed.schema(),
            passed.milestoneName(),
            passed.target(),
            passed.steelHookVersion(),
            passed.sourceSteelHook03Milestone(),
            passed.sourceSteelHook03Status(),
            passed.sourceSteelHook03CompletionReady(),
            passed.sourceSteelHook03HandoffStatus(),
            passed.gatePassed(),
            passed.gateFailureReason(),
            passed.boundaryStatus(),
            passed.nextDirection(),
            passed.nextRecommendedAction(),
            passed.analysisOnly(),
            passed.bytecodeModified(),
            passed.transformedClassBytesProduced(),
            passed.runtimeClassLoadingPathEnabled(),
            passed.classLoadingOccurred(),
            passed.serverLaunchOccurred(),
            passed.minecraftMainInvoked(),
            passed.hookInstallationOccurred(),
            passed.runtimeDispatchOccurred(),
            passed.publicApiExposed(),
            passed.javaAgentUsed(),
            passed.mixinUsed(),
            passed.javaModExecutionSandboxed(),
            passed.approvedPrimitiveCount(),
            passed.candidates().stream()
                .filter(
                    candidate ->
                        candidate.primitiveKind()
                            != SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT)
                .toList(),
            passed.allowedFixtureShapes(),
            passed.unsupportedFixtureShapes(),
            passed.rejectionTaxonomy(),
            passed.evidenceRequirements(),
            passed.findings());

    SteelHook04ReturnValueInterceptOfflineProofReport report = runner.run(source);

    assertFalse(report.proofReady());
  }

  @Test
  void publicApiLeakedFromSourceCandidateBlocksTarget33() {
    SteelHook04ReturnValueInterceptOfflineProofReport report =
        runner.run(candidateMutation(true, false));

    assertFalse(report.proofReady());
  }

  @Test
  void runtimeReadySourceCandidateBlocksTarget33() {
    SteelHook04ReturnValueInterceptOfflineProofReport report =
        runner.run(candidateMutation(false, true));

    assertFalse(report.proofReady());
  }

  @Test
  void target32RuntimeSideEffectBooleanTrueBlocksTarget33() {
    SteelHook04PrimitiveBoundaryReport passed = SteelHook04TestFixtures.passedTarget32Report();
    SteelHook04PrimitiveBoundaryReport source =
        new SteelHook04PrimitiveBoundaryReport(
            passed.schema(),
            passed.milestoneName(),
            passed.target(),
            passed.steelHookVersion(),
            passed.sourceSteelHook03Milestone(),
            passed.sourceSteelHook03Status(),
            passed.sourceSteelHook03CompletionReady(),
            passed.sourceSteelHook03HandoffStatus(),
            passed.gatePassed(),
            passed.gateFailureReason(),
            passed.boundaryStatus(),
            passed.nextDirection(),
            passed.nextRecommendedAction(),
            passed.analysisOnly(),
            passed.bytecodeModified(),
            passed.transformedClassBytesProduced(),
            true,
            passed.classLoadingOccurred(),
            passed.serverLaunchOccurred(),
            passed.minecraftMainInvoked(),
            passed.hookInstallationOccurred(),
            passed.runtimeDispatchOccurred(),
            passed.publicApiExposed(),
            passed.javaAgentUsed(),
            passed.mixinUsed(),
            passed.javaModExecutionSandboxed(),
            passed.approvedPrimitiveCount(),
            passed.candidates(),
            passed.allowedFixtureShapes(),
            passed.unsupportedFixtureShapes(),
            passed.rejectionTaxonomy(),
            passed.evidenceRequirements(),
            passed.findings());

    SteelHook04ReturnValueInterceptOfflineProofReport report = runner.run(source);

    assertFalse(report.proofReady());
  }

  @Test
  void validReportIncludesFourSuccessfulProofCases() {
    SteelHook04ReturnValueInterceptOfflineProofReport report =
        runner.run(SteelHook04TestFixtures.passedTarget32Report());

    assertEquals(4, report.successfulProofCaseCount());
  }

  @Test
  void validReportIncludesPrimitiveObserveOnlyProof() {
    assertTrue(
        labels(runner.run(SteelHook04TestFixtures.passedTarget32Report()))
            .contains("primitive observe-only"));
  }

  @Test
  void validReportIncludesPrimitiveReplacementProof() {
    assertTrue(
        labels(runner.run(SteelHook04TestFixtures.passedTarget32Report()))
            .contains("primitive replacement"));
  }

  @Test
  void validReportIncludesReferenceObserveOnlyProof() {
    assertTrue(
        labels(runner.run(SteelHook04TestFixtures.passedTarget32Report()))
            .contains("reference observe-only"));
  }

  @Test
  void validReportIncludesReferenceReplacementProof() {
    assertTrue(
        labels(runner.run(SteelHook04TestFixtures.passedTarget32Report()))
            .contains("reference replacement"));
  }

  @Test
  void
      validReportLeavesRuntimeClassloadingHookInstallationMinecraftExecutionDispatcherExecutionPublicApiExposureAndSandboxClaimFalse() {
    SteelHook04ReturnValueInterceptOfflineProofReport report =
        runner.run(SteelHook04TestFixtures.passedTarget32Report());

    assertFalse(report.runtimeClassLoadingPathEnabled());
    assertFalse(report.classLoadingOccurred());
    assertFalse(report.serverLaunchOccurred());
    assertFalse(report.minecraftMainInvoked());
    assertFalse(report.hookInstallationOccurred());
    assertFalse(report.runtimeDispatchOccurred());
    assertFalse(report.publicApiExposed());
    assertFalse(report.javaModExecutionSandboxed());
  }

  private List<String> labels(SteelHook04ReturnValueInterceptOfflineProofReport report) {
    return report.proofCases().stream()
        .map(SteelHook04ReturnValueInterceptProofCase::label)
        .toList();
  }

  private SteelHook04PrimitiveBoundaryReport candidateMutation(
      boolean publicApiExposed, boolean runtimeReady) {
    SteelHook04PrimitiveBoundaryReport passed = SteelHook04TestFixtures.passedTarget32Report();
    SteelHook04PrimitiveCandidate candidate =
        new SteelHook04PrimitiveCandidate(
            passed.candidates().getFirst().id(),
            SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
            passed.candidates().getFirst().candidateStatus(),
            true,
            publicApiExposed,
            true,
            runtimeReady,
            false,
            false,
            passed.candidates().getFirst().targetFollowOnPass(),
            passed.candidates().getFirst().fixtureShapeSummary(),
            passed.candidates().getFirst().evidenceSummary(),
            passed.candidates().getFirst().allowedFixtureShapes(),
            passed.candidates().getFirst().notes());
    return new SteelHook04PrimitiveBoundaryReport(
        passed.schema(),
        passed.milestoneName(),
        passed.target(),
        passed.steelHookVersion(),
        passed.sourceSteelHook03Milestone(),
        passed.sourceSteelHook03Status(),
        passed.sourceSteelHook03CompletionReady(),
        passed.sourceSteelHook03HandoffStatus(),
        passed.gatePassed(),
        passed.gateFailureReason(),
        passed.boundaryStatus(),
        passed.nextDirection(),
        passed.nextRecommendedAction(),
        passed.analysisOnly(),
        passed.bytecodeModified(),
        passed.transformedClassBytesProduced(),
        passed.runtimeClassLoadingPathEnabled(),
        passed.classLoadingOccurred(),
        passed.serverLaunchOccurred(),
        passed.minecraftMainInvoked(),
        passed.hookInstallationOccurred(),
        passed.runtimeDispatchOccurred(),
        passed.publicApiExposed(),
        passed.javaAgentUsed(),
        passed.mixinUsed(),
        passed.javaModExecutionSandboxed(),
        passed.approvedPrimitiveCount(),
        List.of(candidate),
        passed.allowedFixtureShapes(),
        passed.unsupportedFixtureShapes(),
        passed.rejectionTaxonomy(),
        passed.evidenceRequirements(),
        passed.findings());
  }

  private SteelHook04PrimitiveBoundaryReport copy(
      SteelHook04PrimitiveBoundaryReport passed, boolean gatePassed) {
    return new SteelHook04PrimitiveBoundaryReport(
        passed.schema(),
        passed.milestoneName(),
        passed.target(),
        passed.steelHookVersion(),
        passed.sourceSteelHook03Milestone(),
        passed.sourceSteelHook03Status(),
        passed.sourceSteelHook03CompletionReady(),
        passed.sourceSteelHook03HandoffStatus(),
        gatePassed,
        gatePassed ? passed.gateFailureReason() : "blocked",
        passed.boundaryStatus(),
        passed.nextDirection(),
        passed.nextRecommendedAction(),
        passed.analysisOnly(),
        passed.bytecodeModified(),
        passed.transformedClassBytesProduced(),
        passed.runtimeClassLoadingPathEnabled(),
        passed.classLoadingOccurred(),
        passed.serverLaunchOccurred(),
        passed.minecraftMainInvoked(),
        passed.hookInstallationOccurred(),
        passed.runtimeDispatchOccurred(),
        passed.publicApiExposed(),
        passed.javaAgentUsed(),
        passed.mixinUsed(),
        passed.javaModExecutionSandboxed(),
        passed.approvedPrimitiveCount(),
        passed.candidates(),
        passed.allowedFixtureShapes(),
        passed.unsupportedFixtureShapes(),
        passed.rejectionTaxonomy(),
        passed.evidenceRequirements(),
        passed.findings());
  }
}
