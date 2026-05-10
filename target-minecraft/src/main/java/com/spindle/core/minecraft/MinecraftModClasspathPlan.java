package com.spindle.core.minecraft;

import java.util.List;

public record MinecraftModClasspathPlan(
    List<String> plannedFutureModClasspathEntries, boolean placedOnMinecraftRuntimeClasspath) {
  public MinecraftModClasspathPlan {
    plannedFutureModClasspathEntries = List.copyOf(plannedFutureModClasspathEntries);
  }
}
