package com.spindle.core.minecraft.hook.transform;

public record SteelHookMethodExitTransformedClass(
    String classInternalName, byte[] classBytes, String classSha256) {
  public SteelHookMethodExitTransformedClass {
    classBytes = classBytes == null ? null : classBytes.clone();
  }
}
