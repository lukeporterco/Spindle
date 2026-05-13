package com.spindle.core.minecraft.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.concept.MinecraftTargetConceptCatalog;
import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretation;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedClass;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedField;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedJar;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedMethod;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadArcDecisionAnalysis;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadArcDecisionFinding;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadArcDecisionStatus;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadNextDirection;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftRegistryBootstrapAnalyzerTest {
  private final MinecraftRegistryBootstrapAnalyzer analyzer =
      new MinecraftRegistryBootstrapAnalyzer();
  private final MinecraftTargetConceptCatalog conceptCatalog = new MinecraftTargetConceptCatalog();

  @Test
  void passedTarget20WithNoMatchingSymbolsProducesNoCandidates() {
    MinecraftRegistryBootstrapAnalysis analysis =
        analyzer.analyze(
            conceptCatalog,
            interpretation(
                interpretedClass(
                    "net/minecraft/server/Main",
                    List.of(new MinecraftInterpretedField("value", "I", 1, List.of("PUBLIC"))),
                    List.of(
                        new MinecraftInterpretedMethod(
                            "run", "()V", 1, List.of("PUBLIC"), false, false)))),
            target20(true));

    assertTrue(analysis.gatePassed());
    assertEquals(MinecraftRegistryDiscoveryStatus.NO_CANDIDATES, analysis.discoveryStatus());
    assertEquals(MinecraftRegistryBindingStatus.NO_SYMBOL_CANDIDATES, analysis.bindingStatus());
    assertEquals(0, analysis.candidateCount());
  }

  @Test
  void passedTarget20WithNetMinecraftClassMatchProducesSelectableClassCandidate() {
    MinecraftRegistryBootstrapAnalysis analysis =
        analyzer.analyze(
            conceptCatalog,
            interpretation(
                interpretedClass(
                    "net/minecraft/core/registries/BuiltInRegistries", List.of(), List.of())),
            target20(true));

    MinecraftRegistryCandidate candidate = analysis.candidates().getFirst();
    assertEquals(MinecraftRegistryCandidateKind.CLASS_NAME_REFERENCE, candidate.kind());
    assertTrue(candidate.selectable());
    assertNull(candidate.memberName());
    assertNull(candidate.descriptor());
    assertEquals(MinecraftRegistryAccessStrategy.CLASS_REFERENCE_ONLY, candidate.accessStrategy());
    assertEquals(
        "Class/package discovery is useful registry evidence but does not identify a safe bootstrap window or a writable registry value.",
        candidate.notes());
  }

  @Test
  void passedTarget20WithFieldNameMatchProducesFieldCandidate() {
    MinecraftRegistryBootstrapAnalysis analysis =
        analyzer.analyze(
            conceptCatalog,
            interpretation(
                interpretedClass(
                    "net/minecraft/core/RegistryState",
                    List.of(
                        new MinecraftInterpretedField(
                            "rootRegistry", "Ljava/lang/Object;", 8, List.of("PUBLIC", "STATIC"))),
                    List.of())),
            target20(true));

    MinecraftRegistryCandidate candidate =
        candidate(analysis, MinecraftRegistryCandidateKind.FIELD_NAME_REFERENCE, "rootRegistry");
    assertEquals(
        MinecraftRegistryAccessStrategy.STATIC_FIELD_ACCESS_REQUIRED, candidate.accessStrategy());
    assertTrue(candidate.requiresFieldAccess());
    assertTrue(candidate.requiresRegistryValueAccess());
  }

  @Test
  void passedTarget20WithFieldDescriptorMatchProducesDescriptorCandidate() {
    MinecraftRegistryBootstrapAnalysis analysis =
        analyzer.analyze(
            conceptCatalog,
            interpretation(
                interpretedClass(
                    "net/minecraft/core/RegistryState",
                    List.of(
                        new MinecraftInterpretedField(
                            "holder", "Lnet/minecraft/core/RegistryAccess;", 1, List.of("PUBLIC"))),
                    List.of())),
            target20(true));

    MinecraftRegistryCandidate candidate =
        candidate(analysis, MinecraftRegistryCandidateKind.FIELD_DESCRIPTOR_REFERENCE, "holder");
    assertEquals(
        MinecraftRegistryAccessStrategy.INSTANCE_FIELD_OWNER_CAPTURE_REQUIRED,
        candidate.accessStrategy());
    assertTrue(candidate.requiresReceiverCapture());
    assertTrue(candidate.requiresFieldAccess());
  }

  @Test
  void passedTarget20WithMethodNameMatchProducesMethodCandidate() {
    MinecraftRegistryBootstrapAnalysis analysis =
        analyzer.analyze(
            conceptCatalog,
            interpretation(
                interpretedClass(
                    "net/minecraft/core/RegistryState",
                    List.of(),
                    List.of(
                        new MinecraftInterpretedMethod(
                            "bootstrapContext",
                            "()V",
                            9,
                            List.of("PUBLIC", "STATIC"),
                            false,
                            true)))),
            target20(true));

    MinecraftRegistryCandidate candidate =
        candidate(
            analysis, MinecraftRegistryCandidateKind.METHOD_NAME_REFERENCE, "bootstrapContext");
    assertEquals(
        MinecraftRegistryAccessStrategy.STATIC_METHOD_BOUNDARY_ANALYSIS_REQUIRED,
        candidate.accessStrategy());
    assertTrue(candidate.requiresMethodBoundaryAnalysis());
  }

  @Test
  void passedTarget20WithMethodDescriptorMatchProducesDescriptorCandidate() {
    MinecraftRegistryBootstrapAnalysis analysis =
        analyzer.analyze(
            conceptCatalog,
            interpretation(
                interpretedClass(
                    "net/minecraft/core/RegistryState",
                    List.of(),
                    List.of(
                        new MinecraftInterpretedMethod(
                            "create",
                            "(Lnet/minecraft/core/HolderLookup;)V",
                            1,
                            List.of("PUBLIC"),
                            false,
                            false)))),
            target20(true));

    MinecraftRegistryCandidate candidate =
        candidate(analysis, MinecraftRegistryCandidateKind.METHOD_DESCRIPTOR_REFERENCE, "create");
    assertEquals(
        MinecraftRegistryAccessStrategy.INSTANCE_METHOD_RECEIVER_CAPTURE_REQUIRED,
        candidate.accessStrategy());
    assertTrue(candidate.requiresReceiverCapture());
    assertTrue(candidate.requiresRegistryValueAccess());
  }

  @Test
  void nonNetMinecraftMatchingOwnerProducesOnlyRejectedCandidates() {
    MinecraftRegistryBootstrapAnalysis analysis =
        analyzer.analyze(
            conceptCatalog,
            interpretation(interpretedClass("com/example/BuiltinRegistries", List.of(), List.of())),
            target20(true));

    assertEquals(
        MinecraftRegistryDiscoveryStatus.ONLY_REJECTED_CANDIDATES, analysis.discoveryStatus());
    assertEquals(
        MinecraftRegistryBindingStatus.ONLY_REJECTED_SYMBOL_CANDIDATES, analysis.bindingStatus());
    assertEquals(0, analysis.selectableCandidateCount());
    assertEquals(1, analysis.rejectedCandidateCount());
    assertEquals(
        "Only net/minecraft/* owners are selectable registry bootstrap/content registration candidates in Target-21.",
        analysis.candidates().getFirst().rejectionReason());
  }

  @Test
  void target20GateBlockedProducesUpstreamGateBlocked() {
    MinecraftRegistryBootstrapAnalysis analysis =
        analyzer.analyze(
            conceptCatalog,
            interpretation(
                interpretedClass(
                    "net/minecraft/core/registries/BuiltInRegistries", List.of(), List.of())),
            target20(false));

    assertFalse(analysis.gatePassed());
    assertEquals(
        MinecraftRegistryDiscoveryStatus.UPSTREAM_GATE_BLOCKED, analysis.discoveryStatus());
    assertEquals(MinecraftRegistryBindingStatus.UPSTREAM_GATE_BLOCKED, analysis.bindingStatus());
    assertEquals(
        "Restore the Target-20 registry handoff before using registry bootstrap/content registration analysis.",
        analysis.nextRecommendedAction());
  }

  @Test
  void candidateKindsMapToRequiredAccessStrategiesInDeterministicOrder() {
    MinecraftRegistryBootstrapAnalysis analysis =
        analyzer.analyze(conceptCatalog, deterministicInterpretation(), target20(true));

    assertEquals(
        List.of(
            "target-21.minecraft.registries.candidate.001",
            "target-21.minecraft.registries.candidate.002",
            "target-21.minecraft.registries.candidate.003",
            "target-21.minecraft.registries.candidate.004",
            "target-21.minecraft.registries.candidate.005",
            "target-21.minecraft.registries.candidate.006",
            "target-21.minecraft.registries.candidate.007",
            "target-21.minecraft.registries.candidate.008",
            "target-21.minecraft.registries.candidate.009"),
        analysis.candidates().stream().map(MinecraftRegistryCandidate::id).toList());
    assertEquals(
        List.of(
            MinecraftRegistryAccessStrategy.NONE,
            MinecraftRegistryAccessStrategy.CLASS_REFERENCE_ONLY,
            MinecraftRegistryAccessStrategy.INSTANCE_FIELD_OWNER_CAPTURE_REQUIRED,
            MinecraftRegistryAccessStrategy.STATIC_FIELD_ACCESS_REQUIRED,
            MinecraftRegistryAccessStrategy.STATIC_FIELD_ACCESS_REQUIRED,
            MinecraftRegistryAccessStrategy.STATIC_METHOD_BOUNDARY_ANALYSIS_REQUIRED,
            MinecraftRegistryAccessStrategy.INSTANCE_METHOD_RECEIVER_CAPTURE_REQUIRED,
            MinecraftRegistryAccessStrategy.INSTANCE_METHOD_RECEIVER_CAPTURE_REQUIRED,
            MinecraftRegistryAccessStrategy.STATIC_METHOD_BOUNDARY_ANALYSIS_REQUIRED),
        analysis.candidates().stream().map(MinecraftRegistryCandidate::accessStrategy).toList());
  }

  @Test
  void aggregateCountsAndFlagsAreDeterministic() {
    MinecraftRegistryBootstrapAnalysis analysis =
        analyzer.analyze(conceptCatalog, deterministicInterpretation(), target20(true));

    assertEquals(8, analysis.boundaryCount());
    assertEquals(1, analysis.anchorBoundaryCount());
    assertEquals(1, analysis.metadataAnalyzedBoundaryCount());
    assertEquals(6, analysis.declaredUnboundBoundaryCount());
    assertEquals(0, analysis.blockedBoundaryCount());
    assertEquals(9, analysis.candidateCount());
    assertEquals(2, analysis.classNameCandidateCount());
    assertEquals(2, analysis.fieldNameCandidateCount());
    assertEquals(1, analysis.fieldDescriptorCandidateCount());
    assertEquals(2, analysis.methodNameCandidateCount());
    assertEquals(2, analysis.methodDescriptorCandidateCount());
    assertEquals(8, analysis.selectableCandidateCount());
    assertEquals(1, analysis.rejectedCandidateCount());
    assertEquals(1, analysis.classReferenceOnlyCount());
    assertEquals(4, analysis.methodBoundaryAnalysisRequiredCount());
    assertEquals(3, analysis.fieldAccessRequiredCount());
    assertEquals(3, analysis.receiverCaptureRequiredCount());
    assertEquals(7, analysis.futureSteelHookPrimitiveRequiredCount());
    assertFalse(analysis.registryProofRecommended());
    assertFalse(analysis.currentSteelHookMethodEntryCompatible());
    assertFalse(analysis.steelHookPrimitiveDesignRecommended());
  }

  @Test
  void allRuntimeMutationApiAndSandboxFlagsRemainFalse() {
    MinecraftRegistryBootstrapAnalysis analysis =
        analyzer.analyze(conceptCatalog, deterministicInterpretation(), target20(true));

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
  void analyzerRejectsNonTarget20ArcDecisionInput() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    conceptCatalog,
                    deterministicInterpretation(),
                    new MinecraftResourceReloadArcDecisionAnalysis(
                        1,
                        "Target-19",
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
                        List.of())));

    assertTrue(exception.getMessage().contains("Target-20"));
  }

  @Test
  void analyzerRejectsWrongTarget20ConceptInput() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    conceptCatalog,
                    deterministicInterpretation(),
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
                        List.of())));

    assertTrue(exception.getMessage().contains("minecraft.concept.data_resources_reload"));
  }

  @Test
  void analyzerRejectsNonTarget1ArtifactInterpretationInput() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                analyzer.analyze(
                    conceptCatalog,
                    new MinecraftArtifactInterpretation(
                        1,
                        "Target-2",
                        "minecraft",
                        "26.1.2",
                        "server",
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        "DRY_RUN",
                        List.of(),
                        0,
                        0,
                        0,
                        0,
                        0,
                        List.of(),
                        List.of()),
                    target20(true)));

    assertTrue(exception.getMessage().contains("Target-1"));
  }

  private MinecraftArtifactInterpretation deterministicInterpretation() {
    return interpretation(
        interpretedClass("com/example/BuiltinRegistries", List.of(), List.of()),
        interpretedClass(
            "net/minecraft/core/registries/BuiltInRegistries",
            List.of(
                new MinecraftInterpretedField(
                    "rootRegistry", "Ljava/lang/Object;", 8, List.of("PUBLIC", "STATIC")),
                new MinecraftInterpretedField(
                    "registryAccess", "Ljava/lang/Object;", 1, List.of("PUBLIC")),
                new MinecraftInterpretedField(
                    "holder",
                    "Lnet/minecraft/core/RegistryAccess;",
                    8,
                    List.of("PUBLIC", "STATIC"))),
            List.of(
                new MinecraftInterpretedMethod(
                    "bootstrapContext", "()V", 9, List.of("PUBLIC", "STATIC"), false, true),
                new MinecraftInterpretedMethod(
                    "registryAccess", "()V", 1, List.of("PUBLIC"), false, false),
                new MinecraftInterpretedMethod(
                    "create",
                    "(Lnet/minecraft/core/HolderLookup;)V",
                    9,
                    List.of("PUBLIC", "STATIC"),
                    false,
                    true),
                new MinecraftInterpretedMethod(
                    "bind",
                    "(Lnet/minecraft/core/HolderLookup;)V",
                    1,
                    List.of("PUBLIC"),
                    false,
                    false))));
  }

  private MinecraftResourceReloadArcDecisionAnalysis target20(boolean gatePassed) {
    return new MinecraftResourceReloadArcDecisionAnalysis(
        1,
        "Target-20",
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

  private MinecraftArtifactInterpretation interpretation(MinecraftInterpretedClass... classes) {
    int fieldCount = 0;
    int methodCount = 0;
    for (MinecraftInterpretedClass interpretedClass : classes) {
      fieldCount += interpretedClass.fields().size();
      methodCount +=
          interpretedClass.methods().stream().filter(method -> !method.constructor()).count();
    }
    return new MinecraftArtifactInterpretation(
        1,
        "Target-1",
        "minecraft",
        "26.1.2",
        "server",
        true,
        false,
        false,
        false,
        false,
        false,
        "DRY_RUN",
        List.of(
            new MinecraftInterpretedJar(
                "server.jar",
                "MINECRAFT",
                "fixture",
                "sha",
                classes.length,
                fieldCount,
                methodCount,
                0,
                List.of(),
                List.of(classes))),
        0,
        classes.length,
        fieldCount,
        methodCount,
        0,
        List.of(),
        List.of());
  }

  private MinecraftInterpretedClass interpretedClass(
      String internalName,
      List<MinecraftInterpretedField> fields,
      List<MinecraftInterpretedMethod> methods) {
    return new MinecraftInterpretedClass(
        internalName.replace('/', '.'),
        internalName,
        internalName.contains("/") ? internalName.substring(0, internalName.lastIndexOf('/')) : "",
        "java/lang/Object",
        List.of(),
        1,
        List.of("PUBLIC"),
        fields,
        methods);
  }

  private MinecraftRegistryCandidate candidate(
      MinecraftRegistryBootstrapAnalysis analysis,
      MinecraftRegistryCandidateKind kind,
      String memberName) {
    return analysis.candidates().stream()
        .filter(candidate -> candidate.kind() == kind)
        .filter(candidate -> memberName == null || memberName.equals(candidate.memberName()))
        .findFirst()
        .orElseThrow();
  }
}
