package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook04PrimitiveKind {
  RETURN_VALUE_INTERCEPT("RETURN_VALUE_INTERCEPT"),
  INVOKE_REDIRECT("INVOKE_REDIRECT"),
  INVOKE_WRAP("INVOKE_WRAP");

  private final String id;

  SteelHook04PrimitiveKind(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
