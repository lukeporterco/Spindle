package com.spindle.core.minecraft.hook.verify;

public enum SteelHook03CompletionHandoffStatus {
  STEELHOOK_0_3_COMPLETE("steelhook-0-3-complete"),
  STEELHOOK_0_3_INCOMPLETE("steelhook-0-3-incomplete");

  private final String id;

  SteelHook03CompletionHandoffStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
