package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.verify.SteelHook03CompletionHandoffStatus;
import com.spindle.core.minecraft.hook.verify.SteelHook03CompletionReport;
import com.spindle.core.minecraft.hook.verify.SteelHook03CompletionStatus;
import java.util.ArrayList;
import java.util.List;

public final class SteelHook04PrimitiveBoundaryAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-32";
  private static final String TARGET = "minecraft";
  private static final String STEELHOOK_VERSION = "0.4";
  private static final String TARGET_33 = "Target-33";
  private static final String TARGET_34 = "Target-34";
  private static final String TARGET_35 = "Target-35";
  private static final String TARGET_36 = "Target-36";
  private static final String READY_ACTION =
      "Move next to Target-33 and Target-34 bounded offline evidence while preserving internal-only, non-runtime-ready SteelHook 0.4 primitive scope.";
  private static final String BLOCKED_ACTION =
      "Restore the Target-31 SteelHook 0.3 completion handoff before planning SteelHook 0.4 primitive evidence.";

  public SteelHook04PrimitiveBoundaryReport analyze(SteelHook03CompletionReport sourceReport) {
    List<SteelHook04PrimitiveFinding> findings = new ArrayList<>();

    boolean sourceReady = sourceReady(sourceReport, findings);
    if (!sourceReady) {
      return new SteelHook04PrimitiveBoundaryReport(
          REPORT_SCHEMA,
          MILESTONE_NAME,
          TARGET,
          STEELHOOK_VERSION,
          sourceReport == null ? null : sourceReport.milestoneName(),
          sourceReport == null || sourceReport.status() == null ? null : sourceReport.status().id(),
          sourceReport != null && sourceReport.completionReady(),
          sourceReport == null || sourceReport.handoffStatus() == null
              ? null
              : sourceReport.handoffStatus().id(),
          false,
          "Target-31 SteelHook 0.3 completion handoff is required before Target-32 can approve the SteelHook 0.4 primitive boundary.",
          SteelHook04PrimitiveBoundaryStatus.SOURCE_GATE_BLOCKED,
          SteelHook04PrimitiveBoundaryNextDirection.RESTORE_TARGET_31_STEELHOOK_0_3_COMPLETION,
          BLOCKED_ACTION,
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
          0,
          List.of(),
          allowedFixtureShapes(),
          unsupportedFixtureShapes(),
          rejectionTaxonomy(),
          evidenceRequirements(),
          findings);
    }

    addFinding(
        findings,
        5,
        "Target-32 remains analysis-only and preserves zero runtime side effects.",
        SteelHook04PrimitiveFindingStatus.PASS,
        true,
        "SteelHook 0.4 begins as a deterministic primitive-boundary report only.",
        "No bytecode transformation, class definition, hook installation, dispatcher execution, Minecraft launch, or sandbox claim occurred.");

    return new SteelHook04PrimitiveBoundaryReport(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        STEELHOOK_VERSION,
        sourceReport.milestoneName(),
        sourceReport.status().id(),
        sourceReport.completionReady(),
        sourceReport.handoffStatus().id(),
        true,
        null,
        SteelHook04PrimitiveBoundaryStatus.BOUNDARY_READY,
        SteelHook04PrimitiveBoundaryNextDirection.MOVE_TO_TARGET_33_RETURN_VALUE_INTERCEPT_EVIDENCE,
        READY_ACTION,
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
        approvedCandidates(),
        allowedFixtureShapes(),
        unsupportedFixtureShapes(),
        rejectionTaxonomy(),
        evidenceRequirements(),
        findings);
  }

  private boolean sourceReady(
      SteelHook03CompletionReport sourceReport, List<SteelHook04PrimitiveFinding> findings) {
    addFinding(
        findings,
        1,
        "Target-31 completion report exists.",
        sourceReport == null
            ? SteelHook04PrimitiveFindingStatus.FAIL
            : SteelHook04PrimitiveFindingStatus.PASS,
        true,
        sourceReport == null
            ? "Target-32 requires the Target-31 completion report."
            : "Target-31 completion report is present.",
        sourceReport == null
            ? "No SteelHook03CompletionReport was provided."
            : "Input report object provided.");
    if (sourceReport == null) {
      return false;
    }

    boolean milestoneMatches =
        MILESTONE_NAME.replace("32", "31").equals(sourceReport.milestoneName());
    addFinding(
        findings,
        2,
        "Source milestone is Target-31.",
        milestoneMatches
            ? SteelHook04PrimitiveFindingStatus.PASS
            : SteelHook04PrimitiveFindingStatus.FAIL,
        true,
        milestoneMatches
            ? "Target-32 is consuming the expected SteelHook 0.3 completion milestone."
            : "Target-32 received the wrong source milestone.",
        "sourceSteelHook03Milestone=" + sourceReport.milestoneName());

    boolean statusPassed = sourceReport.status() == SteelHook03CompletionStatus.PASSED;
    addFinding(
        findings,
        3,
        "Source status is passed and completionReady is true.",
        statusPassed && sourceReport.completionReady()
            ? SteelHook04PrimitiveFindingStatus.PASS
            : SteelHook04PrimitiveFindingStatus.FAIL,
        true,
        statusPassed && sourceReport.completionReady()
            ? "Target-31 completion evidence is accepted."
            : "Target-31 completion evidence is incomplete.",
        "sourceSteelHook03Status="
            + (sourceReport.status() == null ? null : sourceReport.status().id())
            + ", sourceSteelHook03CompletionReady="
            + sourceReport.completionReady());

    boolean handoffMatches =
        sourceReport.handoffStatus() == SteelHook03CompletionHandoffStatus.STEELHOOK_0_3_COMPLETE;
    addFinding(
        findings,
        4,
        "Source handoff is steelhook-0-3-complete.",
        handoffMatches
            ? SteelHook04PrimitiveFindingStatus.PASS
            : SteelHook04PrimitiveFindingStatus.FAIL,
        true,
        handoffMatches
            ? "Target-31 exposes the completed SteelHook 0.3 handoff."
            : "Target-32 requires the steelhook-0-3-complete handoff.",
        "sourceSteelHook03HandoffStatus="
            + (sourceReport.handoffStatus() == null ? null : sourceReport.handoffStatus().id()));

    return milestoneMatches && statusPassed && sourceReport.completionReady() && handoffMatches;
  }

  private List<SteelHook04PrimitiveCandidate> approvedCandidates() {
    return List.of(
        new SteelHook04PrimitiveCandidate(
            "target-32.steelhook-0-4.primitive.candidate.001",
            SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
            SteelHook04PrimitiveCandidateStatus.APPROVED_INTERNAL_PLANNED_PRIMITIVE,
            true,
            false,
            true,
            false,
            false,
            false,
            TARGET_33,
            "Allow only single-return-shape controlled fixtures for primitive and reference return interception evidence.",
            "Target-33 must prove offline-only observation and replacement for supported return shapes and reject malformed or unsupported intercept plans deterministically.",
            List.of(
                SteelHook04FixtureShape.RETURN_SINGLE_PRIMITIVE_VALUE,
                SteelHook04FixtureShape.RETURN_SINGLE_REFERENCE_VALUE),
            List.of(
                "Internal SteelHook primitive name only.",
                "Not a public SteelHook API, Minecraft Modding API, or runtime-ready hook.")),
        new SteelHook04PrimitiveCandidate(
            "target-32.steelhook-0-4.primitive.candidate.002",
            SteelHook04PrimitiveKind.INVOKE_REDIRECT,
            SteelHook04PrimitiveCandidateStatus.APPROVED_INTERNAL_PLANNED_PRIMITIVE,
            true,
            false,
            true,
            false,
            false,
            false,
            TARGET_34,
            "Allow one controlled static or virtual callsite with strict owner, name, descriptor, and opcode matching.",
            "Target-34 must prove offline-only callsite replacement and reject mismatches, absent matches, ambiguous matches, and constructor or special invocation shapes.",
            List.of(SteelHook04FixtureShape.INVOKE_SINGLE_STATIC_OR_VIRTUAL_CALLSITE),
            List.of(
                "Internal SteelHook primitive name only.",
                "Not a public SteelHook API, Minecraft Modding API, or runtime-ready hook.")),
        new SteelHook04PrimitiveCandidate(
            "target-32.steelhook-0-4.primitive.candidate.003",
            SteelHook04PrimitiveKind.INVOKE_WRAP,
            SteelHook04PrimitiveCandidateStatus.APPROVED_INTERNAL_PLANNED_PRIMITIVE,
            true,
            false,
            true,
            false,
            false,
            false,
            TARGET_34,
            "Allow one controlled static or virtual callsite with the same strict matching surface as invoke redirect.",
            "Target-34 must prove offline-only wrapper mechanics while preserving strict callsite matching and rejecting the same mismatch and unsupported callsite cases as invoke redirect.",
            List.of(SteelHook04FixtureShape.INVOKE_SINGLE_STATIC_OR_VIRTUAL_CALLSITE),
            List.of(
                "Internal SteelHook primitive name only.",
                "Not a public SteelHook API, Minecraft Modding API, or runtime-ready hook.")));
  }

  private List<SteelHook04FixtureShape> allowedFixtureShapes() {
    return List.of(
        SteelHook04FixtureShape.RETURN_SINGLE_PRIMITIVE_VALUE,
        SteelHook04FixtureShape.RETURN_SINGLE_REFERENCE_VALUE,
        SteelHook04FixtureShape.INVOKE_SINGLE_STATIC_OR_VIRTUAL_CALLSITE);
  }

  private List<SteelHook04FixtureShape> unsupportedFixtureShapes() {
    return List.of(
        SteelHook04FixtureShape.CONSTRUCTOR_INVOCATION,
        SteelHook04FixtureShape.SPECIAL_INVOCATION,
        SteelHook04FixtureShape.MULTIPLE_MATCHING_CALLSITES,
        SteelHook04FixtureShape.BRANCHING_METHOD,
        SteelHook04FixtureShape.SWITCH_METHOD,
        SteelHook04FixtureShape.EXCEPTION_TABLE_METHOD,
        SteelHook04FixtureShape.SYNCHRONIZED_METHOD,
        SteelHook04FixtureShape.CLASS_INITIALIZER,
        SteelHook04FixtureShape.FRAME_RECOMPUTATION_REQUIRED);
  }

  private List<SteelHook04RejectionReason> rejectionTaxonomy() {
    return List.of(SteelHook04RejectionReason.values());
  }

  private List<SteelHook04EvidenceRequirement> evidenceRequirements() {
    return List.of(
        new SteelHook04EvidenceRequirement(
            "target-32.steelhook-0-4.evidence.target-33",
            TARGET_33,
            List.of(SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT),
            "Prove RETURN_VALUE_INTERCEPT offline only for controlled return shapes, including observation-only and replacement behavior, with at least one primitive return and one reference return when cleanly local, and reject unsupported return shapes plus malformed intercept plans deterministically.",
            List.of(
                SteelHook04RejectionReason.UNSUPPORTED_FIXTURE_SHAPE,
                SteelHook04RejectionReason.MALFORMED_PRIMITIVE_PLAN)),
        new SteelHook04EvidenceRequirement(
            "target-32.steelhook-0-4.evidence.target-34-redirect",
            TARGET_34,
            List.of(SteelHook04PrimitiveKind.INVOKE_REDIRECT),
            "Prove INVOKE_REDIRECT offline only for one controlled invoke shape with strict owner, name, descriptor, and opcode matching, and reject wrong owner, wrong name, wrong descriptor, wrong opcode, no match, ambiguous multiple matches, and constructor or special invocation shapes.",
            List.of(
                SteelHook04RejectionReason.WRONG_OWNER,
                SteelHook04RejectionReason.WRONG_NAME,
                SteelHook04RejectionReason.WRONG_DESCRIPTOR,
                SteelHook04RejectionReason.WRONG_OPCODE,
                SteelHook04RejectionReason.NO_MATCHING_CALLSITE,
                SteelHook04RejectionReason.AMBIGUOUS_MULTIPLE_CALLSITES,
                SteelHook04RejectionReason.CONSTRUCTOR_OR_SPECIAL_INVOKE_UNSUPPORTED)),
        new SteelHook04EvidenceRequirement(
            "target-32.steelhook-0-4.evidence.target-34-wrap",
            TARGET_34,
            List.of(SteelHook04PrimitiveKind.INVOKE_WRAP),
            "Prove INVOKE_WRAP offline only for the same bounded callsite matching surface as invoke redirect while preserving strict matching and rejecting the same mismatch and unsupported callsite cases.",
            List.of(
                SteelHook04RejectionReason.WRONG_OWNER,
                SteelHook04RejectionReason.WRONG_NAME,
                SteelHook04RejectionReason.WRONG_DESCRIPTOR,
                SteelHook04RejectionReason.WRONG_OPCODE,
                SteelHook04RejectionReason.NO_MATCHING_CALLSITE,
                SteelHook04RejectionReason.AMBIGUOUS_MULTIPLE_CALLSITES,
                SteelHook04RejectionReason.CONSTRUCTOR_OR_SPECIAL_INVOKE_UNSUPPORTED)),
        new SteelHook04EvidenceRequirement(
            "target-32.steelhook-0-4.evidence.target-35",
            TARGET_35,
            List.of(
                SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
                SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                SteelHook04PrimitiveKind.INVOKE_WRAP),
            "Prove isolated gated runtime class definition for all three approved primitives and reject unsupported primitive plans before class definition.",
            List.of(SteelHook04RejectionReason.UNSUPPORTED_PRIMITIVE_KIND)),
        new SteelHook04EvidenceRequirement(
            "target-32.steelhook-0-4.evidence.target-36",
            TARGET_36,
            List.of(
                SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
                SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                SteelHook04PrimitiveKind.INVOKE_WRAP),
            "Verify Target-32 through Target-35 evidence, reject stale side-effect reports, schema mismatches, missing primitive evidence, unsupported primitive leakage, raw byte payloads, and reports that imply execution beyond class definition.",
            List.of(
                SteelHook04RejectionReason.RAW_BYTE_PAYLOAD_PRESENT,
                SteelHook04RejectionReason.RUNTIME_CLASSLOADING_ATTEMPTED,
                SteelHook04RejectionReason.HOOK_INSTALLATION_ATTEMPTED,
                SteelHook04RejectionReason.DISPATCHER_EXECUTION_ATTEMPTED,
                SteelHook04RejectionReason.MINECRAFT_EXECUTION_ATTEMPTED,
                SteelHook04RejectionReason.PUBLIC_API_LEAKAGE)));
  }

  private void addFinding(
      List<SteelHook04PrimitiveFinding> findings,
      int sequence,
      String checkName,
      SteelHook04PrimitiveFindingStatus status,
      boolean blocking,
      String summary,
      String details) {
    findings.add(
        new SteelHook04PrimitiveFinding(
            "target-32.steelhook-0-4.primitive.finding.%03d".formatted(sequence),
            checkName,
            status,
            blocking,
            summary,
            details));
  }
}
