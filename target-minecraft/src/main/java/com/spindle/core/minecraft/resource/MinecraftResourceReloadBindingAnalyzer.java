package com.spindle.core.minecraft.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MinecraftResourceReloadBindingAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-18";
  private static final String TARGET = "minecraft";
  private static final String CONCEPT_ID = "minecraft.concept.data_resources_reload";
  private static final String SOURCE_RESOURCE_RELOAD_SYMBOL_ANALYSIS_MILESTONE = "Target-17";
  private static final String RESOURCE_BOUNDARY_ID = "minecraft.resources.reload.discovery";
  private static final String UPSTREAM_GATE_FAILURE_REASON =
      "Target-18 requires a passed Target-17 resource/reload symbol analysis before classifying binding requirements.";
  private static final String REJECTED_CANDIDATE_NOTES =
      "Rejected source candidate carried forward; non-net/minecraft candidates are not resource/reload binding targets.";
  private static final String CLASS_REFERENCE_ONLY_NOTES =
      "Class/package discovery is useful evidence but does not identify a callable reload boundary or accessible resource value.";
  private static final String STATIC_METHOD_NOTES =
      "Static method metadata may identify a future reload boundary, but Target-18 does not prove timing, apply semantics, or hook compatibility.";
  private static final String INSTANCE_METHOD_NOTES =
      "Instance method metadata may identify a future reload boundary, but receiver/value capture and reload semantics remain unresolved.";
  private static final String STATIC_FIELD_NOTES =
      "Static field metadata may identify future resource state access, but Target-18 does not access fields or expose resource values.";
  private static final String INSTANCE_FIELD_NOTES =
      "Instance field metadata may identify future resource state access, but owner capture and controlled field access remain unresolved.";

  public MinecraftResourceReloadBindingAnalysis analyze(
      MinecraftResourceReloadSymbolAnalysis symbolAnalysis) {
    Objects.requireNonNull(symbolAnalysis, "symbolAnalysis");
    requireExpectedAnalysis(symbolAnalysis);

    List<MinecraftResourceReloadBindingCandidate> candidates =
        classifyCandidates(symbolAnalysis.candidates());
    int selectableCandidateCount =
        (int)
            candidates.stream().filter(MinecraftResourceReloadBindingCandidate::selectable).count();
    int rejectedCandidateCount = candidates.size() - selectableCandidateCount;
    int classReferenceOnlyCount =
        countByAccessStrategy(
            candidates, MinecraftResourceReloadAccessStrategy.CLASS_REFERENCE_ONLY);
    int methodBoundaryAnalysisRequiredCount =
        countByAccessStrategy(
                candidates,
                MinecraftResourceReloadAccessStrategy.STATIC_METHOD_BOUNDARY_ANALYSIS_REQUIRED)
            + countByAccessStrategy(
                candidates,
                MinecraftResourceReloadAccessStrategy.INSTANCE_METHOD_RECEIVER_CAPTURE_REQUIRED);
    int fieldAccessRequiredCount =
        countByAccessStrategy(
                candidates, MinecraftResourceReloadAccessStrategy.STATIC_FIELD_ACCESS_REQUIRED)
            + countByAccessStrategy(
                candidates,
                MinecraftResourceReloadAccessStrategy.INSTANCE_FIELD_OWNER_CAPTURE_REQUIRED);
    int receiverCaptureRequiredCount =
        (int)
            candidates.stream()
                .filter(MinecraftResourceReloadBindingCandidate::requiresReceiverCapture)
                .count();
    int futureSteelHookPrimitiveRequiredCount =
        (int)
            candidates.stream()
                .filter(MinecraftResourceReloadBindingCandidate::requiresFutureSteelHookPrimitive)
                .count();

    boolean gatePassed;
    String gateFailureReason;
    MinecraftResourceReloadBindingStatus bindingStatus;
    String nextRecommendedAction;
    if (!symbolAnalysis.gatePassed()
        || symbolAnalysis.discoveryStatus()
            == MinecraftResourceReloadSymbolDiscoveryStatus.UPSTREAM_GATE_BLOCKED) {
      gatePassed = false;
      gateFailureReason =
          symbolAnalysis.gateFailureReason() == null
              ? UPSTREAM_GATE_FAILURE_REASON
              : symbolAnalysis.gateFailureReason();
      bindingStatus = MinecraftResourceReloadBindingStatus.UPSTREAM_GATE_BLOCKED;
      nextRecommendedAction =
          "Restore the Target-16 and Target-17 upstream gates before classifying resource/reload binding requirements.";
    } else {
      gatePassed = true;
      gateFailureReason = null;
      bindingStatus =
          switch (symbolAnalysis.discoveryStatus()) {
            case NO_CANDIDATES -> MinecraftResourceReloadBindingStatus.NO_SYMBOL_CANDIDATES;
            case ONLY_REJECTED_CANDIDATES ->
                MinecraftResourceReloadBindingStatus.ONLY_REJECTED_SYMBOL_CANDIDATES;
            case CANDIDATES_DISCOVERED ->
                MinecraftResourceReloadBindingStatus.BINDING_REQUIREMENTS_CLASSIFIED;
            case UPSTREAM_GATE_BLOCKED ->
                throw new IllegalStateException(
                    "Unexpected upstream gate status after gate-passed classification.");
          };
      nextRecommendedAction =
          switch (bindingStatus) {
            case NO_SYMBOL_CANDIDATES ->
                "Do not implement resource reload handling yet; no resource/reload symbol candidates were discovered.";
            case ONLY_REJECTED_SYMBOL_CANDIDATES ->
                "Do not implement resource reload handling yet; only rejected non-net/minecraft candidates were discovered.";
            case BINDING_REQUIREMENTS_CLASSIFIED ->
                "Do not implement resource reload handling yet; use these classified requirements as input to future resource visibility and SteelHook primitive decisions.";
            case UPSTREAM_GATE_BLOCKED ->
                "Restore the Target-16 and Target-17 upstream gates before classifying resource/reload binding requirements.";
          };
    }

    return new MinecraftResourceReloadBindingAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        symbolAnalysis.minecraftVersion(),
        symbolAnalysis.side(),
        CONCEPT_ID,
        SOURCE_RESOURCE_RELOAD_SYMBOL_ANALYSIS_MILESTONE,
        RESOURCE_BOUNDARY_ID,
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
        symbolAnalysis.gatePassed(),
        symbolAnalysis.discoveryStatus().name(),
        symbolAnalysis.bindingStrategyAnalysisEligible(),
        gatePassed,
        gateFailureReason,
        bindingStatus,
        symbolAnalysis.candidateCount(),
        candidates.size(),
        selectableCandidateCount,
        rejectedCandidateCount,
        classReferenceOnlyCount,
        methodBoundaryAnalysisRequiredCount,
        fieldAccessRequiredCount,
        receiverCaptureRequiredCount,
        futureSteelHookPrimitiveRequiredCount,
        false,
        false,
        nextRecommendedAction,
        candidates);
  }

  private void requireExpectedAnalysis(MinecraftResourceReloadSymbolAnalysis analysis) {
    if (!CONCEPT_ID.equals(analysis.conceptId())) {
      throw new IllegalArgumentException(
          "Target-18 requires concept `" + CONCEPT_ID + "` from Target-17.");
    }
    if (!SOURCE_RESOURCE_RELOAD_SYMBOL_ANALYSIS_MILESTONE.equals(analysis.milestoneName())) {
      throw new IllegalArgumentException(
          "Target-18 requires Target-17 resource/reload symbol analysis input.");
    }
  }

  private List<MinecraftResourceReloadBindingCandidate> classifyCandidates(
      List<MinecraftResourceReloadSymbolCandidate> sourceCandidates) {
    List<MinecraftResourceReloadBindingCandidate> candidates = new ArrayList<>();
    for (MinecraftResourceReloadSymbolCandidate sourceCandidate : sourceCandidates) {
      candidates.add(classifyCandidate(sourceCandidate));
    }
    return List.copyOf(candidates);
  }

  private MinecraftResourceReloadBindingCandidate classifyCandidate(
      MinecraftResourceReloadSymbolCandidate sourceCandidate) {
    if (!sourceCandidate.selectable()) {
      return new MinecraftResourceReloadBindingCandidate(
          sourceCandidate.id(),
          sourceCandidate.kind().name(),
          sourceCandidate.boundaryId(),
          sourceCandidate.ownerInternalName(),
          sourceCandidate.memberName(),
          sourceCandidate.descriptor(),
          sourceCandidate.staticMember(),
          sourceCandidate.matchedTokens(),
          false,
          sourceCandidate.rejectionReason(),
          MinecraftResourceReloadAccessStrategy.NONE,
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
          REJECTED_CANDIDATE_NOTES);
    }

    return switch (sourceCandidate.kind()) {
      case CLASS_NAME_REFERENCE ->
          candidate(
              sourceCandidate,
              MinecraftResourceReloadAccessStrategy.CLASS_REFERENCE_ONLY,
              true,
              false,
              false,
              false,
              false,
              false,
              false,
              false,
              CLASS_REFERENCE_ONLY_NOTES);
      case METHOD_NAME_REFERENCE, METHOD_DESCRIPTOR_REFERENCE ->
          sourceCandidate.staticMember()
              ? candidate(
                  sourceCandidate,
                  MinecraftResourceReloadAccessStrategy.STATIC_METHOD_BOUNDARY_ANALYSIS_REQUIRED,
                  true,
                  true,
                  false,
                  false,
                  false,
                  true,
                  true,
                  true,
                  STATIC_METHOD_NOTES)
              : candidate(
                  sourceCandidate,
                  MinecraftResourceReloadAccessStrategy.INSTANCE_METHOD_RECEIVER_CAPTURE_REQUIRED,
                  true,
                  true,
                  true,
                  false,
                  false,
                  true,
                  true,
                  true,
                  INSTANCE_METHOD_NOTES);
      case FIELD_NAME_REFERENCE, FIELD_DESCRIPTOR_REFERENCE ->
          sourceCandidate.staticMember()
              ? candidate(
                  sourceCandidate,
                  MinecraftResourceReloadAccessStrategy.STATIC_FIELD_ACCESS_REQUIRED,
                  true,
                  false,
                  false,
                  true,
                  true,
                  false,
                  false,
                  true,
                  STATIC_FIELD_NOTES)
              : candidate(
                  sourceCandidate,
                  MinecraftResourceReloadAccessStrategy.INSTANCE_FIELD_OWNER_CAPTURE_REQUIRED,
                  true,
                  false,
                  true,
                  true,
                  true,
                  false,
                  false,
                  true,
                  INSTANCE_FIELD_NOTES);
    };
  }

  private MinecraftResourceReloadBindingCandidate candidate(
      MinecraftResourceReloadSymbolCandidate sourceCandidate,
      MinecraftResourceReloadAccessStrategy accessStrategy,
      boolean requiresSymbolNarrowing,
      boolean requiresMethodBoundaryAnalysis,
      boolean requiresReceiverCapture,
      boolean requiresFieldAccess,
      boolean requiresRuntimeResourceAccess,
      boolean requiresReloadTimingDecision,
      boolean requiresReloadApplySemanticsDecision,
      boolean requiresFutureSteelHookPrimitive,
      String notes) {
    return new MinecraftResourceReloadBindingCandidate(
        sourceCandidate.id(),
        sourceCandidate.kind().name(),
        sourceCandidate.boundaryId(),
        sourceCandidate.ownerInternalName(),
        sourceCandidate.memberName(),
        sourceCandidate.descriptor(),
        sourceCandidate.staticMember(),
        sourceCandidate.matchedTokens(),
        true,
        null,
        accessStrategy,
        requiresSymbolNarrowing,
        requiresMethodBoundaryAnalysis,
        requiresReceiverCapture,
        requiresFieldAccess,
        requiresRuntimeResourceAccess,
        requiresReloadTimingDecision,
        requiresReloadApplySemanticsDecision,
        requiresFutureSteelHookPrimitive,
        false,
        false,
        notes);
  }

  private int countByAccessStrategy(
      List<MinecraftResourceReloadBindingCandidate> candidates,
      MinecraftResourceReloadAccessStrategy accessStrategy) {
    return (int)
        candidates.stream()
            .filter(candidate -> candidate.accessStrategy() == accessStrategy)
            .count();
  }
}
