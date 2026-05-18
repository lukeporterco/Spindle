package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationMode;

public record SteelHook03RuntimeTransformSpec(
    SteelHook03PrimitiveKind primitiveKind,
    String sourceMilestone,
    String targetBinaryName,
    String targetInternalName,
    String targetClassEntryName,
    String targetMethodName,
    String targetDescriptor,
    String dispatcherOwnerInternalName,
    String dispatcherBinaryName,
    String dispatcherMethodName,
    String dispatcherDescriptor,
    String opcodeMnemonic,
    String opcodeHex,
    int instructionLength,
    Integer insertionOffset,
    boolean stackMapTableRewriteSupported,
    boolean runtimeClassLoadingPathEnabled,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed) {

  static SteelHook03RuntimeTransformSpec methodEntryStaticDispatch() {
    return new SteelHook03RuntimeTransformSpec(
        SteelHook03PrimitiveKind.METHOD_ENTRY_STATIC_DISPATCH,
        "Target-28",
        "net.minecraft.server.Main",
        "net/minecraft/server/Main",
        "net/minecraft/server/Main.class",
        "main",
        "([Ljava/lang/String;)V",
        "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
        "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher",
        "beforeMinecraftServerMain",
        "()V",
        "invokestatic",
        "b8",
        3,
        0,
        true,
        true,
        false,
        false);
  }

  static SteelHook03RuntimeTransformSpec methodExitStaticDispatch() {
    return new SteelHook03RuntimeTransformSpec(
        SteelHook03PrimitiveKind.METHOD_EXIT_STATIC_DISPATCH,
        "Target-29",
        "net.minecraft.server.Main",
        "net/minecraft/server/Main",
        "net/minecraft/server/Main.class",
        "main",
        "([Ljava/lang/String;)V",
        "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
        "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher",
        "afterMinecraftServerMain",
        "()V",
        "invokestatic",
        "b8",
        3,
        null,
        false,
        true,
        false,
        false);
  }

  MinecraftBootstrapHookTransformationMode transformationMode() {
    if (primitiveKind == null) {
      return null;
    }
    return switch (primitiveKind) {
      case METHOD_ENTRY_STATIC_DISPATCH ->
          MinecraftBootstrapHookTransformationMode
              .STEELHOOK_0_3_GATED_RUNTIME_METHOD_ENTRY_TRANSFORM;
      case METHOD_EXIT_STATIC_DISPATCH ->
          MinecraftBootstrapHookTransformationMode
              .STEELHOOK_0_3_GATED_RUNTIME_METHOD_EXIT_TRANSFORM;
    };
  }
}
