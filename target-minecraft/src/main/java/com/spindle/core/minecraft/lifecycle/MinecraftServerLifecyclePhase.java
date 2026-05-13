package com.spindle.core.minecraft.lifecycle;

public enum MinecraftServerLifecyclePhase {
  SERVER_STARTING("minecraft.server.lifecycle.starting", "Starting"),
  SERVER_STARTED("minecraft.server.lifecycle.started", "Started"),
  SERVER_STOPPING("minecraft.server.lifecycle.stopping", "Stopping"),
  SERVER_STOPPED("minecraft.server.lifecycle.stopped", "Stopped"),
  SERVER_CRASHED("minecraft.server.lifecycle.crashed", "Crashed"),
  SERVER_RELOAD_REQUESTED("minecraft.server.lifecycle.reload_requested", "Reload Requested");

  private final String id;
  private final String displayName;

  MinecraftServerLifecyclePhase(String id, String displayName) {
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
