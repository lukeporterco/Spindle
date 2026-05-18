package com.spindle.core.minecraft.hook.bootstrap;

public enum MinecraftBootstrapHookTransformationMode {
  BOOTSTRAP_FAKE_SERVER_METHOD_ENTRY_TRANSFORM("bootstrap-fake-server-method-entry-transform"),
  STEELHOOK_0_2_GATED_RUNTIME_METHOD_ENTRY_TRANSFORM(
      "steelhook-0-2-gated-runtime-method-entry-transform"),
  STEELHOOK_0_3_GATED_RUNTIME_METHOD_ENTRY_TRANSFORM(
      "steelhook-0-3-gated-runtime-method-entry-transform"),
  STEELHOOK_0_3_GATED_RUNTIME_METHOD_EXIT_TRANSFORM(
      "steelhook-0-3-gated-runtime-method-exit-transform"),
  STEELHOOK_0_4_GATED_RUNTIME_RETURN_VALUE_INTERCEPT_TRANSFORM(
      "steelhook-0-4-gated-runtime-return-value-intercept-transform"),
  STEELHOOK_0_4_GATED_RUNTIME_INVOKE_REDIRECT_TRANSFORM(
      "steelhook-0-4-gated-runtime-invoke-redirect-transform"),
  STEELHOOK_0_4_GATED_RUNTIME_INVOKE_WRAP_TRANSFORM(
      "steelhook-0-4-gated-runtime-invoke-wrap-transform");

  private final String id;

  MinecraftBootstrapHookTransformationMode(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
