package com.spindle.core.minecraft.hook.bootstrap;

public enum MinecraftBootstrapHookTransformationStatus {
  TRANSFORMED("transformed"),
  PATCH_PLAN_GATE_FAILED("patch-plan-gate-failed"),
  REJECTED("rejected");

  private final String id;

  MinecraftBootstrapHookTransformationStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
