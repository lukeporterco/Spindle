package com.spindle.core.minecraft.resource;

import java.util.List;

public record MinecraftResourceReloadBindingCandidate(
    String sourceCandidateId,
    String sourceCandidateKind,
    String boundaryId,
    String ownerInternalName,
    String memberName,
    String descriptor,
    boolean staticMember,
    List<String> matchedTokens,
    boolean selectable,
    String sourceRejectionReason,
    MinecraftResourceReloadAccessStrategy accessStrategy,
    boolean requiresSymbolNarrowing,
    boolean requiresMethodBoundaryAnalysis,
    boolean requiresReceiverCapture,
    boolean requiresFieldAccess,
    boolean requiresRuntimeResourceAccess,
    boolean requiresReloadTimingDecision,
    boolean requiresReloadApplySemanticsDecision,
    boolean requiresFutureSteelHookPrimitive,
    boolean currentSteelHookMethodEntryCompatible,
    boolean reloadProofRecommended,
    String notes) {
  public MinecraftResourceReloadBindingCandidate {
    matchedTokens = List.copyOf(matchedTokens == null ? List.of() : matchedTokens);
  }
}
