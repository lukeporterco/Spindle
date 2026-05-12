package com.spindle.core.minecraft.hook.bootstrap;

public enum MinecraftBootstrapHookTransformationMode {
  BOOTSTRAP_FAKE_SERVER_METHOD_ENTRY_TRANSFORM("bootstrap-fake-server-method-entry-transform");

  private final String id;

  MinecraftBootstrapHookTransformationMode(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
