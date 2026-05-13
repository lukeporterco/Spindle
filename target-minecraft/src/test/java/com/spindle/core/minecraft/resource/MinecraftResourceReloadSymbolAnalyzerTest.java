package com.spindle.core.minecraft.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretation;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedClass;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedField;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedJar;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedMethod;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftResourceReloadSymbolAnalyzerTest {
  private final MinecraftResourceReloadSymbolAnalyzer analyzer =
      new MinecraftResourceReloadSymbolAnalyzer();

  @Test
  void gatePassedWithNoMatchingSymbolsProducesNoCandidates() {
    MinecraftResourceReloadSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/server/Main",
                    List.of(new MinecraftInterpretedField("value", "I", 1, List.of("PUBLIC"))),
                    List.of(
                        new MinecraftInterpretedMethod(
                            "run", "()V", 1, List.of("PUBLIC"), false, false)))),
            resourceReloadAnalysis(true, true, true));

    assertTrue(analysis.gatePassed());
    assertEquals(
        MinecraftResourceReloadSymbolDiscoveryStatus.NO_CANDIDATES, analysis.discoveryStatus());
    assertEquals(0, analysis.candidateCount());
    assertFalse(analysis.bindingStrategyAnalysisEligible());
  }

  @Test
  void gatePassedWithNetMinecraftClassMatchProducesCandidatesDiscovered() {
    MinecraftResourceReloadSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass("net/minecraft/server/ServerResources", List.of(), List.of())),
            resourceReloadAnalysis(true, true, true));

    assertEquals(
        MinecraftResourceReloadSymbolDiscoveryStatus.CANDIDATES_DISCOVERED,
        analysis.discoveryStatus());
    assertTrue(analysis.bindingStrategyAnalysisEligible());
    assertEquals(1, analysis.classNameCandidateCount());
    MinecraftResourceReloadSymbolCandidate candidate = analysis.candidates().getFirst();
    assertEquals(MinecraftResourceReloadSymbolCandidateKind.CLASS_NAME_REFERENCE, candidate.kind());
    assertTrue(candidate.selectable());
    assertNull(candidate.memberName());
    assertNull(candidate.descriptor());
    assertFalse(candidate.staticMember());
  }

  @Test
  void gatePassedWithNetMinecraftFieldNameMatchProducesFieldNameReference() {
    MinecraftResourceReloadSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/server/ReloadState",
                    List.of(
                        new MinecraftInterpretedField(
                            "reloadInstance",
                            "Ljava/lang/Object;",
                            8,
                            List.of("public", "static"))),
                    List.of())),
            resourceReloadAnalysis(true, true, true));

    MinecraftResourceReloadSymbolCandidate candidate =
        candidate(
            analysis,
            MinecraftResourceReloadSymbolCandidateKind.FIELD_NAME_REFERENCE,
            "reloadInstance");
    assertEquals(MinecraftResourceReloadSymbolCandidateKind.FIELD_NAME_REFERENCE, candidate.kind());
    assertTrue(candidate.staticMember());
    assertEquals(List.of("reload", "reloadinstance"), candidate.matchedTokens());
  }

  @Test
  void gatePassedWithNetMinecraftFieldDescriptorMatchProducesFieldDescriptorReference() {
    MinecraftResourceReloadSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/server/ReloadState",
                    List.of(
                        new MinecraftInterpretedField(
                            "listener",
                            "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;",
                            1,
                            List.of("PUBLIC"))),
                    List.of())),
            resourceReloadAnalysis(true, true, true));

    MinecraftResourceReloadSymbolCandidate candidate =
        candidate(
            analysis,
            MinecraftResourceReloadSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE,
            "listener");
    assertEquals(
        MinecraftResourceReloadSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE, candidate.kind());
    assertEquals(
        List.of(
            "reload",
            "resource",
            "resources",
            "resourcemanager",
            "resource_manager",
            "reloadableresourcemanager",
            "server/packs",
            "server/packs/resources"),
        candidate.matchedTokens());
  }

  @Test
  void gatePassedWithNetMinecraftMethodNameMatchProducesMethodNameReference() {
    MinecraftResourceReloadSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/server/ReloadState",
                    List.of(),
                    List.of(
                        new MinecraftInterpretedMethod(
                            "reloadResources", "()V", 1, List.of("PUBLIC"), false, false)))),
            resourceReloadAnalysis(true, true, true));

    MinecraftResourceReloadSymbolCandidate candidate =
        candidate(
            analysis,
            MinecraftResourceReloadSymbolCandidateKind.METHOD_NAME_REFERENCE,
            "reloadResources");
    assertEquals(
        MinecraftResourceReloadSymbolCandidateKind.METHOD_NAME_REFERENCE, candidate.kind());
    assertFalse(candidate.staticMember());
  }

  @Test
  void gatePassedWithNetMinecraftMethodDescriptorMatchProducesMethodDescriptorReference() {
    MinecraftResourceReloadSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/server/ReloadState",
                    List.of(),
                    List.of(
                        new MinecraftInterpretedMethod(
                            "create",
                            "(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V",
                            9,
                            List.of("PUBLIC", "STATIC"),
                            false,
                            true)))),
            resourceReloadAnalysis(true, true, true));

    MinecraftResourceReloadSymbolCandidate candidate =
        candidate(
            analysis,
            MinecraftResourceReloadSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE,
            "create");
    assertEquals(
        MinecraftResourceReloadSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE, candidate.kind());
    assertTrue(candidate.staticMember());
    assertTrue(candidate.matchedTokens().contains("preparablereloadlistener"));
  }

  @Test
  void nonNetMinecraftMatchingOwnerProducesOnlyRejectedCandidatesWhenOnlyMatch() {
    MinecraftResourceReloadSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(interpretedClass("com/example/ServerResources", List.of(), List.of())),
            resourceReloadAnalysis(true, true, true));

    assertEquals(
        MinecraftResourceReloadSymbolDiscoveryStatus.ONLY_REJECTED_CANDIDATES,
        analysis.discoveryStatus());
    assertEquals(0, analysis.selectableCandidateCount());
    assertEquals(1, analysis.rejectedCandidateCount());
    assertFalse(analysis.bindingStrategyAnalysisEligible());
    assertEquals(
        "Only net/minecraft/* owners are selectable resource/reload symbol candidates in Target-17.",
        analysis.candidates().getFirst().rejectionReason());
  }

  @Test
  void gateBlockedProducesUpstreamGateBlockedEvenIfCandidatesAreDiscovered() {
    MinecraftResourceReloadSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass("net/minecraft/server/ServerResources", List.of(), List.of())),
            resourceReloadAnalysis(false, false, true));

    assertFalse(analysis.gatePassed());
    assertEquals(
        MinecraftResourceReloadSymbolDiscoveryStatus.UPSTREAM_GATE_BLOCKED,
        analysis.discoveryStatus());
    assertFalse(analysis.bindingStrategyAnalysisEligible());
    assertEquals(
        "Target-17 requires Target-16 resource/reload analysis with an available lifecycle anchor and declared reload discovery boundary.",
        analysis.gateFailureReason());
  }

  @Test
  void compactTokenMatchingDetectsNamedVariants() {
    MinecraftResourceReloadSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/server/PackResources",
                    List.of(
                        new MinecraftInterpretedField(
                            "listener",
                            "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;",
                            1,
                            List.of("PUBLIC")),
                        new MinecraftInterpretedField(
                            "data_pack_holder", "Ljava/lang/String;", 1, List.of("PUBLIC"))),
                    List.of(
                        new MinecraftInterpretedMethod(
                            "bind",
                            "(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V",
                            1,
                            List.of("PUBLIC"),
                            false,
                            false)))),
            resourceReloadAnalysis(true, true, true));

    List<String> tokenUniverse =
        analysis.candidates().stream()
            .flatMap(candidate -> candidate.matchedTokens().stream())
            .distinct()
            .toList();
    assertTrue(tokenUniverse.contains("packresources"));
    assertTrue(tokenUniverse.contains("reloadableresourcemanager"));
    assertTrue(tokenUniverse.contains("preparablereloadlistener"));
    assertTrue(tokenUniverse.contains("data_pack"));
  }

  @Test
  void candidateOrderIdsAndMatchedTokenOrderAreDeterministic() {
    MinecraftResourceReloadSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass(
                    "net/minecraft/server/ServerResources",
                    List.of(
                        new MinecraftInterpretedField(
                            "resourceManager",
                            "Ljava/lang/String;",
                            8,
                            List.of("PUBLIC", "STATIC")),
                        new MinecraftInterpretedField(
                            "holder",
                            "Lnet/minecraft/server/packs/resources/PackResources;",
                            1,
                            List.of("PUBLIC"))),
                    List.of(
                        new MinecraftInterpretedMethod(
                            "reload", "()V", 1, List.of("PUBLIC"), false, false),
                        new MinecraftInterpretedMethod(
                            "create",
                            "(Lnet/minecraft/server/packs/resources/ReloadInstance;)V",
                            9,
                            List.of("PUBLIC", "STATIC"),
                            false,
                            true)))),
            resourceReloadAnalysis(true, true, true));

    assertEquals(
        java.util.stream.IntStream.rangeClosed(1, analysis.candidates().size())
            .mapToObj(
                index -> "target-17.minecraft.resources.reload.candidate.%03d".formatted(index))
            .toList(),
        analysis.candidates().stream().map(MinecraftResourceReloadSymbolCandidate::id).toList());
    assertEquals(
        List.of(
            MinecraftResourceReloadSymbolCandidateKind.CLASS_NAME_REFERENCE,
            MinecraftResourceReloadSymbolCandidateKind.FIELD_NAME_REFERENCE,
            MinecraftResourceReloadSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE,
            MinecraftResourceReloadSymbolCandidateKind.METHOD_NAME_REFERENCE,
            MinecraftResourceReloadSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE),
        analysis.candidates().stream()
            .map(MinecraftResourceReloadSymbolCandidate::kind)
            .distinct()
            .toList());
    assertEquals(
        List.of("resource", "resources", "serverresources"),
        candidate(analysis, MinecraftResourceReloadSymbolCandidateKind.CLASS_NAME_REFERENCE, null)
            .matchedTokens());
  }

  @Test
  void allMutationRuntimeApiAndSandboxFlagsRemainFalse() {
    MinecraftResourceReloadSymbolAnalysis analysis =
        analyzer.analyze(
            interpretation(
                interpretedClass("net/minecraft/server/ServerResources", List.of(), List.of())),
            resourceReloadAnalysis(true, true, true));

    assertEquals(1, analysis.schema());
    assertEquals("Target-17", analysis.milestoneName());
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

  private MinecraftResourceReloadAnalysis resourceReloadAnalysis(
      boolean sourceGatePassed, boolean lifecycleAnchorAvailable, boolean reloadDiscoveryDeclared) {
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
        sourceGatePassed,
        lifecycleAnchorAvailable,
        sourceGatePassed && lifecycleAnchorAvailable,
        sourceGatePassed && lifecycleAnchorAvailable
            ? null
            : "Target-16 requires the Target-12 starting lifecycle dispatch anchor before resource/reload boundaries can be anchored.",
        lifecycleAnchorAvailable ? 1 : 0,
        reloadDiscoveryDeclared ? 1 : 0,
        lifecycleAnchorAvailable ? 0 : 1,
        boundaries(lifecycleAnchorAvailable, reloadDiscoveryDeclared));
  }

  private List<MinecraftAnalyzedResourceReloadBoundary> boundaries(
      boolean lifecycleAnchorAvailable, boolean reloadDiscoveryDeclared) {
    java.util.ArrayList<MinecraftAnalyzedResourceReloadBoundary> boundaries =
        new java.util.ArrayList<>();
    boundaries.add(
        new MinecraftAnalyzedResourceReloadBoundary(
            "minecraft.resources.lifecycle_anchor",
            "Lifecycle Anchor",
            1,
            lifecycleAnchorAvailable
                ? MinecraftResourceReloadBoundaryStatus.AVAILABLE
                : MinecraftResourceReloadBoundaryStatus.UPSTREAM_GATE_BLOCKED,
            MinecraftResourceReloadRepresentationKind.SERVER_LIFECYCLE_ANCHOR,
            lifecycleAnchorAvailable,
            lifecycleAnchorAvailable ? "minecraft.server.lifecycle.starting" : null,
            lifecycleAnchorAvailable
                ? "target-12.minecraft.server.lifecycle.starting.dispatch"
                : null,
            false,
            false,
            false,
            false,
            lifecycleAnchorAvailable ? "Available." : "Blocked."));
    if (reloadDiscoveryDeclared) {
      boundaries.add(
          new MinecraftAnalyzedResourceReloadBoundary(
              "minecraft.resources.reload.discovery",
              "Reload Discovery",
              2,
              MinecraftResourceReloadBoundaryStatus.DECLARED_UNBOUND,
              MinecraftResourceReloadRepresentationKind.RESOURCE_RELOAD_SYMBOL_BOUNDARY,
              false,
              null,
              null,
              true,
              false,
              false,
              false,
              "Declared."));
    }
    return List.copyOf(boundaries);
  }

  private MinecraftResourceReloadSymbolCandidate candidate(
      MinecraftResourceReloadSymbolAnalysis analysis,
      MinecraftResourceReloadSymbolCandidateKind kind,
      String memberName) {
    return analysis.candidates().stream()
        .filter(candidate -> candidate.kind() == kind)
        .filter(candidate -> memberName == null || memberName.equals(candidate.memberName()))
        .findFirst()
        .orElseThrow();
  }
}
