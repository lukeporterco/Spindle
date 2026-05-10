package com.spindle.core.minecraft;

import java.util.List;

public record MinecraftExecutableMod(
    String modId,
    String version,
    String side,
    String modJarPath,
    String modJarSha256,
    long modJarSize,
    List<MinecraftEntrypointDeclaration> entrypoints,
    String plannedModClassLoaderId,
    String plannedParentClassLoaderId,
    List<String> protectedPackages,
    List<String> deniedPackages,
    List<String> allowedApiPackages,
    String plannedDelegationPolicy) {
  public MinecraftExecutableMod {
    entrypoints = List.copyOf(entrypoints);
    protectedPackages = List.copyOf(protectedPackages);
    deniedPackages = List.copyOf(deniedPackages);
    allowedApiPackages = List.copyOf(allowedApiPackages);
  }
}
