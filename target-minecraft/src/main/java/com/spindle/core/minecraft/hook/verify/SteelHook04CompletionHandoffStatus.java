package com.spindle.core.minecraft.hook.verify;

public enum SteelHook04CompletionHandoffStatus {
  STEELHOOK_0_4_COMPLETE("steelhook-0-4-complete"),
  STEELHOOK_0_4_INCOMPLETE("steelhook-0-4-incomplete");

  private final String id;

  SteelHook04CompletionHandoffStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
