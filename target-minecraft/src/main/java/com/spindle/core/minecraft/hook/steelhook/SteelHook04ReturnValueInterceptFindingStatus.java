package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook04ReturnValueInterceptFindingStatus {
  PASS("pass"),
  FAIL("fail");

  private final String id;

  SteelHook04ReturnValueInterceptFindingStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
