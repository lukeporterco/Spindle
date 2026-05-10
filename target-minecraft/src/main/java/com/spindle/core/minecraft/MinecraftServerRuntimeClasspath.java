package com.spindle.core.minecraft;

import java.util.List;

public record MinecraftServerRuntimeClasspath(List<Entry> entries) {
  public MinecraftServerRuntimeClasspath {
    entries = List.copyOf(entries);
  }

  public record Entry(String path, String ownership, String origin, String sha256) {}
}
