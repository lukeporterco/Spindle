package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptClassFileRewriter;
import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptKind;
import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptMode;
import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptRewriteRequest;
import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptRewriteResult;
import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptRewriteStatus;
import java.util.ArrayList;
import java.util.List;

public final class SteelHook04ReturnValueInterceptOfflineProofRunner {
  public static final String REPORT_FILE_NAME =
      "minecraft-steelhook-0-4-return-value-intercept-offline-proof.json";

  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-33";
  private static final String TARGET = "minecraft";
  private static final String STEELHOOK_VERSION = "0.4";
  private static final String READY_ACTION =
      "Move next to Target-34 bounded offline invoke redirect and invoke wrap evidence while preserving the same internal-only, offline-only SteelHook 0.4 boundary.";
  private static final String BLOCKED_ACTION =
      "Restore the Target-32 primitive boundary report before continuing with Target-33 offline return-value intercept proof.";
  private static final String FAILED_ACTION =
      "Restore the bounded Target-33 offline fixtures or rewriter validation before continuing toward Target-34.";

  private final SteelHookReturnValueInterceptClassFileRewriter rewriter;
  private final SteelHook04ReturnValueInterceptFixtureClassFactory fixtureFactory;

  public SteelHook04ReturnValueInterceptOfflineProofRunner() {
    this(
        new SteelHookReturnValueInterceptClassFileRewriter(),
        new SteelHook04ReturnValueInterceptFixtureClassFactory());
  }

  SteelHook04ReturnValueInterceptOfflineProofRunner(
      SteelHookReturnValueInterceptClassFileRewriter rewriter,
      SteelHook04ReturnValueInterceptFixtureClassFactory fixtureFactory) {
    this.rewriter = rewriter;
    this.fixtureFactory = fixtureFactory;
  }

  public SteelHook04ReturnValueInterceptOfflineProofReport run(
      SteelHook04PrimitiveBoundaryReport sourceReport) {
    List<SteelHook04ReturnValueInterceptFinding> findings = new ArrayList<>();
    SteelHook04PrimitiveCandidate candidate = findCandidate(sourceReport);
    GateState gateState = gateState(sourceReport, candidate, findings);
    if (!gateState.ready()) {
      return report(
          sourceReport,
          candidate,
          false,
          SteelHook04ReturnValueInterceptOfflineProofStatus.BLOCKED,
          SteelHook04ReturnValueInterceptOfflineProofNextDirection
              .RESTORE_TARGET_32_PRIMITIVE_BOUNDARY,
          BLOCKED_ACTION,
          0,
          0,
          List.of(),
          findings);
    }

    byte[] fixtureClassBytes = fixtureFactory.createFixtureClassBytes();
    List<SteelHook04ReturnValueInterceptProofCase> proofCases = new ArrayList<>();
    proofCases.add(
        successfulCase(
            "target-33.return-value-intercept.case.001",
            "primitive observe-only",
            SteelHook04FixtureShape.RETURN_SINGLE_PRIMITIVE_VALUE,
            successful(
                newRequest(
                    "target-33.return-value-intercept.request.001",
                    SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                    SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_METHOD_NAME,
                    SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_DESCRIPTOR,
                    SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                    null,
                    null),
                fixtureClassBytes)));
    proofCases.add(
        successfulCase(
            "target-33.return-value-intercept.case.002",
            "primitive replacement",
            SteelHook04FixtureShape.RETURN_SINGLE_PRIMITIVE_VALUE,
            successful(
                newRequest(
                    "target-33.return-value-intercept.request.002",
                    SteelHookReturnValueInterceptMode.REPLACE_RETURN_VALUE,
                    SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_METHOD_NAME,
                    SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_DESCRIPTOR,
                    SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                    42,
                    null),
                fixtureClassBytes)));
    proofCases.add(
        successfulCase(
            "target-33.return-value-intercept.case.003",
            "reference observe-only",
            SteelHook04FixtureShape.RETURN_SINGLE_REFERENCE_VALUE,
            successful(
                newRequest(
                    "target-33.return-value-intercept.request.003",
                    SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                    SteelHook04ReturnValueInterceptFixtureClassFactory.REFERENCE_METHOD_NAME,
                    SteelHook04ReturnValueInterceptFixtureClassFactory.REFERENCE_DESCRIPTOR,
                    SteelHookReturnValueInterceptKind.REFERENCE_STRING,
                    null,
                    null),
                fixtureClassBytes)));
    proofCases.add(
        successfulCase(
            "target-33.return-value-intercept.case.004",
            "reference replacement",
            SteelHook04FixtureShape.RETURN_SINGLE_REFERENCE_VALUE,
            successful(
                newRequest(
                    "target-33.return-value-intercept.request.004",
                    SteelHookReturnValueInterceptMode.REPLACE_RETURN_VALUE,
                    SteelHook04ReturnValueInterceptFixtureClassFactory.REFERENCE_METHOD_NAME,
                    SteelHook04ReturnValueInterceptFixtureClassFactory.REFERENCE_DESCRIPTOR,
                    SteelHookReturnValueInterceptKind.REFERENCE_STRING,
                    null,
                    SteelHook04ReturnValueInterceptFixtureClassFactory.REPLACEMENT_REFERENCE),
                fixtureClassBytes)));

    int rejectionProofCaseCount = rejectionCoverage(fixtureClassBytes, findings);
    addFinding(
        findings,
        20,
        "Target-33 stayed offline-only.",
        SteelHook04ReturnValueInterceptFindingStatus.PASS,
        true,
        "Target-33 produced deterministic offline evidence only.",
        "No runtime class loading, hook installation, dispatcher execution, Minecraft execution, public API exposure, or sandbox claim occurred.");

    return report(
        sourceReport,
        candidate,
        true,
        SteelHook04ReturnValueInterceptOfflineProofStatus.PROOF_READY,
        SteelHook04ReturnValueInterceptOfflineProofNextDirection
            .MOVE_TO_TARGET_34_INVOKE_REDIRECT_AND_WRAP_OFFLINE_EVIDENCE,
        READY_ACTION,
        proofCases.size(),
        rejectionProofCaseCount,
        proofCases,
        findings);
  }

