package com.spindle.core.minecraft.command;

import java.util.Objects;

public final class MinecraftCommandDispatcherBindingAnalyzer {
  private static final int REPORT_SCHEMA = 1;
  private static final String MILESTONE_NAME = "Target-15";
  private static final String TARGET = "minecraft";
  private static final String CONCEPT_ID = "minecraft.concept.command_registration";
  private static final String SOURCE_COMMAND_DISPATCHER_SYMBOL_ANALYSIS_MILESTONE = "Target-14";
  private static final String COMMAND_BOUNDARY_ID = "minecraft.commands.dispatcher.discovery";
  private static final String UPSTREAM_GATE_FAILURE_REASON =
      "Target-15 requires a passed Target-14 command dispatcher symbol analysis.";

  public MinecraftCommandDispatcherBindingAnalysis analyze(
      MinecraftCommandDispatcherSymbolAnalysis symbolAnalysis) {
    Objects.requireNonNull(symbolAnalysis, "symbolAnalysis");
    requireExpectedAnalysis(symbolAnalysis);

    if (!symbolAnalysis.gatePassed()
        || symbolAnalysis.selectionStatus()
            == MinecraftCommandDispatcherSymbolSelectionStatus.UPSTREAM_GATE_BLOCKED) {
      return baseAnalysis(
          symbolAnalysis,
          false,
          symbolAnalysis.gateFailureReason() == null
              ? UPSTREAM_GATE_FAILURE_REASON
              : symbolAnalysis.gateFailureReason(),
          MinecraftCommandDispatcherBindingStatus.UPSTREAM_GATE_BLOCKED,
          MinecraftCommandDispatcherAccessStrategy.NONE,
          null,
          null,
          null,
          null,
          null,
          false,
          false,
          false,
          false,
          false,
          false,
          "Restore the Target-13 and Target-14 upstream gates before analyzing command dispatcher binding strategy.",
          null);
    }

    return switch (symbolAnalysis.selectionStatus()) {
      case NO_CANDIDATES ->
          baseAnalysis(
              symbolAnalysis,
              true,
              null,
              MinecraftCommandDispatcherBindingStatus.NO_SYMBOL_TARGET,
              MinecraftCommandDispatcherAccessStrategy.NONE,
              null,
              null,
              null,
              null,
              null,
              false,
              false,
              false,
              false,
              false,
              false,
              "Do not implement command registration yet; no selectable Minecraft command dispatcher symbol target is known.",
              null);
      case AMBIGUOUS_CANDIDATES ->
          baseAnalysis(
              symbolAnalysis,
              true,
              null,
              MinecraftCommandDispatcherBindingStatus.AMBIGUOUS_SYMBOL_TARGETS,
              MinecraftCommandDispatcherAccessStrategy.NONE,
              null,
              null,
              null,
              null,
              null,
              false,
              false,
              false,
              false,
              false,
              false,
              "Do not implement command registration yet; narrow dispatcher discovery before planning access or mutation.",
              null);
      case STABLE_TARGET_SELECTED -> selectedCandidateAnalysis(symbolAnalysis);
      case UPSTREAM_GATE_BLOCKED ->
          baseAnalysis(
              symbolAnalysis,
              false,
              symbolAnalysis.gateFailureReason() == null
                  ? UPSTREAM_GATE_FAILURE_REASON
                  : symbolAnalysis.gateFailureReason(),
              MinecraftCommandDispatcherBindingStatus.UPSTREAM_GATE_BLOCKED,
              MinecraftCommandDispatcherAccessStrategy.NONE,
              null,
              null,
              null,
              null,
              null,
              false,
              false,
              false,
              false,
              false,
              false,
              "Restore the Target-13 and Target-14 upstream gates before analyzing command dispatcher binding strategy.",
              null);
    };
  }

  private void requireExpectedAnalysis(MinecraftCommandDispatcherSymbolAnalysis analysis) {
    if (!CONCEPT_ID.equals(analysis.conceptId())) {
      throw new IllegalArgumentException(
          "Target-15 requires concept `" + CONCEPT_ID + "` from Target-14.");
    }
    if (!SOURCE_COMMAND_DISPATCHER_SYMBOL_ANALYSIS_MILESTONE.equals(analysis.milestoneName())) {
      throw new IllegalArgumentException(
          "Target-15 requires Target-14 command dispatcher symbol analysis input.");
    }
  }

