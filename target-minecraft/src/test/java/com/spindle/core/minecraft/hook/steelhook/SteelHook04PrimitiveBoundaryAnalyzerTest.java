package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.hook.verify.SteelHook03CompletionHandoffStatus;
import com.spindle.core.minecraft.hook.verify.SteelHook03CompletionReport;
import com.spindle.core.minecraft.hook.verify.SteelHook03CompletionStatus;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class SteelHook04PrimitiveBoundaryAnalyzerTest {
  private final SteelHook04PrimitiveBoundaryAnalyzer analyzer =
      new SteelHook04PrimitiveBoundaryAnalyzer();

  @Test
  void passedTarget31CompletionApprovesExactlyThreeInternalPrimitives() {
    SteelHook04PrimitiveBoundaryReport report = analyzer.analyze(validCompletionReport());

    assertTrue(report.gatePassed());
    assertEquals(SteelHook04PrimitiveBoundaryStatus.BOUNDARY_READY, report.boundaryStatus());
    assertEquals(3, report.approvedPrimitiveCount());
    assertEquals(
        List.of(
            SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
            SteelHook04PrimitiveKind.INVOKE_REDIRECT,
            SteelHook04PrimitiveKind.INVOKE_WRAP),
        report.candidates().stream().map(SteelHook04PrimitiveCandidate::primitiveKind).toList());
  }

  @Test
  void nullTarget31SourceBlocksTarget32() {
    SteelHook04PrimitiveBoundaryReport report = analyzer.analyze(null);

    assertFalse(report.gatePassed());
    assertEquals(SteelHook04PrimitiveBoundaryStatus.SOURCE_GATE_BLOCKED, report.boundaryStatus());
  }

  @Test
  void wrongTarget31MilestoneBlocksTarget32() {
    SteelHook04PrimitiveBoundaryReport report =
        analyzer.analyze(reportBuilder().milestoneName("Target-99").build());

    assertFalse(report.gatePassed());
    assertEquals(SteelHook04PrimitiveBoundaryStatus.SOURCE_GATE_BLOCKED, report.boundaryStatus());
  }

  @Test
  void failedTarget31StatusBlocksTarget32() {
    SteelHook04PrimitiveBoundaryReport report =
        analyzer.analyze(reportBuilder().status(SteelHook03CompletionStatus.FAILED).build());

    assertFalse(report.gatePassed());
  }

  @Test
  void completionReadyFalseBlocksTarget32() {
    SteelHook04PrimitiveBoundaryReport report =
        analyzer.analyze(reportBuilder().completionReady(false).build());

    assertFalse(report.gatePassed());
  }

  @Test
  void handoffStatusOtherThanSteelHook03CompleteBlocksTarget32() {
    SteelHook04PrimitiveBoundaryReport report =
        analyzer.analyze(
            reportBuilder()
                .handoffStatus(SteelHook03CompletionHandoffStatus.STEELHOOK_0_3_INCOMPLETE)
                .build());

    assertFalse(report.gatePassed());
  }

  @Test
  void approvedPrimitiveCandidatesAreInternalOnlyAndNotPublicApi() {
    SteelHook04PrimitiveBoundaryReport report = analyzer.analyze(validCompletionReport());

    assertTrue(report.candidates().stream().allMatch(SteelHook04PrimitiveCandidate::internalOnly));
    assertTrue(
        report.candidates().stream().noneMatch(SteelHook04PrimitiveCandidate::publicApiExposed));
    assertTrue(report.candidates().stream().allMatch(SteelHook04PrimitiveCandidate::nonPublicApi));
  }

  @Test
  void approvedPrimitiveCandidatesAreNotRuntimeReady() {
    SteelHook04PrimitiveBoundaryReport report = analyzer.analyze(validCompletionReport());

    assertTrue(report.candidates().stream().noneMatch(SteelHook04PrimitiveCandidate::runtimeReady));
    assertTrue(
        report.candidates().stream().noneMatch(SteelHook04PrimitiveCandidate::gatedRuntimeReady));
    assertTrue(
        report.candidates().stream()
            .noneMatch(SteelHook04PrimitiveCandidate::implementedInTarget32));
  }

  @Test
  void rejectionTaxonomyContainsRequiredTarget33ThroughTarget36Reasons() {
    SteelHook04PrimitiveBoundaryReport report = analyzer.analyze(validCompletionReport());

    assertEquals(Arrays.asList(SteelHook04RejectionReason.values()), report.rejectionTaxonomy());
  }

  @Test
  void evidenceRequirementsNameTarget33Target34Target35AndTarget36() {
    SteelHook04PrimitiveBoundaryReport report = analyzer.analyze(validCompletionReport());

    List<String> targetPasses =
        report.evidenceRequirements().stream()
            .map(SteelHook04EvidenceRequirement::targetPass)
            .toList();
    assertTrue(targetPasses.contains("Target-33"));
    assertTrue(targetPasses.contains("Target-34"));
    assertTrue(targetPasses.contains("Target-35"));
    assertTrue(targetPasses.contains("Target-36"));
  }

  private SteelHook03CompletionReport validCompletionReport() {
    return reportBuilder().build();
  }

  private Builder reportBuilder() {
    return new Builder();
  }

  private static final class Builder {
    private String milestoneName = "Target-31";
    private boolean completionReady = true;
    private SteelHook03CompletionStatus status = SteelHook03CompletionStatus.PASSED;
    private SteelHook03CompletionHandoffStatus handoffStatus =
        SteelHook03CompletionHandoffStatus.STEELHOOK_0_3_COMPLETE;

    private Builder milestoneName(String value) {
      milestoneName = value;
      return this;
    }

    private Builder completionReady(boolean value) {
      completionReady = value;
      return this;
    }

    private Builder status(SteelHook03CompletionStatus value) {
      status = value;
      return this;
    }

    private Builder handoffStatus(SteelHook03CompletionHandoffStatus value) {
      handoffStatus = value;
      return this;
    }

    private SteelHook03CompletionReport build() {
      return new SteelHook03CompletionReport(
          1,
          milestoneName,
          "minecraft",
          "0.3",
          completionReady,
          status,
          handoffStatus,
          "Target-27",
          "passed",
          true,
          "steelhook-0-2-complete",
          "Target-28",
          "passed",
          true,
          "Target-29",
          "passed",
          true,
          "Target-30",
          "passed",
          true,
          List.of("bounded METHOD_ENTRY_STATIC_DISPATCH", "bounded METHOD_EXIT_STATIC_DISPATCH"),
          List.of("public-api", "sandboxing"),
          List.of(),
          List.of(),
          List.of(),
          2,
          2,
          true,
          true,
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
          null,
          List.of());
    }
  }
}
