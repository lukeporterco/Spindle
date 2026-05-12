package com.spindle.core.minecraft.concept;

import java.util.List;

public record MinecraftTargetConcept(
    String id,
    int order,
    String displayName,
    MinecraftTargetConceptFamily family,
    MinecraftTargetConceptLayer layer,
    String description,
    List<String> targetConceptNames,
    String status) {
  public MinecraftTargetConcept {
    targetConceptNames = List.copyOf(targetConceptNames == null ? List.of() : targetConceptNames);
  }
}
