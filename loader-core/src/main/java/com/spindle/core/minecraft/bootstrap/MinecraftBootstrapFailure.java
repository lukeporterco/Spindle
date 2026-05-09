package com.spindle.core.minecraft.bootstrap;

import java.util.List;

public record MinecraftBootstrapFailure(String category, String message, List<String> details) {
  public MinecraftBootstrapFailure {
    details = List.copyOf(details);
  }
}
