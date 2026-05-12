package com.spindle.core.minecraft.hook.transform;

public record MinecraftFixtureTransformedClass(
    String internalName, byte[] classBytes, String classSha256) {
  public MinecraftFixtureTransformedClass {
    classBytes = classBytes == null ? null : classBytes.clone();
  }

  @Override
  public byte[] classBytes() {
    return classBytes == null ? null : classBytes.clone();
  }
}
