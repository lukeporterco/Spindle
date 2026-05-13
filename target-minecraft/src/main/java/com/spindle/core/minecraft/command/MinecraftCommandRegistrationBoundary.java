package com.spindle.core.minecraft.command;

public enum MinecraftCommandRegistrationBoundary {
  LIFECYCLE_ANCHOR("minecraft.commands.lifecycle_anchor", "Lifecycle Anchor"),
  DISPATCHER_DISCOVERY("minecraft.commands.dispatcher.discovery", "Dispatcher Discovery"),
  REGISTRATION_WINDOW("minecraft.commands.registration.window", "Registration Window"),
  REGISTRATION_APPLY("minecraft.commands.registration.apply", "Registration Apply"),
  RELOAD_REAPPLY("minecraft.commands.reload.reapply", "Reload Reapply");

  private final String id;
  private final String displayName;

  MinecraftCommandRegistrationBoundary(String id, String displayName) {
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
