package com.spindle.core.minecraft;

import java.util.List;
import java.util.Map;

public record MinecraftRuntimePackageIndex(Map<String, List<String>> packages) {
  public MinecraftRuntimePackageIndex {
    packages = Map.copyOf(packages);
  }
}
