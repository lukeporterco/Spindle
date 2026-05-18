package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook03RuntimeProofNextDirection {
  MOVE_TO_TARGET_31_STEELHOOK_0_3_COMPLETION("move-to-target-31-steelhook-0-3-completion"),
  RESTORE_TARGET_29_METHOD_EXIT_STATIC_DISPATCH("restore-target-29-method-exit-static-dispatch"),
  RESTORE_TARGET_30_GENERALIZED_TRANSFORMER_GATED_RUNTIME_PROOF(
      "restore-target-30-generalized-transformer-gated-runtime-proof");

  private final String id;

  SteelHook03RuntimeProofNextDirection(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
