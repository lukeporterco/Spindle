package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook03PrimitiveKind {
  METHOD_ENTRY_STATIC_DISPATCH("METHOD_ENTRY_STATIC_DISPATCH"),
  METHOD_EXIT_STATIC_DISPATCH("METHOD_EXIT_STATIC_DISPATCH");

  private final String id;

  SteelHook03PrimitiveKind(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
