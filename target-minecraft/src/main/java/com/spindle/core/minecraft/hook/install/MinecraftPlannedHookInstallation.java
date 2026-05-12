package com.spindle.core.minecraft.hook.install;

public record MinecraftPlannedHookInstallation(
    String id,
    String sourceContractId,
    String catalogId,
    String kind,
    String ownerInternalName,
    String memberName,
    String descriptor,
    boolean required,
    MinecraftHookInstallationMode mode) {}
