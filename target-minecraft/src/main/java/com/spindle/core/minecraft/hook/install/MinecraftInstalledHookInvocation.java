package com.spindle.core.minecraft.hook.install;

public record MinecraftInstalledHookInvocation(
    String id,
    String sourceContractId,
    String ownerInternalName,
    String memberName,
    String descriptor,
    MinecraftHookInstallationMode mode,
    boolean installed,
    boolean invoked,
    String failureMessage) {}
