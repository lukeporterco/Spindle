package com.spindle.core.minecraft;

public enum MinecraftBoundarySeverity {
  INFO("info"),
  WARNING("warning"),
  ERROR("error"),
  FATAL("fatal");

  private final String id;

  MinecraftBoundarySeverity(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
