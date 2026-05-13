package com.spindle.core.minecraft.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftResourceReloadArcDecisionAnalyzerTest {
  private final MinecraftResourceReloadArcDecisionAnalyzer analyzer =
      new MinecraftResourceReloadArcDecisionAnalyzer();

  @Test
  void passedInputsProduceResourceReloadArcCaboosed() {
    MinecraftResourceReloadArcDecisionAnalysis analysis =
        analyzer.analyze(target16(true), target17(true), target18(true), target19(true));

    assertTrue(analysis.gatePassed());
    assertNull(analysis.gateFailureReason());
    assertEquals(
        MinecraftResourceReloadArcDecisionStatus.RESOURCE_RELOAD_ARC_CABOOSED,
        analysis.decisionStatus());
    assertEquals(
        MinecraftResourceReloadNextDirection.MOVE_TO_REGISTRY_BOOTSTRAP, analysis.nextDirection());
    assertTrue(analysis.resourceReloadArcCompleteForNow());
    assertTrue(analysis.registryBootstrapRecommended());
    assertFalse(analysis.steelHookPrimitiveDesignRecommendedNow());
    assertFalse(analysis.continueResourceReloadAnalysisRecommended());
    assertFalse(analysis.resourceReloadImplementationReady());
    assertFalse(analysis.resourceReloadProofRecommended());
    assertFalse(analysis.currentSteelHookMethodEntryCompatible());
  }

  @Test
  void blockedTarget16InputProducesUpstreamGateBlockedAndUndecided() {
    assertBlockedDecision(
        analyzer.analyze(target16(false), target17(true), target18(true), target19(true)));
  }

  @Test
  void blockedTarget17InputProducesUpstreamGateBlockedAndUndecided() {
    assertBlockedDecision(
        analyzer.analyze(target16(true), target17(false), target18(true), target19(true)));
  }

  @Test
  void blockedTarget18InputProducesUpstreamGateBlockedAndUndecided() {
    assertBlockedDecision(
        analyzer.analyze(target16(true), target17(true), target18(false), target19(true)));
  }

  @Test
  void blockedTarget19InputProducesUpstreamGateBlockedAndUndecided() {
    assertBlockedDecision(
        analyzer.analyze(target16(true), target17(true), target18(true), target19(false)));
  }

  @Test
  void sourceStatusesAndTarget19SeparationFlagsAreCopied() {
    MinecraftResourceReloadArcDecisionAnalysis analysis =
        analyzer.analyze(target16(true), target17(true), target18(true), target19(true));

    assertEquals("CANDIDATES_DISCOVERED", analysis.sourceSymbolDiscoveryStatus());
    assertEquals("BINDING_REQUIREMENTS_CLASSIFIED", analysis.sourceBindingStatus());
    assertEquals("SEPARATION_CLASSIFIED", analysis.sourceVisibilityGenerationStatus());
    assertTrue(analysis.sourceRuntimeVisibilitySeparatedFromOfflineGeneration());
    assertTrue(analysis.sourceDataGenerationRequiresOfflineDesign());
    assertTrue(analysis.sourceRuntimeReloadRequiresFutureBindingDecision());
  }

  @Test
  void recommendedNextFieldsAreRegistryBootstrapTarget21() {
    MinecraftResourceReloadArcDecisionAnalysis analysis =
        analyzer.analyze(target16(true), target17(true), target18(true), target19(true));

    assertEquals("minecraft.concept.registry_bootstrap", analysis.recommendedNextConceptId());
    assertEquals("Target-21", analysis.recommendedNextMilestoneName());
    assertEquals("Registry Bootstrap Boundary Analysis", analysis.recommendedNextPassTitle());
  }

  @Test
  void allFiveFindingsAppearInExactOrder() {
    MinecraftResourceReloadArcDecisionAnalysis analysis =
        analyzer.analyze(target16(true), target17(true), target18(true), target19(true));

    assertEquals(
        List.of(
            "target-20.resource.reload.arc.finding.001",
            "target-20.resource.reload.arc.finding.002",
            "target-20.resource.reload.arc.finding.003",
            "target-20.resource.reload.arc.finding.004",
            "target-20.resource.reload.arc.finding.005"),
        analysis.findings().stream().map(MinecraftResourceReloadArcDecisionFinding::id).toList());
    assertTrue(analysis.findings().get(4).recommendedForImmediateImplementation());
  }

  @Test
  void mutationRuntimeApiAndSandboxFlagsRemainFalse() {
    MinecraftResourceReloadArcDecisionAnalysis analysis =
        analyzer.analyze(target16(true), target17(true), target18(true), target19(true));

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
    assertFalse(analysis.registryBootstrapOccurred());
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
                        List.of()),
                    target17(true),
                    target18(true),
                    target19(true)));

    assertTrue(exception.getMessage().contains("Target-16"));
  }

  @Test
  void analyzerRejectsNonTarget17SymbolAnalysisInput() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    target16(true),
                    new MinecraftResourceReloadSymbolAnalysis(
                        1,
                        "Target-16",
                        "minecraft",
                        "26.1.2",
                        MinecraftSide.SERVER,
                        "minecraft.concept.data_resources_reload",
                        "Target-16",
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
                        true,
                        true,
                        true,
                        null,
                        List.of("reload"),
                        1,
                        1,
                        0,
                        0,
                        0,
                        0,
                        1,
                        0,
                        MinecraftResourceReloadSymbolDiscoveryStatus.CANDIDATES_DISCOVERED,
                        true,
                        List.of()),
                    target18(true),
                    target19(true)));

    assertTrue(exception.getMessage().contains("Target-17"));
  }

  @Test
  void analyzerRejectsNonTarget18BindingAnalysisInput() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    target16(true),
                    target17(true),
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
                        "CANDIDATES_DISCOVERED",
                        true,
                        true,
                        null,
                        MinecraftResourceReloadBindingStatus.BINDING_REQUIREMENTS_CLASSIFIED,
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
                        List.of()),
                    target19(true)));

    assertTrue(exception.getMessage().contains("Target-18"));
  }

  @Test
  void analyzerRejectsNonTarget19VisibilityGenerationAnalysisInput() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    target16(true),
                    target17(true),
                    target18(true),
                    new MinecraftResourceVisibilityGenerationAnalysis(
                        1,
                        "Target-18",
                        "minecraft",
                        "26.1.2",
                        MinecraftSide.SERVER,
                        "minecraft.concept.data_resources_reload",
                        "Target-16",
                        "Target-18",
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
                        true,
                        true,
                        "BINDING_REQUIREMENTS_CLASSIFIED",
                        false,
                        false,
                        true,
                        null,
                        MinecraftResourceVisibilityGenerationStatus.SEPARATION_CLASSIFIED,
                        5,
                        1,
                        2,
                        2,
                        1,
                        0,
                        4,
                        true,
                        true,
                        true,
                        false,
                        false,
                        "Next.",
                        List.of())));

    assertTrue(exception.getMessage().contains("Target-19"));
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
                        List.of()),
                    target17(true),
                    target18(true),
                    target19(true)));
    assertTrue(resourceException.getMessage().contains("minecraft.concept.data_resources_reload"));

    IllegalArgumentException symbolException =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    target16(true),
                    new MinecraftResourceReloadSymbolAnalysis(
                        1,
                        "Target-17",
                        "minecraft",
                        "26.1.2",
                        MinecraftSide.SERVER,
                        "minecraft.concept.unexpected",
                        "Target-16",
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
                        true,
                        true,
                        true,
                        null,
                        List.of("reload"),
                        1,
                        1,
                        0,
                        0,
                        0,
                        0,
                        1,
                        0,
                        MinecraftResourceReloadSymbolDiscoveryStatus.CANDIDATES_DISCOVERED,
                        true,
                        List.of()),
                    target18(true),
                    target19(true)));
    assertTrue(symbolException.getMessage().contains("minecraft.concept.data_resources_reload"));

    IllegalArgumentException bindingException =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    target16(true),
                    target17(true),
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
                        "CANDIDATES_DISCOVERED",
                        true,
                        true,
                        null,
                        MinecraftResourceReloadBindingStatus.BINDING_REQUIREMENTS_CLASSIFIED,
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
                        List.of()),
                    target19(true)));
    assertTrue(bindingException.getMessage().contains("minecraft.concept.data_resources_reload"));

    IllegalArgumentException visibilityException =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    target16(true),
                    target17(true),
                    target18(true),
                    new MinecraftResourceVisibilityGenerationAnalysis(
                        1,
                        "Target-19",
                        "minecraft",
                        "26.1.2",
                        MinecraftSide.SERVER,
                        "minecraft.concept.unexpected",
                        "Target-16",
                        "Target-18",
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
                        true,
                        true,
                        "BINDING_REQUIREMENTS_CLASSIFIED",
                        false,
                        false,
                        true,
                        null,
                        MinecraftResourceVisibilityGenerationStatus.SEPARATION_CLASSIFIED,
                        5,
                        1,
                        2,
                        2,
                        1,
                        0,
                        4,
                        true,
                        true,
                        true,
                        false,
                        false,
                        "Next.",
                        List.of())));
    assertTrue(
        visibilityException.getMessage().contains("minecraft.concept.data_resources_reload"));
  }

  private void assertBlockedDecision(MinecraftResourceReloadArcDecisionAnalysis analysis) {
    assertFalse(analysis.gatePassed());
    assertEquals(
        MinecraftResourceReloadArcDecisionStatus.UPSTREAM_GATE_BLOCKED, analysis.decisionStatus());
    assertEquals(
        MinecraftResourceReloadNextDirection.UNDECIDED_UPSTREAM_BLOCKED, analysis.nextDirection());
    assertFalse(analysis.resourceReloadArcCompleteForNow());
    assertFalse(analysis.registryBootstrapRecommended());
    assertFalse(analysis.steelHookPrimitiveDesignRecommendedNow());
    assertFalse(analysis.continueResourceReloadAnalysisRecommended());
    assertFalse(analysis.resourceReloadImplementationReady());
    assertFalse(analysis.resourceReloadProofRecommended());
    assertFalse(analysis.currentSteelHookMethodEntryCompatible());
    assertEquals(
        "Target-20 requires passed Target-16, Target-17, Target-18, and Target-19 resource/reload analyses before recording the registry handoff decision.",
        analysis.gateFailureReason());
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
        gatePassed ? null : "Target-16 gate failure reason.",
        gatePassed ? 1 : 0,
        6,
        gatePassed ? 0 : 1,
        List.of());
  }

  private MinecraftResourceReloadSymbolAnalysis target17(boolean gatePassed) {
    return new MinecraftResourceReloadSymbolAnalysis(
        1,
        "Target-17",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-16",
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
        true,
        true,
        gatePassed,
        gatePassed ? null : "Target-17 gate failure reason.",
        List.of("reload"),
        1,
        1,
        0,
        0,
        0,
        0,
        gatePassed ? 1 : 0,
        gatePassed ? 0 : 1,
        gatePassed
            ? MinecraftResourceReloadSymbolDiscoveryStatus.CANDIDATES_DISCOVERED
            : MinecraftResourceReloadSymbolDiscoveryStatus.UPSTREAM_GATE_BLOCKED,
        gatePassed,
        List.of());
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
        true,
        "CANDIDATES_DISCOVERED",
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

  private MinecraftResourceVisibilityGenerationAnalysis target19(boolean gatePassed) {
    return new MinecraftResourceVisibilityGenerationAnalysis(
        1,
        "Target-19",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-16",
        "Target-18",
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
        true,
        true,
        "BINDING_REQUIREMENTS_CLASSIFIED",
        false,
        false,
        gatePassed,
        gatePassed ? null : "Target-19 gate failure reason.",
        gatePassed
            ? MinecraftResourceVisibilityGenerationStatus.SEPARATION_CLASSIFIED
            : MinecraftResourceVisibilityGenerationStatus.UPSTREAM_GATE_BLOCKED,
        5,
        1,
        2,
        2,
        1,
        0,
        4,
        true,
        true,
        true,
        false,
        false,
        "Next.",
        List.of());
  }
}
