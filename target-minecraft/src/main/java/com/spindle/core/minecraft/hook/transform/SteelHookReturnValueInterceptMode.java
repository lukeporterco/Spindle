package com.spindle.core.minecraft.hook.transform;

public enum SteelHookReturnValueInterceptMode {
  OBSERVE_ONLY("observe-only"),
  REPLACE_RETURN_VALUE("replace-return-value");

  private final String id;

  SteelHookReturnValueInterceptMode(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
