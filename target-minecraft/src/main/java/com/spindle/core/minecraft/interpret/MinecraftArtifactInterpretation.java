package com.spindle.core.minecraft.interpret;

import java.util.List;

public record MinecraftArtifactInterpretation(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    String side,
    boolean analysisOnly,
    boolean classLoadingOccurred,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean hookInstallationOccurred,
    String interpretedAtRuntimePhase,
    List<MinecraftInterpretedJar> jars,
    int packageCount,
    int classCount,
    int fieldCount,
    int methodCount,
    int constructorCount,
    List<String> packages,
    List<String> warnings) {
  public MinecraftArtifactInterpretation {
    jars = List.copyOf(jars);
    packages = List.copyOf(packages);
    warnings = List.copyOf(warnings);
  }
}
