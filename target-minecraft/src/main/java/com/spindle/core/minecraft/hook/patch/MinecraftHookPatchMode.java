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

  public static MinecraftHookPatchMode fromId(String id) {
    if (id == null || id.isBlank()) {
      return null;
    }
    for (MinecraftHookPatchMode value : values()) {
      if (value.id.equals(id)) {
        return value;
      }
    }
    throw new IllegalArgumentException("Unknown Minecraft hook patch mode: " + id);
  }
}
