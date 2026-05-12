package com.spindle.core.minecraft.hook.patch;

public enum MinecraftHookPatchEligibility {
  FIXTURE_ONLY_FUTURE_TRANSFORM("fixture-only-future-transform"),
  NOT_ELIGIBLE("not-eligible");

  private final String id;

  MinecraftHookPatchEligibility(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }

  public static MinecraftHookPatchEligibility fromId(String id) {
    if (id == null || id.isBlank()) {
      return null;
    }
    for (MinecraftHookPatchEligibility value : values()) {
      if (value.id.equals(id)) {
        return value;
      }
    }
    throw new IllegalArgumentException("Unknown Minecraft hook patch eligibility: " + id);
  }
}
