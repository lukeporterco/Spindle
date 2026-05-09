package com.spindle.core.minecraft;

import java.util.List;

public record MinecraftRuntimeOwnershipIndex(
    List<MinecraftServerRuntimeClasspath.Entry> classpath,
    MinecraftRuntimePackageIndex packages,
    MinecraftRuntimeResourceIndex resources,
    MinecraftRuntimeServiceIndex services) {
  public MinecraftRuntimeOwnershipIndex {
    classpath = List.copyOf(classpath);
  }
}
