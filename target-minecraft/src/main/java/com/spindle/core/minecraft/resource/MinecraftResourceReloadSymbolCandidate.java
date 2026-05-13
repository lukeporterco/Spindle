package com.spindle.core.minecraft.resource;

import java.util.List;

public record MinecraftResourceReloadSymbolCandidate(
    String id,
    MinecraftResourceReloadSymbolCandidateKind kind,
    String boundaryId,
    String ownerInternalName,
    String memberName,
    String descriptor,
    boolean staticMember,
    List<String> accessFlags,
    List<String> matchedTokens,
    boolean selectable,
    String rejectionReason,
    String notes) {
  public MinecraftResourceReloadSymbolCandidate {
    accessFlags = List.copyOf(accessFlags == null ? List.of() : accessFlags);
    matchedTokens = List.copyOf(matchedTokens == null ? List.of() : matchedTokens);
  }
}
