package com.spindle.core.minecraft.hook.bootstrap;

public enum MinecraftBootstrapHookTransformationMode {
  BOOTSTRAP_FAKE_SERVER_METHOD_ENTRY_TRANSFORM("bootstrap-fake-server-method-entry-transform"),
  STEELHOOK_0_2_GATED_RUNTIME_METHOD_ENTRY_TRANSFORM(
      "steelhook-0-2-gated-runtime-method-entry-transform"),
  STEELHOOK_0_3_GATED_RUNTIME_METHOD_ENTRY_TRANSFORM(
      "steelhook-0-3-gated-runtime-method-entry-transform"),
  STEELHOOK_0_3_GATED_RUNTIME_METHOD_EXIT_TRANSFORM(
      "steelhook-0-3-gated-runtime-method-exit-transform");

  private final String id;

  MinecraftBootstrapHookTransformationMode(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
