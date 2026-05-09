package com.spindle.core.minecraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MinecraftBootstrapClassLoaderGraphBuilder {
  public MinecraftBootstrapClassLoaderGraph build(MinecraftModExecutionPlan executionPlan) {
    List<MinecraftBootstrapClassLoaderGraph.ClassLoaderNode> nodes = new ArrayList<>();
    nodes.add(
        new MinecraftBootstrapClassLoaderGraph.ClassLoaderNode(
            "bootstrap", null, "jvm-bootstrap", List.of(), List.of("JVM basics only")));
    nodes.add(
        new MinecraftBootstrapClassLoaderGraph.ClassLoaderNode(
            "loader-app",
            "bootstrap",
            "loader implementation",
            List.of("current-jvm-classpath"),
            List.of("loader implementation and loader API")));
    nodes.add(
        new MinecraftBootstrapClassLoaderGraph.ClassLoaderNode(
            "minecraft-runtime",
            "loader-app",
            "minecraft runtime",
            executionPlan.minecraftRuntimeClasspathSummary(),
            List.of("runtime jars only", "mod jars excluded")));
    for (MinecraftExecutableMod mod :
        executionPlan.acceptedExecutableMods().stream()
            .sorted(Comparator.comparing(MinecraftExecutableMod::modId))
            .toList()) {
      nodes.add(
          new MinecraftBootstrapClassLoaderGraph.ClassLoaderNode(
              mod.plannedModClassLoaderId(),
              mod.plannedParentClassLoaderId(),
              "approved mod jar",
              List.of(mod.modJarPath()),
              List.of(
                  mod.plannedDelegationPolicy(),
                  "loader-core packages denied",
                  "loader-api visible")));
    }
    return new MinecraftBootstrapClassLoaderGraph(
        1,
        "Milestone 8",
        nodes,
        executionPlan.classLoaderPolicy().protectedPackages(),
        executionPlan.classLoaderPolicy().deniedPackages(),
        executionPlan.classLoaderPolicy().allowedApiPackages(),
        executionPlan.acceptedExecutableMods().stream()
            .map(MinecraftExecutableMod::modJarPath)
            .sorted()
            .toList(),
        executionPlan.minecraftRuntimeClasspathSummary(),
        false);
  }
}
