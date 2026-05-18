package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook03MethodExitDispatchStatus {
  METHOD_EXIT_DISPATCH_READY("method-exit-dispatch-ready"),
  BLOCKED("blocked"),
  FAILED("failed");

  private final String id;

  SteelHook03MethodExitDispatchStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
