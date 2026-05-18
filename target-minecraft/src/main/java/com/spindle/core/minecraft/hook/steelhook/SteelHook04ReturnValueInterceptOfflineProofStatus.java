package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook04ReturnValueInterceptOfflineProofStatus {
  PROOF_READY("proof-ready"),
  BLOCKED("blocked"),
  FAILED("failed");

  private final String id;

  SteelHook04ReturnValueInterceptOfflineProofStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
