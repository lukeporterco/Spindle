package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook03FramedMethodFoundationStatus {
  FOUNDATION_READY("foundation-ready"),
  BLOCKED("blocked"),
  FAILED("failed");

  private final String id;

  SteelHook03FramedMethodFoundationStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
