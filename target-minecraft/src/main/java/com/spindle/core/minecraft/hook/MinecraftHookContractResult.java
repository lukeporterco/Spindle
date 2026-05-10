package com.spindle.core.minecraft.hook;

import java.util.List;

public record MinecraftHookContractResult(
    String id,
    String description,
    String side,
    String kind,
    String ownerInternalName,
    String memberName,
    String descriptor,
    String requirement,
    String status,
    boolean valid,
    boolean required,
    boolean optional,
    List<String> diagnosticIds,
    String matchedClass,
    String matchedMember) {
  public MinecraftHookContractResult {
    diagnosticIds = List.copyOf(diagnosticIds == null ? List.of() : diagnosticIds);
  }
}
