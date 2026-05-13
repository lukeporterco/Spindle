package com.spindle.core.minecraft.lifecycle;

public record MinecraftPlannedServerLifecycleDispatch(
    String id,
    String phaseId,
    String displayName,
    String sourceBindingId,
    String sourceContractId,
    MinecraftServerLifecycleDispatchStatus status,
    MinecraftServerLifecycleDispatchMode mode,
    String dispatchTiming,
    String dispatcherOwnerInternalName,
    String dispatcherMethodName,
    String dispatcherDescriptor,
    boolean cancellable,
    boolean allowsResultReplacement,
    boolean publicListenerRegistration,
    boolean modCallbackExecution,
    boolean runtimeDispatcherImplemented,
    boolean symbolicOnly,
    String notes) {}
