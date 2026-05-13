package com.spindle.core.minecraft.command;

import java.util.List;

public record MinecraftCommandDispatcherSymbolCandidate(
    String id,
    MinecraftCommandDispatcherSymbolCandidateKind kind,
    String ownerInternalName,
    String memberName,
    String descriptor,
    boolean staticMember,
    List<String> accessFlags,
    boolean selectable,
    boolean selected,
    String rejectionReason,
    String notes) {
  public MinecraftCommandDispatcherSymbolCandidate {
    accessFlags = List.copyOf(accessFlags == null ? List.of() : accessFlags);
  }
}
