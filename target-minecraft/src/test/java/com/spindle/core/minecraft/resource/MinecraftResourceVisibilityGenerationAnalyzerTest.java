package com.spindle.core.minecraft.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftResourceVisibilityGenerationAnalyzerTest {
  private final MinecraftResourceVisibilityGenerationAnalyzer analyzer =
      new MinecraftResourceVisibilityGenerationAnalyzer();

  @Test
  void passedTarget16AndTarget18InputsProduceSeparationClassified() {
    MinecraftResourceVisibilityGenerationAnalysis analysis =
        analyzer.analyze(target16(true), target18(true));

    assertTrue(analysis.gatePassed());
    assertNull(analysis.gateFailureReason());
    assertEquals(
        MinecraftResourceVisibilityGenerationStatus.SEPARATION_CLASSIFIED,
        analysis.separationStatus());
  }

  @Test
  void blockedTarget16InputProducesUpstreamGateBlocked() {
    MinecraftResourceVisibilityGenerationAnalysis analysis =
        analyzer.analyze(target16(false), target18(true));

    assertFalse(analysis.gatePassed());
    assertEquals(
        MinecraftResourceVisibilityGenerationStatus.UPSTREAM_GATE_BLOCKED,
        analysis.separationStatus());
    assertEquals(
        "Target-19 requires passed Target-16 resource/reload analysis and passed Target-18 binding analysis before separating runtime visibility from future data generation.",
        analysis.gateFailureReason());
  }

  @Test
  void blockedTarget18InputProducesUpstreamGateBlocked() {
    MinecraftResourceVisibilityGenerationAnalysis analysis =
        analyzer.analyze(target16(true), target18(false));

    assertFalse(analysis.gatePassed());
    assertEquals(
        MinecraftResourceVisibilityGenerationStatus.UPSTREAM_GATE_BLOCKED,
        analysis.separationStatus());
  }

  @Test
  void allSevenTarget16BoundariesBecomeSevenTarget19SurfacesInExactOrder() {
    MinecraftResourceVisibilityGenerationAnalysis analysis =
        analyzer.analyze(target16(true), target18(true));

    assertEquals(
        List.of(
            "minecraft.resources.lifecycle_anchor",
            "minecraft.resources.reload.discovery",
            "minecraft.resources.reload.window",
            "minecraft.resources.reload.apply",
            "minecraft.resources.datapack.view",
            "minecraft.resources.resource_manager.view",
            "minecraft.resources.future_data_generation"),
        analysis.surfaces().stream()
            .map(MinecraftResourceVisibilityGenerationSurface::boundaryId)
            .toList());
  }

  @Test
  void surfacesMapToExpectedLanesAndFacingFlags() {
    MinecraftResourceVisibilityGenerationAnalysis analysis =
        analyzer.analyze(target16(true), target18(true));

    assertEquals(
        MinecraftResourceVisibilityGenerationLane.SERVER_LIFECYCLE_ANCHOR,
        analysis.surfaces().get(0).lane());
    assertEquals(
        MinecraftResourceVisibilityGenerationLane.SYMBOL_DISCOVERY,
        analysis.surfaces().get(1).lane());
    assertEquals(
        MinecraftResourceVisibilityGenerationLane.RESOURCE_RELOAD_TIMING,
        analysis.surfaces().get(2).lane());
    assertEquals(
        MinecraftResourceVisibilityGenerationLane.RESOURCE_RELOAD_TIMING,
        analysis.surfaces().get(3).lane());
    assertEquals(
        MinecraftResourceVisibilityGenerationLane.RUNTIME_RESOURCE_VISIBILITY,
        analysis.surfaces().get(4).lane());
    assertEquals(
        MinecraftResourceVisibilityGenerationLane.RUNTIME_RESOURCE_VISIBILITY,
        analysis.surfaces().get(5).lane());
    assertEquals(
        MinecraftResourceVisibilityGenerationLane.OFFLINE_DATA_GENERATION,
        analysis.surfaces().get(6).lane());

    assertFalse(analysis.surfaces().get(6).runtimeFacing());
    assertTrue(analysis.surfaces().get(6).offlineGenerationFacing());
    assertTrue(analysis.surfaces().get(4).runtimeFacing());
    assertFalse(analysis.surfaces().get(4).offlineGenerationFacing());
    assertTrue(analysis.surfaces().get(5).runtimeFacing());
    assertFalse(analysis.surfaces().get(5).offlineGenerationFacing());
  }

  @Test
  void onlyLifecycleAnchorMayBeAvailableInTarget19WhenTarget16MadeItAvailable() {
    MinecraftResourceVisibilityGenerationAnalysis analysis =
        analyzer.analyze(target16(true), target18(true));

    assertTrue(analysis.surfaces().getFirst().availableInTarget19());
    assertEquals(
        0,
        analysis.surfaces().subList(1, analysis.surfaces().size()).stream()
            .filter(MinecraftResourceVisibilityGenerationSurface::availableInTarget19)
            .count());
  }

  @Test
  void aggregateAndConservativeFlagsRemainFixed() {
    MinecraftResourceVisibilityGenerationAnalysis analysis =
        analyzer.analyze(target16(true), target18(true));

    assertTrue(analysis.runtimeVisibilitySeparatedFromOfflineGeneration());
    assertTrue(analysis.dataGenerationRequiresOfflineDesign());
    assertTrue(analysis.runtimeReloadRequiresFutureBindingDecision());
    assertEquals(0, analysis.implementationReadySurfaceCount());
    assertFalse(analysis.reloadProofRecommended());
    assertFalse(analysis.currentSteelHookMethodEntryCompatible());
    assertFalse(analysis.classLoadingOccurred());
    assertFalse(analysis.injectionOccurred());
    assertFalse(analysis.transformationOccurred());
    assertFalse(analysis.patchingOccurred());
    assertFalse(analysis.hookInstallationOccurred());
    assertFalse(analysis.runtimeDispatchOccurred());
    assertFalse(analysis.resourceReloadOccurred());
    assertFalse(analysis.resourceAccessOccurred());
    assertFalse(analysis.datapackAccessOccurred());
    assertFalse(analysis.dataGenerationOccurred());
    assertFalse(analysis.generatedFileWriteOccurred());
    assertFalse(analysis.registryMutationOccurred());
    assertFalse(analysis.publicApiExposed());
    assertFalse(analysis.javaModExecutionSandboxed());
  }

  @Test
  void analyzerRejectsNonTarget16ResourceAnalysisInput() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    new MinecraftResourceReloadAnalysis(
                        1,
                        "Target-15",
                        "minecraft",
                        "26.1.2",
                        MinecraftSide.SERVER,
                        "minecraft.concept.data_resources_reload",
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
                        "Target-12",
                        true,
                        true,
                        true,
                        null,
                        1,
                        6,
                        0,
                        target16(true).boundaries()),
                    target18(true)));

    assertTrue(exception.getMessage().contains("Target-16"));
  }

  @Test
  void analyzerRejectsNonTarget18BindingAnalysisInput() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    target16(true),
                    new MinecraftResourceReloadBindingAnalysis(
                        1,
                        "Target-17",
                        "minecraft",
                        "26.1.2",
                        MinecraftSide.SERVER,
                        "minecraft.concept.data_resources_reload",
                        "Target-17",
                        "minecraft.resources.reload.discovery",
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
                        true,
                        "BINDING_REQUIREMENTS_CLASSIFIED",
                        true,
                        true,
                        null,
                        MinecraftResourceReloadBindingStatus.BINDING_REQUIREMENTS_CLASSIFIED,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        false,
                        false,
                        "Next.",
                        List.of())));

    assertTrue(exception.getMessage().contains("Target-18"));
  }

  @Test
  void analyzerRejectsUnexpectedConceptIds() {
    IllegalArgumentException resourceException =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    new MinecraftResourceReloadAnalysis(
                        1,
                        "Target-16",
                        "minecraft",
                        "26.1.2",
                        MinecraftSide.SERVER,
                        "minecraft.concept.unexpected",
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
                        "Target-12",
                        true,
                        true,
                        true,
                        null,
                        1,
                        6,
                        0,
                        target16(true).boundaries()),
                    target18(true)));
    assertTrue(resourceException.getMessage().contains("minecraft.concept.data_resources_reload"));

    IllegalArgumentException bindingException =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    target16(true),
                    new MinecraftResourceReloadBindingAnalysis(
                        1,
                        "Target-18",
                        "minecraft",
                        "26.1.2",
                        MinecraftSide.SERVER,
                        "minecraft.concept.unexpected",
                        "Target-17",
                        "minecraft.resources.reload.discovery",
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
                        true,
                        "BINDING_REQUIREMENTS_CLASSIFIED",
                        true,
                        true,
                        null,
                        MinecraftResourceReloadBindingStatus.BINDING_REQUIREMENTS_CLASSIFIED,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        false,
                        false,
                        "Next.",
                        List.of())));
    assertTrue(bindingException.getMessage().contains("minecraft.concept.data_resources_reload"));
  }

  private MinecraftResourceReloadAnalysis target16(boolean gatePassed) {
    return new MinecraftResourceReloadAnalysis(
        1,
        "Target-16",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
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
        "Target-12",
        gatePassed,
        gatePassed,
        gatePassed,
        gatePassed
            ? null
            : "Target-16 requires the Target-12 starting lifecycle dispatch anchor before resource/reload boundaries can be anchored.",
        gatePassed ? 1 : 0,
        6,
        gatePassed ? 0 : 1,
        List.of(
            boundary(
                "minecraft.resources.lifecycle_anchor",
                "Lifecycle Anchor",
                1,
                gatePassed
                    ? MinecraftResourceReloadBoundaryStatus.AVAILABLE
                    : MinecraftResourceReloadBoundaryStatus.UPSTREAM_GATE_BLOCKED),
            boundary(
                "minecraft.resources.reload.discovery",
                "Reload Discovery",
                2,
                MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND),
            boundary(
                "minecraft.resources.reload.window",
                "Reload Window",
                3,
                MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND),
            boundary(
                "minecraft.resources.reload.apply",
                "Reload Apply",
                4,
                MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND),
            boundary(
                "minecraft.resources.datapack.view",
                "Datapack View",
                5,
                MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND),
            boundary(
                "minecraft.resources.resource_manager.view",
                "Resource Manager View",
                6,
                MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND),
            boundary(
                "minecraft.resources.future_data_generation",
                "Future Data Generation",
                7,
                MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND)));
  }

  private MinecraftAnalyzedResourceReloadBoundary boundary(
      String id, String displayName, int order, MinecraftResourceReloadBoundaryStatus status) {
    return new MinecraftAnalyzedResourceReloadBoundary(
        id,
        displayName,
        order,
        status,
        MinecraftResourceReloadRepresentationKind.SERVER_LIFECYCLE_ANCHOR,
        status == MinecraftResourceReloadBoundaryStatus.AVAILABLE,
        null,
        null,
        false,
        false,
        false,
        false,
        "Notes.");
  }

  private MinecraftResourceReloadBindingAnalysis target18(boolean gatePassed) {
    return new MinecraftResourceReloadBindingAnalysis(
        1,
        "Target-18",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-17",
        "minecraft.resources.reload.discovery",
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
        gatePassed,
        "BINDING_REQUIREMENTS_CLASSIFIED",
        true,
        gatePassed,
        gatePassed ? null : "Target-18 gate failure reason.",
        gatePassed
            ? MinecraftResourceReloadBindingStatus.BINDING_REQUIREMENTS_CLASSIFIED
            : MinecraftResourceReloadBindingStatus.UPSTREAM_GATE_BLOCKED,
        1,
        1,
        1,
        0,
        1,
        0,
        0,
        0,
        0,
        false,
        false,
        "Next.",
        List.of());
  }
}
