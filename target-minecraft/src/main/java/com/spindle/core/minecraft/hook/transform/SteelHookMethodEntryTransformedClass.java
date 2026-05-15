package com.spindle.core.minecraft.hook.transform;

public record SteelHookMethodEntryTransformedClass(
    String internalName, byte[] classBytes, String classSha256) {
  public SteelHookMethodEntryTransformedClass {
    classBytes = classBytes == null ? null : classBytes.clone();
  }

  @Override
  public byte[] classBytes() {
    return classBytes == null ? null : classBytes.clone();
  }
}
