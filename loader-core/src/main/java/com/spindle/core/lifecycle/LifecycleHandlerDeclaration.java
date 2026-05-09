package com.spindle.core.lifecycle;

public record LifecycleHandlerDeclaration(
    String phase,
    String modId,
    String ownerModId,
    String kind,
    String className,
    String methodName,
    String interfaceName,
    String jarPath,
    String jarHash) {
  public static final String KIND_STATIC_METHOD = "static-method";
  public static final String KIND_LEGACY_MOD_INITIALIZER = "legacy-mod-initializer";
}
