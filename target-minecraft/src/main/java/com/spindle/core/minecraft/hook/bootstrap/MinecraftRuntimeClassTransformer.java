package com.spindle.core.minecraft.hook.bootstrap;

public interface MinecraftRuntimeClassTransformer {
  boolean shouldTransform(String binaryName);

  MinecraftBootstrapHookTransformationResult transform(
      String binaryName, byte[] originalClassBytes);

  MinecraftBootstrapHookTransformationResult currentResult();
}
