package com.spindle.core.minecraft;

public enum MinecraftModSide {
  UNIVERSAL("universal"),
  CLIENT("client"),
  SERVER("server");

  private final String id;

  MinecraftModSide(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }

  public static MinecraftModSide from(String value) {
    for (MinecraftModSide side : values()) {
      if (side.id.equals(value)) {
        return side;
      }
    }
    return UNIVERSAL;
  }
}
