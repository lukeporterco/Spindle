package com.spindle.core.minecraft.hook.steelhook;

import java.util.List;

public record SteelHook02PrimitiveCandidate(
    String id,
    SteelHook02PrimitiveKind primitiveKind,
    SteelHook02PrimitiveCandidateStatus candidateStatus,
    String sourcePatchId,
    String ownerInternalName,
    String memberName,
    String descriptor,
    int insertionOffset,
    String dispatcherOwnerInternalName,
    String dispatcherMethodName,
    String dispatcherDescriptor,
    boolean fixtureTransformReady,
    boolean minecraftRuntimeTransformReady,
    boolean eligibleForTarget24ContractGeneralization,
    boolean eligibleForTarget25TransformerExtraction,
    boolean eligibleForTarget26RuntimeTransformation,
    List<String> notes) {
  public SteelHook02PrimitiveCandidate {
    notes = List.copyOf(notes == null ? List.of() : notes);
  }
}
