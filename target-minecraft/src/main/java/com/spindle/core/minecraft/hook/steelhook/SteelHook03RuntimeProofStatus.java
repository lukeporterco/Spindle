package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook03RuntimeProofStatus {
  GATED_RUNTIME_PROOF_READY("gated-runtime-proof-ready"),
  BLOCKED("blocked"),
  FAILED("failed");

  private final String id;

  SteelHook03RuntimeProofStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
