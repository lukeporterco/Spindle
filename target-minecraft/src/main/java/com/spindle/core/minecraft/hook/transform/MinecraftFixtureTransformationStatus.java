package com.spindle.core.minecraft.hook.transform;

public enum MinecraftFixtureTransformationStatus {
  TRANSFORMED("transformed"),
  PATCH_PLAN_GATE_FAILED("patch-plan-gate-failed"),
  REJECTED("rejected");

  private final String id;

  MinecraftFixtureTransformationStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
