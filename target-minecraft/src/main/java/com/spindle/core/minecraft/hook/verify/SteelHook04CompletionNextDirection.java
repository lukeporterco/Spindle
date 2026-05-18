package com.spindle.core.minecraft.hook.verify;

public enum SteelHook04CompletionNextDirection {
  STEELHOOK_0_4_COMPLETE("steelhook-0-4-complete"),
  RESTORE_TARGET_32_PRIMITIVE_BOUNDARY("restore-target-32-primitive-boundary"),
  RESTORE_TARGET_33_RETURN_VALUE_INTERCEPT_OFFLINE_PROOF(
      "restore-target-33-return-value-intercept-offline-proof"),
  RESTORE_TARGET_34_INVOKE_REDIRECT_WRAP_OFFLINE_PROOF(
      "restore-target-34-invoke-redirect-wrap-offline-proof"),
  RESTORE_TARGET_35_GATED_RUNTIME_PROOF("restore-target-35-gated-runtime-proof");

  private final String id;

  SteelHook04CompletionNextDirection(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
