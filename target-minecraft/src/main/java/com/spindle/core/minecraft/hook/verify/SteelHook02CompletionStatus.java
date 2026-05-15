package com.spindle.core.minecraft.hook.verify;

public enum SteelHook02CompletionStatus {
  PASSED("passed"),
  FAILED("failed");

  private final String id;

  SteelHook02CompletionStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
