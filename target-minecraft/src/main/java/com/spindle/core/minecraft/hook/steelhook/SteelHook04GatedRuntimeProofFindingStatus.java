package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook04GatedRuntimeProofFindingStatus {
  PASS("pass"),
  FAIL("fail");

  private final String id;

  SteelHook04GatedRuntimeProofFindingStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
