package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook04GatedRuntimeProofNextDirection {
  MOVE_TO_TARGET_36_STEELHOOK_0_4_COMPLETION_VERIFICATION(
      "move-to-target-36-steelhook-0-4-completion-verification"),
  RESTORE_TARGET_32_PRIMITIVE_BOUNDARY("restore-target-32-primitive-boundary"),
  RESTORE_TARGET_33_RETURN_VALUE_INTERCEPT_OFFLINE_PROOF(
      "restore-target-33-return-value-intercept-offline-proof"),
  RESTORE_TARGET_34_INVOKE_REDIRECT_WRAP_OFFLINE_PROOF(
      "restore-target-34-invoke-redirect-wrap-offline-proof"),
  RESTORE_TARGET_35_GATED_RUNTIME_CLASS_DEFINITION_PROOF(
      "restore-target-35-gated-runtime-class-definition-proof");

  private final String id;

  SteelHook04GatedRuntimeProofNextDirection(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
