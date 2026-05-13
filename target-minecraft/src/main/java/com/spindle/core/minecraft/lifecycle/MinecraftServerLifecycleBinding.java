package com.spindle.core.minecraft.lifecycle;

public record MinecraftServerLifecycleBinding(
    String id,
    String phaseId,
    String displayName,
    MinecraftServerLifecycleBindingStatus status,
    boolean supportedInThisPass,
    String boundContractId,
    String ownerInternalName,
    String memberName,
    String descriptor,
    String bindingKind,
    String notes) {}
