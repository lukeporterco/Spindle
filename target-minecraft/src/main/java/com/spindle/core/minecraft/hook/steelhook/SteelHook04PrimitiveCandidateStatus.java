package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook04PrimitiveCandidateStatus {
  APPROVED_INTERNAL_PLANNED_PRIMITIVE("approved-internal-planned-primitive"),
  BLOCKED_BY_SOURCE_GATE("blocked-by-source-gate");

  private final String id;

  SteelHook04PrimitiveCandidateStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
