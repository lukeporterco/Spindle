package com.spindle.core.minecraft.registry;

import java.util.List;

public record MinecraftRegistryCandidate(
    String id,
    MinecraftRegistryCandidateKind kind,
    String boundaryId,
    String ownerInternalName,
    String memberName,
    String descriptor,
    boolean staticMember,
    List<String> accessFlags,
    List<String> matchedTokens,
    boolean selectable,
    String rejectionReason,
    MinecraftRegistryAccessStrategy accessStrategy,
    boolean requiresSymbolNarrowing,
    boolean requiresMethodBoundaryAnalysis,
    boolean requiresReceiverCapture,
    boolean requiresFieldAccess,
    boolean requiresRegistryValueAccess,
    boolean requiresRegistrationTimingDecision,
    boolean requiresRegistrationApplySemanticsDecision,
    boolean requiresFutureSteelHookPrimitive,
    boolean currentSteelHookMethodEntryCompatible,
    boolean registryProofRecommended,
    String notes) {
  public MinecraftRegistryCandidate {
    accessFlags = List.copyOf(accessFlags == null ? List.of() : accessFlags);
    matchedTokens = List.copyOf(matchedTokens == null ? List.of() : matchedTokens);
  }
}
