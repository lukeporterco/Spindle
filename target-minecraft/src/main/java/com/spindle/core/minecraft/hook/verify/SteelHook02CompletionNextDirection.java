package com.spindle.core.minecraft.hook.verify;

public enum SteelHook02CompletionNextDirection {
  MOVE_TO_STEELHOOK_0_3_STACKMAP_AND_EXIT_PRIMITIVES(
      "move-to-steelhook-0-3-stackmap-and-exit-primitives"),
  RESTORE_TARGET_26_GATED_RUNTIME_TRANSFORMATION("restore-target-26-gated-runtime-transformation"),
  RESTORE_UPSTREAM_STEELHOOK_0_2_CHAIN("restore-upstream-steelhook-0-2-chain");

  private final String id;

  SteelHook02CompletionNextDirection(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
