package com.spindle.core.minecraft.resource;

import java.util.List;
import java.util.Objects;

public final class MinecraftResourceReloadArcDecisionAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-20";
  private static final String TARGET = "minecraft";
  private static final String CONCEPT_ID = "minecraft.concept.data_resources_reload";
  private static final String SOURCE_RESOURCE_RELOAD_ANALYSIS_MILESTONE = "Target-16";
  private static final String SOURCE_RESOURCE_RELOAD_SYMBOL_ANALYSIS_MILESTONE = "Target-17";
  private static final String SOURCE_RESOURCE_RELOAD_BINDING_ANALYSIS_MILESTONE = "Target-18";
  private static final String SOURCE_RESOURCE_VISIBILITY_GENERATION_ANALYSIS_MILESTONE =
      "Target-19";
  private static final String RECOMMENDED_NEXT_CONCEPT_ID = "minecraft.concept.registry_bootstrap";
  private static final String RECOMMENDED_NEXT_MILESTONE_NAME = "Target-21";
  private static final String RECOMMENDED_NEXT_PASS_TITLE = "Registry Bootstrap Boundary Analysis";
  private static final String GATE_FAILURE_REASON =
      "Target-20 requires passed Target-16, Target-17, Target-18, and Target-19 resource/reload analyses before recording the registry handoff decision.";
  private static final String CABOOSED_NEXT_ACTION =
      "Move to Registry Bootstrap and Content Registration boundary analysis next; do not design a new SteelHook primitive until registry concept grounding adds more evidence.";
  private static final String BLOCKED_NEXT_ACTION =
      "Restore the Target-16 through Target-19 resource/reload analysis chain before recording a registry handoff decision.";

  public MinecraftResourceReloadArcDecisionAnalysis analyze(
      MinecraftResourceReloadAnalysis resourceReloadAnalysis,
      MinecraftResourceReloadSymbolAnalysis resourceReloadSymbolAnalysis,
      MinecraftResourceReloadBindingAnalysis resourceReloadBindingAnalysis,
      MinecraftResourceVisibilityGenerationAnalysis resourceVisibilityGenerationAnalysis) {
    Objects.requireNonNull(resourceReloadAnalysis, "resourceReloadAnalysis");
    Objects.requireNonNull(resourceReloadSymbolAnalysis, "resourceReloadSymbolAnalysis");
    Objects.requireNonNull(resourceReloadBindingAnalysis, "resourceReloadBindingAnalysis");
    Objects.requireNonNull(
        resourceVisibilityGenerationAnalysis, "resourceVisibilityGenerationAnalysis");
    requireExpectedResourceReloadAnalysis(resourceReloadAnalysis);
    requireExpectedSymbolAnalysis(resourceReloadSymbolAnalysis);
    requireExpectedBindingAnalysis(resourceReloadBindingAnalysis);
    requireExpectedVisibilityGenerationAnalysis(resourceVisibilityGenerationAnalysis);

    boolean gatePassed =
        resourceReloadAnalysis.gatePassed()
            && resourceReloadSymbolAnalysis.gatePassed()
            && resourceReloadBindingAnalysis.gatePassed()
            && resourceVisibilityGenerationAnalysis.gatePassed();
    String gateFailureReason = gatePassed ? null : GATE_FAILURE_REASON;
    MinecraftResourceReloadArcDecisionStatus decisionStatus =
        gatePassed
            ? MinecraftResourceReloadArcDecisionStatus.RESOURCE_RELOAD_ARC_CABOOSED
            : MinecraftResourceReloadArcDecisionStatus.UPSTREAM_GATE_BLOCKED;
    MinecraftResourceReloadNextDirection nextDirection =
        gatePassed
            ? MinecraftResourceReloadNextDirection.MOVE_TO_REGISTRY_BOOTSTRAP
            : MinecraftResourceReloadNextDirection.UNDECIDED_UPSTREAM_BLOCKED;

    return new MinecraftResourceReloadArcDecisionAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        resourceReloadAnalysis.minecraftVersion(),
        resourceReloadAnalysis.side(),
        CONCEPT_ID,
        SOURCE_RESOURCE_RELOAD_ANALYSIS_MILESTONE,
        SOURCE_RESOURCE_RELOAD_SYMBOL_ANALYSIS_MILESTONE,
        SOURCE_RESOURCE_RELOAD_BINDING_ANALYSIS_MILESTONE,
        SOURCE_RESOURCE_VISIBILITY_GENERATION_ANALYSIS_MILESTONE,
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
        false,
        resourceReloadAnalysis.gatePassed(),
        resourceReloadSymbolAnalysis.gatePassed(),
        resourceReloadSymbolAnalysis.discoveryStatus().name(),
        resourceReloadBindingAnalysis.gatePassed(),
        resourceReloadBindingAnalysis.bindingStatus().name(),
        resourceReloadBindingAnalysis.reloadProofRecommended(),
        resourceReloadBindingAnalysis.currentSteelHookMethodEntryCompatible(),
        resourceVisibilityGenerationAnalysis.gatePassed(),
        resourceVisibilityGenerationAnalysis.separationStatus().name(),
        resourceVisibilityGenerationAnalysis.runtimeVisibilitySeparatedFromOfflineGeneration(),
        resourceVisibilityGenerationAnalysis.dataGenerationRequiresOfflineDesign(),
        resourceVisibilityGenerationAnalysis.runtimeReloadRequiresFutureBindingDecision(),
        gatePassed,
        gateFailureReason,
        decisionStatus,
        nextDirection,
        gatePassed,
        false,
        false,
        false,
        false,
        false,
        gatePassed,
        RECOMMENDED_NEXT_CONCEPT_ID,
        RECOMMENDED_NEXT_MILESTONE_NAME,
        RECOMMENDED_NEXT_PASS_TITLE,
        gatePassed ? CABOOSED_NEXT_ACTION : BLOCKED_NEXT_ACTION,
        findings());
  }

  private void requireExpectedResourceReloadAnalysis(
      MinecraftResourceReloadAnalysis resourceReloadAnalysis) {
    requireConceptId(resourceReloadAnalysis.conceptId(), "Target-16");
    if (!SOURCE_RESOURCE_RELOAD_ANALYSIS_MILESTONE.equals(resourceReloadAnalysis.milestoneName())) {
      throw new IllegalArgumentException(
          "Target-20 requires Target-16 resource/reload analysis input.");
    }
  }

  private void requireExpectedSymbolAnalysis(
      MinecraftResourceReloadSymbolAnalysis resourceReloadSymbolAnalysis) {
    requireConceptId(resourceReloadSymbolAnalysis.conceptId(), "Target-17");
    if (!SOURCE_RESOURCE_RELOAD_SYMBOL_ANALYSIS_MILESTONE.equals(
        resourceReloadSymbolAnalysis.milestoneName())) {
      throw new IllegalArgumentException(
          "Target-20 requires Target-17 resource/reload symbol analysis input.");
    }
  }

  private void requireExpectedBindingAnalysis(
      MinecraftResourceReloadBindingAnalysis resourceReloadBindingAnalysis) {
    requireConceptId(resourceReloadBindingAnalysis.conceptId(), "Target-18");
    if (!SOURCE_RESOURCE_RELOAD_BINDING_ANALYSIS_MILESTONE.equals(
        resourceReloadBindingAnalysis.milestoneName())) {
      throw new IllegalArgumentException(
          "Target-20 requires Target-18 resource/reload binding analysis input.");
    }
  }

  private void requireExpectedVisibilityGenerationAnalysis(
      MinecraftResourceVisibilityGenerationAnalysis resourceVisibilityGenerationAnalysis) {
    requireConceptId(resourceVisibilityGenerationAnalysis.conceptId(), "Target-19");
    if (!SOURCE_RESOURCE_VISIBILITY_GENERATION_ANALYSIS_MILESTONE.equals(
        resourceVisibilityGenerationAnalysis.milestoneName())) {
      throw new IllegalArgumentException(
          "Target-20 requires Target-19 resource visibility/data generation analysis input.");
    }
  }

  private void requireConceptId(String actualConceptId, String sourceMilestone) {
    if (!CONCEPT_ID.equals(actualConceptId)) {
      throw new IllegalArgumentException(
          "Target-20 requires concept `" + CONCEPT_ID + "` from " + sourceMilestone + ".");
    }
  }

  private List<MinecraftResourceReloadArcDecisionFinding> findings() {
    return List.of(
        finding(
            "target-20.resource.reload.arc.finding.001",
            "Target-16",
            "Resource/reload boundaries were named and anchored only to a coarse lifecycle boundary.",
            false,
            false,
            true,
            "The lifecycle anchor is not a reload hook."),
        finding(
            "target-20.resource.reload.arc.finding.002",
            "Target-17",
            "Resource/reload metadata candidates were discovered without selecting a stable reload target.",
            false,
            false,
            true,
            "Candidate discovery does not imply reload readiness."),
        finding(
            "target-20.resource.reload.arc.finding.003",
            "Target-18",
            "Resource/reload binding and access requirements were classified without recommending a reload proof.",
            false,
            false,
            true,
            "Classified requirements are not SteelHook primitive design."),
        finding(
            "target-20.resource.reload.arc.finding.004",
            "Target-19",
            "Runtime reload timing, runtime resource visibility, and future offline data generation were separated.",
            false,
            false,
            true,
            "Runtime resource visibility is not an API, and offline data generation is not implemented."),
        finding(
            "target-20.resource.reload.arc.finding.005",
            "Target-20",
            "The next target concept direction is Registry Bootstrap and Content Registration.",
            false,
            true,
            true,
            "Move to registry boundary analysis next instead of designing a new SteelHook primitive now."));
  }

  private MinecraftResourceReloadArcDecisionFinding finding(
      String id,
      String sourceMilestoneName,
      String summary,
      boolean implementationReady,
      boolean recommendedForImmediateImplementation,
      boolean requiresFutureWork,
      String notes) {
    return new MinecraftResourceReloadArcDecisionFinding(
        id,
        sourceMilestoneName,
        summary,
        implementationReady,
        recommendedForImmediateImplementation,
        requiresFutureWork,
        notes);
  }
}
