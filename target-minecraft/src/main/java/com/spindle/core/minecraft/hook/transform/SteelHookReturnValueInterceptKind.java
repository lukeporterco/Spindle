package com.spindle.core.minecraft.hook.transform;

public enum SteelHookReturnValueInterceptKind {
  PRIMITIVE_INT("primitive-int"),
  REFERENCE_STRING("reference-string");

  private final String id;

  SteelHookReturnValueInterceptKind(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
