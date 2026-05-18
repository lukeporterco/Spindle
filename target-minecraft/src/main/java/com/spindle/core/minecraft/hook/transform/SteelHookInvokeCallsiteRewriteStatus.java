package com.spindle.core.minecraft.hook.transform;

public enum SteelHookInvokeCallsiteRewriteStatus {
  TRANSFORMED("transformed"),
  REJECTED("rejected");

  private final String id;

  SteelHookInvokeCallsiteRewriteStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
