package com.spindle.core.minecraft.hook.steelhook;

public record SteelHook02TargetClassBytes(
    String classEntryName,
    String sourcePath,
    String sourceKind,
    String classSha256,
    byte[] classBytes,
    boolean present,
    boolean readable,
    String failureReason) {
  public SteelHook02TargetClassBytes {
    classBytes = classBytes == null ? null : classBytes.clone();
  }

  @Override
  public byte[] classBytes() {
    return classBytes == null ? null : classBytes.clone();
  }
}
