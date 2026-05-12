package com.spindle.core.minecraft.hook.patch;

public enum MinecraftHookPatchMode {
  DRY_RUN_STATIC_DISPATCH_INVOKESTATIC("dry-run-static-dispatch-invokestatic");

  private final String id;

  MinecraftHookPatchMode(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
