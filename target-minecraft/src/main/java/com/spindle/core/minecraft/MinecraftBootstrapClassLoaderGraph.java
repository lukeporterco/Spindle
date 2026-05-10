package com.spindle.core.minecraft;

import java.util.List;

public record MinecraftBootstrapClassLoaderGraph(
    int schema,
    String milestoneName,
    List<ClassLoaderNode> classLoaders,
    List<String> protectedPackages,
    List<String> deniedPackages,
    List<String> allowedApiPackages,
    List<String> modJars,
    List<String> minecraftRuntimeJars,
    boolean modJarsOnMinecraftRuntimeClasspath) {
  public MinecraftBootstrapClassLoaderGraph {
    classLoaders = List.copyOf(classLoaders);
    protectedPackages = List.copyOf(protectedPackages);
    deniedPackages = List.copyOf(deniedPackages);
    allowedApiPackages = List.copyOf(allowedApiPackages);
    modJars = List.copyOf(modJars);
    minecraftRuntimeJars = List.copyOf(minecraftRuntimeJars);
  }

  public record ClassLoaderNode(
      String id,
      String parentId,
      String ownership,
      List<String> classpathEntries,
      List<String> visibilityRules) {
    public ClassLoaderNode {
      classpathEntries = List.copyOf(classpathEntries);
      visibilityRules = List.copyOf(visibilityRules);
    }
  }
}
