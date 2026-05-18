package com.spindle.core.minecraft.hook.steelhook;

public enum SteelHook04InvokeRedirectWrapOfflineProofNextDirection {
  MOVE_TO_TARGET_35_GATED_RUNTIME_CLASS_DEFINITION_PROOF(
      "move-to-target-35-gated-runtime-class-definition-proof"),
  RESTORE_TARGET_32_PRIMITIVE_BOUNDARY("restore-target-32-primitive-boundary"),
  RESTORE_TARGET_33_RETURN_VALUE_INTERCEPT_OFFLINE_PROOF(
      "restore-target-33-return-value-intercept-offline-proof"),
  RESTORE_TARGET_34_INVOKE_REWRITE_FIXTURES("restore-target-34-invoke-rewrite-fixtures");

  private final String id;

  SteelHook04InvokeRedirectWrapOfflineProofNextDirection(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
