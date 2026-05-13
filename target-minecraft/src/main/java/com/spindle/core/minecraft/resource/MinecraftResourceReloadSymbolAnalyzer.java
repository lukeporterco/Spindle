package com.spindle.core.minecraft.resource;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretation;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedClass;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedField;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedJar;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedMethod;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class MinecraftResourceReloadSymbolAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-17";
  private static final String TARGET = "minecraft";
  private static final String CONCEPT_ID = "minecraft.concept.data_resources_reload";
  private static final String SOURCE_RESOURCE_RELOAD_ANALYSIS_MILESTONE = "Target-16";
  private static final String RESOURCE_BOUNDARY_ID = "minecraft.resources.reload.discovery";
  private static final String LIFECYCLE_ANCHOR_BOUNDARY_ID = "minecraft.resources.lifecycle_anchor";
  private static final String GATE_FAILURE_REASON =
      "Target-17 requires Target-16 resource/reload analysis with an available lifecycle anchor and declared reload discovery boundary.";
  private static final String SELECTABILITY_REJECTION_REASON =
      "Only net/minecraft/* owners are selectable resource/reload symbol candidates in Target-17.";
  private static final List<String> DISCOVERY_TOKENS =
      List.of(
          "reload",
          "resource",
          "resources",
          "datapack",
          "data_pack",
          "packresources",
          "resourcemanager",
          "resource_manager",
          "preparablereloadlistener",
          "reloadableresourcemanager",
          "serverresources",
          "reloadinstance",
          "server/packs",
          "server/packs/resources");

  public MinecraftResourceReloadSymbolAnalysis analyze(
      MinecraftArtifactInterpretation artifactInterpretation,
      MinecraftResourceReloadAnalysis resourceReloadAnalysis) {
    Objects.requireNonNull(artifactInterpretation, "artifactInterpretation");
    Objects.requireNonNull(resourceReloadAnalysis, "resourceReloadAnalysis");
    requireExpectedAnalysis(resourceReloadAnalysis);

    boolean sourceResourceReloadGatePassed = resourceReloadAnalysis.gatePassed();
    boolean resourceLifecycleAnchorAvailable =
        resourceReloadAnalysis.boundaries().stream()
            .anyMatch(
                boundary ->
                    LIFECYCLE_ANCHOR_BOUNDARY_ID.equals(boundary.boundaryId())
                        && boundary.status() == MinecraftResourceReloadBoundaryStatus.AVAILABLE);
    boolean reloadDiscoveryBoundaryDeclared =
        resourceReloadAnalysis.boundaries().stream()
            .anyMatch(boundary -> RESOURCE_BOUNDARY_ID.equals(boundary.boundaryId()));
    boolean gatePassed =
        sourceResourceReloadGatePassed
            && resourceLifecycleAnchorAvailable
            && reloadDiscoveryBoundaryDeclared;
    String gateFailureReason = gatePassed ? null : GATE_FAILURE_REASON;

    List<MinecraftResourceReloadSymbolCandidate> candidates =
        collectCandidates(artifactInterpretation);
    int classNameCandidateCount =
        countByKind(candidates, MinecraftResourceReloadSymbolCandidateKind.CLASS_NAME_REFERENCE);
    int fieldNameCandidateCount =
        countByKind(candidates, MinecraftResourceReloadSymbolCandidateKind.FIELD_NAME_REFERENCE);
    int fieldDescriptorCandidateCount =
        countByKind(
            candidates, MinecraftResourceReloadSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE);
    int methodNameCandidateCount =
        countByKind(candidates, MinecraftResourceReloadSymbolCandidateKind.METHOD_NAME_REFERENCE);
    int methodDescriptorCandidateCount =
        countByKind(
            candidates, MinecraftResourceReloadSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE);
    int selectableCandidateCount =
        (int)
            candidates.stream().filter(MinecraftResourceReloadSymbolCandidate::selectable).count();
    int rejectedCandidateCount = candidates.size() - selectableCandidateCount;

    MinecraftResourceReloadSymbolDiscoveryStatus discoveryStatus;
    boolean bindingStrategyAnalysisEligible;
    if (!gatePassed) {
      discoveryStatus = MinecraftResourceReloadSymbolDiscoveryStatus.UPSTREAM_GATE_BLOCKED;
      bindingStrategyAnalysisEligible = false;
    } else if (selectableCandidateCount > 0) {
      discoveryStatus = MinecraftResourceReloadSymbolDiscoveryStatus.CANDIDATES_DISCOVERED;
      bindingStrategyAnalysisEligible = true;
    } else if (candidates.isEmpty()) {
      discoveryStatus = MinecraftResourceReloadSymbolDiscoveryStatus.NO_CANDIDATES;
      bindingStrategyAnalysisEligible = false;
    } else {
      discoveryStatus = MinecraftResourceReloadSymbolDiscoveryStatus.ONLY_REJECTED_CANDIDATES;
      bindingStrategyAnalysisEligible = false;
    }

    return new MinecraftResourceReloadSymbolAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        artifactInterpretation.minecraftVersion(),
        sideFromInterpretation(artifactInterpretation.side()),
        CONCEPT_ID,
        SOURCE_RESOURCE_RELOAD_ANALYSIS_MILESTONE,
        RESOURCE_BOUNDARY_ID,
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
        sourceResourceReloadGatePassed,
        resourceLifecycleAnchorAvailable,
        reloadDiscoveryBoundaryDeclared,
        gatePassed,
        gateFailureReason,
        DISCOVERY_TOKENS,
        candidates.size(),
        classNameCandidateCount,
        fieldNameCandidateCount,
        fieldDescriptorCandidateCount,
        methodNameCandidateCount,
        methodDescriptorCandidateCount,
        selectableCandidateCount,
        rejectedCandidateCount,
        discoveryStatus,
        bindingStrategyAnalysisEligible,
        candidates);
  }

  private void requireExpectedAnalysis(MinecraftResourceReloadAnalysis analysis) {
    if (!CONCEPT_ID.equals(analysis.conceptId())) {
      throw new IllegalArgumentException(
          "Target-17 requires concept `" + CONCEPT_ID + "` from Target-16.");
    }
    if (!SOURCE_RESOURCE_RELOAD_ANALYSIS_MILESTONE.equals(analysis.milestoneName())) {
      throw new IllegalArgumentException(
          "Target-17 requires Target-16 resource/reload analysis input.");
    }
  }

  private List<MinecraftResourceReloadSymbolCandidate> collectCandidates(
      MinecraftArtifactInterpretation interpretation) {
    List<CandidateSeed> seeds = new ArrayList<>();
    for (MinecraftInterpretedJar jar : interpretation.jars()) {
      for (MinecraftInterpretedClass interpretedClass : jar.classes()) {
        List<String> classMatchedTokens =
            matchedTokens(interpretedClass.internalName(), interpretedClass.packageName());
        if (!classMatchedTokens.isEmpty()) {
          seeds.add(
              new CandidateSeed(
                  MinecraftResourceReloadSymbolCandidateKind.CLASS_NAME_REFERENCE,
                  interpretedClass.internalName(),
                  null,
                  null,
                  false,
                  interpretedClass.accessFlags(),
                  classMatchedTokens,
                  notesFor(MinecraftResourceReloadSymbolCandidateKind.CLASS_NAME_REFERENCE)));
        }
        for (MinecraftInterpretedField field : interpretedClass.fields()) {
          List<String> fieldNameTokens = matchedTokens(field.name());
          if (!fieldNameTokens.isEmpty()) {
            seeds.add(
                new CandidateSeed(
                    MinecraftResourceReloadSymbolCandidateKind.FIELD_NAME_REFERENCE,
                    interpretedClass.internalName(),
                    field.name(),
                    field.descriptor(),
                    hasStaticAccessFlag(field.accessFlags()),
                    field.accessFlags(),
                    fieldNameTokens,
                    notesFor(MinecraftResourceReloadSymbolCandidateKind.FIELD_NAME_REFERENCE)));
          }
          List<String> fieldDescriptorTokens = matchedTokens(field.descriptor());
          if (!fieldDescriptorTokens.isEmpty()) {
            seeds.add(
                new CandidateSeed(
                    MinecraftResourceReloadSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE,
                    interpretedClass.internalName(),
                    field.name(),
                    field.descriptor(),
                    hasStaticAccessFlag(field.accessFlags()),
                    field.accessFlags(),
                    fieldDescriptorTokens,
                    notesFor(
                        MinecraftResourceReloadSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE)));
          }
        }
        for (MinecraftInterpretedMethod method : interpretedClass.methods()) {
          List<String> methodNameTokens = matchedTokens(method.name());
          if (!methodNameTokens.isEmpty()) {
            seeds.add(
                new CandidateSeed(
                    MinecraftResourceReloadSymbolCandidateKind.METHOD_NAME_REFERENCE,
                    interpretedClass.internalName(),
                    method.name(),
                    method.descriptor(),
                    method.staticMethod(),
                    method.accessFlags(),
                    methodNameTokens,
                    notesFor(MinecraftResourceReloadSymbolCandidateKind.METHOD_NAME_REFERENCE)));
          }
          List<String> methodDescriptorTokens = matchedTokens(method.descriptor());
          if (!methodDescriptorTokens.isEmpty()) {
            seeds.add(
                new CandidateSeed(
                    MinecraftResourceReloadSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE,
                    interpretedClass.internalName(),
                    method.name(),
                    method.descriptor(),
                    method.staticMethod(),
                    method.accessFlags(),
                    methodDescriptorTokens,
                    notesFor(
                        MinecraftResourceReloadSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE)));
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

    List<MinecraftResourceReloadSymbolCandidate> candidates = new ArrayList<>();
    for (int index = 0; index < sortedSeeds.size(); index++) {
      CandidateSeed seed = sortedSeeds.get(index);
      boolean selectable = seed.ownerInternalName().startsWith("net/minecraft/");
      candidates.add(
          new MinecraftResourceReloadSymbolCandidate(
              "target-17.minecraft.resources.reload.candidate.%03d".formatted(index + 1),
              seed.kind(),
              RESOURCE_BOUNDARY_ID,
              seed.ownerInternalName(),
              seed.memberName(),
              seed.descriptor(),
              seed.staticMember(),
              seed.accessFlags(),
              seed.matchedTokens(),
              selectable,
              selectable ? null : SELECTABILITY_REJECTION_REASON,
              seed.notes()));
    }
    return List.copyOf(candidates);
  }

  private int countByKind(
      List<MinecraftResourceReloadSymbolCandidate> candidates,
      MinecraftResourceReloadSymbolCandidateKind kind) {
    return (int) candidates.stream().filter(candidate -> candidate.kind() == kind).count();
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

  private String notesFor(MinecraftResourceReloadSymbolCandidateKind kind) {
    return switch (kind) {
      case CLASS_NAME_REFERENCE ->
          "Class or package name matched resource/reload discovery tokens.";
      case FIELD_NAME_REFERENCE -> "Field name matched resource/reload discovery tokens.";
      case FIELD_DESCRIPTOR_REFERENCE ->
          "Field descriptor references a resource/reload-like type name.";
      case METHOD_NAME_REFERENCE -> "Method name matched resource/reload discovery tokens.";
      case METHOD_DESCRIPTOR_REFERENCE ->
          "Method descriptor references a resource/reload-like type name.";
    };
  }

  private record CandidateSeed(
      MinecraftResourceReloadSymbolCandidateKind kind,
      String ownerInternalName,
      String memberName,
      String descriptor,
      boolean staticMember,
      List<String> accessFlags,
      List<String> matchedTokens,
      String notes) {
    private CandidateSeed {
      accessFlags = List.copyOf(accessFlags == null ? List.of() : accessFlags);
      matchedTokens = List.copyOf(matchedTokens == null ? List.of() : matchedTokens);
    }
  }
}
