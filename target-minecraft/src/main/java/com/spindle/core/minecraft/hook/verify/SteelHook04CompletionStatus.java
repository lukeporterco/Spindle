package com.spindle.core.minecraft.hook.verify;

public enum SteelHook04CompletionStatus {
  PASSED("passed"),
  FAILED("failed");

  private final String id;

  SteelHook04CompletionStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