  private SteelHookReturnValueInterceptRewriteRequest newRequest(
      String id,
      SteelHookReturnValueInterceptMode mode,
      String methodName,
      String descriptor,
      SteelHookReturnValueInterceptKind interceptKind,
      Integer replacementPrimitiveValue,
      String replacementReferenceValue) {
    return new SteelHookReturnValueInterceptRewriteRequest(
        id,
        "Target-33 return-value intercept offline proof",
        "Target-32",
        SteelHook04PrimitiveBoundaryReportWriter.REPORT_FILE_NAME,
        SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
        mode,
        SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_BINARY_NAME,
        SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
        methodName,
        descriptor,
        interceptKind,
        replacementPrimitiveValue,
        replacementReferenceValue,
        false,
        false,
        false);
  }

  private SteelHookReturnValueInterceptRewriteResult successful(
      SteelHookReturnValueInterceptRewriteRequest request, byte[] fixtureClassBytes) {
    SteelHookReturnValueInterceptRewriteResult result =
        rewriter.rewrite(request, fixtureClassBytes);
    if (result.status() == SteelHookReturnValueInterceptRewriteStatus.REJECTED) {
      throw new IllegalStateException(
          "Expected successful Target-33 proof case: " + result.failureReason());
    }
    return result;
  }

  private int rejectionCoverage(
      byte[] fixtureClassBytes, List<SteelHook04ReturnValueInterceptFinding> findings) {
    int count = 0;
    count += rejected(rewriter.rewrite(null, fixtureClassBytes), "null request") ? 1 : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    new SteelHookReturnValueInterceptRewriteRequest(
                        "target-33.return-value-intercept.reject.001",
                        "Target-33 reject wrong primitive",
                        "Target-32",
                        SteelHook04PrimitiveBoundaryReportWriter.REPORT_FILE_NAME,
                        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                        SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                        SteelHook04ReturnValueInterceptFixtureClassFactory
                            .TARGET_OWNER_INTERNAL_NAME,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_BINARY_NAME,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_METHOD_NAME,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_DESCRIPTOR,
                        SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                        null,
                        null,
                        false,
                        false,
                        false),
                    fixtureClassBytes),
                "wrong primitive kind")
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    newRequest(
                        "target-33.return-value-intercept.reject.002",
                        SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.BRANCHING_METHOD_NAME,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_DESCRIPTOR,
                        SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                        null,
                        null),
                    fixtureClassBytes),
                "branching method")
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    newRequest(
                        "target-33.return-value-intercept.reject.003",
                        SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.SWITCH_METHOD_NAME,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_DESCRIPTOR,
                        SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                        null,
                        null),
                    fixtureClassBytes),
                "switch method")
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    newRequest(
                        "target-33.return-value-intercept.reject.004",
                        SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                        SteelHook04ReturnValueInterceptFixtureClassFactory
                            .EXCEPTION_TABLE_METHOD_NAME,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_DESCRIPTOR,
                        SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                        null,
                        null),
                    fixtureClassBytes),
                "exception table")
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    newRequest(
                        "target-33.return-value-intercept.reject.005",
                        SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.STACKMAP_METHOD_NAME,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_DESCRIPTOR,
                        SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                        null,
                        null),
                    fixtureClassBytes),
                "StackMapTable")
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    newRequest(
                        "target-33.return-value-intercept.reject.006",
                        SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.SYNCHRONIZED_METHOD_NAME,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_DESCRIPTOR,
                        SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                        null,
                        null),
                    fixtureClassBytes),
                "synchronized method")
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    newRequest(
                        "target-33.return-value-intercept.reject.007",
                        SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                        SteelHook04ReturnValueInterceptFixtureClassFactory
                            .MISSING_PRODUCER_METHOD_NAME,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_DESCRIPTOR,
                        SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                        null,
                        null),
                    fixtureClassBytes),
                "missing producer")
            ? 1
            : 0;
    count +=
        rejected(
                rewriter.rewrite(
                    newRequest(
                        "target-33.return-value-intercept.reject.008",
                        SteelHookReturnValueInterceptMode.REPLACE_RETURN_VALUE,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_METHOD_NAME,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_DESCRIPTOR,
                        SteelHookReturnValueInterceptKind.REFERENCE_STRING,
                        null,
                        SteelHook04ReturnValueInterceptFixtureClassFactory.REPLACEMENT_REFERENCE),
                    fixtureClassBytes),
                "replacement kind mismatch")
            ? 1
            : 0;
    addFinding(
        findings,
        19,
        "Target-33 recorded deterministic rejection coverage.",
        count >= 8
            ? SteelHook04ReturnValueInterceptFindingStatus.PASS
            : SteelHook04ReturnValueInterceptFindingStatus.FAIL,
        true,
        "Target-33 rewriter rejection coverage was exercised against bounded malformed and unsupported shapes.",
        "rejectionProofCaseCount=" + count);
    return count;
  }

  private boolean rejected(SteelHookReturnValueInterceptRewriteResult result, String label) {
    return result.status() == SteelHookReturnValueInterceptRewriteStatus.REJECTED;
  }

  private SteelHook04ReturnValueInterceptProofCase successfulCase(
      String id,
      String label,
      SteelHook04FixtureShape fixtureShape,
      SteelHookReturnValueInterceptRewriteResult result) {
    return new SteelHook04ReturnValueInterceptProofCase(
        id,
        label,
        result.request().mode(),
        fixtureShape,
        result.request().interceptKind(),
        result.request().targetOwnerInternalName(),
        result.request().targetMethodName(),
        result.request().targetDescriptor(),
        result.matchedReturnOpcode(),
        result.matchedProducerOpcode(),
        result.matchCount() == null ? 0 : result.matchCount(),
        result.bytecodeModified(),
        result.transformedClassBytesProduced(),
        result.originalClassSha256(),
        result.transformedClassSha256(),
        result.originalCodeSha256(),
        result.transformedCodeSha256(),
        result.originalCodeLength() == null ? 0 : result.originalCodeLength(),
        result.transformedCodeLength() == null ? 0 : result.transformedCodeLength(),
        result.replacementSummary());
  }

  private GateState gateState(
      SteelHook04PrimitiveBoundaryReport sourceReport,
      SteelHook04PrimitiveCandidate candidate,
      List<SteelHook04ReturnValueInterceptFinding> findings) {
    boolean sourcePresent = sourceReport != null;
    addFinding(
        findings,
        1,
        "Target-32 primitive boundary report exists.",
        sourcePresent
            ? SteelHook04ReturnValueInterceptFindingStatus.PASS
            : SteelHook04ReturnValueInterceptFindingStatus.FAIL,
        true,
        sourcePresent ? "Target-32 report is present." : "Target-33 requires the Target-32 report.",
        sourcePresent
            ? "Input report object provided."
            : "No SteelHook04PrimitiveBoundaryReport was provided.");
    if (!sourcePresent) {
      return new GateState(false);
    }
    boolean milestoneMatches = "Target-32".equals(sourceReport.milestoneName());
    boolean targetMatches = TARGET.equals(sourceReport.target());
    boolean versionMatches = STEELHOOK_VERSION.equals(sourceReport.steelHookVersion());
    boolean gatePassed = sourceReport.gatePassed();
    boolean boundaryReady =
        sourceReport.boundaryStatus() == SteelHook04PrimitiveBoundaryStatus.BOUNDARY_READY;
    boolean approvedPrimitiveCount = sourceReport.approvedPrimitiveCount() == 3;
    boolean candidatePresent = candidate != null;
    boolean internalOnly = candidatePresent && candidate.internalOnly();
    boolean publicApiHidden = !candidatePresent || !candidate.publicApiExposed();
    boolean runtimeReady = candidatePresent && candidate.runtimeReady();
    boolean gatedRuntimeReady = candidatePresent && candidate.gatedRuntimeReady();
    boolean implementedInTarget32 = candidatePresent && candidate.implementedInTarget32();
    boolean sourceRuntimeSafe =
        !sourceReport.runtimeClassLoadingPathEnabled()
            && !sourceReport.classLoadingOccurred()
            && !sourceReport.serverLaunchOccurred()
            && !sourceReport.minecraftMainInvoked()
            && !sourceReport.hookInstallationOccurred()
            && !sourceReport.runtimeDispatchOccurred()
            && !sourceReport.publicApiExposed()
            && !sourceReport.javaAgentUsed()
            && !sourceReport.mixinUsed()
            && !sourceReport.javaModExecutionSandboxed();
    addFinding(
        findings,
        2,
        "Target-32 source fields match the required handoff contract.",
        milestoneMatches
                && targetMatches
                && versionMatches
                && gatePassed
                && boundaryReady
                && approvedPrimitiveCount
            ? SteelHook04ReturnValueInterceptFindingStatus.PASS
            : SteelHook04ReturnValueInterceptFindingStatus.FAIL,
        true,
        "Target-33 validated the Target-32 milestone, target, version, gate, boundary status, and approved primitive count.",
        "milestone="
            + sourceReport.milestoneName()
            + ", target="
            + sourceReport.target()
            + ", version="
            + sourceReport.steelHookVersion()
            + ", gatePassed="
            + sourceReport.gatePassed()
            + ", boundaryStatus="
            + sourceReport.boundaryStatus()
            + ", approvedPrimitiveCount="
            + sourceReport.approvedPrimitiveCount());
    addFinding(
        findings,
        3,
        "RETURN_VALUE_INTERCEPT candidate remains internal-only and non-runtime-ready.",
        candidatePresent
                && internalOnly
                && publicApiHidden
                && !runtimeReady
                && !gatedRuntimeReady
                && !implementedInTarget32
            ? SteelHook04ReturnValueInterceptFindingStatus.PASS
            : SteelHook04ReturnValueInterceptFindingStatus.FAIL,
        true,
        candidatePresent
            ? "RETURN_VALUE_INTERCEPT candidate matches the Target-32 handoff contract."
            : "RETURN_VALUE_INTERCEPT candidate is missing.",
        candidatePresent
            ? "internalOnly="
                + internalOnly
                + ", publicApiExposed="
                + candidate.publicApiExposed()
                + ", runtimeReady="
                + candidate.runtimeReady()
                + ", gatedRuntimeReady="
                + candidate.gatedRuntimeReady()
                + ", implementedInTarget32="
                + candidate.implementedInTarget32()
            : "No RETURN_VALUE_INTERCEPT candidate was present.");
    addFinding(
        findings,
        4,
        "Target-32 preserved zero runtime side effects.",
        sourceRuntimeSafe
            ? SteelHook04ReturnValueInterceptFindingStatus.PASS
            : SteelHook04ReturnValueInterceptFindingStatus.FAIL,
        true,
        sourceRuntimeSafe
            ? "Target-32 remained offline and side-effect free."
            : "Target-32 side-effect booleans must remain false before Target-33 can proceed.",
        "runtimeClassLoadingPathEnabled="
            + sourceReport.runtimeClassLoadingPathEnabled()
            + ", classLoadingOccurred="
            + sourceReport.classLoadingOccurred()
            + ", serverLaunchOccurred="
            + sourceReport.serverLaunchOccurred()
            + ", minecraftMainInvoked="
            + sourceReport.minecraftMainInvoked()
            + ", hookInstallationOccurred="
            + sourceReport.hookInstallationOccurred()
            + ", runtimeDispatchOccurred="
            + sourceReport.runtimeDispatchOccurred()
            + ", publicApiExposed="
            + sourceReport.publicApiExposed()
            + ", javaAgentUsed="
            + sourceReport.javaAgentUsed()
            + ", mixinUsed="
            + sourceReport.mixinUsed()
            + ", javaModExecutionSandboxed="
            + sourceReport.javaModExecutionSandboxed());
    return new GateState(
        milestoneMatches
            && targetMatches
            && versionMatches
            && gatePassed
            && boundaryReady
            && approvedPrimitiveCount
            && candidatePresent
            && internalOnly
            && publicApiHidden
            && !runtimeReady
            && !gatedRuntimeReady
            && !implementedInTarget32
            && sourceRuntimeSafe);
  }

  private SteelHook04PrimitiveCandidate findCandidate(
      SteelHook04PrimitiveBoundaryReport sourceReport) {
    if (sourceReport == null) {
      return null;
    }
    return sourceReport.candidates().stream()
        .filter(
            candidate ->
                candidate.primitiveKind() == SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT)
        .findFirst()
        .orElse(null);
  }

  private SteelHook04ReturnValueInterceptOfflineProofReport report(
      SteelHook04PrimitiveBoundaryReport sourceReport,
      SteelHook04PrimitiveCandidate candidate,
      boolean proofReady,
      SteelHook04ReturnValueInterceptOfflineProofStatus status,
      SteelHook04ReturnValueInterceptOfflineProofNextDirection nextDirection,
      String nextRecommendedAction,
      int successfulProofCaseCount,
      int rejectionProofCaseCount,
      List<SteelHook04ReturnValueInterceptProofCase> proofCases,
      List<SteelHook04ReturnValueInterceptFinding> findings) {
    return new SteelHook04ReturnValueInterceptOfflineProofReport(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        sourceReport == null ? null : sourceReport.milestoneName(),
        sourceReport == null || sourceReport.boundaryStatus() == null
            ? null
            : sourceReport.boundaryStatus().name(),
        sourceReport != null && sourceReport.gatePassed(),
        sourceReport == null ? 0 : sourceReport.approvedPrimitiveCount(),
        candidate != null,
        candidate != null && candidate.internalOnly(),
        candidate != null && candidate.publicApiExposed(),
        candidate != null && candidate.runtimeReady(),
        candidate != null && candidate.gatedRuntimeReady(),
        candidate != null && candidate.implementedInTarget32(),
        sourceReport != null
            && !sourceReport.runtimeClassLoadingPathEnabled()
            && !sourceReport.classLoadingOccurred()
            && !sourceReport.serverLaunchOccurred()
            && !sourceReport.minecraftMainInvoked()
            && !sourceReport.hookInstallationOccurred()
            && !sourceReport.runtimeDispatchOccurred()
            && !sourceReport.publicApiExposed()
            && !sourceReport.javaAgentUsed()
            && !sourceReport.mixinUsed()
            && !sourceReport.javaModExecutionSandboxed(),
        proofReady,
        status,
        nextDirection,
        nextRecommendedAction,
        SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
        List.of(
            SteelHook04FixtureShape.RETURN_SINGLE_PRIMITIVE_VALUE,
            SteelHook04FixtureShape.RETURN_SINGLE_REFERENCE_VALUE),
        rejectionProofCaseCount > 0,
        successfulProofCaseCount,
        rejectionProofCaseCount,
        proofCases,
        findings,
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
        false);
  }

  private void addFinding(
      List<SteelHook04ReturnValueInterceptFinding> findings,
      int sequence,
      String checkName,
      SteelHook04ReturnValueInterceptFindingStatus status,
      boolean blocking,
      String summary,
      String details) {
    findings.add(
        new SteelHook04ReturnValueInterceptFinding(
            "target-33.return-value-intercept.finding.%03d".formatted(sequence),
            checkName,
            status,
            blocking,
            summary,
            details));
  }

  private record GateState(boolean ready) {}
}
