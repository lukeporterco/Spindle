package com.spindle.core.minecraft.resource;

public enum MinecraftResourceReloadBoundary {
  LIFECYCLE_ANCHOR("minecraft.resources.lifecycle_anchor", "Lifecycle Anchor"),
  RELOAD_DISCOVERY("minecraft.resources.reload.discovery", "Reload Discovery"),
  RELOAD_WINDOW("minecraft.resources.reload.window", "Reload Window"),
  RELOAD_APPLY("minecraft.resources.reload.apply", "Reload Apply"),
  DATAPACK_VIEW("minecraft.resources.datapack.view", "Datapack View"),
  RESOURCE_MANAGER_VIEW("minecraft.resources.resource_manager.view", "Resource Manager View"),
  FUTURE_DATA_GENERATION("minecraft.resources.future_data_generation", "Future Data Generation");

  private final String id;
  private final String displayName;

  MinecraftResourceReloadBoundary(String id, String displayName) {
    this.id = id;
    this.displayName = displayName;
  }

  public String id() {
    return id;
  }

  public String displayName() {
    return displayName;
  }
}