  private MinecraftCommandDispatcherBindingAnalysis selectedCandidateAnalysis(
      MinecraftCommandDispatcherSymbolAnalysis symbolAnalysis) {
    MinecraftCommandDispatcherSymbolCandidate candidate =
        symbolAnalysis.candidates().stream()
            .filter(entry -> symbolAnalysis.selectedCandidateId().equals(entry.id()))
            .findFirst()
            .orElse(null);
    if (candidate == null) {
      return unsupportedSymbolAnalysis(symbolAnalysis, null);
    }

    return switch (candidate.kind()) {
      case METHOD_DESCRIPTOR_REFERENCE ->
          candidate.staticMember()
              ? baseAnalysis(
                  symbolAnalysis,
                  true,
                  null,
                  MinecraftCommandDispatcherBindingStatus.SELECTED_SYMBOL_ANALYZED,
                  MinecraftCommandDispatcherAccessStrategy.METHOD_DESCRIPTOR_REFERENCE_ONLY,
                  candidate.id(),
                  candidate.kind().name(),
                  candidate.ownerInternalName(),
                  candidate.memberName(),
                  candidate.descriptor(),
                  true,
                  true,
                  false,
                  false,
                  true,
                  false,
                  "Plan a future value-capturing command dispatcher primitive before command registration.",
                  "A selected method descriptor reference identifies a possible method boundary but does not provide dispatcher value access. SteelHook 0.1 method-entry dispatch cannot pass or capture dispatcher values.")
              : baseAnalysis(
                  symbolAnalysis,
                  true,
                  null,
                  MinecraftCommandDispatcherBindingStatus.SELECTED_SYMBOL_ANALYZED,
                  MinecraftCommandDispatcherAccessStrategy
                      .INSTANCE_METHOD_RECEIVER_CAPTURE_REQUIRED,
                  candidate.id(),
                  candidate.kind().name(),
                  candidate.ownerInternalName(),
                  candidate.memberName(),
                  candidate.descriptor(),
                  false,
                  true,
                  true,
                  false,
                  true,
                  false,
                  "Plan a future receiver/value-capturing command dispatcher primitive before command registration.",
                  null);
      case FIELD_DESCRIPTOR_REFERENCE ->
          candidate.staticMember()
              ? baseAnalysis(
                  symbolAnalysis,
                  true,
                  null,
                  MinecraftCommandDispatcherBindingStatus.SELECTED_SYMBOL_ANALYZED,
                  MinecraftCommandDispatcherAccessStrategy.STATIC_FIELD_ACCESS_REQUIRED,
                  candidate.id(),
                  candidate.kind().name(),
                  candidate.ownerInternalName(),
                  candidate.memberName(),
                  candidate.descriptor(),
                  true,
                  true,
                  false,
                  true,
                  true,
                  false,
                  "Plan a future controlled static-field dispatcher access primitive before command registration.",
                  null)
              : baseAnalysis(
                  symbolAnalysis,
                  true,
                  null,
                  MinecraftCommandDispatcherBindingStatus.SELECTED_SYMBOL_ANALYZED,
                  MinecraftCommandDispatcherAccessStrategy.INSTANCE_FIELD_OWNER_CAPTURE_REQUIRED,
                  candidate.id(),
                  candidate.kind().name(),
                  candidate.ownerInternalName(),
                  candidate.memberName(),
                  candidate.descriptor(),
                  false,
                  true,
                  true,
                  true,
                  true,
                  false,
                  "Plan a future owner-instance capture plus controlled field access primitive before command registration.",
                  null);
      case BRIGADIER_LIBRARY_CLASS -> unsupportedSymbolAnalysis(symbolAnalysis, candidate);
    };
  }

  private MinecraftCommandDispatcherBindingAnalysis unsupportedSymbolAnalysis(
      MinecraftCommandDispatcherSymbolAnalysis symbolAnalysis,
      MinecraftCommandDispatcherSymbolCandidate candidate) {
    return baseAnalysis(
        symbolAnalysis,
        true,
        null,
        MinecraftCommandDispatcherBindingStatus.UNSUPPORTED_SYMBOL_KIND,
        MinecraftCommandDispatcherAccessStrategy.NONE,
        candidate == null ? symbolAnalysis.selectedCandidateId() : candidate.id(),
        candidate == null ? null : candidate.kind().name(),
        candidate == null ? null : candidate.ownerInternalName(),
        candidate == null ? null : candidate.memberName(),
        candidate == null ? null : candidate.descriptor(),
        candidate != null && candidate.staticMember(),
        false,
        false,
        false,
        false,
        false,
        "Do not continue command registration planning until Target-15 supports this selected symbol kind.",
        null);
  }

  private MinecraftCommandDispatcherBindingAnalysis baseAnalysis(
      MinecraftCommandDispatcherSymbolAnalysis symbolAnalysis,
      boolean gatePassed,
      String gateFailureReason,
      MinecraftCommandDispatcherBindingStatus bindingStatus,
      MinecraftCommandDispatcherAccessStrategy accessStrategy,
      String selectedCandidateId,
      String selectedCandidateKind,
      String ownerInternalName,
      String memberName,
      String descriptor,
      boolean staticMember,
      boolean requiresDispatcherValueCapture,
      boolean requiresOwnerInstanceCapture,
      boolean requiresFieldAccess,
      boolean requiresFutureSteelHookPrimitive,
      boolean currentSteelHookMethodEntryCompatible,
      String nextRecommendedAction,
      String notes) {
    return new MinecraftCommandDispatcherBindingAnalysis(
        REPORT_SCHEMA,
        MILESTONE_NAME,
        TARGET,
        symbolAnalysis.minecraftVersion(),
        symbolAnalysis.side(),
        CONCEPT_ID,
        SOURCE_COMMAND_DISPATCHER_SYMBOL_ANALYSIS_MILESTONE,
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
        symbolAnalysis.gatePassed(),
        symbolAnalysis.selectionStatus().name(),
        symbolAnalysis.minimalCommandRegistrationProofEligible(),
        gatePassed,
        gateFailureReason,
        bindingStatus,
        accessStrategy,
        selectedCandidateId,
        selectedCandidateKind,
        ownerInternalName,
        memberName,
        descriptor,
        staticMember,
        requiresDispatcherValueCapture,
        requiresOwnerInstanceCapture,
        requiresFieldAccess,
        requiresFutureSteelHookPrimitive,
        currentSteelHookMethodEntryCompatible,
        false,
        nextRecommendedAction,
        notes);
  }
}
