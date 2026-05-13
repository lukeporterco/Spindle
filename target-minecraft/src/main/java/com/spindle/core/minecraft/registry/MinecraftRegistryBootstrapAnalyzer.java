package com.spindle.core.minecraft.registry;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.concept.MinecraftTargetConceptCatalog;
import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretation;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedClass;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedField;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedJar;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedMethod;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadArcDecisionAnalysis;
import com.spindle.core.minecraft.resource.MinecraftResourceReloadNextDirection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class MinecraftRegistryBootstrapAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-21";
  private static final String TARGET = "minecraft";
  private static final String CONCEPT_ID = "minecraft.concept.registry_bootstrap";
  private static final String UPSTREAM_CONCEPT_ID = "minecraft.concept.data_resources_reload";
  private static final String SOURCE_ARTIFACT_INTERPRETATION_MILESTONE = "Target-1";
  private static final String SOURCE_RESOURCE_RELOAD_ARC_DECISION_MILESTONE = "Target-20";
  private static final String SELECTABILITY_REJECTION_REASON =
      "Only net/minecraft/* owners are selectable registry bootstrap/content registration candidates in Target-21.";
  private static final String REJECTED_CANDIDATE_NOTES =
      "Rejected source candidate carried forward; non-net/minecraft candidates are not registry bootstrap/content registration targets.";
  private static final String CLASS_REFERENCE_ONLY_NOTES =
      "Class/package discovery is useful registry evidence but does not identify a safe bootstrap window or a writable registry value.";
  private static final String STATIC_METHOD_NOTES =
      "Static method metadata may identify a future registry bootstrap or registration boundary, but Target-21 does not prove timing, mutation safety, or hook compatibility.";
  private static final String INSTANCE_METHOD_NOTES =
      "Instance method metadata may identify registry behavior, but receiver capture, registry value ownership, timing, and mutation semantics remain unresolved.";
  private static final String STATIC_FIELD_NOTES =
      "Static field metadata may identify registry holder state, but Target-21 does not access fields or expose registry values.";
  private static final String INSTANCE_FIELD_NOTES =
      "Instance field metadata may identify registry holder state, but owner capture, field access, registry value ownership, and mutation semantics remain unresolved.";
  private static final String UPSTREAM_GATE_FAILURE_REASON =
      "Target-21 requires the Target-20 registry handoff to pass before registry bootstrap/content registration analysis can be gate-passed.";
  private static final List<String> DISCOVERY_TOKENS =
      List.of(
          "registry",
          "registries",
          "builtinregistries",
          "built_in_registries",
          "builtins",
          "registryaccess",
          "registry_access",
          "mappedregistry",
          "mapped_registry",
          "writableregistry",
          "writable_registry",
          "defaultedregistry",
          "defaulted_registry",
          "resourcekey",
          "resource_key",
          "registrykey",
          "registry_key",
          "holderlookup",
          "holder_lookup",
          "bootstrapcontext",
          "bootstrap_context");

  public MinecraftRegistryBootstrapAnalysis analyze(
      MinecraftTargetConceptCatalog conceptCatalog,
      MinecraftArtifactInterpretation artifactInterpretation,
      MinecraftResourceReloadArcDecisionAnalysis resourceReloadArcDecisionAnalysis) {
    Objects.requireNonNull(conceptCatalog, "conceptCatalog");
    Objects.requireNonNull(artifactInterpretation, "artifactInterpretation");
    Objects.requireNonNull(resourceReloadArcDecisionAnalysis, "resourceReloadArcDecisionAnalysis");

    conceptCatalog
        .findById(CONCEPT_ID)
        .orElseThrow(() -> new IllegalArgumentException("Missing concept `" + CONCEPT_ID + "`."));
    requireExpectedArtifactInterpretation(artifactInterpretation);
    requireExpectedArcDecision(resourceReloadArcDecisionAnalysis);

    boolean gatePassed =
        resourceReloadArcDecisionAnalysis.gatePassed()
            && resourceReloadArcDecisionAnalysis.registryBootstrapRecommended()
            && resourceReloadArcDecisionAnalysis.nextDirection()
                == MinecraftResourceReloadNextDirection.MOVE_TO_REGISTRY_BOOTSTRAP;
    String gateFailureReason = gatePassed ? null : UPSTREAM_GATE_FAILURE_REASON;

    List<MinecraftRegistryCandidate> candidates = collectCandidates(artifactInterpretation);
    int selectableCandidateCount =
        (int) candidates.stream().filter(MinecraftRegistryCandidate::selectable).count();
    int rejectedCandidateCount = candidates.size() - selectableCandidateCount;

    MinecraftRegistryDiscoveryStatus discoveryStatus;
    MinecraftRegistryBindingStatus bindingStatus;
    String nextRecommendedAction;
    if (!gatePassed) {
      discoveryStatus = MinecraftRegistryDiscoveryStatus.UPSTREAM_GATE_BLOCKED;
      bindingStatus = MinecraftRegistryBindingStatus.UPSTREAM_GATE_BLOCKED;
      nextRecommendedAction =
          "Restore the Target-20 registry handoff before using registry bootstrap/content registration analysis.";
    } else if (candidates.isEmpty()) {
      discoveryStatus = MinecraftRegistryDiscoveryStatus.NO_CANDIDATES;
      bindingStatus = MinecraftRegistryBindingStatus.NO_SYMBOL_CANDIDATES;
      nextRecommendedAction =
          "Do not implement registry behavior yet; no registry bootstrap/content registration metadata candidates were discovered.";
    } else if (selectableCandidateCount == 0) {
      discoveryStatus = MinecraftRegistryDiscoveryStatus.ONLY_REJECTED_CANDIDATES;
      bindingStatus = MinecraftRegistryBindingStatus.ONLY_REJECTED_SYMBOL_CANDIDATES;
      nextRecommendedAction =
          "Do not implement registry behavior yet; only rejected non-net/minecraft candidates were discovered.";
    } else {
      discoveryStatus = MinecraftRegistryDiscoveryStatus.CANDIDATES_DISCOVERED;
      bindingStatus = MinecraftRegistryBindingStatus.BINDING_REQUIREMENTS_CLASSIFIED;
      nextRecommendedAction =
          "Do not implement registry mutation yet; use these classified requirements as input to Target-22 hardening and later SteelHook primitive decisions.";
    }

    List<MinecraftAnalyzedRegistryBoundary> boundaries = boundaries(gatePassed);

    return new MinecraftRegistryBootstrapAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        artifactInterpretation.minecraftVersion(),
        sideFromInterpretation(artifactInterpretation.side()),
        CONCEPT_ID,
        SOURCE_ARTIFACT_INTERPRETATION_MILESTONE,
        SOURCE_RESOURCE_RELOAD_ARC_DECISION_MILESTONE,
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
        resourceReloadArcDecisionAnalysis.gatePassed(),
        resourceReloadArcDecisionAnalysis.registryBootstrapRecommended(),
        resourceReloadArcDecisionAnalysis.nextDirection().name(),
        gatePassed,
        gateFailureReason,
        DISCOVERY_TOKENS,
        boundaries.size(),
        countBoundaries(boundaries, MinecraftRegistryBoundaryStatus.UPSTREAM_HANDOFF_AVAILABLE),
        countBoundaries(boundaries, MinecraftRegistryBoundaryStatus.ANALYZED_FROM_METADATA),
        countBoundaries(boundaries, MinecraftRegistryBoundaryStatus.DECLARED_UNBOUND),
        countBoundaries(boundaries, MinecraftRegistryBoundaryStatus.BLOCKED),
        candidates.size(),
        countByKind(candidates, MinecraftRegistryCandidateKind.CLASS_NAME_REFERENCE),
        countByKind(candidates, MinecraftRegistryCandidateKind.FIELD_NAME_REFERENCE),
        countByKind(candidates, MinecraftRegistryCandidateKind.FIELD_DESCRIPTOR_REFERENCE),
        countByKind(candidates, MinecraftRegistryCandidateKind.METHOD_NAME_REFERENCE),
        countByKind(candidates, MinecraftRegistryCandidateKind.METHOD_DESCRIPTOR_REFERENCE),
        selectableCandidateCount,
        rejectedCandidateCount,
        countByAccessStrategy(candidates, MinecraftRegistryAccessStrategy.CLASS_REFERENCE_ONLY),
        countMethodBoundaryAnalysisRequired(candidates),
        countFieldAccessRequired(candidates),
        countReceiverCaptureRequired(candidates),
        countFutureSteelHookPrimitiveRequired(candidates),
        discoveryStatus,
        bindingStatus,
        false,
        false,
        false,
        nextRecommendedAction,
        boundaries,
        candidates);
  }

  private void requireExpectedArtifactInterpretation(
      MinecraftArtifactInterpretation artifactInterpretation) {
    if (!SOURCE_ARTIFACT_INTERPRETATION_MILESTONE.equals(artifactInterpretation.milestoneName())) {
      throw new IllegalArgumentException(
          "Target-21 requires Target-1 artifact interpretation input.");
    }
  }

  private void requireExpectedArcDecision(
      MinecraftResourceReloadArcDecisionAnalysis resourceReloadArcDecisionAnalysis) {
    if (!SOURCE_RESOURCE_RELOAD_ARC_DECISION_MILESTONE.equals(
        resourceReloadArcDecisionAnalysis.milestoneName())) {
      throw new IllegalArgumentException(
          "Target-21 requires Target-20 resource/reload arc decision input.");
    }
    if (!UPSTREAM_CONCEPT_ID.equals(resourceReloadArcDecisionAnalysis.conceptId())) {
      throw new IllegalArgumentException(
          "Target-21 requires concept `" + UPSTREAM_CONCEPT_ID + "` from Target-20.");
    }
  }

  private List<MinecraftRegistryCandidate> collectCandidates(
      MinecraftArtifactInterpretation interpretation) {
    List<CandidateSeed> seeds = new ArrayList<>();
    for (MinecraftInterpretedJar jar : interpretation.jars()) {
      for (MinecraftInterpretedClass interpretedClass : jar.classes()) {
        List<String> classMatchedTokens =
            matchedTokens(interpretedClass.internalName(), interpretedClass.packageName());
        if (!classMatchedTokens.isEmpty()) {
          seeds.add(
              new CandidateSeed(
                  MinecraftRegistryCandidateKind.CLASS_NAME_REFERENCE,
                  interpretedClass.internalName(),
                  null,
                  null,
                  false,
                  interpretedClass.accessFlags(),
                  classMatchedTokens));
        }
        for (MinecraftInterpretedField field : interpretedClass.fields()) {
          List<String> fieldNameTokens = matchedTokens(field.name());
          if (!fieldNameTokens.isEmpty()) {
            seeds.add(
                new CandidateSeed(
                    MinecraftRegistryCandidateKind.FIELD_NAME_REFERENCE,
                    interpretedClass.internalName(),
                    field.name(),
                    field.descriptor(),
                    hasStaticAccessFlag(field.accessFlags()),
                    field.accessFlags(),
                    fieldNameTokens));
          }
          List<String> fieldDescriptorTokens = matchedTokens(field.descriptor());
          if (!fieldDescriptorTokens.isEmpty()) {
            seeds.add(
                new CandidateSeed(
                    MinecraftRegistryCandidateKind.FIELD_DESCRIPTOR_REFERENCE,
                    interpretedClass.internalName(),
                    field.name(),
                    field.descriptor(),
                    hasStaticAccessFlag(field.accessFlags()),
                    field.accessFlags(),
                    fieldDescriptorTokens));
          }
        }
        for (MinecraftInterpretedMethod method : interpretedClass.methods()) {
          List<String> methodNameTokens = matchedTokens(method.name());
          if (!methodNameTokens.isEmpty()) {
            seeds.add(
                new CandidateSeed(
                    MinecraftRegistryCandidateKind.METHOD_NAME_REFERENCE,
                    interpretedClass.internalName(),
                    method.name(),
                    method.descriptor(),
                    method.staticMethod(),
                    method.accessFlags(),
                    methodNameTokens));
          }
          List<String> methodDescriptorTokens = matchedTokens(method.descriptor());
          if (!methodDescriptorTokens.isEmpty()) {
            seeds.add(
                new CandidateSeed(
                    MinecraftRegistryCandidateKind.METHOD_DESCRIPTOR_REFERENCE,
                    interpretedClass.internalName(),
                    method.name(),
                    method.descriptor(),
                    method.staticMethod(),
                    method.accessFlags(),
                    methodDescriptorTokens));
          }
        }
      }
    }

    List<CandidateSeed> sortedSeeds =
        seeds.stream()
            .sorted(
                Comparator.comparing(CandidateSeed::kind)
                    .thenComparing(CandidateSeed::ownerInternalName)
                    .thenComparing(seed -> sortable(seed.memberName()))
                    .thenComparing(seed -> sortable(seed.descriptor()))
                    .thenComparing(seed -> sortable(firstMatchedToken(seed.matchedTokens()))))
            .toList();

    List<MinecraftRegistryCandidate> candidates = new ArrayList<>();
    for (int index = 0; index < sortedSeeds.size(); index++) {
      CandidateSeed seed = sortedSeeds.get(index);
      candidates.add(
          classifyCandidate(
              "target-21.minecraft.registries.candidate.%03d".formatted(index + 1), seed));
    }
    return List.copyOf(candidates);
  }

  private MinecraftRegistryCandidate classifyCandidate(String id, CandidateSeed seed) {
    boolean selectable = seed.ownerInternalName().startsWith("net/minecraft/");
    if (!selectable) {
      return new MinecraftRegistryCandidate(
          id,
          seed.kind(),
          MinecraftRegistryBoundary.REGISTRY_SYMBOL_DISCOVERY.id(),
          seed.ownerInternalName(),
          seed.memberName(),
          seed.descriptor(),
          seed.staticMember(),
          seed.accessFlags(),
          seed.matchedTokens(),
          false,
          SELECTABILITY_REJECTION_REASON,
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
          REJECTED_CANDIDATE_NOTES);
    }

    return switch (seed.kind()) {
      case CLASS_NAME_REFERENCE ->
          candidate(
              id,
              seed,
              MinecraftRegistryAccessStrategy.CLASS_REFERENCE_ONLY,
              true,
              false,
              false,
              false,
              false,
              true,
              false,
              false,
              CLASS_REFERENCE_ONLY_NOTES);
      case METHOD_NAME_REFERENCE, METHOD_DESCRIPTOR_REFERENCE ->
          seed.staticMember()
              ? candidate(
                  id,
                  seed,
                  MinecraftRegistryAccessStrategy.STATIC_METHOD_BOUNDARY_ANALYSIS_REQUIRED,
                  true,
                  true,
                  false,
                  false,
                  false,
                  true,
                  true,
                  true,
                  STATIC_METHOD_NOTES)
              : candidate(
                  id,
                  seed,
                  MinecraftRegistryAccessStrategy.INSTANCE_METHOD_RECEIVER_CAPTURE_REQUIRED,
                  true,
                  true,
                  true,
                  false,
                  true,
                  true,
                  true,
                  true,
                  INSTANCE_METHOD_NOTES);
      case FIELD_NAME_REFERENCE, FIELD_DESCRIPTOR_REFERENCE ->
          seed.staticMember()
              ? candidate(
                  id,
                  seed,
                  MinecraftRegistryAccessStrategy.STATIC_FIELD_ACCESS_REQUIRED,
                  true,
                  false,
                  false,
                  true,
                  true,
                  true,
                  true,
                  true,
                  STATIC_FIELD_NOTES)
              : candidate(
                  id,
                  seed,
                  MinecraftRegistryAccessStrategy.INSTANCE_FIELD_OWNER_CAPTURE_REQUIRED,
                  true,
                  false,
                  true,
                  true,
                  true,
                  true,
                  true,
                  true,
                  INSTANCE_FIELD_NOTES);
    };
  }

  private MinecraftRegistryCandidate candidate(
      String id,
      CandidateSeed seed,
      MinecraftRegistryAccessStrategy accessStrategy,
      boolean requiresSymbolNarrowing,
      boolean requiresMethodBoundaryAnalysis,
      boolean requiresReceiverCapture,
      boolean requiresFieldAccess,
      boolean requiresRegistryValueAccess,
      boolean requiresRegistrationTimingDecision,
      boolean requiresRegistrationApplySemanticsDecision,
      boolean requiresFutureSteelHookPrimitive,
      String notes) {
    return new MinecraftRegistryCandidate(
        id,
        seed.kind(),
        MinecraftRegistryBoundary.REGISTRY_SYMBOL_DISCOVERY.id(),
        seed.ownerInternalName(),
        seed.memberName(),
        seed.descriptor(),
        seed.staticMember(),
        seed.accessFlags(),
        seed.matchedTokens(),
        true,
        null,
        accessStrategy,
        requiresSymbolNarrowing,
        requiresMethodBoundaryAnalysis,
        requiresReceiverCapture,
        requiresFieldAccess,
        requiresRegistryValueAccess,
        requiresRegistrationTimingDecision,
        requiresRegistrationApplySemanticsDecision,
        requiresFutureSteelHookPrimitive,
        false,
        false,
        notes);
  }

  private List<MinecraftAnalyzedRegistryBoundary> boundaries(boolean gatePassed) {
    return List.of(
        boundary(
            MinecraftRegistryBoundary.RESOURCE_RELOAD_ARC_HANDOFF,
            gatePassed
                ? MinecraftRegistryBoundaryStatus.UPSTREAM_HANDOFF_AVAILABLE
                : MinecraftRegistryBoundaryStatus.BLOCKED,
            MinecraftRegistryRepresentationKind.UPSTREAM_RESOURCE_RELOAD_ARC_DECISION,
            gatePassed
                ? "The Target-20 resource/reload arc explicitly handed off to registry bootstrap/content registration analysis."
                : "The Target-20 registry handoff did not pass, so Target-21 remains upstream-blocked."),
        boundary(
            MinecraftRegistryBoundary.REGISTRY_SYMBOL_DISCOVERY,
            MinecraftRegistryBoundaryStatus.ANALYZED_FROM_METADATA,
            MinecraftRegistryRepresentationKind.TARGET1_INTERPRETED_METADATA,
            "Target-21 scans only Target-1 interpreted metadata for registry-like evidence."),
        boundary(
            MinecraftRegistryBoundary.REGISTRY_BOOTSTRAP_WINDOW,
            MinecraftRegistryBoundaryStatus.DECLARED_UNBOUND,
            MinecraftRegistryRepresentationKind.FUTURE_REGISTRY_BOOTSTRAP_PHASE,
            "Target-21 does not prove a safe registry bootstrap window."),
        boundary(
            MinecraftRegistryBoundary.ROOT_REGISTRY_ACCESS,
            MinecraftRegistryBoundaryStatus.DECLARED_UNBOUND,
            MinecraftRegistryRepresentationKind.FUTURE_REGISTRY_ACCESS,
            "Target-21 does not access a live root registry value."),
        boundary(
            MinecraftRegistryBoundary.REGISTRY_KEY_MODEL,
            MinecraftRegistryBoundaryStatus.DECLARED_UNBOUND,
            MinecraftRegistryRepresentationKind.FUTURE_REGISTRY_KEY_MODEL,
            "Target-21 does not bind a stable registry key model."),
        boundary(
            MinecraftRegistryBoundary.CONTENT_REGISTRATION_WINDOW,
            MinecraftRegistryBoundaryStatus.DECLARED_UNBOUND,
            MinecraftRegistryRepresentationKind.FUTURE_CONTENT_REGISTRATION_PHASE,
            "Target-21 does not prove content registration timing."),
        boundary(
            MinecraftRegistryBoundary.CONTENT_REGISTRATION_APPLY,
            MinecraftRegistryBoundaryStatus.DECLARED_UNBOUND,
            MinecraftRegistryRepresentationKind.FUTURE_REGISTRY_MUTATION_OPERATION,
            "Target-21 does not classify live registry mutation semantics as implementation-ready."),
        boundary(
            MinecraftRegistryBoundary.DYNAMIC_REGISTRY_RELOAD_LINK,
            MinecraftRegistryBoundaryStatus.DECLARED_UNBOUND,
            MinecraftRegistryRepresentationKind.FUTURE_DYNAMIC_REGISTRY_RELOAD_LINK,
            "Target-21 does not implement or bind dynamic registry/reload behavior."));
  }

  private MinecraftAnalyzedRegistryBoundary boundary(
      MinecraftRegistryBoundary boundary,
      MinecraftRegistryBoundaryStatus status,
      MinecraftRegistryRepresentationKind representationKind,
      String notes) {
    return new MinecraftAnalyzedRegistryBoundary(
        boundary.id(),
        boundary.displayName(),
        boundary.ordinal() + 1,
        status,
        representationKind,
        notes);
  }

  private int countBoundaries(
      List<MinecraftAnalyzedRegistryBoundary> boundaries, MinecraftRegistryBoundaryStatus status) {
    return (int) boundaries.stream().filter(boundary -> boundary.status() == status).count();
  }

  private int countByKind(
      List<MinecraftRegistryCandidate> candidates, MinecraftRegistryCandidateKind kind) {
    return (int) candidates.stream().filter(candidate -> candidate.kind() == kind).count();
  }

  private int countByAccessStrategy(
      List<MinecraftRegistryCandidate> candidates, MinecraftRegistryAccessStrategy accessStrategy) {
    return (int)
        candidates.stream()
            .filter(candidate -> candidate.accessStrategy() == accessStrategy)
            .count();
  }

  private int countMethodBoundaryAnalysisRequired(List<MinecraftRegistryCandidate> candidates) {
    return (int)
        candidates.stream()
            .filter(MinecraftRegistryCandidate::requiresMethodBoundaryAnalysis)
            .count();
  }

  private int countFieldAccessRequired(List<MinecraftRegistryCandidate> candidates) {
    return (int)
        candidates.stream().filter(MinecraftRegistryCandidate::requiresFieldAccess).count();
  }

  private int countReceiverCaptureRequired(List<MinecraftRegistryCandidate> candidates) {
    return (int)
        candidates.stream().filter(MinecraftRegistryCandidate::requiresReceiverCapture).count();
  }

  private int countFutureSteelHookPrimitiveRequired(List<MinecraftRegistryCandidate> candidates) {
    return (int)
        candidates.stream()
            .filter(MinecraftRegistryCandidate::requiresFutureSteelHookPrimitive)
            .count();
  }

  private MinecraftSide sideFromInterpretation(String side) {
    return "client".equalsIgnoreCase(side) ? MinecraftSide.CLIENT : MinecraftSide.SERVER;
  }

  private List<String> matchedTokens(String... values) {
    List<String> matchedTokens = new ArrayList<>();
    for (String token : DISCOVERY_TOKENS) {
      for (String value : values) {
        if (matchesToken(value, token)) {
          matchedTokens.add(token);
          break;
        }
      }
    }
    return List.copyOf(matchedTokens);
  }

  private boolean matchesToken(String value, String token) {
    if (value == null || value.isBlank()) {
      return false;
    }
    String lowerValue = value.toLowerCase(Locale.ROOT);
    String lowerToken = token.toLowerCase(Locale.ROOT);
    if (lowerValue.contains(lowerToken)) {
      return true;
    }
    return compact(lowerValue).contains(compact(lowerToken));
  }

  private String compact(String value) {
    StringBuilder compacted = new StringBuilder(value.length());
    for (int index = 0; index < value.length(); index++) {
      char character = value.charAt(index);
      if (character == '/'
          || character == '_'
          || character == '$'
          || character == '.'
          || character == ';'
          || character == '('
          || character == ')'
          || character == '['
          || character == 'l') {
        continue;
      }
      compacted.append(character);
    }
    return compacted.toString();
  }

  private boolean hasStaticAccessFlag(List<String> accessFlags) {
    return accessFlags.stream().anyMatch(flag -> "static".equals(flag.toLowerCase(Locale.ROOT)));
  }

  private String firstMatchedToken(List<String> matchedTokens) {
    return matchedTokens.isEmpty() ? null : matchedTokens.getFirst();
  }

  private String sortable(String value) {
    return value == null ? "" : value;
  }

  private record CandidateSeed(
      MinecraftRegistryCandidateKind kind,
      String ownerInternalName,
      String memberName,
      String descriptor,
      boolean staticMember,
      List<String> accessFlags,
      List<String> matchedTokens) {
    private CandidateSeed {
      accessFlags = List.copyOf(accessFlags == null ? List.of() : accessFlags);
      matchedTokens = List.copyOf(matchedTokens == null ? List.of() : matchedTokens);
    }
  }
}
