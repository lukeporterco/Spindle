package com.spindle.core.minecraft.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadArcDecisionAnalysis;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadArcDecisionFinding;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadArcDecisionStatus;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadNextDirection;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftRegistryArcHardeningAnalyzerTest {
  private final MinecraftRegistryArcHardeningAnalyzer analyzer =
      new MinecraftRegistryArcHardeningAnalyzer();

  @Test
  void passedTarget21WithSelectablePrimitiveRequirementsRecommendsSteelHook02Design() {
    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(target20(true), validTarget21());

    assertTrue(analysis.gatePassed());
    assertNull(analysis.gateFailureReason());
    assertEquals(
        MinecraftRegistryArcHardeningStatus.REGISTRY_ARC_HARDENED_FOR_STEELHOOK_0_2,
        analysis.hardeningStatus());
    assertEquals(
        MinecraftRegistryArcNextDirection.MOVE_TO_STEELHOOK_0_2_PRIMITIVE_DESIGN,
        analysis.nextDirection());
    assertTrue(analysis.registryArcCompleteForNow());
    assertTrue(analysis.steelHook02PrimitiveDesignRecommended());
    assertFalse(analysis.continueRegistryAnalysisRecommended());
  }

  @Test
  void passedTarget21WithNoSelectableCandidatesContinuesRegistryConceptAnalysis() {
    MinecraftRegistryBootstrapAnalysis base = validTarget21();
    List<MinecraftRegistryCandidate> rejectedOnly =
        base.candidates().stream()
            .map(
                candidate ->
                    new MinecraftRegistryCandidate(
                        candidate.id(),
                        candidate.kind(),
                        candidate.boundaryId(),
                        "com/example/" + simpleName(candidate.ownerInternalName()),
                        candidate.memberName(),
                        candidate.descriptor(),
                        candidate.staticMember(),
                        candidate.accessFlags(),
                        candidate.matchedTokens(),
                        false,
                        "Rejected for Target-21 hardening fixture.",
                        MinecraftRegistryAccessStrategy.NONE,
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
                        "Rejected."))
            .toList();

    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(
            target20(true),
            new RegistryBootstrapAnalysisBuilder(base)
                .candidates(rejectedOnly)
                .selectableCandidateCount(0)
                .rejectedCandidateCount(rejectedOnly.size())
                .classReferenceOnlyCount(0)
                .methodBoundaryAnalysisRequiredCount(0)
                .fieldAccessRequiredCount(0)
                .receiverCaptureRequiredCount(0)
                .futureSteelHookPrimitiveRequiredCount(0)
                .discoveryStatus(MinecraftRegistryDiscoveryStatus.ONLY_REJECTED_CANDIDATES)
                .bindingStatus(MinecraftRegistryBindingStatus.ONLY_REJECTED_SYMBOL_CANDIDATES)
                .build());

    assertTrue(analysis.gatePassed());
    assertEquals(
        MinecraftRegistryArcHardeningStatus.REGISTRY_ARC_HARDENED_MORE_CONCEPT_EVIDENCE_REQUIRED,
        analysis.hardeningStatus());
    assertEquals(
        MinecraftRegistryArcNextDirection.CONTINUE_REGISTRY_CONCEPT_ANALYSIS,
        analysis.nextDirection());
    assertFalse(analysis.registryArcCompleteForNow());
    assertTrue(analysis.continueRegistryAnalysisRecommended());
  }

  @Test
  void upstreamBlockedTarget21ProducesUpstreamBlockedHardening() {
    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(
            target20(true),
            new RegistryBootstrapAnalysisBuilder(validTarget21())
                .gatePassed(false)
                .gateFailureReason("Target-21 gate failure.")
                .discoveryStatus(MinecraftRegistryDiscoveryStatus.UPSTREAM_GATE_BLOCKED)
                .bindingStatus(MinecraftRegistryBindingStatus.UPSTREAM_GATE_BLOCKED)
                .build());

    assertFalse(analysis.gatePassed());
    assertEquals(
        MinecraftRegistryArcHardeningStatus.UPSTREAM_GATE_BLOCKED, analysis.hardeningStatus());
    assertEquals(
        MinecraftRegistryArcNextDirection.UNDECIDED_UPSTREAM_BLOCKED, analysis.nextDirection());
    assertEquals(
        "Restore the Target-20 to Target-21 registry handoff before hardening the registry arc.",
        analysis.nextRecommendedAction());
  }

  @Test
  void runtimeMutationApiOrSandboxFlagBlocksHardening() {
    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(
            target20(true),
            new RegistryBootstrapAnalysisBuilder(validTarget21())
                .classLoadingOccurred(true)
                .build());

    assertInvariantFailure(analysis);
  }

  @Test
  void target21ProofOrCompatibilityFlagBlocksHardening() {
    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(
            target20(true),
            new RegistryBootstrapAnalysisBuilder(validTarget21())
                .registryProofRecommended(true)
                .build());

    assertInvariantFailure(analysis);
  }

  @Test
  void missingExpectedBoundaryBlocksHardening() {
    List<MinecraftAnalyzedRegistryBoundary> boundaries = new ArrayList<>(expectedBoundaries());
    boundaries.removeLast();

    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(
            target20(true),
            new RegistryBootstrapAnalysisBuilder(validTarget21()).boundaries(boundaries).build());

    assertInvariantFailure(analysis);
  }

  @Test
  void boundaryCountMismatchBlocksHardening() {
    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(
            target20(true),
            new RegistryBootstrapAnalysisBuilder(validTarget21()).boundaryCount(999).build());

    assertInvariantFailure(analysis);
  }

  @Test
  void candidateCountMismatchBlocksHardening() {
    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(
            target20(true),
            new RegistryBootstrapAnalysisBuilder(validTarget21()).candidateCount(999).build());

    assertInvariantFailure(analysis);
  }

  @Test
  void candidateIdSequenceMismatchBlocksHardening() {
    List<MinecraftRegistryCandidate> candidates = new ArrayList<>(validTarget21().candidates());
    MinecraftRegistryCandidate first = candidates.getFirst();
    candidates.set(
        0,
        new MinecraftRegistryCandidate(
            "target-21.minecraft.registries.candidate.999",
            first.kind(),
            first.boundaryId(),
            first.ownerInternalName(),
            first.memberName(),
            first.descriptor(),
            first.staticMember(),
            first.accessFlags(),
            first.matchedTokens(),
            first.selectable(),
            first.rejectionReason(),
            first.accessStrategy(),
            first.requiresSymbolNarrowing(),
            first.requiresMethodBoundaryAnalysis(),
            first.requiresReceiverCapture(),
            first.requiresFieldAccess(),
            first.requiresRegistryValueAccess(),
            first.requiresRegistrationTimingDecision(),
            first.requiresRegistrationApplySemanticsDecision(),
            first.requiresFutureSteelHookPrimitive(),
            first.currentSteelHookMethodEntryCompatible(),
            first.registryProofRecommended(),
            first.notes()));

    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(
            target20(true),
            new RegistryBootstrapAnalysisBuilder(validTarget21()).candidates(candidates).build());

    assertInvariantFailure(analysis);
  }

  @Test
  void registerDiscoveryTokenBlocksHardening() {
    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(
            target20(true),
            new RegistryBootstrapAnalysisBuilder(validTarget21())
                .discoveryTokens(List.of("registry", "register"))
                .build());

    assertInvariantFailure(analysis);
  }

  @Test
  void selectableNonMinecraftOwnerBlocksHardening() {
    List<MinecraftRegistryCandidate> candidates = new ArrayList<>(validTarget21().candidates());
    MinecraftRegistryCandidate first = candidates.getFirst();
    candidates.set(
        0,
        new MinecraftRegistryCandidate(
            first.id(),
            first.kind(),
            first.boundaryId(),
            "com/example/BuiltInRegistries",
            first.memberName(),
            first.descriptor(),
            first.staticMember(),
            first.accessFlags(),
            first.matchedTokens(),
            true,
            first.rejectionReason(),
            first.accessStrategy(),
            first.requiresSymbolNarrowing(),
            first.requiresMethodBoundaryAnalysis(),
            first.requiresReceiverCapture(),
            first.requiresFieldAccess(),
            first.requiresRegistryValueAccess(),
            first.requiresRegistrationTimingDecision(),
            first.requiresRegistrationApplySemanticsDecision(),
            first.requiresFutureSteelHookPrimitive(),
            first.currentSteelHookMethodEntryCompatible(),
            first.registryProofRecommended(),
            first.notes()));

    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(
            target20(true),
            new RegistryBootstrapAnalysisBuilder(validTarget21()).candidates(candidates).build());

    assertInvariantFailure(analysis);
  }

  @Test
  void rejectedMinecraftOwnerBlocksHardening() {
    List<MinecraftRegistryCandidate> candidates = new ArrayList<>(validTarget21().candidates());
    MinecraftRegistryCandidate rejected = candidates.get(2);
    candidates.set(
        2,
        new MinecraftRegistryCandidate(
            rejected.id(),
            rejected.kind(),
            rejected.boundaryId(),
            "net/minecraft/core/RejectedRegistry",
            rejected.memberName(),
            rejected.descriptor(),
            rejected.staticMember(),
            rejected.accessFlags(),
            rejected.matchedTokens(),
            false,
            rejected.rejectionReason(),
            rejected.accessStrategy(),
            rejected.requiresSymbolNarrowing(),
            rejected.requiresMethodBoundaryAnalysis(),
            rejected.requiresReceiverCapture(),
            rejected.requiresFieldAccess(),
            rejected.requiresRegistryValueAccess(),
            rejected.requiresRegistrationTimingDecision(),
            rejected.requiresRegistrationApplySemanticsDecision(),
            rejected.requiresFutureSteelHookPrimitive(),
            rejected.currentSteelHookMethodEntryCompatible(),
            rejected.registryProofRecommended(),
            rejected.notes()));

    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(
            target20(true),
            new RegistryBootstrapAnalysisBuilder(validTarget21()).candidates(candidates).build());

    assertInvariantFailure(analysis);
  }

  @Test
  void rejectedCandidateWithoutReasonBlocksHardening() {
    List<MinecraftRegistryCandidate> candidates = new ArrayList<>(validTarget21().candidates());
    MinecraftRegistryCandidate rejected = candidates.get(2);
    candidates.set(
        2,
        new MinecraftRegistryCandidate(
            rejected.id(),
            rejected.kind(),
            rejected.boundaryId(),
            rejected.ownerInternalName(),
            rejected.memberName(),
            rejected.descriptor(),
            rejected.staticMember(),
            rejected.accessFlags(),
            rejected.matchedTokens(),
            false,
            null,
            rejected.accessStrategy(),
            rejected.requiresSymbolNarrowing(),
            rejected.requiresMethodBoundaryAnalysis(),
            rejected.requiresReceiverCapture(),
            rejected.requiresFieldAccess(),
            rejected.requiresRegistryValueAccess(),
            rejected.requiresRegistrationTimingDecision(),
            rejected.requiresRegistrationApplySemanticsDecision(),
            rejected.requiresFutureSteelHookPrimitive(),
            rejected.currentSteelHookMethodEntryCompatible(),
            rejected.registryProofRecommended(),
            rejected.notes()));

    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(
            target20(true),
            new RegistryBootstrapAnalysisBuilder(validTarget21()).candidates(candidates).build());

    assertInvariantFailure(analysis);
  }

  @Test
  void selectableValueAccessWithoutFuturePrimitiveBlocksHardening() {
    List<MinecraftRegistryCandidate> candidates = new ArrayList<>(validTarget21().candidates());
    MinecraftRegistryCandidate second = candidates.get(1);
    candidates.set(
        1,
        new MinecraftRegistryCandidate(
            second.id(),
            second.kind(),
            second.boundaryId(),
            second.ownerInternalName(),
            second.memberName(),
            second.descriptor(),
            second.staticMember(),
            second.accessFlags(),
            second.matchedTokens(),
            second.selectable(),
            second.rejectionReason(),
            second.accessStrategy(),
            second.requiresSymbolNarrowing(),
            second.requiresMethodBoundaryAnalysis(),
            second.requiresReceiverCapture(),
            second.requiresFieldAccess(),
            true,
            second.requiresRegistrationTimingDecision(),
            second.requiresRegistrationApplySemanticsDecision(),
            false,
            second.currentSteelHookMethodEntryCompatible(),
            second.registryProofRecommended(),
            second.notes()));

    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(
            target20(true),
            new RegistryBootstrapAnalysisBuilder(validTarget21())
                .candidates(candidates)
                .futureSteelHookPrimitiveRequiredCount(0)
                .build());

    assertInvariantFailure(analysis);
  }

  @Test
  void allRuntimeMutationApiAndSandboxFlagsRemainFalse() {
    MinecraftRegistryArcHardeningAnalysis analysis =
        analyzer.analyze(target20(true), validTarget21());

    assertTrue(analysis.analysisOnly());
    assertFalse(analysis.classLoadingOccurred());
    assertFalse(analysis.injectionOccurred());
    assertFalse(analysis.transformationOccurred());
    assertFalse(analysis.patchingOccurred());
    assertFalse(analysis.hookInstallationOccurred());
    assertFalse(analysis.runtimeDispatchOccurred());
    assertFalse(analysis.registryBootstrapOccurred());
    assertFalse(analysis.registryMutationOccurred());
    assertFalse(analysis.contentRegistrationOccurred());
    assertFalse(analysis.resourceAccessOccurred());
    assertFalse(analysis.datapackAccessOccurred());
    assertFalse(analysis.dataGenerationOccurred());
    assertFalse(analysis.publicApiExposed());
    assertFalse(analysis.javaModExecutionSandboxed());
  }

  @Test
  void rejectsNonTarget20ArcDecisionInput() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> analyzer.analyze(target20("Target-19", true), validTarget21()));

    assertTrue(exception.getMessage().contains("Target-20"));
  }

  @Test
  void rejectsWrongTarget20ConceptInput() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    new MinecraftResourceReloadArcDecisionAnalysis(
                        1,
                        "Target-20",
                        "minecraft",
                        "26.1.2",
                        MinecraftSide.SERVER,
                        "minecraft.concept.unexpected",
                        "Target-16",
                        "Target-17",
                        "Target-18",
                        "Target-19",
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
                        true,
                        true,
                        "CANDIDATES_DISCOVERED",
                        true,
                        "BINDING_REQUIREMENTS_CLASSIFIED",
                        false,
                        false,
                        true,
                        "SEPARATION_CLASSIFIED",
                        true,
                        true,
                        true,
                        true,
                        null,
                        MinecraftResourceReloadArcDecisionStatus.RESOURCE_RELOAD_ARC_CABOOSED,
                        MinecraftResourceReloadNextDirection.MOVE_TO_REGISTRY_BOOTSTRAP,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        true,
                        "minecraft.concept.registry_bootstrap",
                        "Target-21",
                        "Registry Bootstrap Boundary Analysis",
                        "Next.",
                        List.of()),
                    validTarget21()));

    assertTrue(exception.getMessage().contains("minecraft.concept.data_resources_reload"));
  }

  @Test
  void rejectsNonTarget21RegistryAnalysisInput() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    target20(true),
                    new RegistryBootstrapAnalysisBuilder(validTarget21())
                        .milestoneName("Target-20")
                        .build()));

    assertTrue(exception.getMessage().contains("Target-21"));
  }

  @Test
  void rejectsWrongTarget21ConceptInput() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    target20(true),
                    new RegistryBootstrapAnalysisBuilder(validTarget21())
                        .conceptId("minecraft.concept.unexpected")
                        .build()));

    assertTrue(exception.getMessage().contains("minecraft.concept.registry_bootstrap"));
  }

  private void assertInvariantFailure(MinecraftRegistryArcHardeningAnalysis analysis) {
    assertFalse(analysis.gatePassed());
    assertEquals(MinecraftRegistryArcHardeningStatus.INVARIANTS_FAILED, analysis.hardeningStatus());
    assertEquals(
        MinecraftRegistryArcNextDirection.RESTORE_TARGET21_ANALYSIS, analysis.nextDirection());
    assertEquals(
        "Fix Target-21 registry analysis drift before making the next architecture decision.",
        analysis.nextRecommendedAction());
  }

  private MinecraftRegistryBootstrapAnalysis validTarget21() {
    return new MinecraftRegistryBootstrapAnalysis(
        1,
        "Target-21",
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.registry_bootstrap",
        "Target-1",
        "Target-20",
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
        "MOVE_TO_REGISTRY_BOOTSTRAP",
        true,
        null,
        List.of("registry", "registryaccess", "holderlookup"),
        8,
        1,
        1,
        6,
        0,
        3,
        2,
        0,
        0,
        0,
        1,
        2,
        1,
        1,
        0,
        0,
        1,
        1,
        MinecraftRegistryDiscoveryStatus.CANDIDATES_DISCOVERED,
        MinecraftRegistryBindingStatus.BINDING_REQUIREMENTS_CLASSIFIED,
        false,
        false,
        false,
        "Use this as Target-22 input.",
        expectedBoundaries(),
        expectedCandidates());
  }

  private List<MinecraftAnalyzedRegistryBoundary> expectedBoundaries() {
    List<MinecraftAnalyzedRegistryBoundary> boundaries = new ArrayList<>();
    for (int index = 0; index < MinecraftRegistryBoundary.values().length; index++) {
      MinecraftRegistryBoundary boundary = MinecraftRegistryBoundary.values()[index];
      MinecraftRegistryBoundaryStatus status =
          index == 0
              ? MinecraftRegistryBoundaryStatus.UPSTREAM_HANDOFF_AVAILABLE
              : index == 1
                  ? MinecraftRegistryBoundaryStatus.ANALYZED_FROM_METADATA
                  : MinecraftRegistryBoundaryStatus.DECLARED_UNBOUND;
      MinecraftRegistryRepresentationKind representationKind =
          switch (boundary) {
            case RESOURCE_RELOAD_ARC_HANDOFF ->
                MinecraftRegistryRepresentationKind.UPSTREAM_RESOURCE_RELOAD_ARC_DECISION;
            case REGISTRY_SYMBOL_DISCOVERY ->
                MinecraftRegistryRepresentationKind.TARGET1_INTERPRETED_METADATA;
            case REGISTRY_BOOTSTRAP_WINDOW ->
                MinecraftRegistryRepresentationKind.FUTURE_REGISTRY_BOOTSTRAP_PHASE;
            case ROOT_REGISTRY_ACCESS -> MinecraftRegistryRepresentationKind.FUTURE_REGISTRY_ACCESS;
            case REGISTRY_KEY_MODEL ->
                MinecraftRegistryRepresentationKind.FUTURE_REGISTRY_KEY_MODEL;
            case CONTENT_REGISTRATION_WINDOW ->
                MinecraftRegistryRepresentationKind.FUTURE_CONTENT_REGISTRATION_PHASE;
            case CONTENT_REGISTRATION_APPLY ->
                MinecraftRegistryRepresentationKind.FUTURE_REGISTRY_MUTATION_OPERATION;
            case DYNAMIC_REGISTRY_RELOAD_LINK ->
                MinecraftRegistryRepresentationKind.FUTURE_DYNAMIC_REGISTRY_RELOAD_LINK;
          };
      boundaries.add(
          new MinecraftAnalyzedRegistryBoundary(
              boundary.id(),
              boundary.displayName(),
              index + 1,
              status,
              representationKind,
              "Boundary."));
    }
    return List.copyOf(boundaries);
  }

  private List<MinecraftRegistryCandidate> expectedCandidates() {
    return List.of(
        new MinecraftRegistryCandidate(
            "target-21.minecraft.registries.candidate.001",
            MinecraftRegistryCandidateKind.CLASS_NAME_REFERENCE,
            MinecraftRegistryBoundary.REGISTRY_SYMBOL_DISCOVERY.id(),
            "net/minecraft/core/registries/BuiltInRegistries",
            null,
            null,
            false,
            List.of("PUBLIC"),
            List.of("registry"),
            true,
            null,
            MinecraftRegistryAccessStrategy.CLASS_REFERENCE_ONLY,
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
            "Class reference only."),
        new MinecraftRegistryCandidate(
            "target-21.minecraft.registries.candidate.002",
            MinecraftRegistryCandidateKind.METHOD_DESCRIPTOR_REFERENCE,
            MinecraftRegistryBoundary.REGISTRY_SYMBOL_DISCOVERY.id(),
            "net/minecraft/core/RegistryState",
            "create",
            "(Lnet/minecraft/core/HolderLookup;)V",
            false,
            List.of("PUBLIC"),
            List.of("holderlookup"),
            true,
            null,
            MinecraftRegistryAccessStrategy.INSTANCE_METHOD_RECEIVER_CAPTURE_REQUIRED,
            false,
            false,
            true,
            false,
            true,
            false,
            false,
            true,
            false,
            false,
            "Requires future primitive."),
        new MinecraftRegistryCandidate(
            "target-21.minecraft.registries.candidate.003",
            MinecraftRegistryCandidateKind.CLASS_NAME_REFERENCE,
            MinecraftRegistryBoundary.REGISTRY_SYMBOL_DISCOVERY.id(),
            "com/example/BuiltinRegistries",
            null,
            null,
            false,
            List.of("PUBLIC"),
            List.of("registry"),
            false,
            "Only net/minecraft/* owners are selectable registry bootstrap/content registration candidates in Target-21.",
            MinecraftRegistryAccessStrategy.NONE,
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
            "Rejected."));
  }

  private MinecraftResourceReloadArcDecisionAnalysis target20(boolean gatePassed) {
    return target20("Target-20", gatePassed);
  }

  private MinecraftResourceReloadArcDecisionAnalysis target20(
      String milestoneName, boolean gatePassed) {
    return new MinecraftResourceReloadArcDecisionAnalysis(
        1,
        milestoneName,
        "minecraft",
        "26.1.2",
        MinecraftSide.SERVER,
        "minecraft.concept.data_resources_reload",
        "Target-16",
        "Target-17",
        "Target-18",
        "Target-19",
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
        true,
        true,
        "CANDIDATES_DISCOVERED",
        true,
        "BINDING_REQUIREMENTS_CLASSIFIED",
        false,
        false,
        true,
        "SEPARATION_CLASSIFIED",
        true,
        true,
        true,
        gatePassed,
        gatePassed ? null : "Target-20 gate failure reason.",
        gatePassed
            ? MinecraftResourceReloadArcDecisionStatus.RESOURCE_RELOAD_ARC_CABOOSED
            : MinecraftResourceReloadArcDecisionStatus.UPSTREAM_GATE_BLOCKED,
        gatePassed
            ? MinecraftResourceReloadNextDirection.MOVE_TO_REGISTRY_BOOTSTRAP
            : MinecraftResourceReloadNextDirection.UNDECIDED_UPSTREAM_BLOCKED,
        gatePassed,
        false,
        false,
        false,
        false,
        false,
        gatePassed,
        "minecraft.concept.registry_bootstrap",
        "Target-21",
        "Registry Bootstrap Boundary Analysis",
        gatePassed ? "Next." : "Restore Target-20.",
        List.of(
            new MinecraftResourceReloadArcDecisionFinding(
                "target-20.resource.reload.arc.finding.001",
                "Target-20",
                "Registry bootstrap is next.",
                false,
                false,
                true,
                "Notes.")));
  }

  private String simpleName(String internalName) {
    int slash = internalName.lastIndexOf('/');
    return slash >= 0 ? internalName.substring(slash + 1) : internalName;
  }

  private static final class RegistryBootstrapAnalysisBuilder {
    private int schema;
    private String milestoneName;
    private String target;
    private String minecraftVersion;
    private MinecraftSide side;
    private String conceptId;
    private String sourceArtifactInterpretationMilestone;
    private String sourceResourceReloadArcDecisionMilestone;
    private boolean analysisOnly;
    private boolean classLoadingOccurred;
    private boolean injectionOccurred;
    private boolean transformationOccurred;
    private boolean patchingOccurred;
    private boolean hookInstallationOccurred;
    private boolean runtimeDispatchOccurred;
    private boolean registryBootstrapOccurred;
    private boolean registryMutationOccurred;
    private boolean contentRegistrationOccurred;
    private boolean resourceAccessOccurred;
    private boolean datapackAccessOccurred;
    private boolean dataGenerationOccurred;
    private boolean publicApiExposed;
    private boolean javaModExecutionSandboxed;
    private boolean sourceResourceReloadArcDecisionGatePassed;
    private boolean sourceResourceReloadArcDecisionRegistryBootstrapRecommended;
    private String sourceResourceReloadArcDecisionNextDirection;
    private boolean gatePassed;
    private String gateFailureReason;
    private List<String> discoveryTokens;
    private int boundaryCount;
    private int anchorBoundaryCount;
    private int metadataAnalyzedBoundaryCount;
    private int declaredUnboundBoundaryCount;
    private int blockedBoundaryCount;
    private int candidateCount;
    private int classNameCandidateCount;
    private int fieldNameCandidateCount;
    private int fieldDescriptorCandidateCount;
    private int methodNameCandidateCount;
    private int methodDescriptorCandidateCount;
    private int selectableCandidateCount;
    private int rejectedCandidateCount;
    private int classReferenceOnlyCount;
    private int methodBoundaryAnalysisRequiredCount;
    private int fieldAccessRequiredCount;
    private int receiverCaptureRequiredCount;
    private int futureSteelHookPrimitiveRequiredCount;
    private MinecraftRegistryDiscoveryStatus discoveryStatus;
    private MinecraftRegistryBindingStatus bindingStatus;
    private boolean registryProofRecommended;
    private boolean currentSteelHookMethodEntryCompatible;
    private boolean steelHookPrimitiveDesignRecommended;
    private String nextRecommendedAction;
    private List<MinecraftAnalyzedRegistryBoundary> boundaries;
    private List<MinecraftRegistryCandidate> candidates;

    private RegistryBootstrapAnalysisBuilder(MinecraftRegistryBootstrapAnalysis base) {
      schema = base.schema();
      milestoneName = base.milestoneName();
      target = base.target();
      minecraftVersion = base.minecraftVersion();
      side = base.side();
      conceptId = base.conceptId();
      sourceArtifactInterpretationMilestone = base.sourceArtifactInterpretationMilestone();
      sourceResourceReloadArcDecisionMilestone = base.sourceResourceReloadArcDecisionMilestone();
      analysisOnly = base.analysisOnly();
      classLoadingOccurred = base.classLoadingOccurred();
      injectionOccurred = base.injectionOccurred();
      transformationOccurred = base.transformationOccurred();
      patchingOccurred = base.patchingOccurred();
      hookInstallationOccurred = base.hookInstallationOccurred();
      runtimeDispatchOccurred = base.runtimeDispatchOccurred();
      registryBootstrapOccurred = base.registryBootstrapOccurred();
      registryMutationOccurred = base.registryMutationOccurred();
      contentRegistrationOccurred = base.contentRegistrationOccurred();
      resourceAccessOccurred = base.resourceAccessOccurred();
      datapackAccessOccurred = base.datapackAccessOccurred();
      dataGenerationOccurred = base.dataGenerationOccurred();
      publicApiExposed = base.publicApiExposed();
      javaModExecutionSandboxed = base.javaModExecutionSandboxed();
      sourceResourceReloadArcDecisionGatePassed = base.sourceResourceReloadArcDecisionGatePassed();
      sourceResourceReloadArcDecisionRegistryBootstrapRecommended =
          base.sourceResourceReloadArcDecisionRegistryBootstrapRecommended();
      sourceResourceReloadArcDecisionNextDirection =
          base.sourceResourceReloadArcDecisionNextDirection();
      gatePassed = base.gatePassed();
      gateFailureReason = base.gateFailureReason();
      discoveryTokens = base.discoveryTokens();
      boundaryCount = base.boundaryCount();
      anchorBoundaryCount = base.anchorBoundaryCount();
      metadataAnalyzedBoundaryCount = base.metadataAnalyzedBoundaryCount();
      declaredUnboundBoundaryCount = base.declaredUnboundBoundaryCount();
      blockedBoundaryCount = base.blockedBoundaryCount();
      candidateCount = base.candidateCount();
      classNameCandidateCount = base.classNameCandidateCount();
      fieldNameCandidateCount = base.fieldNameCandidateCount();
      fieldDescriptorCandidateCount = base.fieldDescriptorCandidateCount();
      methodNameCandidateCount = base.methodNameCandidateCount();
      methodDescriptorCandidateCount = base.methodDescriptorCandidateCount();
      selectableCandidateCount = base.selectableCandidateCount();
      rejectedCandidateCount = base.rejectedCandidateCount();
      classReferenceOnlyCount = base.classReferenceOnlyCount();
      methodBoundaryAnalysisRequiredCount = base.methodBoundaryAnalysisRequiredCount();
      fieldAccessRequiredCount = base.fieldAccessRequiredCount();
      receiverCaptureRequiredCount = base.receiverCaptureRequiredCount();
      futureSteelHookPrimitiveRequiredCount = base.futureSteelHookPrimitiveRequiredCount();
      discoveryStatus = base.discoveryStatus();
      bindingStatus = base.bindingStatus();
      registryProofRecommended = base.registryProofRecommended();
      currentSteelHookMethodEntryCompatible = base.currentSteelHookMethodEntryCompatible();
      steelHookPrimitiveDesignRecommended = base.steelHookPrimitiveDesignRecommended();
      nextRecommendedAction = base.nextRecommendedAction();
      boundaries = base.boundaries();
      candidates = base.candidates();
    }

    private RegistryBootstrapAnalysisBuilder milestoneName(String value) {
      milestoneName = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder conceptId(String value) {
      conceptId = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder gatePassed(boolean value) {
      gatePassed = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder gateFailureReason(String value) {
      gateFailureReason = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder classLoadingOccurred(boolean value) {
      classLoadingOccurred = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder registryProofRecommended(boolean value) {
      registryProofRecommended = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder discoveryTokens(List<String> value) {
      discoveryTokens = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder boundaries(
        List<MinecraftAnalyzedRegistryBoundary> value) {
      boundaries = value;
      boundaryCount = value.size();
      anchorBoundaryCount =
          (int)
              value.stream()
                  .filter(
                      boundary ->
                          boundary.status()
                              == MinecraftRegistryBoundaryStatus.UPSTREAM_HANDOFF_AVAILABLE)
                  .count();
      metadataAnalyzedBoundaryCount =
          (int)
              value.stream()
                  .filter(
                      boundary ->
                          boundary.status()
                              == MinecraftRegistryBoundaryStatus.ANALYZED_FROM_METADATA)
                  .count();
      declaredUnboundBoundaryCount =
          (int)
              value.stream()
                  .filter(
                      boundary ->
                          boundary.status() == MinecraftRegistryBoundaryStatus.DECLARED_UNBOUND)
                  .count();
      blockedBoundaryCount =
          (int)
              value.stream()
                  .filter(boundary -> boundary.status() == MinecraftRegistryBoundaryStatus.BLOCKED)
                  .count();
      return this;
    }

    private RegistryBootstrapAnalysisBuilder boundaryCount(int value) {
      boundaryCount = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder candidates(List<MinecraftRegistryCandidate> value) {
      candidates = value;
      candidateCount = value.size();
      classNameCandidateCount =
          (int)
              value.stream()
                  .filter(
                      candidate ->
                          candidate.kind() == MinecraftRegistryCandidateKind.CLASS_NAME_REFERENCE)
                  .count();
      fieldNameCandidateCount =
          (int)
              value.stream()
                  .filter(
                      candidate ->
                          candidate.kind() == MinecraftRegistryCandidateKind.FIELD_NAME_REFERENCE)
                  .count();
      fieldDescriptorCandidateCount =
          (int)
              value.stream()
                  .filter(
                      candidate ->
                          candidate.kind()
                              == MinecraftRegistryCandidateKind.FIELD_DESCRIPTOR_REFERENCE)
                  .count();
      methodNameCandidateCount =
          (int)
              value.stream()
                  .filter(
                      candidate ->
                          candidate.kind() == MinecraftRegistryCandidateKind.METHOD_NAME_REFERENCE)
                  .count();
      methodDescriptorCandidateCount =
          (int)
              value.stream()
                  .filter(
                      candidate ->
                          candidate.kind()
                              == MinecraftRegistryCandidateKind.METHOD_DESCRIPTOR_REFERENCE)
                  .count();
      selectableCandidateCount =
          (int) value.stream().filter(MinecraftRegistryCandidate::selectable).count();
      rejectedCandidateCount = value.size() - selectableCandidateCount;
      classReferenceOnlyCount =
          (int)
              value.stream()
                  .filter(
                      candidate ->
                          candidate.accessStrategy()
                              == MinecraftRegistryAccessStrategy.CLASS_REFERENCE_ONLY)
                  .count();
      methodBoundaryAnalysisRequiredCount =
          (int)
              value.stream()
                  .filter(MinecraftRegistryCandidate::requiresMethodBoundaryAnalysis)
                  .count();
      fieldAccessRequiredCount =
          (int) value.stream().filter(MinecraftRegistryCandidate::requiresFieldAccess).count();
      receiverCaptureRequiredCount =
          (int) value.stream().filter(MinecraftRegistryCandidate::requiresReceiverCapture).count();
      futureSteelHookPrimitiveRequiredCount =
          (int)
              value.stream()
                  .filter(MinecraftRegistryCandidate::requiresFutureSteelHookPrimitive)
                  .count();
      return this;
    }

    private RegistryBootstrapAnalysisBuilder candidateCount(int value) {
      candidateCount = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder selectableCandidateCount(int value) {
      selectableCandidateCount = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder rejectedCandidateCount(int value) {
      rejectedCandidateCount = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder classReferenceOnlyCount(int value) {
      classReferenceOnlyCount = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder methodBoundaryAnalysisRequiredCount(int value) {
      methodBoundaryAnalysisRequiredCount = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder fieldAccessRequiredCount(int value) {
      fieldAccessRequiredCount = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder receiverCaptureRequiredCount(int value) {
      receiverCaptureRequiredCount = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder futureSteelHookPrimitiveRequiredCount(int value) {
      futureSteelHookPrimitiveRequiredCount = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder discoveryStatus(
        MinecraftRegistryDiscoveryStatus value) {
      discoveryStatus = value;
      return this;
    }

    private RegistryBootstrapAnalysisBuilder bindingStatus(MinecraftRegistryBindingStatus value) {
      bindingStatus = value;
      return this;
    }

    private MinecraftRegistryBootstrapAnalysis build() {
      return new MinecraftRegistryBootstrapAnalysis(
          schema,
          milestoneName,
          target,
          minecraftVersion,
          side,
          conceptId,
          sourceArtifactInterpretationMilestone,
          sourceResourceReloadArcDecisionMilestone,
          analysisOnly,
          classLoadingOccurred,
          injectionOccurred,
          transformationOccurred,
          patchingOccurred,
          hookInstallationOccurred,
          runtimeDispatchOccurred,
          registryBootstrapOccurred,
          registryMutationOccurred,
          contentRegistrationOccurred,
          resourceAccessOccurred,
          datapackAccessOccurred,
          dataGenerationOccurred,
          publicApiExposed,
          javaModExecutionSandboxed,
          sourceResourceReloadArcDecisionGatePassed,
          sourceResourceReloadArcDecisionRegistryBootstrapRecommended,
          sourceResourceReloadArcDecisionNextDirection,
          gatePassed,
          gateFailureReason,
          discoveryTokens,
          boundaryCount,
          anchorBoundaryCount,
          metadataAnalyzedBoundaryCount,
          declaredUnboundBoundaryCount,
          blockedBoundaryCount,
          candidateCount,
          classNameCandidateCount,
          fieldNameCandidateCount,
          fieldDescriptorCandidateCount,
          methodNameCandidateCount,
          methodDescriptorCandidateCount,
          selectableCandidateCount,
          rejectedCandidateCount,
          classReferenceOnlyCount,
          methodBoundaryAnalysisRequiredCount,
          fieldAccessRequiredCount,
          receiverCaptureRequiredCount,
          futureSteelHookPrimitiveRequiredCount,
          discoveryStatus,
          bindingStatus,
          registryProofRecommended,
          currentSteelHookMethodEntryCompatible,
          steelHookPrimitiveDesignRecommended,
          nextRecommendedAction,
          boundaries,
          candidates);
    }
  }
}
