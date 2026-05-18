package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook04PrimitiveFindingStatus {
  PASS("pass"),
  FAIL("fail");

  private final String id;

  SteelHook04PrimitiveFindingStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
