package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook04InvokeCallsiteFindingStatus {
  PASS("pass"),
  FAIL("fail");

  private final String id;

  SteelHook04InvokeCallsiteFindingStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
