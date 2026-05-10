package com.spindle.core.minecraft;

import java.util.List;

public record MinecraftClassLoaderPolicy(
    List<String> protectedPackages,
    List<String> deniedPackages,
    List<String> allowedApiPackages,
    String delegationPolicy,
    boolean denyLoaderInternals) {
  public MinecraftClassLoaderPolicy {
    protectedPackages = List.copyOf(protectedPackages);
    deniedPackages = List.copyOf(deniedPackages);
    allowedApiPackages = List.copyOf(allowedApiPackages);
  }

  public static MinecraftClassLoaderPolicy strictDefault(
      MinecraftProtectedPackagePolicy policy, boolean denyLoaderInternals) {
    return new MinecraftClassLoaderPolicy(
        policy.protectedDefinitionPrefixes(),
        policy.deniedLoadPrefixes(),
        policy.allowedApiPrefixes(),
        "child-first-mod-parent-runtime",
        denyLoaderInternals);
  }
}
