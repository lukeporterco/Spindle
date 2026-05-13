package com.spindle.core.minecraft.resource;

import com.spindle.core.minecraft.concept.MinecraftTargetConcept;
import com.spindle.core.minecraft.concept.MinecraftTargetConceptCatalog;
import com.spindle.core.minecraft.lifecycle.MinecraftServerLifecycleDispatchPlan;
import com.spindle.core.minecraft.lifecycle.MinecraftServerLifecycleDispatchStatus;
import java.util.List;
import java.util.Objects;

public final class MinecraftResourceReloadAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-16";
  private static final String TARGET = "minecraft";
  private static final String CONCEPT_ID = "minecraft.concept.data_resources_reload";
  private static final String SOURCE_LIFECYCLE_DISPATCH_PLAN_MILESTONE = "Target-12";
  private static final String STARTING_PHASE_ID = "minecraft.server.lifecycle.starting";
  private static final String STARTING_DISPATCH_ID =
      "target-12.minecraft.server.lifecycle.starting.dispatch";
  private static final String GATE_FAILURE_REASON =
      "Target-16 requires the Target-12 starting lifecycle dispatch anchor before resource/reload boundaries can be anchored.";

  public MinecraftResourceReloadAnalysis analyze(
      MinecraftTargetConceptCatalog conceptCatalog,
      MinecraftServerLifecycleDispatchPlan lifecycleDispatchPlan) {
    Objects.requireNonNull(conceptCatalog, "conceptCatalog");
    Objects.requireNonNull(lifecycleDispatchPlan, "lifecycleDispatchPlan");

    MinecraftTargetConcept concept =
        conceptCatalog
            .findById(CONCEPT_ID)
            .orElseThrow(
                () -> new IllegalArgumentException("Missing concept `" + CONCEPT_ID + "`."));

    MinecraftServerLifecycleDispatchStatus startingDispatchStatus =
        lifecycleDispatchPlan.dispatches().stream()
            .filter(dispatch -> STARTING_DISPATCH_ID.equals(dispatch.id()))
            .map(dispatch -> dispatch.status())
            .findFirst()
            .orElse(MinecraftServerLifecycleDispatchStatus.BLOCKED);

    boolean sourceLifecycleGatePassed = lifecycleDispatchPlan.gatePassed();
    boolean sourceLifecycleStartingDispatchAvailable =
        sourceLifecycleGatePassed
            && startingDispatchStatus == MinecraftServerLifecycleDispatchStatus.PLANNED;
    boolean gatePassed = sourceLifecycleStartingDispatchAvailable;
    String gateFailureReason = gatePassed ? null : GATE_FAILURE_REASON;

    List<MinecraftAnalyzedResourceReloadBoundary> boundaries =
        List.of(
            lifecycleAnchorBoundary(sourceLifecycleStartingDispatchAvailable),
            futureBoundary(
                MinecraftResourceReloadBoundary.RELOAD_DISCOVERY,
                2,
                MinecraftResourceReloadRepresentationKind.RESOURCE_RELOAD_SYMBOL_BOUNDARY,
                true,
                false,
                false,
                false,
                "Declared for a future Target-17-style resource/reload symbol discovery pass."),
            futureBoundary(
                MinecraftResourceReloadBoundary.RELOAD_WINDOW,
                3,
                MinecraftResourceReloadRepresentationKind.RESOURCE_RELOAD_TIMING_BOUNDARY,
                true,
                true,
                false,
                false,
                "Declared for future reload timing analysis; no runtime reload window is exposed in Target-16."),
            futureBoundary(
                MinecraftResourceReloadBoundary.RELOAD_APPLY,
                4,
                MinecraftResourceReloadRepresentationKind.RESOURCE_RELOAD_TIMING_BOUNDARY,
                true,
                true,
                false,
                false,
                "Declared for future reload-application analysis; no reload application behavior is implemented in Target-16."),
            futureBoundary(
                MinecraftResourceReloadBoundary.DATAPACK_VIEW,
                5,
                MinecraftResourceReloadRepresentationKind.RUNTIME_RESOURCE_VIEW_BOUNDARY,
                true,
                true,
                true,
                false,
                "Declared as a future read/visibility boundary for datapack state, not mutation."),
            futureBoundary(
                MinecraftResourceReloadBoundary.RESOURCE_MANAGER_VIEW,
                6,
                MinecraftResourceReloadRepresentationKind.RUNTIME_RESOURCE_VIEW_BOUNDARY,
                true,
                true,
                true,
                false,
                "Declared as a future read/visibility boundary for resource manager access, not mutation."),
            futureBoundary(
                MinecraftResourceReloadBoundary.FUTURE_DATA_GENERATION,
                7,
                MinecraftResourceReloadRepresentationKind.OFFLINE_DATA_GENERATION_BOUNDARY,
                false,
                false,
                false,
                true,
                "Declared as a future offline/generated-data concept, intentionally separate from runtime resource reload."));

    int availableBoundaryCount =
        (int)
            boundaries.stream()
                .filter(
                    boundary ->
                        boundary.status() == MinecraftResourceReloadBoundaryStatus.AVAILABLE)
                .count();
    int declaredUnboundBoundaryCount =
        (int)
            boundaries.stream()
                .filter(
                    boundary ->
                        boundary.status() == MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND)
                .count();
    int upstreamBlockedBoundaryCount =
        (int)
            boundaries.stream()
                .filter(
                    boundary ->
                        boundary.status()
                            == MinecraftResourceReloadBoundaryStatus.UPSTREAM_GATE_BLOCKED)
                .count();

    return new MinecraftResourceReloadAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        lifecycleDispatchPlan.minecraftVersion(),
        lifecycleDispatchPlan.side(),
        concept.id(),
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
        SOURCE_LIFECYCLE_DISPATCH_PLAN_MILESTONE,
        sourceLifecycleGatePassed,
        sourceLifecycleStartingDispatchAvailable,
        gatePassed,
        gateFailureReason,
        availableBoundaryCount,
        declaredUnboundBoundaryCount,
        upstreamBlockedBoundaryCount,
        boundaries);
  }

  private MinecraftAnalyzedResourceReloadBoundary lifecycleAnchorBoundary(boolean available) {
    return new MinecraftAnalyzedResourceReloadBoundary(
        MinecraftResourceReloadBoundary.LIFECYCLE_ANCHOR.id(),
        MinecraftResourceReloadBoundary.LIFECYCLE_ANCHOR.displayName(),
        1,
        available
            ? MinecraftResourceReloadBoundaryStatus.AVAILABLE
            : MinecraftResourceReloadBoundaryStatus.UPSTREAM_GATE_BLOCKED,
        MinecraftResourceReloadRepresentationKind.SERVER_LIFECYCLE_ANCHOR,
        available,
        available ? STARTING_PHASE_ID : null,
        available ? STARTING_DISPATCH_ID : null,
        false,
        false,
        false,
        false,
        available
            ? "Available only as a coarse lifecycle anchor; this is not a Minecraft resource reload hook."
            : GATE_FAILURE_REASON);
  }

  private MinecraftAnalyzedResourceReloadBoundary futureBoundary(
      MinecraftResourceReloadBoundary boundary,
      int order,
      MinecraftResourceReloadRepresentationKind representationKind,
      boolean requiresSymbolDiscovery,
      boolean requiresBindingStrategyAnalysis,
      boolean requiresRuntimeResourceAccess,
      boolean requiresOfflineGenerationDesign,
      String notes) {
    return new MinecraftAnalyzedResourceReloadBoundary(
        boundary.id(),
        boundary.displayName(),
        order,
        MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
        representationKind,
        false,
        null,
        null,
        requiresSymbolDiscovery,
        requiresBindingStrategyAnalysis,
        requiresRuntimeResourceAccess,
        requiresOfflineGenerationDesign,
        notes);
  }
}
