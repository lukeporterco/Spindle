package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class SteelHook04InvokeRedirectWrapOfflineProofRunnerTest {
  private final SteelHook04InvokeRedirectWrapOfflineProofRunner runner =
      new SteelHook04InvokeRedirectWrapOfflineProofRunner();

  @Test
  void validTarget32AndTarget33SourcesProduceProofReadyTrue() {
    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(
            SteelHook04TestFixtures.passedTarget32Report(),
            SteelHook04TestFixtures.passedTarget33Report());

    assertTrue(report.proofReady());
    assertEquals(SteelHook04InvokeRedirectWrapOfflineProofStatus.PROOF_READY, report.proofStatus());
  }

  @Test
  void nullTarget32SourceBlocksTarget34() {
    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(null, SteelHook04TestFixtures.passedTarget33Report());

    assertFalse(report.proofReady());
    assertEquals(SteelHook04InvokeRedirectWrapOfflineProofStatus.BLOCKED, report.proofStatus());
  }

  @Test
  void nullTarget33SourceBlocksTarget34() {
    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(SteelHook04TestFixtures.passedTarget32Report(), null);

    assertFalse(report.proofReady());
    assertEquals(SteelHook04InvokeRedirectWrapOfflineProofStatus.BLOCKED, report.proofStatus());
  }

  @Test
  void target32GateFalseBlocksTarget34() {
    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(copyTarget32(false), SteelHook04TestFixtures.passedTarget33Report());

    assertFalse(report.proofReady());
  }

  @Test
  void target32BoundaryStatusOtherThanBoundaryReadyBlocksTarget34() {
    SteelHook04PrimitiveBoundaryReport source =
        copyTarget32BoundaryStatus(SteelHook04PrimitiveBoundaryStatus.SOURCE_GATE_BLOCKED);

    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(source, SteelHook04TestFixtures.passedTarget33Report());

    assertFalse(report.proofReady());
  }

  @Test
  void missingInvokeRedirectCandidateBlocksTarget34() {
    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(
            removeCandidate(SteelHook04PrimitiveKind.INVOKE_REDIRECT),
            SteelHook04TestFixtures.passedTarget33Report());

    assertFalse(report.proofReady());
  }

  @Test
  void missingInvokeWrapCandidateBlocksTarget34() {
    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(
            removeCandidate(SteelHook04PrimitiveKind.INVOKE_WRAP),
            SteelHook04TestFixtures.passedTarget33Report());

    assertFalse(report.proofReady());
  }

  @Test
  void publicApiLeakedFromInvokeRedirectCandidateBlocksTarget34() {
    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(
            mutateCandidate(SteelHook04PrimitiveKind.INVOKE_REDIRECT, true, false),
            SteelHook04TestFixtures.passedTarget33Report());

    assertFalse(report.proofReady());
  }

  @Test
  void publicApiLeakedFromInvokeWrapCandidateBlocksTarget34() {
    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(
            mutateCandidate(SteelHook04PrimitiveKind.INVOKE_WRAP, true, false),
            SteelHook04TestFixtures.passedTarget33Report());

    assertFalse(report.proofReady());
  }

  @Test
  void runtimeReadyInvokeRedirectCandidateBlocksTarget34() {
    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(
            mutateCandidate(SteelHook04PrimitiveKind.INVOKE_REDIRECT, false, true),
            SteelHook04TestFixtures.passedTarget33Report());

    assertFalse(report.proofReady());
  }

  @Test
  void runtimeReadyInvokeWrapCandidateBlocksTarget34() {
    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(
            mutateCandidate(SteelHook04PrimitiveKind.INVOKE_WRAP, false, true),
            SteelHook04TestFixtures.passedTarget33Report());

    assertFalse(report.proofReady());
  }

  @Test
  void target32RuntimeSideEffectBooleanTrueBlocksTarget34() {
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

    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(source, SteelHook04TestFixtures.passedTarget33Report());

    assertFalse(report.proofReady());
  }

  @Test
  void target33ProofReadyFalseBlocksTarget34() {
    SteelHook04ReturnValueInterceptOfflineProofReport blocked =
        copyTarget33(
            false,
            SteelHook04ReturnValueInterceptOfflineProofStatus.BLOCKED,
            SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
            4,
            true);

    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(SteelHook04TestFixtures.passedTarget32Report(), blocked);

    assertFalse(report.proofReady());
  }

  @Test
  void target33ProofStatusOtherThanProofReadyBlocksTarget34() {
    SteelHook04ReturnValueInterceptOfflineProofReport blocked =
        copyTarget33(
            true,
            SteelHook04ReturnValueInterceptOfflineProofStatus.BLOCKED,
            SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
            4,
            true);

    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(SteelHook04TestFixtures.passedTarget32Report(), blocked);

    assertFalse(report.proofReady());
  }

  @Test
  void target33PrimitiveKindNotReturnValueInterceptBlocksTarget34() {
    SteelHook04ReturnValueInterceptOfflineProofReport blocked =
        copyTarget33(
            true,
            SteelHook04ReturnValueInterceptOfflineProofStatus.PROOF_READY,
            SteelHook04PrimitiveKind.INVOKE_REDIRECT,
            4,
            true);

    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(SteelHook04TestFixtures.passedTarget32Report(), blocked);

    assertFalse(report.proofReady());
  }

  @Test
  void target33RuntimeSideEffectBooleanTrueBlocksTarget34() {
    SteelHook04ReturnValueInterceptOfflineProofReport blocked =
        new SteelHook04ReturnValueInterceptOfflineProofReport(
            SteelHook04TestFixtures.passedTarget33Report().schema(),
            SteelHook04TestFixtures.passedTarget33Report().milestoneName(),
            SteelHook04TestFixtures.passedTarget33Report().target(),
            SteelHook04TestFixtures.passedTarget33Report().steelHookVersion(),
            SteelHook04TestFixtures.passedTarget33Report().sourceTarget32Milestone(),
            SteelHook04TestFixtures.passedTarget33Report().sourceTarget32BoundaryStatus(),
            SteelHook04TestFixtures.passedTarget33Report().sourceTarget32GatePassed(),
            SteelHook04TestFixtures.passedTarget33Report().sourceTarget32ApprovedPrimitiveCount(),
            SteelHook04TestFixtures.passedTarget33Report()
                .sourceReturnValueInterceptCandidatePresent(),
            SteelHook04TestFixtures.passedTarget33Report()
                .sourceReturnValueInterceptCandidateInternalOnly(),
            SteelHook04TestFixtures.passedTarget33Report()
                .sourceReturnValueInterceptCandidatePublicApiExposed(),
            SteelHook04TestFixtures.passedTarget33Report()
                .sourceReturnValueInterceptCandidateRuntimeReady(),
            SteelHook04TestFixtures.passedTarget33Report()
                .sourceReturnValueInterceptCandidateGatedRuntimeReady(),
            SteelHook04TestFixtures.passedTarget33Report()
                .sourceReturnValueInterceptCandidateImplementedInTarget32(),
            SteelHook04TestFixtures.passedTarget33Report().sourceRuntimeSideEffectsSafe(),
            SteelHook04TestFixtures.passedTarget33Report().proofReady(),
            SteelHook04TestFixtures.passedTarget33Report().proofStatus(),
            SteelHook04TestFixtures.passedTarget33Report().nextDirection(),
            SteelHook04TestFixtures.passedTarget33Report().nextRecommendedAction(),
            SteelHook04TestFixtures.passedTarget33Report().primitiveKind(),
            SteelHook04TestFixtures.passedTarget33Report().approvedFixtureShapes(),
            SteelHook04TestFixtures.passedTarget33Report().unsupportedFixtureShapesRejected(),
            SteelHook04TestFixtures.passedTarget33Report().successfulProofCaseCount(),
            SteelHook04TestFixtures.passedTarget33Report().rejectionProofCaseCount(),
            SteelHook04TestFixtures.passedTarget33Report().proofCases(),
            SteelHook04TestFixtures.passedTarget33Report().findings(),
            SteelHook04TestFixtures.passedTarget33Report().offlineOnly(),
            true,
            SteelHook04TestFixtures.passedTarget33Report().classLoadingOccurred(),
            SteelHook04TestFixtures.passedTarget33Report().serverLaunchOccurred(),
            SteelHook04TestFixtures.passedTarget33Report().minecraftMainInvoked(),
            SteelHook04TestFixtures.passedTarget33Report().hookInstallationOccurred(),
            SteelHook04TestFixtures.passedTarget33Report().runtimeDispatchOccurred(),
            SteelHook04TestFixtures.passedTarget33Report().publicApiExposed(),
            SteelHook04TestFixtures.passedTarget33Report().javaAgentUsed(),
            SteelHook04TestFixtures.passedTarget33Report().mixinUsed(),
            SteelHook04TestFixtures.passedTarget33Report().javaModExecutionSandboxed());

    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(SteelHook04TestFixtures.passedTarget32Report(), blocked);

    assertFalse(report.proofReady());
  }

  @Test
  void validReportIncludesExactlyTwoSuccessfulProofCases() {
    SteelHook04InvokeRedirectWrapOfflineProofReport report =
        runner.run(
            SteelHook04TestFixtures.passedTarget32Report(),
            SteelHook04TestFixtures.passedTarget33Report());

    assertEquals(2, report.successfulProofCaseCount());
  }

  @Test
  void validReportIncludesInvokeRedirectProof() {
    assertTrue(labels().contains("invoke redirect replacement"));
  }

  @Test
  void validReportIncludesInvokeWrapProof() {
    assertTrue(labels().contains("invoke wrap replacement"));
  }

  @Test
  void validReportRecordsStrictOwnerNameDescriptorAndOpcodeMatching() {
    SteelHook04InvokeCallsiteProofCase redirectCase = report().proofCases().getFirst();

    assertEquals(
        SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
        redirectCase.targetOwnerInternalName());
    assertEquals(
        SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME, redirectCase.expectedInvokeName());
    assertEquals(
        SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR, redirectCase.expectedInvokeDescriptor());
    assertEquals("invokestatic", redirectCase.matchedInvokeOpcode());
  }

  @Test
  void validReportRecordsWrappedDelegateMetadataForInvokeWrap() {
    SteelHook04InvokeCallsiteProofCase wrapCase = report().proofCases().get(1);

    assertNotNull(wrapCase.wrappedDelegateOwnerInternalName());
    assertEquals(
        SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME, wrapCase.wrappedDelegateName());
    assertEquals(SteelHook04TestFixtures.INVOKE_WRAPPED_METHOD_NAME, wrapCase.wrapperName());
  }

  @Test
  void
      validReportLeavesRuntimeClassloadingHookInstallationMinecraftExecutionDispatcherExecutionPublicApiExposureAndSandboxClaimFalse() {
    SteelHook04InvokeRedirectWrapOfflineProofReport report = report();

    assertFalse(report.runtimeClassLoadingPathEnabled());
    assertFalse(report.classLoadingOccurred());
    assertFalse(report.serverLaunchOccurred());
    assertFalse(report.minecraftMainInvoked());
    assertFalse(report.hookInstallationOccurred());
    assertFalse(report.runtimeDispatchOccurred());
    assertFalse(report.publicApiExposed());
    assertFalse(report.javaModExecutionSandboxed());
  }

  private List<String> labels() {
    return report().proofCases().stream().map(SteelHook04InvokeCallsiteProofCase::label).toList();
  }

  private SteelHook04InvokeRedirectWrapOfflineProofReport report() {
    return runner.run(
        SteelHook04TestFixtures.passedTarget32Report(),
        SteelHook04TestFixtures.passedTarget33Report());
  }

  private SteelHook04PrimitiveBoundaryReport copyTarget32(boolean gatePassed) {
    SteelHook04PrimitiveBoundaryReport passed = SteelHook04TestFixtures.passedTarget32Report();
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

  private SteelHook04PrimitiveBoundaryReport copyTarget32BoundaryStatus(
      SteelHook04PrimitiveBoundaryStatus boundaryStatus) {
    SteelHook04PrimitiveBoundaryReport passed = SteelHook04TestFixtures.passedTarget32Report();
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
        boundaryStatus,
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

  private SteelHook04PrimitiveBoundaryReport removeCandidate(
      SteelHook04PrimitiveKind primitiveKind) {
    SteelHook04PrimitiveBoundaryReport passed = SteelHook04TestFixtures.passedTarget32Report();
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
        passed.candidates().stream()
            .filter(candidate -> candidate.primitiveKind() != primitiveKind)
            .toList(),
        passed.allowedFixtureShapes(),
        passed.unsupportedFixtureShapes(),
        passed.rejectionTaxonomy(),
        passed.evidenceRequirements(),
        passed.findings());
  }

  private SteelHook04PrimitiveBoundaryReport mutateCandidate(
      SteelHook04PrimitiveKind primitiveKind, boolean publicApiExposed, boolean runtimeReady) {
    SteelHook04PrimitiveBoundaryReport passed = SteelHook04TestFixtures.passedTarget32Report();
    List<SteelHook04PrimitiveCandidate> candidates =
        passed.candidates().stream()
            .map(
                candidate ->
                    candidate.primitiveKind() == primitiveKind
                        ? new SteelHook04PrimitiveCandidate(
                            candidate.id(),
                            candidate.primitiveKind(),
                            candidate.candidateStatus(),
                            candidate.internalOnly(),
                            publicApiExposed,
                            candidate.nonPublicApi(),
                            runtimeReady,
                            candidate.gatedRuntimeReady(),
                            candidate.implementedInTarget32(),
                            candidate.targetFollowOnPass(),
                            candidate.fixtureShapeSummary(),
                            candidate.evidenceSummary(),
                            candidate.allowedFixtureShapes(),
                            candidate.notes())
                        : candidate)
            .toList();
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
        candidates,
        passed.allowedFixtureShapes(),
        passed.unsupportedFixtureShapes(),
        passed.rejectionTaxonomy(),
        passed.evidenceRequirements(),
        passed.findings());
  }

  private SteelHook04ReturnValueInterceptOfflineProofReport copyTarget33(
      boolean proofReady,
      SteelHook04ReturnValueInterceptOfflineProofStatus proofStatus,
      SteelHook04PrimitiveKind primitiveKind,
      int successfulProofCaseCount,
      boolean sourceRuntimeSideEffectsSafe) {
    SteelHook04ReturnValueInterceptOfflineProofReport passed =
        SteelHook04TestFixtures.passedTarget33Report();
    return new SteelHook04ReturnValueInterceptOfflineProofReport(
        passed.schema(),
        passed.milestoneName(),
        passed.target(),
        passed.steelHookVersion(),
        passed.sourceTarget32Milestone(),
        passed.sourceTarget32BoundaryStatus(),
        passed.sourceTarget32GatePassed(),
        passed.sourceTarget32ApprovedPrimitiveCount(),
        passed.sourceReturnValueInterceptCandidatePresent(),
        passed.sourceReturnValueInterceptCandidateInternalOnly(),
        passed.sourceReturnValueInterceptCandidatePublicApiExposed(),
        passed.sourceReturnValueInterceptCandidateRuntimeReady(),
        passed.sourceReturnValueInterceptCandidateGatedRuntimeReady(),
        passed.sourceReturnValueInterceptCandidateImplementedInTarget32(),
        sourceRuntimeSideEffectsSafe,
        proofReady,
        proofStatus,
        passed.nextDirection(),
        passed.nextRecommendedAction(),
        primitiveKind,
        passed.approvedFixtureShapes(),
        passed.unsupportedFixtureShapesRejected(),
        successfulProofCaseCount,
        passed.rejectionProofCaseCount(),
        passed.proofCases(),
        passed.findings(),
        passed.offlineOnly(),
        passed.runtimeClassLoadingPathEnabled(),
        passed.classLoadingOccurred(),
        passed.serverLaunchOccurred(),
        passed.minecraftMainInvoked(),
        passed.hookInstallationOccurred(),
        passed.runtimeDispatchOccurred(),
        passed.publicApiExposed(),
        passed.javaAgentUsed(),
        passed.mixinUsed(),
        passed.javaModExecutionSandboxed());
  }
}
