package com.spindle.core.minecraft.hook.verify;

public enum SteelHook02CompletionHandoffStatus {
  STEELHOOK_0_2_COMPLETE("steelhook-0-2-complete"),
  STEELHOOK_0_2_BLOCKED("steelhook-0-2-blocked");

  private final String id;

  SteelHook02CompletionHandoffStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
