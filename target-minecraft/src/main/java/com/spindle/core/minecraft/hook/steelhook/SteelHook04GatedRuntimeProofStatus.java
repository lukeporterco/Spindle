package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook04GatedRuntimeProofStatus {
  GATED_RUNTIME_PROOF_READY("gated-runtime-proof-ready"),
  BLOCKED("blocked"),
  FAILED("failed");

  private final String id;

  SteelHook04GatedRuntimeProofStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
