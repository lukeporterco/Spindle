package com.spindle.core.minecraft.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftResourceReloadBindingAnalyzerTest {
  private final MinecraftResourceReloadBindingAnalyzer analyzer =
      new MinecraftResourceReloadBindingAnalyzer();

  @Test
  void target17UpstreamGateBlockedMapsToTarget18UpstreamGateBlocked() {
    MinecraftResourceReloadBindingAnalysis analysis =
        analyzer.analyze(
            symbolAnalysis(
                MinecraftResourceReloadSymbolDiscoveryStatus.UPSTREAM_GATE_BLOCKED,
                false,
                false,
                List.of(selectableClassCandidate())));

    assertEquals(
        MinecraftResourceReloadBindingStatus.UPSTREAM_GATE_BLOCKED, analysis.bindingStatus());
    assertFalse(analysis.gatePassed());
    assertEquals("Target-17 gate failure reason.", analysis.gateFailureReason());
    assertFalse(analysis.reloadProofRecommended());
    assertFalse(analysis.currentSteelHookMethodEntryCompatible());
    assertEquals(1, analysis.analyzedCandidateCount());
    assertEquals(
        MinecraftResourceReloadAccessStrategy.CLASS_REFERENCE_ONLY,
        analysis.candidates().getFirst().accessStrategy());
  }

  @Test
  void target17NoCandidatesMapsToNoSymbolCandidates() {
    MinecraftResourceReloadBindingAnalysis analysis =
        analyzer.analyze(
            symbolAnalysis(
                MinecraftResourceReloadSymbolDiscoveryStatus.NO_CANDIDATES,
                true,
                false,
                List.of()));

    assertEquals(
        MinecraftResourceReloadBindingStatus.NO_SYMBOL_CANDIDATES, analysis.bindingStatus());
    assertTrue(analysis.gatePassed());
    assertFalse(analysis.reloadProofRecommended());
    assertFalse(analysis.currentSteelHookMethodEntryCompatible());
    assertEquals(
        "Do not implement resource reload handling yet; no resource/reload symbol candidates were discovered.",
        analysis.nextRecommendedAction());
  }

  @Test
  void target17OnlyRejectedCandidatesMapsToOnlyRejectedSymbolCandidates() {
    MinecraftResourceReloadBindingAnalysis analysis =
        analyzer.analyze(
            symbolAnalysis(
                MinecraftResourceReloadSymbolDiscoveryStatus.ONLY_REJECTED_CANDIDATES,
                true,
                false,
                List.of(rejectedClassCandidate())));

    assertEquals(
        MinecraftResourceReloadBindingStatus.ONLY_REJECTED_SYMBOL_CANDIDATES,
        analysis.bindingStatus());
    assertTrue(analysis.gatePassed());
    MinecraftResourceReloadBindingCandidate candidate = analysis.candidates().getFirst();
    assertFalse(candidate.selectable());
    assertEquals(MinecraftResourceReloadAccessStrategy.NONE, candidate.accessStrategy());
    assertEquals(
        "Rejected source candidate carried forward; non-net/minecraft candidates are not resource/reload binding targets.",
        candidate.notes());
  }

  @Test
  void target17CandidatesDiscoveredMapToBindingRequirementsClassified() {
    MinecraftResourceReloadBindingAnalysis analysis =
        analyzer.analyze(
            symbolAnalysis(
                MinecraftResourceReloadSymbolDiscoveryStatus.CANDIDATES_DISCOVERED,
                true,
                true,
                allCandidateKinds()));

    assertEquals(
        MinecraftResourceReloadBindingStatus.BINDING_REQUIREMENTS_CLASSIFIED,
        analysis.bindingStatus());
    assertTrue(analysis.gatePassed());
    assertEquals(
        "Do not implement resource reload handling yet; use these classified requirements as input to future resource visibility and SteelHook primitive decisions.",
        analysis.nextRecommendedAction());
  }

  @Test
  void candidateKindsMapToRequiredAccessStrategiesInSourceOrder() {
    MinecraftResourceReloadBindingAnalysis analysis =
        analyzer.analyze(
            symbolAnalysis(
                MinecraftResourceReloadSymbolDiscoveryStatus.CANDIDATES_DISCOVERED,
                true,
                true,
                allCandidateKinds()));

    assertEquals(
        List.of(
            "target-17.minecraft.resources.reload.candidate.001",
            "target-17.minecraft.resources.reload.candidate.002",
            "target-17.minecraft.resources.reload.candidate.003",
            "target-17.minecraft.resources.reload.candidate.004",
            "target-17.minecraft.resources.reload.candidate.005",
            "target-17.minecraft.resources.reload.candidate.006",
            "target-17.minecraft.resources.reload.candidate.007",
            "target-17.minecraft.resources.reload.candidate.008",
            "target-17.minecraft.resources.reload.candidate.009"),
        analysis.candidates().stream()
            .map(MinecraftResourceReloadBindingCandidate::sourceCandidateId)
            .toList());
    assertEquals(
        List.of(
            MinecraftResourceReloadAccessStrategy.NONE,
            MinecraftResourceReloadAccessStrategy.CLASS_REFERENCE_ONLY,
            MinecraftResourceReloadAccessStrategy.STATIC_METHOD_BOUNDARY_ANALYSIS_REQUIRED,
            MinecraftResourceReloadAccessStrategy.INSTANCE_METHOD_RECEIVER_CAPTURE_REQUIRED,
            MinecraftResourceReloadAccessStrategy.STATIC_METHOD_BOUNDARY_ANALYSIS_REQUIRED,
            MinecraftResourceReloadAccessStrategy.INSTANCE_METHOD_RECEIVER_CAPTURE_REQUIRED,
            MinecraftResourceReloadAccessStrategy.STATIC_FIELD_ACCESS_REQUIRED,
            MinecraftResourceReloadAccessStrategy.INSTANCE_FIELD_OWNER_CAPTURE_REQUIRED,
            MinecraftResourceReloadAccessStrategy.STATIC_FIELD_ACCESS_REQUIRED),
        analysis.candidates().stream()
            .map(MinecraftResourceReloadBindingCandidate::accessStrategy)
            .toList());
  }

  @Test
  void aggregateCountsAndFlagsAreDeterministic() {
    MinecraftResourceReloadBindingAnalysis analysis =
        analyzer.analyze(
            symbolAnalysis(
                MinecraftResourceReloadSymbolDiscoveryStatus.CANDIDATES_DISCOVERED,
                true,
                true,
                allCandidateKinds()));

    assertEquals(9, analysis.sourceCandidateCount());
    assertEquals(9, analysis.analyzedCandidateCount());
    assertEquals(8, analysis.selectableCandidateCount());
    assertEquals(1, analysis.rejectedCandidateCount());
    assertEquals(1, analysis.classReferenceOnlyCount());
    assertEquals(4, analysis.methodBoundaryAnalysisRequiredCount());
    assertEquals(3, analysis.fieldAccessRequiredCount());
    assertEquals(3, analysis.receiverCaptureRequiredCount());
    assertEquals(7, analysis.futureSteelHookPrimitiveRequiredCount());
    assertFalse(analysis.reloadProofRecommended());
    assertFalse(analysis.currentSteelHookMethodEntryCompatible());
  }

  @Test
  void allMutationRuntimeApiAndSandboxFlagsRemainFalse() {
    MinecraftResourceReloadBindingAnalysis analysis =
        analyzer.analyze(
            symbolAnalysis(
                MinecraftResourceReloadSymbolDiscoveryStatus.NO_CANDIDATES,
                true,
                false,
                List.of()));

    assertEquals(1, analysis.schema());
    assertEquals("Target-18", analysis.milestoneName());
    assertTrue(analysis.analysisOnly());
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
    assertFalse(analysis.registryMutationOccurred());
    assertFalse(analysis.publicApiExposed());
    assertFalse(analysis.javaModExecutionSandboxed());
  }

  @Test
  void analyzerRejectsNonTarget17InputMilestone() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
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
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        MinecraftResourceReloadSymbolDiscoveryStatus.NO_CANDIDATES,
                        false,
                        List.of())));

    assertTrue(exception.getMessage().contains("Target-17"));
  }

  @Test
  void analyzerRejectsUnexpectedConceptId() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
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
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        MinecraftResourceReloadSymbolDiscoveryStatus.NO_CANDIDATES,
                        false,
                        List.of())));

    assertTrue(exception.getMessage().contains("minecraft.concept.data_resources_reload"));
  }

  private MinecraftResourceReloadSymbolAnalysis symbolAnalysis(
      MinecraftResourceReloadSymbolDiscoveryStatus discoveryStatus,
      boolean gatePassed,
      boolean bindingStrategyAnalysisEligible,
      List<MinecraftResourceReloadSymbolCandidate> candidates) {
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
        gatePassed,
        true,
        true,
        gatePassed,
        gatePassed ? null : "Target-17 gate failure reason.",
        List.of("reload", "resource"),
        candidates.size(),
        count(candidates, MinecraftResourceReloadSymbolCandidateKind.CLASS_NAME_REFERENCE),
        count(candidates, MinecraftResourceReloadSymbolCandidateKind.FIELD_NAME_REFERENCE),
        count(candidates, MinecraftResourceReloadSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE),
        count(candidates, MinecraftResourceReloadSymbolCandidateKind.METHOD_NAME_REFERENCE),
        count(candidates, MinecraftResourceReloadSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE),
        (int)
            candidates.stream().filter(MinecraftResourceReloadSymbolCandidate::selectable).count(),
        (int) candidates.stream().filter(candidate -> !candidate.selectable()).count(),
        discoveryStatus,
        bindingStrategyAnalysisEligible,
        candidates);
  }

  private int count(
      List<MinecraftResourceReloadSymbolCandidate> candidates,
      MinecraftResourceReloadSymbolCandidateKind kind) {
    return (int) candidates.stream().filter(candidate -> candidate.kind() == kind).count();
  }

  private List<MinecraftResourceReloadSymbolCandidate> allCandidateKinds() {
    return List.of(
        rejectedClassCandidate(),
        selectableClassCandidate(),
        candidate(
            "003",
            MinecraftResourceReloadSymbolCandidateKind.METHOD_NAME_REFERENCE,
            "net/minecraft/server/ReloadState",
            "reloadStatic",
            "()V",
            true,
            true,
            null),
        candidate(
            "004",
            MinecraftResourceReloadSymbolCandidateKind.METHOD_NAME_REFERENCE,
            "net/minecraft/server/ReloadState",
            "reloadInstance",
            "()V",
            false,
            true,
            null),
        candidate(
            "005",
            MinecraftResourceReloadSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE,
            "net/minecraft/server/ReloadState",
            "create",
            "(Lnet/minecraft/server/packs/resources/ReloadInstance;)V",
            true,
            true,
            null),
        candidate(
            "006",
            MinecraftResourceReloadSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE,
            "net/minecraft/server/ReloadState",
            "bind",
            "(Lnet/minecraft/server/packs/resources/ReloadInstance;)V",
            false,
            true,
            null),
        candidate(
            "007",
            MinecraftResourceReloadSymbolCandidateKind.FIELD_NAME_REFERENCE,
            "net/minecraft/server/ReloadState",
            "RESOURCE_MANAGER",
            "Ljava/lang/Object;",
            true,
            true,
            null),
        candidate(
            "008",
            MinecraftResourceReloadSymbolCandidateKind.FIELD_NAME_REFERENCE,
            "net/minecraft/server/ReloadState",
            "resourceManager",
            "Ljava/lang/Object;",
            false,
            true,
            null),
        candidate(
            "009",
            MinecraftResourceReloadSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE,
            "net/minecraft/server/ReloadState",
            "holder",
            "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;",
            true,
            true,
            null));
  }

  private MinecraftResourceReloadSymbolCandidate selectableClassCandidate() {
    return candidate(
        "002",
        MinecraftResourceReloadSymbolCandidateKind.CLASS_NAME_REFERENCE,
        "net/minecraft/server/ServerResources",
        null,
        null,
        false,
        true,
        null);
  }

  private MinecraftResourceReloadSymbolCandidate rejectedClassCandidate() {
    return candidate(
        "001",
        MinecraftResourceReloadSymbolCandidateKind.CLASS_NAME_REFERENCE,
        "com/example/ServerResources",
        null,
        null,
        false,
        false,
        "Only net/minecraft/* owners are selectable resource/reload symbol candidates in Target-17.");
  }

  private MinecraftResourceReloadSymbolCandidate candidate(
      String suffix,
      MinecraftResourceReloadSymbolCandidateKind kind,
      String ownerInternalName,
      String memberName,
      String descriptor,
      boolean staticMember,
      boolean selectable,
      String rejectionReason) {
    return new MinecraftResourceReloadSymbolCandidate(
        "target-17.minecraft.resources.reload.candidate.%s".formatted(suffix),
        kind,
        "minecraft.resources.reload.discovery",
        ownerInternalName,
        memberName,
        descriptor,
        staticMember,
        List.of("PUBLIC"),
        List.of("reload", "resource"),
        selectable,
        rejectionReason,
        "Target-17 candidate notes.");
  }
}
