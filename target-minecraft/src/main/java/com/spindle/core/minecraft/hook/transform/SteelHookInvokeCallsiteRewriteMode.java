package com.spindle.core.minecraft.hook.transform;

public enum SteelHookInvokeCallsiteRewriteMode {
  REDIRECT("redirect"),
  WRAP("wrap");

  private final String id;

  SteelHookInvokeCallsiteRewriteMode(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
