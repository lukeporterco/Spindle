package com.spindle.core.minecraft.registry;

import com.spindle.core.minecraft.resource.MinecraftResourceReloadArcDecisionAnalysis;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadNextDirection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MinecraftRegistryArcHardeningAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-22";
  private static final String TARGET = "minecraft";
  private static final String CONCEPT_ID = "minecraft.concept.registry_bootstrap";
  private static final String SOURCE_RESOURCE_RELOAD_ARC_DECISION_MILESTONE = "Target-20";
  private static final String SOURCE_RESOURCE_RELOAD_ARC_DECISION_CONCEPT_ID =
      "minecraft.concept.data_resources_reload";
  private static final String SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE = "Target-21";
  private static final String UPSTREAM_BLOCKED_ACTION =
      "Restore the Target-20 to Target-21 registry handoff before hardening the registry arc.";
  private static final String INVARIANTS_FAILED_ACTION =
      "Fix Target-21 registry analysis drift before making the next architecture decision.";
  private static final String STEELHOOK_02_ACTION =
      "Move next to SteelHook 0.2 primitive design; do not implement registry behavior until the primitive design pass defines bounded value capture and mutation semantics.";
  private static final String CONTINUE_ANALYSIS_ACTION =
      "Continue registry concept analysis before SteelHook 0.2 design because Target-21 did not produce enough selectable primitive-relevant evidence.";

  public MinecraftRegistryArcHardeningAnalysis analyze(
      MinecraftResourceReloadArcDecisionAnalysis target20,
      MinecraftRegistryBootstrapAnalysis target21) {
    Objects.requireNonNull(target20, "target20");
    Objects.requireNonNull(target21, "target21");

    requireExpectedTarget20(target20);
    requireExpectedTarget21(target21);

    List<MinecraftRegistryArcHardeningFinding> findings = buildFindings(target20, target21);
    int blockingFindingCount =
        (int)
            findings.stream()
                .filter(MinecraftRegistryArcHardeningFinding::blocking)
                .filter(
                    finding -> finding.status() == MinecraftRegistryArcHardeningFindingStatus.FAIL)
                .count();
    int warningFindingCount =
        (int)
            findings.stream()
                .filter(
                    finding ->
                        finding.status() == MinecraftRegistryArcHardeningFindingStatus.WARNING)
                .count();
    int passingFindingCount =
        (int)
            findings.stream()
                .filter(
                    finding -> finding.status() == MinecraftRegistryArcHardeningFindingStatus.PASS)
                .count();

    boolean upstreamBlocked = !target20.gatePassed() || !target21.gatePassed();
    boolean gatePassed;
    String gateFailureReason;
    MinecraftRegistryArcHardeningStatus hardeningStatus;
    MinecraftRegistryArcNextDirection nextDirection;
    boolean registryArcCompleteForNow;
    boolean steelHook02PrimitiveDesignRecommended;
    boolean continueRegistryAnalysisRecommended;
    String nextRecommendedAction;

    if (upstreamBlocked) {
      gatePassed = false;
      gateFailureReason = UPSTREAM_BLOCKED_ACTION;
      hardeningStatus = MinecraftRegistryArcHardeningStatus.UPSTREAM_GATE_BLOCKED;
      nextDirection = MinecraftRegistryArcNextDirection.UNDECIDED_UPSTREAM_BLOCKED;
      registryArcCompleteForNow = false;
      steelHook02PrimitiveDesignRecommended = false;
      continueRegistryAnalysisRecommended = false;
      nextRecommendedAction = UPSTREAM_BLOCKED_ACTION;
    } else if (blockingFindingCount > 0) {
      gatePassed = false;
      gateFailureReason = INVARIANTS_FAILED_ACTION;
      hardeningStatus = MinecraftRegistryArcHardeningStatus.INVARIANTS_FAILED;
      nextDirection = MinecraftRegistryArcNextDirection.RESTORE_TARGET21_ANALYSIS;
      registryArcCompleteForNow = false;
      steelHook02PrimitiveDesignRecommended = false;
      continueRegistryAnalysisRecommended = false;
      nextRecommendedAction = INVARIANTS_FAILED_ACTION;
    } else if (target21.selectableCandidateCount() > 0
        && target21.futureSteelHookPrimitiveRequiredCount() > 0) {
      gatePassed = true;
      gateFailureReason = null;
      hardeningStatus = MinecraftRegistryArcHardeningStatus.REGISTRY_ARC_HARDENED_FOR_STEELHOOK_0_2;
      nextDirection = MinecraftRegistryArcNextDirection.MOVE_TO_STEELHOOK_0_2_PRIMITIVE_DESIGN;
      registryArcCompleteForNow = true;
      steelHook02PrimitiveDesignRecommended = true;
      continueRegistryAnalysisRecommended = false;
      nextRecommendedAction = STEELHOOK_02_ACTION;
    } else {
      gatePassed = true;
      gateFailureReason = null;
      hardeningStatus =
          MinecraftRegistryArcHardeningStatus.REGISTRY_ARC_HARDENED_MORE_CONCEPT_EVIDENCE_REQUIRED;
      nextDirection = MinecraftRegistryArcNextDirection.CONTINUE_REGISTRY_CONCEPT_ANALYSIS;
      registryArcCompleteForNow = false;
      steelHook02PrimitiveDesignRecommended = false;
      continueRegistryAnalysisRecommended = true;
      nextRecommendedAction = CONTINUE_ANALYSIS_ACTION;
    }

    return new MinecraftRegistryArcHardeningAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        target21.minecraftVersion(),
        target21.side(),
        CONCEPT_ID,
        SOURCE_RESOURCE_RELOAD_ARC_DECISION_MILESTONE,
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
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
        false,
        false,
        target20.gatePassed(),
        target20.registryBootstrapRecommended(),
        target20.nextDirection(),
        target21.gatePassed(),
        target21.discoveryStatus(),
        target21.bindingStatus(),
        target21.candidateCount(),
        target21.selectableCandidateCount(),
        target21.rejectedCandidateCount(),
        target21.futureSteelHookPrimitiveRequiredCount(),
        gatePassed,
        gateFailureReason,
        hardeningStatus,
        nextDirection,
        registryArcCompleteForNow,
        false,
        false,
        false,
        steelHook02PrimitiveDesignRecommended,
        continueRegistryAnalysisRecommended,
        blockingFindingCount,
        warningFindingCount,
        passingFindingCount,
        nextRecommendedAction,
        findings);
  }

  private void requireExpectedTarget20(MinecraftResourceReloadArcDecisionAnalysis target20) {
    if (!SOURCE_RESOURCE_RELOAD_ARC_DECISION_MILESTONE.equals(target20.milestoneName())) {
      throw new IllegalArgumentException("Target-20 input must have milestoneName \"Target-20\".");
    }
    if (!SOURCE_RESOURCE_RELOAD_ARC_DECISION_CONCEPT_ID.equals(target20.conceptId())) {
      throw new IllegalArgumentException(
          "Target-20 input must have conceptId \""
              + SOURCE_RESOURCE_RELOAD_ARC_DECISION_CONCEPT_ID
              + "\".");
    }
  }

  private void requireExpectedTarget21(MinecraftRegistryBootstrapAnalysis target21) {
    if (!SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE.equals(target21.milestoneName())) {
      throw new IllegalArgumentException("Target-21 input must have milestoneName \"Target-21\".");
    }
    if (!CONCEPT_ID.equals(target21.conceptId())) {
      throw new IllegalArgumentException(
          "Target-21 input must have conceptId \"" + CONCEPT_ID + "\".");
    }
    if (!SOURCE_RESOURCE_RELOAD_ARC_DECISION_MILESTONE.equals(
        target21.sourceResourceReloadArcDecisionMilestone())) {
      throw new IllegalArgumentException(
          "Target-21 sourceResourceReloadArcDecisionMilestone must be \"Target-20\".");
    }
  }

  private List<MinecraftRegistryArcHardeningFinding> buildFindings(
      MinecraftResourceReloadArcDecisionAnalysis target20,
      MinecraftRegistryBootstrapAnalysis target21) {
    List<MinecraftRegistryArcHardeningFinding> findings = new ArrayList<>();
    addFinding(
        findings,
        "Target-20 handoff points to registry bootstrap.",
        SOURCE_RESOURCE_RELOAD_ARC_DECISION_MILESTONE,
        target20.registryBootstrapRecommended()
            && target20.nextDirection()
                == MinecraftResourceReloadNextDirection.MOVE_TO_REGISTRY_BOOTSTRAP
            && CONCEPT_ID.equals(target20.recommendedNextConceptId())
            && SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE.equals(
                target20.recommendedNextMilestoneName()),
        "Target-20 preserves the registry bootstrap handoff for Target-21.",
        "Target-20 must still hand off to registry bootstrap/content registration as the next concept family.");
    addFinding(
        findings,
        "Target-21 source milestone and concept are correct.",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        SOURCE_RESOURCE_RELOAD_ARC_DECISION_MILESTONE.equals(
                target21.sourceResourceReloadArcDecisionMilestone())
            && CONCEPT_ID.equals(target21.conceptId()),
        "Target-21 still points to the correct registry concept and Target-20 source milestone.",
        "Target-21 must continue to report conceptId minecraft.concept.registry_bootstrap and source milestone Target-20.");
    addFinding(
        findings,
        "Target-21 analysisOnly is true.",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        target21.analysisOnly(),
        "Target-21 remains analysis-only.",
        "Target-21 must stay analysis-only.");
    addFinding(
        findings,
        "Target-21 runtime/mutation/API/sandbox flags are all false.",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        noRuntimeMutationApiOrSandboxFlags(target21),
        "Target-21 still reports no runtime, mutation, API, or sandbox behavior.",
        "Classloading, injection, transformation, patching, hook installation, runtime dispatch, registry bootstrap, registry mutation, content registration, resource access, datapack access, data generation, public API exposure, and sandbox claims must all remain false.");
    addFinding(
        findings,
        "Target-21 proof/design flags remain false.",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        !target21.registryProofRecommended()
            && !target21.currentSteelHookMethodEntryCompatible()
            && !target21.steelHookPrimitiveDesignRecommended(),
        "Target-21 does not overclaim proof or SteelHook readiness.",
        "Registry proof, current SteelHook method-entry compatibility, and SteelHook primitive design recommendation must all remain false.");
    addFinding(
        findings,
        "Target-21 boundary list has the exact expected boundary IDs in enum order.",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        expectedBoundariesInOrder(target21.boundaries()),
        "Target-21 boundary ordering still matches the enum-defined registry arc.",
        "The boundary list must match MinecraftRegistryBoundary ids in enum order.");
    addFinding(
        findings,
        "Target-21 boundary counts match the actual boundary list.",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        boundaryCountsMatch(target21),
        "Target-21 boundary counts still match the boundary payload.",
        "Boundary count, anchor count, metadata-analyzed count, declared-unbound count, and blocked count must match the actual boundary list.");
    addFinding(
        findings,
        "Target-21 candidate counts match the actual candidate list.",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        candidateCountsMatch(target21),
        "Target-21 candidate counts still match the candidate payload.",
        "Aggregate candidate counts and requirement counts must match the actual candidate list.");
    addFinding(
        findings,
        "Target-21 candidate IDs are deterministic and sorted as target-21.minecraft.registries.candidate.001, .002, etc.",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        candidateIdsDeterministic(target21.candidates()),
        "Target-21 candidate identifiers remain deterministic and sequential.",
        "Candidate ids must remain target-21.minecraft.registries.candidate.%03d in list order.");
    addFinding(
        findings,
        "Target-21 discovery tokens do not contain standalone \"register\".",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        target21.discoveryTokens().stream().noneMatch("register"::equals),
        "Target-21 discovery tokens remain narrow.",
        "Target-21 must not broaden discovery with a standalone register token.");
    addFinding(
        findings,
        "Target-21 selectable candidates all use net/minecraft/* owners.",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        target21.candidates().stream()
            .filter(MinecraftRegistryCandidate::selectable)
            .allMatch(candidate -> candidate.ownerInternalName().startsWith("net/minecraft/")),
        "Selectable candidates stay restricted to net/minecraft owners.",
        "All selectable candidates must use net/minecraft/* owners.");
    addFinding(
        findings,
        "Target-21 rejected candidates all use non-net/minecraft/* owners and carry a rejection reason.",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        target21.candidates().stream()
            .filter(candidate -> !candidate.selectable())
            .allMatch(
                candidate ->
                    !candidate.ownerInternalName().startsWith("net/minecraft/")
                        && candidate.rejectionReason() != null
                        && !candidate.rejectionReason().isBlank()),
        "Rejected candidates remain non-minecraft owners with explicit reasons.",
        "Rejected candidates must stay outside net/minecraft/* and include a rejection reason.");
    addFinding(
        findings,
        "Target-21 candidates do not claim current SteelHook method-entry compatibility.",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        target21.candidates().stream()
            .noneMatch(MinecraftRegistryCandidate::currentSteelHookMethodEntryCompatible),
        "No Target-21 candidate claims current SteelHook 0.1 compatibility.",
        "Target-21 candidates must not claim current SteelHook method-entry compatibility.");
    addFinding(
        findings,
        "Target-21 candidates do not recommend registry proofs.",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        target21.candidates().stream()
            .noneMatch(MinecraftRegistryCandidate::registryProofRecommended),
        "No Target-21 candidate recommends a registry proof.",
        "Target-21 candidates must not recommend registry proofs.");
    addFinding(
        findings,
        "Target-21 selectable non-class candidates requiring registry value access also require future SteelHook machinery.",
        SOURCE_REGISTRY_BOOTSTRAP_ANALYSIS_MILESTONE,
        target21.candidates().stream()
            .filter(MinecraftRegistryCandidate::selectable)
            .filter(
                candidate ->
                    candidate.kind() != MinecraftRegistryCandidateKind.CLASS_NAME_REFERENCE)
            .filter(MinecraftRegistryCandidate::requiresRegistryValueAccess)
            .allMatch(MinecraftRegistryCandidate::requiresFutureSteelHookPrimitive),
        "Value-access candidates still require future SteelHook machinery.",
        "Selectable non-class candidates that require registry value access must also require future SteelHook machinery.");
    return List.copyOf(findings);
  }

  private void addFinding(
      List<MinecraftRegistryArcHardeningFinding> findings,
      String checkName,
      String sourceMilestoneName,
      boolean passed,
      String passSummary,
      String failureNotes) {
    int index = findings.size() + 1;
    findings.add(
        new MinecraftRegistryArcHardeningFinding(
            "target-22.registry.arc.finding.%03d".formatted(index),
            sourceMilestoneName,
            checkName,
            passed
                ? MinecraftRegistryArcHardeningFindingStatus.PASS
                : MinecraftRegistryArcHardeningFindingStatus.FAIL,
            true,
            passed ? passSummary : "Invariant failed: " + checkName,
            passed ? failureNotes : failureNotes));
  }

  private boolean noRuntimeMutationApiOrSandboxFlags(MinecraftRegistryBootstrapAnalysis target21) {
    return !target21.classLoadingOccurred()
        && !target21.injectionOccurred()
        && !target21.transformationOccurred()
        && !target21.patchingOccurred()
        && !target21.hookInstallationOccurred()
        && !target21.runtimeDispatchOccurred()
        && !target21.registryBootstrapOccurred()
        && !target21.registryMutationOccurred()
        && !target21.contentRegistrationOccurred()
        && !target21.resourceAccessOccurred()
        && !target21.datapackAccessOccurred()
        && !target21.dataGenerationOccurred()
        && !target21.publicApiExposed()
        && !target21.javaModExecutionSandboxed();
  }

  private boolean expectedBoundariesInOrder(List<MinecraftAnalyzedRegistryBoundary> boundaries) {
    if (boundaries.size() != MinecraftRegistryBoundary.values().length) {
      return false;
    }
    for (int index = 0; index < MinecraftRegistryBoundary.values().length; index++) {
      if (!MinecraftRegistryBoundary.values()[index]
          .id()
          .equals(boundaries.get(index).boundaryId())) {
        return false;
      }
    }
    return true;
  }

  private boolean boundaryCountsMatch(MinecraftRegistryBootstrapAnalysis target21) {
    List<MinecraftAnalyzedRegistryBoundary> boundaries = target21.boundaries();
    return target21.boundaryCount() == boundaries.size()
        && target21.anchorBoundaryCount()
            == countBoundaries(
                boundaries, MinecraftRegistryBoundaryStatus.UPSTREAM_HANDOFF_AVAILABLE)
        && target21.metadataAnalyzedBoundaryCount()
            == countBoundaries(boundaries, MinecraftRegistryBoundaryStatus.ANALYZED_FROM_METADATA)
        && target21.declaredUnboundBoundaryCount()
            == countBoundaries(boundaries, MinecraftRegistryBoundaryStatus.DECLARED_UNBOUND)
        && target21.blockedBoundaryCount()
            == countBoundaries(boundaries, MinecraftRegistryBoundaryStatus.BLOCKED);
  }

  private int countBoundaries(
      List<MinecraftAnalyzedRegistryBoundary> boundaries, MinecraftRegistryBoundaryStatus status) {
    return (int) boundaries.stream().filter(boundary -> boundary.status() == status).count();
  }

  private boolean candidateCountsMatch(MinecraftRegistryBootstrapAnalysis target21) {
    List<MinecraftRegistryCandidate> candidates = target21.candidates();
    return target21.candidateCount() == candidates.size()
        && target21.classNameCandidateCount()
            == countCandidatesByKind(
                candidates, MinecraftRegistryCandidateKind.CLASS_NAME_REFERENCE)
        && target21.fieldNameCandidateCount()
            == countCandidatesByKind(
                candidates, MinecraftRegistryCandidateKind.FIELD_NAME_REFERENCE)
        && target21.fieldDescriptorCandidateCount()
            == countCandidatesByKind(
                candidates, MinecraftRegistryCandidateKind.FIELD_DESCRIPTOR_REFERENCE)
        && target21.methodNameCandidateCount()
            == countCandidatesByKind(
                candidates, MinecraftRegistryCandidateKind.METHOD_NAME_REFERENCE)
        && target21.methodDescriptorCandidateCount()
            == countCandidatesByKind(
                candidates, MinecraftRegistryCandidateKind.METHOD_DESCRIPTOR_REFERENCE)
        && target21.selectableCandidateCount()
            == (int) candidates.stream().filter(MinecraftRegistryCandidate::selectable).count()
        && target21.rejectedCandidateCount()
            == (int) candidates.stream().filter(candidate -> !candidate.selectable()).count()
        && target21.classReferenceOnlyCount()
            == countAccessStrategy(candidates, MinecraftRegistryAccessStrategy.CLASS_REFERENCE_ONLY)
        && target21.methodBoundaryAnalysisRequiredCount()
            == (int)
                candidates.stream()
                    .filter(MinecraftRegistryCandidate::requiresMethodBoundaryAnalysis)
                    .count()
        && target21.fieldAccessRequiredCount()
            == (int)
                candidates.stream().filter(MinecraftRegistryCandidate::requiresFieldAccess).count()
        && target21.receiverCaptureRequiredCount()
            == (int)
                candidates.stream()
                    .filter(MinecraftRegistryCandidate::requiresReceiverCapture)
                    .count()
        && target21.futureSteelHookPrimitiveRequiredCount()
            == (int)
                candidates.stream()
                    .filter(MinecraftRegistryCandidate::requiresFutureSteelHookPrimitive)
                    .count();
  }

  private int countCandidatesByKind(
      List<MinecraftRegistryCandidate> candidates, MinecraftRegistryCandidateKind kind) {
    return (int) candidates.stream().filter(candidate -> candidate.kind() == kind).count();
  }

  private int countAccessStrategy(
      List<MinecraftRegistryCandidate> candidates, MinecraftRegistryAccessStrategy strategy) {
    return (int)
        candidates.stream().filter(candidate -> candidate.accessStrategy() == strategy).count();
  }

  private boolean candidateIdsDeterministic(List<MinecraftRegistryCandidate> candidates) {
    for (int index = 0; index < candidates.size(); index++) {
      String expectedId = "target-21.minecraft.registries.candidate.%03d".formatted(index + 1);
      if (!expectedId.equals(candidates.get(index).id())) {
        return false;
      }
    }
    return true;
  }
}
