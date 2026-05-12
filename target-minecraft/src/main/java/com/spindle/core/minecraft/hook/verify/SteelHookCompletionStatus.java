package com.spindle.core.minecraft.hook.verify;

public enum SteelHookCompletionStatus {
  PASSED("passed"),
  FAILED("failed");

  private final String id;

  SteelHookCompletionStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
