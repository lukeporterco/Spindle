package com.spindle.core.minecraft.hook.install;

import java.util.List;

public record MinecraftHookInstallationResult(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    String side,
    String minecraftMainClass,
    MinecraftHookInstallationMode installationMode,
    boolean hookInstallationOccurred,
    boolean hookInvocationOccurred,
    boolean minecraftMainClassLoaded,
    boolean minecraftMainInvoked,
    int installedHookCount,
    int invokedHookCount,
    int failedHookCount,
    MinecraftHookInstallationStatus status,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean bytecodeModified,
    boolean javaAgentUsed,
    boolean mixinUsed,
    boolean remappingOccurred,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed,
    List<MinecraftInstalledHookInvocation> installedHooks,
    String failureCategory,
    String failureMessage,
    List<String> failureDetails) {
  public MinecraftHookInstallationResult {
    installedHooks = List.copyOf(installedHooks == null ? List.of() : installedHooks);
    failureDetails = List.copyOf(failureDetails == null ? List.of() : failureDetails);
  }
}
