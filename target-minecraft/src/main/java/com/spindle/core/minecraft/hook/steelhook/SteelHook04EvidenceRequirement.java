package com.spindle.core.minecraft.hook.steelhook;

import java.util.List;

public record SteelHook04EvidenceRequirement(
    String id,
    String targetPass,
    List<SteelHook04PrimitiveKind> primitiveKinds,
    String summary,
    List<SteelHook04RejectionReason> requiredRejections) {
  public SteelHook04EvidenceRequirement {
    primitiveKinds = List.copyOf(primitiveKinds == null ? List.of() : primitiveKinds);
    requiredRejections = List.copyOf(requiredRejections == null ? List.of() : requiredRejections);
  }
}
