package com.spindle.core.minecraft.command;

import com.spindle.core.minecraft.concept.MinecraftTargetConcept;
import com.spindle.core.minecraft.concept.MinecraftTargetConceptCatalog;
import com.spindle.core.minecraft.lifecycle.MinecraftPlannedServerLifecycleDispatch;
import com.spindle.core.minecraft.lifecycle.MinecraftServerLifecycleDispatchPlan;
import com.spindle.core.minecraft.lifecycle.MinecraftServerLifecycleDispatchStatus;
import java.util.List;
import java.util.Objects;

public final class MinecraftCommandRegistrationAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-13";
  private static final String TARGET = "minecraft";
  private static final String CONCEPT_ID = "minecraft.concept.command_registration";
  private static final String UPSTREAM_CONCEPT_ID = "minecraft.concept.server_lifecycle";
  private static final String SOURCE_LIFECYCLE_DISPATCH_PLAN_MILESTONE = "Target-12";
  private static final String STARTING_DISPATCH_ID =
      "target-12.minecraft.server.lifecycle.starting.dispatch";
  private static final String STARTING_PHASE_ID = "minecraft.server.lifecycle.starting";
  private static final String GATE_FAILURE_REASON =
      "Target-13 requires a planned Target-12 starting lifecycle dispatch.";
  private static final String FUTURE_SYMBOL_NOTES =
      "No Minecraft command dispatcher symbol is known in this pass.";
  private static final String FUTURE_REGISTRATION_NOTES =
      "No Minecraft command dispatcher symbol is known in this pass, so command registration remains unbound.";
  private static final String FUTURE_RELOAD_NOTES =
      "No Minecraft command dispatcher symbol is known in this pass, so reload-safe command reapplication remains unbound.";

  public MinecraftCommandRegistrationAnalysis analyze(
      MinecraftTargetConceptCatalog conceptCatalog,
      MinecraftServerLifecycleDispatchPlan lifecycleDispatchPlan) {
    Objects.requireNonNull(conceptCatalog, "conceptCatalog");
    Objects.requireNonNull(lifecycleDispatchPlan, "lifecycleDispatchPlan");

    MinecraftTargetConcept concept =
        conceptCatalog
            .findById(CONCEPT_ID)
            .orElseThrow(
                () -> new IllegalArgumentException("Missing concept `" + CONCEPT_ID + "`."));

    MinecraftPlannedServerLifecycleDispatch startingDispatch =
        lifecycleDispatchPlan.dispatches().stream()
            .filter(dispatch -> STARTING_DISPATCH_ID.equals(dispatch.id()))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Missing lifecycle dispatch `" + STARTING_DISPATCH_ID + "`."));

    boolean sourceLifecycleDispatchGatePassed = lifecycleDispatchPlan.gatePassed();
    boolean anchorAvailable =
        sourceLifecycleDispatchGatePassed
            && startingDispatch.status() == MinecraftServerLifecycleDispatchStatus.PLANNED;
    String gateFailureReason =
        anchorAvailable
            ? null
            : nonBlankFailureReason(lifecycleDispatchPlan.gateFailureReason(), GATE_FAILURE_REASON);

    List<MinecraftAnalyzedCommandRegistrationBoundary> boundaries =
        List.of(
            lifecycleAnchorBoundary(anchorAvailable, gateFailureReason),
            futureBoundary(
                MinecraftCommandRegistrationBoundary.DISPATCHER_DISCOVERY,
                MinecraftCommandRegistrationRepresentationKind.FUTURE_DISPATCHER_SYMBOL,
                FUTURE_SYMBOL_NOTES),
            futureBoundary(
                MinecraftCommandRegistrationBoundary.REGISTRATION_WINDOW,
                MinecraftCommandRegistrationRepresentationKind.FUTURE_REGISTRATION_PHASE,
                FUTURE_REGISTRATION_NOTES),
            futureBoundary(
                MinecraftCommandRegistrationBoundary.REGISTRATION_APPLY,
                MinecraftCommandRegistrationRepresentationKind.FUTURE_REGISTRATION_PHASE,
                FUTURE_REGISTRATION_NOTES),
            futureBoundary(
                MinecraftCommandRegistrationBoundary.RELOAD_REAPPLY,
                MinecraftCommandRegistrationRepresentationKind.FUTURE_RELOAD_PHASE,
                FUTURE_RELOAD_NOTES));

    int anchoredBoundaryCount =
        (int)
            boundaries.stream()
                .filter(
                    boundary ->
                        boundary.status()
                            == MinecraftCommandRegistrationBoundaryStatus.ANCHOR_AVAILABLE)
                .count();
    int unboundBoundaryCount =
        (int)
            boundaries.stream()
                .filter(
                    boundary ->
                        boundary.status()
                            == MinecraftCommandRegistrationBoundaryStatus.DECLARED_UNBOUND)
                .count();
    int blockedBoundaryCount =
        (int)
            boundaries.stream()
                .filter(
                    boundary ->
                        boundary.status() == MinecraftCommandRegistrationBoundaryStatus.BLOCKED)
                .count();

    return new MinecraftCommandRegistrationAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        lifecycleDispatchPlan.minecraftVersion(),
        lifecycleDispatchPlan.side(),
        concept.id(),
        concept.order(),
        concept.displayName(),
        UPSTREAM_CONCEPT_ID,
        SOURCE_LIFECYCLE_DISPATCH_PLAN_MILESTONE,
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
        sourceLifecycleDispatchGatePassed,
        anchorAvailable,
        gateFailureReason,
        boundaries.size(),
        anchoredBoundaryCount,
        unboundBoundaryCount,
        blockedBoundaryCount,
        0,
        0,
        boundaries);
  }

  private MinecraftAnalyzedCommandRegistrationBoundary lifecycleAnchorBoundary(
      boolean anchorAvailable, String gateFailureReason) {
    return new MinecraftAnalyzedCommandRegistrationBoundary(
        boundaryAnalysisId(MinecraftCommandRegistrationBoundary.LIFECYCLE_ANCHOR),
        MinecraftCommandRegistrationBoundary.LIFECYCLE_ANCHOR.id(),
        MinecraftCommandRegistrationBoundary.LIFECYCLE_ANCHOR.displayName(),
        anchorAvailable
            ? MinecraftCommandRegistrationBoundaryStatus.ANCHOR_AVAILABLE
            : MinecraftCommandRegistrationBoundaryStatus.BLOCKED,
        MinecraftCommandRegistrationRepresentationKind.UPSTREAM_LIFECYCLE_DISPATCH,
        UPSTREAM_CONCEPT_ID,
        STARTING_DISPATCH_ID,
        STARTING_PHASE_ID,
        false,
        null,
        null,
        null,
        false,
        false,
        false,
        true,
        anchorAvailable
            ? "Anchored to the symbolic Target-12 starting lifecycle dispatch without binding a Minecraft command dispatcher symbol."
            : gateFailureReason);
  }

  private MinecraftAnalyzedCommandRegistrationBoundary futureBoundary(
      MinecraftCommandRegistrationBoundary boundary,
      MinecraftCommandRegistrationRepresentationKind representationKind,
      String notes) {
    return new MinecraftAnalyzedCommandRegistrationBoundary(
        boundaryAnalysisId(boundary),
        boundary.id(),
        boundary.displayName(),
        MinecraftCommandRegistrationBoundaryStatus.DECLARED_UNBOUND,
        representationKind,
        UPSTREAM_CONCEPT_ID,
        STARTING_DISPATCH_ID,
        STARTING_PHASE_ID,
        false,
        null,
        null,
        null,
        true,
        false,
        false,
        true,
        notes);
  }

  private String boundaryAnalysisId(MinecraftCommandRegistrationBoundary boundary) {
    return "target-13." + boundary.id();
  }

  private String nonBlankFailureReason(String candidate, String fallback) {
    return candidate == null || candidate.isBlank() ? fallback : candidate;
  }
}
