package com.spindle.core.minecraft.hook.verify;

public enum SteelHook03CompletionStatus {
  PASSED("passed"),
  FAILED("failed");

  private final String id;

  SteelHook03CompletionStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
