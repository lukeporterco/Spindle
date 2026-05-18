package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook04GatedRuntimeProofRunnerTest {
  @TempDir Path tempDirectory;

  private final SteelHook04GatedRuntimeProofRunner runner =
      new SteelHook04GatedRuntimeProofRunner();

  @Test
  void validTarget32Target33AndTarget34SourcesProduceGatedRuntimeProofReadyTrue() throws Exception {
    SteelHook04GatedRuntimeProofReport report =
        runner.run(
            baseRuntimePlan(),
            SteelHook04TestFixtures.passedTarget32Report(),
            SteelHook04TestFixtures.passedTarget33Report(),
            SteelHook04TestFixtures.passedTarget34Report(),
            tempDirectory.resolve("fixtures"));

    assertTrue(report.gatedRuntimeProofReady());
    assertEquals(SteelHook04GatedRuntimeProofStatus.GATED_RUNTIME_PROOF_READY, report.status());
  }

  @Test
  void validReportContainsExactlyThreeRuntimeProofCases() throws Exception {
    SteelHook04GatedRuntimeProofReport report = validReport();

    assertEquals(3, report.runtimeClassLoaderProofCount());
    assertEquals(3, report.runtimeClassLoaderSuccessCount());
  }

  @Test
  void validReportProvesReturnValueInterceptClassDefinition() throws Exception {
    SteelHook04RuntimePrimitiveProof proof = validReport().returnValueInterceptProof();

    assertEquals(SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT, proof.primitiveKind());
    assertTrue(proof.runtimeClassLoadingSucceeded());
    assertTrue(proof.targetClassDefined());
  }

  @Test
  void validReportProvesInvokeRedirectClassDefinition() throws Exception {
    SteelHook04RuntimePrimitiveProof proof = validReport().invokeRedirectProof();

    assertEquals(SteelHook04PrimitiveKind.INVOKE_REDIRECT, proof.primitiveKind());
    assertTrue(proof.runtimeClassLoadingSucceeded());
    assertTrue(proof.targetClassDefined());
  }

  @Test
  void validReportProvesInvokeWrapClassDefinition() throws Exception {
    SteelHook04RuntimePrimitiveProof proof = validReport().invokeWrapProof();

    assertEquals(SteelHook04PrimitiveKind.INVOKE_WRAP, proof.primitiveKind());
    assertTrue(proof.runtimeClassLoadingSucceeded());
    assertTrue(proof.targetClassDefined());
  }

  @Test
  void eachProofUsesAnIsolatedRuntimeClassloaderId() throws Exception {
    SteelHook04GatedRuntimeProofReport report = validReport();

    assertEquals(
        "minecraft-runtime-steelhook-0-4-return-value-intercept",
        report.returnValueInterceptProof().runtimeLoaderId());
    assertEquals(
        "minecraft-runtime-steelhook-0-4-invoke-redirect",
        report.invokeRedirectProof().runtimeLoaderId());
    assertEquals(
        "minecraft-runtime-steelhook-0-4-invoke-wrap", report.invokeWrapProof().runtimeLoaderId());
  }

  @Test
  void eachProofDefinesTheTransformedClassThroughTheIsolatedRuntimeClassloader() throws Exception {
    SteelHook04GatedRuntimeProofReport report = validReport();

    assertTrue(report.returnValueInterceptProof().definedBySteelHookRuntimeClassLoader());
    assertTrue(report.invokeRedirectProof().definedBySteelHookRuntimeClassLoader());
    assertTrue(report.invokeWrapProof().definedBySteelHookRuntimeClassLoader());
  }

  @Test
  void eachProofLeavesClassInitializedFalse() throws Exception {
    SteelHook04GatedRuntimeProofReport report = validReport();

    assertFalse(report.returnValueInterceptProof().classInitialized());
    assertFalse(report.invokeRedirectProof().classInitialized());
    assertFalse(report.invokeWrapProof().classInitialized());
  }

  @Test
  void eachProofLeavesTargetMethodInvokedFalse() throws Exception {
    SteelHook04GatedRuntimeProofReport report = validReport();

    assertFalse(report.returnValueInterceptProof().targetMethodInvoked());
    assertFalse(report.invokeRedirectProof().targetMethodInvoked());
    assertFalse(report.invokeWrapProof().targetMethodInvoked());
  }

  @Test
  void invokeWrapProofLeavesWrapperExecutedFalse() throws Exception {
    assertFalse(validReport().invokeWrapProof().wrapperExecuted());
  }

  @Test
  void dispatcherInvocationCountsDoNotChange() throws Exception {
    SteelHook04GatedRuntimeProofReport report = validReport();

    assertEquals(
        report.returnValueInterceptProof().dispatcherInvocationCountBefore(),
        report.returnValueInterceptProof().dispatcherInvocationCountAfter());
    assertEquals(
        report.invokeRedirectProof().dispatcherInvocationCountBefore(),
        report.invokeRedirectProof().dispatcherInvocationCountAfter());
    assertEquals(
        report.invokeWrapProof().dispatcherInvocationCountBefore(),
        report.invokeWrapProof().dispatcherInvocationCountAfter());
  }

  @Test
  void unsupportedPrimitivePlanIsRejectedBeforeClassDefinition() throws Exception {
    SteelHook04GatedRuntimeProofReport report = validReport();

    assertTrue(report.unsupportedPrimitivePlanRejectedBeforeClassDefinition());
    assertFalse(report.unsupportedPrimitivePlanClassDefinitionAttempted());
  }

  @Test
  void nullTarget32SourceBlocksTarget35() throws Exception {
    SteelHook04GatedRuntimeProofReport report =
        runner.run(
            baseRuntimePlan(),
            null,
            SteelHook04TestFixtures.passedTarget33Report(),
            SteelHook04TestFixtures.passedTarget34Report(),
            tempDirectory.resolve("fixtures"));

    assertFalse(report.gatedRuntimeProofReady());
    assertEquals(SteelHook04GatedRuntimeProofStatus.BLOCKED, report.status());
  }

  @Test
  void nullTarget33SourceBlocksTarget35() throws Exception {
    SteelHook04GatedRuntimeProofReport report =
        runner.run(
            baseRuntimePlan(),
            SteelHook04TestFixtures.passedTarget32Report(),
            null,
            SteelHook04TestFixtures.passedTarget34Report(),
            tempDirectory.resolve("fixtures"));

    assertFalse(report.gatedRuntimeProofReady());
  }

  @Test
  void nullTarget34SourceBlocksTarget35() throws Exception {
    SteelHook04GatedRuntimeProofReport report =
        runner.run(
            baseRuntimePlan(),
            SteelHook04TestFixtures.passedTarget32Report(),
            SteelHook04TestFixtures.passedTarget33Report(),
            null,
            tempDirectory.resolve("fixtures"));

    assertFalse(report.gatedRuntimeProofReady());
  }

  @Test
  void target32GateFalseBlocksTarget35() throws Exception {
    SteelHook04PrimitiveBoundaryReport passed = SteelHook04TestFixtures.passedTarget32Report();
    SteelHook04PrimitiveBoundaryReport blocked =
        new SteelHook04PrimitiveBoundaryReport(
            passed.schema(),
            passed.milestoneName(),
            passed.target(),
            passed.steelHookVersion(),
            passed.sourceSteelHook03Milestone(),
            passed.sourceSteelHook03Status(),
            passed.sourceSteelHook03CompletionReady(),
            passed.sourceSteelHook03HandoffStatus(),
            false,
            "blocked",
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

    SteelHook04GatedRuntimeProofReport report =
        runner.run(
            baseRuntimePlan(),
            blocked,
            SteelHook04TestFixtures.passedTarget33Report(),
            SteelHook04TestFixtures.passedTarget34Report(),
            tempDirectory.resolve("fixtures"));

    assertFalse(report.gatedRuntimeProofReady());
  }

  @Test
  void missingApprovedPrimitiveCandidateBlocksTarget35() throws Exception {
    SteelHook04PrimitiveBoundaryReport passed = SteelHook04TestFixtures.passedTarget32Report();
    SteelHook04PrimitiveBoundaryReport blocked =
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
                    candidate -> candidate.primitiveKind() != SteelHook04PrimitiveKind.INVOKE_WRAP)
                .toList(),
            passed.allowedFixtureShapes(),
            passed.unsupportedFixtureShapes(),
            passed.rejectionTaxonomy(),
            passed.evidenceRequirements(),
            passed.findings());

    SteelHook04GatedRuntimeProofReport report =
        runner.run(
            baseRuntimePlan(),
            blocked,
            SteelHook04TestFixtures.passedTarget33Report(),
            SteelHook04TestFixtures.passedTarget34Report(),
            tempDirectory.resolve("fixtures"));

    assertFalse(report.gatedRuntimeProofReady());
  }

  @Test
  void target33ProofReadyFalseBlocksTarget35() throws Exception {
    SteelHook04ReturnValueInterceptOfflineProofReport passed =
        SteelHook04TestFixtures.passedTarget33Report();
    SteelHook04ReturnValueInterceptOfflineProofReport blocked =
        new SteelHook04ReturnValueInterceptOfflineProofReport(
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
            passed.sourceRuntimeSideEffectsSafe(),
            false,
            passed.proofStatus(),
            passed.nextDirection(),
            passed.nextRecommendedAction(),
            passed.primitiveKind(),
            passed.approvedFixtureShapes(),
            passed.unsupportedFixtureShapesRejected(),
            passed.successfulProofCaseCount(),
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

    SteelHook04GatedRuntimeProofReport report =
        runner.run(
            baseRuntimePlan(),
            SteelHook04TestFixtures.passedTarget32Report(),
            blocked,
            SteelHook04TestFixtures.passedTarget34Report(),
            tempDirectory.resolve("fixtures"));

    assertFalse(report.gatedRuntimeProofReady());
  }

  @Test
  void target33PrimitiveKindNotReturnValueInterceptBlocksTarget35() throws Exception {
    SteelHook04ReturnValueInterceptOfflineProofReport passed =
        SteelHook04TestFixtures.passedTarget33Report();
    SteelHook04ReturnValueInterceptOfflineProofReport blocked =
        new SteelHook04ReturnValueInterceptOfflineProofReport(
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
            passed.sourceRuntimeSideEffectsSafe(),
            passed.proofReady(),
            passed.proofStatus(),
            passed.nextDirection(),
            passed.nextRecommendedAction(),
            SteelHook04PrimitiveKind.INVOKE_REDIRECT,
            passed.approvedFixtureShapes(),
            passed.unsupportedFixtureShapesRejected(),
            passed.successfulProofCaseCount(),
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

    assertFalse(
        runner
            .run(
                baseRuntimePlan(),
                SteelHook04TestFixtures.passedTarget32Report(),
                blocked,
                SteelHook04TestFixtures.passedTarget34Report(),
                tempDirectory.resolve("fixtures"))
            .gatedRuntimeProofReady());
  }

  @Test
  void target33RuntimeSideEffectBooleanTrueBlocksTarget35() throws Exception {
    SteelHook04ReturnValueInterceptOfflineProofReport passed =
        SteelHook04TestFixtures.passedTarget33Report();
    SteelHook04ReturnValueInterceptOfflineProofReport blocked =
        new SteelHook04ReturnValueInterceptOfflineProofReport(
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
            passed.sourceRuntimeSideEffectsSafe(),
            passed.proofReady(),
            passed.proofStatus(),
            passed.nextDirection(),
            passed.nextRecommendedAction(),
            passed.primitiveKind(),
            passed.approvedFixtureShapes(),
            passed.unsupportedFixtureShapesRejected(),
            passed.successfulProofCaseCount(),
            passed.rejectionProofCaseCount(),
            passed.proofCases(),
            passed.findings(),
            passed.offlineOnly(),
            true,
            passed.classLoadingOccurred(),
            passed.serverLaunchOccurred(),
            passed.minecraftMainInvoked(),
            passed.hookInstallationOccurred(),
            passed.runtimeDispatchOccurred(),
            passed.publicApiExposed(),
            passed.javaAgentUsed(),
            passed.mixinUsed(),
            passed.javaModExecutionSandboxed());

    assertFalse(
        runner
            .run(
                baseRuntimePlan(),
                SteelHook04TestFixtures.passedTarget32Report(),
                blocked,
                SteelHook04TestFixtures.passedTarget34Report(),
                tempDirectory.resolve("fixtures"))
            .gatedRuntimeProofReady());
  }

  @Test
  void target34ProofReadyFalseBlocksTarget35() throws Exception {
    SteelHook04InvokeRedirectWrapOfflineProofReport passed =
        SteelHook04TestFixtures.passedTarget34Report();
    SteelHook04InvokeRedirectWrapOfflineProofReport blocked =
        new SteelHook04InvokeRedirectWrapOfflineProofReport(
            passed.schema(),
            passed.milestoneName(),
            passed.target(),
            passed.steelHookVersion(),
            passed.sourceTarget32Milestone(),
            passed.sourceTarget32BoundaryStatus(),
            passed.sourceTarget32GatePassed(),
            passed.sourceTarget32ApprovedPrimitiveCount(),
            passed.sourceInvokeRedirectCandidatePresent(),
            passed.sourceInvokeRedirectCandidateInternalOnly(),
            passed.sourceInvokeRedirectCandidatePublicApiExposed(),
            passed.sourceInvokeRedirectCandidateRuntimeReady(),
            passed.sourceInvokeRedirectCandidateGatedRuntimeReady(),
            passed.sourceInvokeRedirectCandidateImplementedInTarget32(),
            passed.sourceInvokeWrapCandidatePresent(),
            passed.sourceInvokeWrapCandidateInternalOnly(),
            passed.sourceInvokeWrapCandidatePublicApiExposed(),
            passed.sourceInvokeWrapCandidateRuntimeReady(),
            passed.sourceInvokeWrapCandidateGatedRuntimeReady(),
            passed.sourceInvokeWrapCandidateImplementedInTarget32(),
            passed.sourceTarget32RuntimeSideEffectsSafe(),
            passed.sourceTarget33Milestone(),
            passed.sourceTarget33ProofStatus(),
            passed.sourceTarget33ProofReady(),
            passed.sourceTarget33PrimitiveKind(),
            passed.sourceTarget33SuccessfulProofCaseCount(),
            passed.sourceTarget33RuntimeSideEffectsSafe(),
            false,
            passed.proofStatus(),
            passed.nextDirection(),
            passed.nextRecommendedAction(),
            passed.approvedPrimitiveKinds(),
            passed.approvedFixtureShape(),
            passed.unsupportedFixtureShapesRejected(),
            passed.successfulProofCaseCount(),
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

    assertFalse(
        runner
            .run(
                baseRuntimePlan(),
                SteelHook04TestFixtures.passedTarget32Report(),
                SteelHook04TestFixtures.passedTarget33Report(),
                blocked,
                tempDirectory.resolve("fixtures"))
            .gatedRuntimeProofReady());
  }

  @Test
  void target34MissingInvokeRedirectBlocksTarget35() throws Exception {
    SteelHook04InvokeRedirectWrapOfflineProofReport passed =
        SteelHook04TestFixtures.passedTarget34Report();
    SteelHook04InvokeRedirectWrapOfflineProofReport blocked =
        new SteelHook04InvokeRedirectWrapOfflineProofReport(
            passed.schema(),
            passed.milestoneName(),
            passed.target(),
            passed.steelHookVersion(),
            passed.sourceTarget32Milestone(),
            passed.sourceTarget32BoundaryStatus(),
            passed.sourceTarget32GatePassed(),
            passed.sourceTarget32ApprovedPrimitiveCount(),
            passed.sourceInvokeRedirectCandidatePresent(),
            passed.sourceInvokeRedirectCandidateInternalOnly(),
            passed.sourceInvokeRedirectCandidatePublicApiExposed(),
            passed.sourceInvokeRedirectCandidateRuntimeReady(),
            passed.sourceInvokeRedirectCandidateGatedRuntimeReady(),
            passed.sourceInvokeRedirectCandidateImplementedInTarget32(),
            passed.sourceInvokeWrapCandidatePresent(),
            passed.sourceInvokeWrapCandidateInternalOnly(),
            passed.sourceInvokeWrapCandidatePublicApiExposed(),
            passed.sourceInvokeWrapCandidateRuntimeReady(),
            passed.sourceInvokeWrapCandidateGatedRuntimeReady(),
            passed.sourceInvokeWrapCandidateImplementedInTarget32(),
            passed.sourceTarget32RuntimeSideEffectsSafe(),
            passed.sourceTarget33Milestone(),
            passed.sourceTarget33ProofStatus(),
            passed.sourceTarget33ProofReady(),
            passed.sourceTarget33PrimitiveKind(),
            passed.sourceTarget33SuccessfulProofCaseCount(),
            passed.sourceTarget33RuntimeSideEffectsSafe(),
            passed.proofReady(),
            passed.proofStatus(),
            passed.nextDirection(),
            passed.nextRecommendedAction(),
            List.of(SteelHook04PrimitiveKind.INVOKE_WRAP),
            passed.approvedFixtureShape(),
            passed.unsupportedFixtureShapesRejected(),
            passed.successfulProofCaseCount(),
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

    assertFalse(
        runner
            .run(
                baseRuntimePlan(),
                SteelHook04TestFixtures.passedTarget32Report(),
                SteelHook04TestFixtures.passedTarget33Report(),
                blocked,
                tempDirectory.resolve("fixtures"))
            .gatedRuntimeProofReady());
  }

  @Test
  void target34MissingInvokeWrapBlocksTarget35() throws Exception {
    SteelHook04InvokeRedirectWrapOfflineProofReport passed =
        SteelHook04TestFixtures.passedTarget34Report();
    SteelHook04InvokeRedirectWrapOfflineProofReport blocked =
        new SteelHook04InvokeRedirectWrapOfflineProofReport(
            passed.schema(),
            passed.milestoneName(),
            passed.target(),
            passed.steelHookVersion(),
            passed.sourceTarget32Milestone(),
            passed.sourceTarget32BoundaryStatus(),
            passed.sourceTarget32GatePassed(),
            passed.sourceTarget32ApprovedPrimitiveCount(),
            passed.sourceInvokeRedirectCandidatePresent(),
            passed.sourceInvokeRedirectCandidateInternalOnly(),
            passed.sourceInvokeRedirectCandidatePublicApiExposed(),
            passed.sourceInvokeRedirectCandidateRuntimeReady(),
            passed.sourceInvokeRedirectCandidateGatedRuntimeReady(),
            passed.sourceInvokeRedirectCandidateImplementedInTarget32(),
            passed.sourceInvokeWrapCandidatePresent(),
            passed.sourceInvokeWrapCandidateInternalOnly(),
            passed.sourceInvokeWrapCandidatePublicApiExposed(),
            passed.sourceInvokeWrapCandidateRuntimeReady(),
            passed.sourceInvokeWrapCandidateGatedRuntimeReady(),
            passed.sourceInvokeWrapCandidateImplementedInTarget32(),
            passed.sourceTarget32RuntimeSideEffectsSafe(),
            passed.sourceTarget33Milestone(),
            passed.sourceTarget33ProofStatus(),
            passed.sourceTarget33ProofReady(),
            passed.sourceTarget33PrimitiveKind(),
            passed.sourceTarget33SuccessfulProofCaseCount(),
            passed.sourceTarget33RuntimeSideEffectsSafe(),
            passed.proofReady(),
            passed.proofStatus(),
            passed.nextDirection(),
            passed.nextRecommendedAction(),
            List.of(SteelHook04PrimitiveKind.INVOKE_REDIRECT),
            passed.approvedFixtureShape(),
            passed.unsupportedFixtureShapesRejected(),
            passed.successfulProofCaseCount(),
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

    assertFalse(
        runner
            .run(
                baseRuntimePlan(),
                SteelHook04TestFixtures.passedTarget32Report(),
                SteelHook04TestFixtures.passedTarget33Report(),
                blocked,
                tempDirectory.resolve("fixtures"))
            .gatedRuntimeProofReady());
  }

  @Test
  void target34RuntimeSideEffectBooleanTrueBlocksTarget35() throws Exception {
    SteelHook04InvokeRedirectWrapOfflineProofReport passed =
        SteelHook04TestFixtures.passedTarget34Report();
    SteelHook04InvokeRedirectWrapOfflineProofReport blocked =
        new SteelHook04InvokeRedirectWrapOfflineProofReport(
            passed.schema(),
            passed.milestoneName(),
            passed.target(),
            passed.steelHookVersion(),
            passed.sourceTarget32Milestone(),
            passed.sourceTarget32BoundaryStatus(),
            passed.sourceTarget32GatePassed(),
            passed.sourceTarget32ApprovedPrimitiveCount(),
            passed.sourceInvokeRedirectCandidatePresent(),
            passed.sourceInvokeRedirectCandidateInternalOnly(),
            passed.sourceInvokeRedirectCandidatePublicApiExposed(),
            passed.sourceInvokeRedirectCandidateRuntimeReady(),
            passed.sourceInvokeRedirectCandidateGatedRuntimeReady(),
            passed.sourceInvokeRedirectCandidateImplementedInTarget32(),
            passed.sourceInvokeWrapCandidatePresent(),
            passed.sourceInvokeWrapCandidateInternalOnly(),
            passed.sourceInvokeWrapCandidatePublicApiExposed(),
            passed.sourceInvokeWrapCandidateRuntimeReady(),
            passed.sourceInvokeWrapCandidateGatedRuntimeReady(),
            passed.sourceInvokeWrapCandidateImplementedInTarget32(),
            passed.sourceTarget32RuntimeSideEffectsSafe(),
            passed.sourceTarget33Milestone(),
            passed.sourceTarget33ProofStatus(),
            passed.sourceTarget33ProofReady(),
            passed.sourceTarget33PrimitiveKind(),
            passed.sourceTarget33SuccessfulProofCaseCount(),
            passed.sourceTarget33RuntimeSideEffectsSafe(),
            passed.proofReady(),
            passed.proofStatus(),
            passed.nextDirection(),
            passed.nextRecommendedAction(),
            passed.approvedPrimitiveKinds(),
            passed.approvedFixtureShape(),
            passed.unsupportedFixtureShapesRejected(),
            passed.successfulProofCaseCount(),
            passed.rejectionProofCaseCount(),
            passed.proofCases(),
            passed.findings(),
            passed.offlineOnly(),
            true,
            passed.classLoadingOccurred(),
            passed.serverLaunchOccurred(),
            passed.minecraftMainInvoked(),
            passed.hookInstallationOccurred(),
            passed.runtimeDispatchOccurred(),
            passed.publicApiExposed(),
            passed.javaAgentUsed(),
            passed.mixinUsed(),
            passed.javaModExecutionSandboxed());

    assertFalse(
        runner
            .run(
                baseRuntimePlan(),
                SteelHook04TestFixtures.passedTarget32Report(),
                SteelHook04TestFixtures.passedTarget33Report(),
                blocked,
                tempDirectory.resolve("fixtures"))
            .gatedRuntimeProofReady());
  }

  @Test
  void
      validReportLeavesServerLaunchMinecraftMainHookInstallationRuntimeDispatchPublicApiExposureJavaAgentMixinAndSandboxClaimFalse()
          throws Exception {
    SteelHook04GatedRuntimeProofReport report = validReport();

    assertFalse(report.serverLaunchOccurred());
    assertFalse(report.minecraftMainInvoked());
    assertFalse(report.hookInstallationOccurred());
    assertFalse(report.runtimeDispatchOccurred());
    assertFalse(report.publicApiExposed());
    assertFalse(report.javaAgentUsed());
    assertFalse(report.mixinUsed());
    assertFalse(report.javaModExecutionSandboxed());
    assertNotNull(report.targetClassesDefined());
  }

  private SteelHook04GatedRuntimeProofReport validReport() throws Exception {
    return runner.run(
        baseRuntimePlan(),
        SteelHook04TestFixtures.passedTarget32Report(),
        SteelHook04TestFixtures.passedTarget33Report(),
        SteelHook04TestFixtures.passedTarget34Report(),
        tempDirectory.resolve("fixtures"));
  }

  private MinecraftServerRuntimePlan baseRuntimePlan() throws IOException {
    Path serverJar =
        SteelHook02TestFixtures.createRuntimeJar(
            tempDirectory.resolve("hook-server.jar"),
            SteelHook02TestFixtures.readResourceBytes("net/minecraft/server/Main.class"));
    return SteelHook02TestFixtures.runtimePlan(serverJar);
  }
}
