package com.spindle.core.minecraft.hook.place;

public enum MinecraftHookPlacementMode {
  METHOD_ENTRY_ANALYSIS_ONLY("method-entry-analysis-only");

  private final String id;

  MinecraftHookPlacementMode(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
