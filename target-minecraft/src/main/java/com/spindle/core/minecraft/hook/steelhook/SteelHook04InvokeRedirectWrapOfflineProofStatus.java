package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook04InvokeRedirectWrapOfflineProofStatus {
  PROOF_READY("proof-ready"),
  BLOCKED("blocked"),
  FAILED("failed");

  private final String id;

  SteelHook04InvokeRedirectWrapOfflineProofStatus(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
