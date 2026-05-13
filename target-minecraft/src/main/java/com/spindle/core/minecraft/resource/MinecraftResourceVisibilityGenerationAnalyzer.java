package com.spindle.core.minecraft.resource;

import java.util.List;
import java.util.Objects;

public final class MinecraftResourceVisibilityGenerationAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-19";
  private static final String TARGET = "minecraft";
  private static final String CONCEPT_ID = "minecraft.concept.data_resources_reload";
  private static final String SOURCE_RESOURCE_RELOAD_ANALYSIS_MILESTONE = "Target-16";
  private static final String SOURCE_RESOURCE_RELOAD_BINDING_ANALYSIS_MILESTONE = "Target-18";
  private static final String GATE_FAILURE_REASON =
      "Target-19 requires passed Target-16 resource/reload analysis and passed Target-18 binding analysis before separating runtime visibility from future data generation.";
  private static final String BLOCKED_NEXT_ACTION =
      "Restore Target-16 and Target-18 upstream gates before using resource visibility/data generation separation.";
  private static final String CLASSIFIED_NEXT_ACTION =
      "Keep runtime resource visibility separate from future offline data generation; use this separation in the Target-20 caboose or registry/content planning.";

  public MinecraftResourceVisibilityGenerationAnalysis analyze(
      MinecraftResourceReloadAnalysis resourceReloadAnalysis,
      MinecraftResourceReloadBindingAnalysis resourceReloadBindingAnalysis) {
    Objects.requireNonNull(resourceReloadAnalysis, "resourceReloadAnalysis");
    Objects.requireNonNull(resourceReloadBindingAnalysis, "resourceReloadBindingAnalysis");
    requireExpectedResourceReloadAnalysis(resourceReloadAnalysis);
    requireExpectedBindingAnalysis(resourceReloadBindingAnalysis);

    List<MinecraftResourceVisibilityGenerationSurface> surfaces =
        resourceReloadAnalysis.boundaries().stream().map(this::mapSurface).toList();

    boolean sourceResourceReloadGatePassed = resourceReloadAnalysis.gatePassed();
    boolean sourceBindingGatePassed = resourceReloadBindingAnalysis.gatePassed();
    boolean sourceReloadProofRecommended = resourceReloadBindingAnalysis.reloadProofRecommended();
    boolean sourceCurrentSteelHookMethodEntryCompatible =
        resourceReloadBindingAnalysis.currentSteelHookMethodEntryCompatible();
    boolean gatePassed = sourceResourceReloadGatePassed && sourceBindingGatePassed;
    String gateFailureReason = gatePassed ? null : GATE_FAILURE_REASON;
    MinecraftResourceVisibilityGenerationStatus separationStatus =
        gatePassed
            ? MinecraftResourceVisibilityGenerationStatus.SEPARATION_CLASSIFIED
            : MinecraftResourceVisibilityGenerationStatus.UPSTREAM_GATE_BLOCKED;

    int runtimeFacingSurfaceCount = countRuntimeFacing(surfaces);
    int offlineGenerationSurfaceCount = countOfflineFacing(surfaces);
    int runtimeReloadTimingSurfaceCount =
        countByLane(surfaces, MinecraftResourceVisibilityGenerationLane.RESOURCE_RELOAD_TIMING);
    int runtimeResourceVisibilitySurfaceCount =
        countByLane(
            surfaces, MinecraftResourceVisibilityGenerationLane.RUNTIME_RESOURCE_VISIBILITY);
    int offlineDataGenerationSurfaceCount =
        countByLane(surfaces, MinecraftResourceVisibilityGenerationLane.OFFLINE_DATA_GENERATION);
    int implementationReadySurfaceCount = countImplementationReady(surfaces);
    int futureSteelHookPrimitiveRequiredSurfaceCount = countFutureSteelHookRequired(surfaces);

    return new MinecraftResourceVisibilityGenerationAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        resourceReloadAnalysis.minecraftVersion(),
        resourceReloadAnalysis.side(),
        CONCEPT_ID,
        SOURCE_RESOURCE_RELOAD_ANALYSIS_MILESTONE,
        SOURCE_RESOURCE_RELOAD_BINDING_ANALYSIS_MILESTONE,
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
        sourceResourceReloadGatePassed,
        sourceBindingGatePassed,
        resourceReloadBindingAnalysis.bindingStatus().name(),
        sourceReloadProofRecommended,
        sourceCurrentSteelHookMethodEntryCompatible,
        gatePassed,
        gateFailureReason,
        separationStatus,
        runtimeFacingSurfaceCount,
        offlineGenerationSurfaceCount,
        runtimeReloadTimingSurfaceCount,
        runtimeResourceVisibilitySurfaceCount,
        offlineDataGenerationSurfaceCount,
        implementationReadySurfaceCount,
        futureSteelHookPrimitiveRequiredSurfaceCount,
        true,
        true,
        true,
        false,
        false,
        gatePassed ? CLASSIFIED_NEXT_ACTION : BLOCKED_NEXT_ACTION,
        surfaces);
  }

  private void requireExpectedResourceReloadAnalysis(
      MinecraftResourceReloadAnalysis resourceReloadAnalysis) {
    if (!CONCEPT_ID.equals(resourceReloadAnalysis.conceptId())) {
      throw new IllegalArgumentException(
          "Target-19 requires concept `" + CONCEPT_ID + "` from Target-16.");
    }
    if (!SOURCE_RESOURCE_RELOAD_ANALYSIS_MILESTONE.equals(resourceReloadAnalysis.milestoneName())) {
      throw new IllegalArgumentException(
          "Target-19 requires Target-16 resource/reload analysis input.");
    }
  }

  private void requireExpectedBindingAnalysis(
      MinecraftResourceReloadBindingAnalysis resourceReloadBindingAnalysis) {
    if (!CONCEPT_ID.equals(resourceReloadBindingAnalysis.conceptId())) {
      throw new IllegalArgumentException(
          "Target-19 requires concept `" + CONCEPT_ID + "` from Target-18.");
    }
    if (!SOURCE_RESOURCE_RELOAD_BINDING_ANALYSIS_MILESTONE.equals(
        resourceReloadBindingAnalysis.milestoneName())) {
      throw new IllegalArgumentException(
          "Target-19 requires Target-18 resource/reload binding analysis input.");
    }
  }

  private MinecraftResourceVisibilityGenerationSurface mapSurface(
      MinecraftAnalyzedResourceReloadBoundary boundary) {
    return switch (boundary.boundaryId()) {
      case "minecraft.resources.lifecycle_anchor" ->
          surface(
              boundary,
              MinecraftResourceVisibilityGenerationLane.SERVER_LIFECYCLE_ANCHOR,
              true,
              false,
              boundary.status() == MinecraftResourceReloadBoundaryStatus.AVAILABLE,
              false,
              false,
              false,
              false,
              false,
              false,
              "Coarse server lifecycle anchor only; not a reload hook, resource view, or data generation surface.");
      case "minecraft.resources.reload.discovery" ->
          surface(
              boundary,
              MinecraftResourceVisibilityGenerationLane.SYMBOL_DISCOVERY,
              false,
              false,
              false,
              false,
              false,
              false,
              true,
              false,
              false,
              "Metadata discovery lane only; not runtime resource visibility and not data generation.");
      case "minecraft.resources.reload.window" ->
          surface(
              boundary,
              MinecraftResourceVisibilityGenerationLane.RESOURCE_RELOAD_TIMING,
              true,
              false,
              false,
              true,
              false,
              false,
              true,
              true,
              false,
              "Runtime reload timing lane; Target-19 does not identify a reload window or install a hook.");
      case "minecraft.resources.reload.apply" ->
          surface(
              boundary,
              MinecraftResourceVisibilityGenerationLane.RESOURCE_RELOAD_TIMING,
              true,
              false,
              false,
              true,
              false,
              false,
              true,
              true,
              false,
              "Runtime reload apply lane; Target-19 does not decide apply semantics or mutate server state.");
      case "minecraft.resources.datapack.view" ->
          surface(
              boundary,
              MinecraftResourceVisibilityGenerationLane.RUNTIME_RESOURCE_VISIBILITY,
              true,
              false,
              false,
              false,
              true,
              false,
              true,
              true,
              false,
              "Runtime datapack visibility lane; Target-19 does not access, expose, or mutate datapack state.");
      case "minecraft.resources.resource_manager.view" ->
          surface(
              boundary,
              MinecraftResourceVisibilityGenerationLane.RUNTIME_RESOURCE_VISIBILITY,
              true,
              false,
              false,
              false,
              true,
              false,
              true,
              true,
              false,
              "Runtime resource manager visibility lane; Target-19 does not access, expose, or mutate resource manager state.");
      case "minecraft.resources.future_data_generation" ->
          surface(
              boundary,
              MinecraftResourceVisibilityGenerationLane.OFFLINE_DATA_GENERATION,
              false,
              true,
              false,
              false,
              false,
              true,
              false,
              false,
              false,
              "Future offline data generation lane; intentionally separate from runtime reload, runtime resource access, and registry mutation.");
      default ->
          throw new IllegalArgumentException(
              "Target-19 encountered unexpected Target-16 boundary `"
                  + boundary.boundaryId()
                  + "`.");
    };
  }

  private MinecraftResourceVisibilityGenerationSurface surface(
      MinecraftAnalyzedResourceReloadBoundary boundary,
      MinecraftResourceVisibilityGenerationLane lane,
      boolean runtimeFacing,
      boolean offlineGenerationFacing,
      boolean availableInTarget19,
      boolean requiresRuntimeReloadTiming,
      boolean requiresRuntimeResourceVisibilityDesign,
      boolean requiresOfflineGenerationDesign,
      boolean requiresBindingRequirements,
      boolean requiresFutureSteelHookPrimitive,
      boolean implementationReady,
      String notes) {
    return new MinecraftResourceVisibilityGenerationSurface(
        boundary.boundaryId(),
        boundary.displayName(),
        boundary.order(),
        lane,
        runtimeFacing,
        offlineGenerationFacing,
        availableInTarget19,
        requiresRuntimeReloadTiming,
        requiresRuntimeResourceVisibilityDesign,
        requiresOfflineGenerationDesign,
        requiresBindingRequirements,
        requiresFutureSteelHookPrimitive,
        implementationReady,
        boundary.status(),
        notes);
  }

  private int countRuntimeFacing(List<MinecraftResourceVisibilityGenerationSurface> surfaces) {
    return (int)
        surfaces.stream()
            .filter(MinecraftResourceVisibilityGenerationSurface::runtimeFacing)
            .count();
  }

  private int countOfflineFacing(List<MinecraftResourceVisibilityGenerationSurface> surfaces) {
    return (int)
        surfaces.stream()
            .filter(MinecraftResourceVisibilityGenerationSurface::offlineGenerationFacing)
            .count();
  }

  private int countByLane(
      List<MinecraftResourceVisibilityGenerationSurface> surfaces,
      MinecraftResourceVisibilityGenerationLane lane) {
    return (int) surfaces.stream().filter(surface -> surface.lane() == lane).count();
  }

  private int countImplementationReady(
      List<MinecraftResourceVisibilityGenerationSurface> surfaces) {
    return (int)
        surfaces.stream()
            .filter(MinecraftResourceVisibilityGenerationSurface::implementationReady)
            .count();
  }

  private int countFutureSteelHookRequired(
      List<MinecraftResourceVisibilityGenerationSurface> surfaces) {
    return (int)
        surfaces.stream()
            .filter(MinecraftResourceVisibilityGenerationSurface::requiresFutureSteelHookPrimitive)
            .count();
  }
}
