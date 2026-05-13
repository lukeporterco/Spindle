package com.spindle.core.minecraft.command;

import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretation;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedClass;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedField;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedJar;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedMethod;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class MinecraftCommandDispatcherSymbolAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-14";
  private static final String TARGET = "minecraft";
  private static final String CONCEPT_ID = "minecraft.concept.command_registration";
  private static final String SOURCE_COMMAND_REGISTRATION_ANALYSIS_MILESTONE = "Target-13";
  private static final String COMMAND_BOUNDARY_ID = "minecraft.commands.dispatcher.discovery";
  private static final String COMMAND_DISPATCHER_DESCRIPTOR =
      "Lcom/mojang/brigadier/CommandDispatcher;";
  private static final String LIFECYCLE_ANCHOR_BOUNDARY_ID = "minecraft.commands.lifecycle_anchor";
  private static final String GATE_FAILURE_REASON =
      "Target-14 requires an available Target-13 command lifecycle anchor.";
  private static final String BRIGADIER_REJECTION_REASON =
      "Brigadier library classes are not Minecraft command registration targets.";
  private static final String SPINDLE_REJECTION_REASON =
      "Spindle classes are not Minecraft command registration targets.";
  private static final String BRIGADIER_LIBRARY_NOTES =
      "Brigadier library class presence is metadata only.";

  public MinecraftCommandDispatcherSymbolAnalysis analyze(
      MinecraftArtifactInterpretation artifactInterpretation,
      MinecraftCommandRegistrationAnalysis commandRegistrationAnalysis) {
    Objects.requireNonNull(artifactInterpretation, "artifactInterpretation");
    Objects.requireNonNull(commandRegistrationAnalysis, "commandRegistrationAnalysis");
    requireExpectedAnalysis(commandRegistrationAnalysis);

    boolean sourceCommandRegistrationGatePassed = commandRegistrationAnalysis.gatePassed();
    boolean lifecycleAnchorAvailable =
        commandRegistrationAnalysis.boundaries().stream()
            .anyMatch(
                boundary ->
                    LIFECYCLE_ANCHOR_BOUNDARY_ID.equals(boundary.boundaryId())
                        && boundary.status()
                            == MinecraftCommandRegistrationBoundaryStatus.ANCHOR_AVAILABLE);
    boolean gatePassed = sourceCommandRegistrationGatePassed && lifecycleAnchorAvailable;
    String gateFailureReason = gatePassed ? null : GATE_FAILURE_REASON;

    List<MinecraftCommandDispatcherSymbolCandidate> candidates =
        collectCandidates(artifactInterpretation, gatePassed);
    int methodCandidateCount =
        (int)
            candidates.stream()
                .filter(
                    candidate ->
                        candidate.kind()
                            == MinecraftCommandDispatcherSymbolCandidateKind
                                .METHOD_DESCRIPTOR_REFERENCE)
                .count();
    int fieldCandidateCount =
        (int)
            candidates.stream()
                .filter(
                    candidate ->
                        candidate.kind()
                            == MinecraftCommandDispatcherSymbolCandidateKind
                                .FIELD_DESCRIPTOR_REFERENCE)
                .count();
    int libraryClassCandidateCount =
        (int)
            candidates.stream()
                .filter(
                    candidate ->
                        candidate.kind()
                            == MinecraftCommandDispatcherSymbolCandidateKind
                                .BRIGADIER_LIBRARY_CLASS)
                .count();
    int selectableCandidateCount =
        (int)
            candidates.stream()
                .filter(MinecraftCommandDispatcherSymbolCandidate::selectable)
                .count();

    MinecraftCommandDispatcherSymbolSelectionStatus selectionStatus;
    boolean minimalCommandRegistrationProofEligible;
    int selectedCandidateCount;
    String selectedCandidateId;

    if (!gatePassed) {
      selectionStatus = MinecraftCommandDispatcherSymbolSelectionStatus.UPSTREAM_GATE_BLOCKED;
      minimalCommandRegistrationProofEligible = false;
      selectedCandidateCount = 0;
      selectedCandidateId = null;
    } else if (selectableCandidateCount == 0) {
      selectionStatus = MinecraftCommandDispatcherSymbolSelectionStatus.NO_CANDIDATES;
      minimalCommandRegistrationProofEligible = false;
      selectedCandidateCount = 0;
      selectedCandidateId = null;
    } else if (selectableCandidateCount == 1) {
      selectionStatus = MinecraftCommandDispatcherSymbolSelectionStatus.STABLE_TARGET_SELECTED;
      minimalCommandRegistrationProofEligible = true;
      selectedCandidateCount = 1;
      selectedCandidateId =
          candidates.stream()
              .filter(MinecraftCommandDispatcherSymbolCandidate::selectable)
              .findFirst()
              .map(MinecraftCommandDispatcherSymbolCandidate::id)
              .orElseThrow();
    } else {
      selectionStatus = MinecraftCommandDispatcherSymbolSelectionStatus.AMBIGUOUS_CANDIDATES;
      minimalCommandRegistrationProofEligible = false;
      selectedCandidateCount = 0;
      selectedCandidateId = null;
    }

    if (selectedCandidateId != null) {
      candidates =
          candidates.stream()
              .map(
                  candidate ->
                      selectedCandidateId.equals(candidate.id())
                          ? new MinecraftCommandDispatcherSymbolCandidate(
                              candidate.id(),
                              candidate.kind(),
                              candidate.ownerInternalName(),
                              candidate.memberName(),
                              candidate.descriptor(),
                              candidate.staticMember(),
                              candidate.accessFlags(),
                              candidate.selectable(),
                              true,
                              candidate.rejectionReason(),
                              candidate.notes())
                          : candidate)
              .toList();
    }

    return new MinecraftCommandDispatcherSymbolAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        artifactInterpretation.minecraftVersion(),
        commandRegistrationAnalysis.side(),
        CONCEPT_ID,
        SOURCE_COMMAND_REGISTRATION_ANALYSIS_MILESTONE,
        COMMAND_BOUNDARY_ID,
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
        sourceCommandRegistrationGatePassed,
        lifecycleAnchorAvailable,
        gatePassed,
        gateFailureReason,
        COMMAND_DISPATCHER_DESCRIPTOR,
        candidates.size(),
        methodCandidateCount,
        fieldCandidateCount,
        libraryClassCandidateCount,
        selectableCandidateCount,
        selectedCandidateCount,
        selectionStatus,
        minimalCommandRegistrationProofEligible,
        selectedCandidateId,
        candidates);
  }

  private void requireExpectedAnalysis(MinecraftCommandRegistrationAnalysis analysis) {
    if (!CONCEPT_ID.equals(analysis.conceptId())) {
      throw new IllegalArgumentException(
          "Target-14 requires concept `" + CONCEPT_ID + "` from Target-13.");
    }
    if (!SOURCE_COMMAND_REGISTRATION_ANALYSIS_MILESTONE.equals(analysis.milestoneName())) {
      throw new IllegalArgumentException("Target-14 requires Target-13 command analysis input.");
    }
  }

  private List<MinecraftCommandDispatcherSymbolCandidate> collectCandidates(
      MinecraftArtifactInterpretation interpretation, boolean gatePassed) {
    List<CandidateSeed> seeds = new ArrayList<>();
    for (MinecraftInterpretedJar jar : interpretation.jars()) {
      for (MinecraftInterpretedClass interpretedClass : jar.classes()) {
        if ("com/mojang/brigadier/CommandDispatcher".equals(interpretedClass.internalName())) {
          seeds.add(
              new CandidateSeed(
                  MinecraftCommandDispatcherSymbolCandidateKind.BRIGADIER_LIBRARY_CLASS,
                  interpretedClass.internalName(),
                  null,
                  null,
                  false,
                  List.of(),
                  false,
                  null,
                  BRIGADIER_LIBRARY_NOTES));
        }
        for (MinecraftInterpretedField field : interpretedClass.fields()) {
          if (field.descriptor().contains(COMMAND_DISPATCHER_DESCRIPTOR)) {
            seeds.add(fieldSeed(interpretedClass, field));
          }
        }
        for (MinecraftInterpretedMethod method : interpretedClass.methods()) {
          if (method.descriptor().contains(COMMAND_DISPATCHER_DESCRIPTOR)) {
            seeds.add(methodSeed(interpretedClass, method));
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
                    .thenComparing(seed -> sortable(seed.descriptor())))
            .toList();

    List<MinecraftCommandDispatcherSymbolCandidate> candidates = new ArrayList<>();
    for (int index = 0; index < sortedSeeds.size(); index++) {
      CandidateSeed seed = sortedSeeds.get(index);
      boolean selected = gatePassed && seed.selectable() && sortedSelectableCount(sortedSeeds) == 1;
      candidates.add(
          new MinecraftCommandDispatcherSymbolCandidate(
              "target-14.minecraft.commands.dispatcher.candidate.%03d".formatted(index + 1),
              seed.kind(),
              seed.ownerInternalName(),
              seed.memberName(),
              seed.descriptor(),
              seed.staticMember(),
              seed.accessFlags(),
              seed.selectable(),
              selected,
              seed.rejectionReason(),
              seed.notes()));
    }
    return List.copyOf(candidates);
  }

  private int sortedSelectableCount(List<CandidateSeed> seeds) {
    return (int) seeds.stream().filter(CandidateSeed::selectable).count();
  }

  private CandidateSeed fieldSeed(
      MinecraftInterpretedClass interpretedClass, MinecraftInterpretedField field) {
    boolean brigadierOwned = interpretedClass.internalName().startsWith("com/mojang/brigadier/");
    boolean spindleOwned = interpretedClass.internalName().startsWith("com/spindle/");
    return new CandidateSeed(
        MinecraftCommandDispatcherSymbolCandidateKind.FIELD_DESCRIPTOR_REFERENCE,
        interpretedClass.internalName(),
        field.name(),
        field.descriptor(),
        field.accessFlags().contains("STATIC"),
        field.accessFlags(),
        !brigadierOwned && !spindleOwned,
        brigadierOwned
            ? BRIGADIER_REJECTION_REASON
            : spindleOwned ? SPINDLE_REJECTION_REASON : null,
        null);
  }

  private CandidateSeed methodSeed(
      MinecraftInterpretedClass interpretedClass, MinecraftInterpretedMethod method) {
    boolean brigadierOwned = interpretedClass.internalName().startsWith("com/mojang/brigadier/");
    boolean spindleOwned = interpretedClass.internalName().startsWith("com/spindle/");
    return new CandidateSeed(
        MinecraftCommandDispatcherSymbolCandidateKind.METHOD_DESCRIPTOR_REFERENCE,
        interpretedClass.internalName(),
        method.name(),
        method.descriptor(),
        method.staticMethod(),
        method.accessFlags(),
        !brigadierOwned && !spindleOwned,
        brigadierOwned
            ? BRIGADIER_REJECTION_REASON
            : spindleOwned ? SPINDLE_REJECTION_REASON : null,
        null);
  }

  private String sortable(String value) {
    return value == null ? "" : value;
  }

  private record CandidateSeed(
      MinecraftCommandDispatcherSymbolCandidateKind kind,
      String ownerInternalName,
      String memberName,
      String descriptor,
      boolean staticMember,
      List<String> accessFlags,
      boolean selectable,
      String rejectionReason,
      String notes) {}
}
