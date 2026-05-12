package com.spindle.core.minecraft.hook.install;

public enum MinecraftHookInstallationMode {
  LAUNCH_BOUNDARY_MAIN_WRAPPER("launch-boundary-main-wrapper");

  private final String id;

  MinecraftHookInstallationMode(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }

  public static MinecraftHookInstallationMode fromId(String id) {
    for (MinecraftHookInstallationMode mode : values()) {
      if (mode.id.equals(id)) {
        return mode;
      }
    }
    return null;
  }
}
