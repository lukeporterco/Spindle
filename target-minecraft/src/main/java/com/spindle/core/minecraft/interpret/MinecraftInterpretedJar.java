package com.spindle.core.minecraft.interpret;

import java.util.List;

public record MinecraftInterpretedJar(
    String path,
    String ownership,
    String origin,
    String sha256,
    int classCount,
    int fieldCount,
    int methodCount,
    int constructorCount,
    List<String> packages,
    List<MinecraftInterpretedClass> classes) {
  public MinecraftInterpretedJar {
    packages = List.copyOf(packages);
    classes = List.copyOf(classes);
  }
}
