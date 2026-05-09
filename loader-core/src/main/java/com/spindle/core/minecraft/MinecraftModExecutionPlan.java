package com.spindle.core.minecraft;

import java.util.List;

public record MinecraftModExecutionPlan(
    int schema,
    String milestoneName,
    String resolvedMinecraftVersion,
    String javaBaseline,
    String side,
    MinecraftPlanFingerprint runtimePlanFingerprint,
    MinecraftPlanFingerprint boundaryFingerprint,
    MinecraftPlanFingerprint integrationPlanFingerprint,
    List<MinecraftExecutableMod> acceptedExecutableMods,
    List<MinecraftModRejection> rejectedMods,
    List<MinecraftEntrypointPlan> executableEntrypoints,
    MinecraftClassLoaderPolicy classLoaderPolicy,
    String minecraftMainClass,
    List<String> minecraftMainArgs,
    List<String> minecraftRuntimeClasspathSummary,
    MinecraftExecutionPolicy executionPolicy,
    MinecraftExecutionProof proof) {
  public MinecraftModExecutionPlan {
    acceptedExecutableMods = List.copyOf(acceptedExecutableMods);
    rejectedMods = List.copyOf(rejectedMods);
    executableEntrypoints = List.copyOf(executableEntrypoints);
    minecraftMainArgs = List.copyOf(minecraftMainArgs);
    minecraftRuntimeClasspathSummary = List.copyOf(minecraftRuntimeClasspathSummary);
  }
}
