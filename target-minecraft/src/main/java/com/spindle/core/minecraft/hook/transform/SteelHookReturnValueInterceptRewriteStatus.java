package com.spindle.core.minecraft.hook.transform;

public enum SteelHookReturnValueInterceptRewriteStatus {
  OBSERVED("observed"),
  TRANSFORMED("transformed"),
  REJECTED("rejected");

  private final String id;

  SteelHookReturnValueInterceptRewriteStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
