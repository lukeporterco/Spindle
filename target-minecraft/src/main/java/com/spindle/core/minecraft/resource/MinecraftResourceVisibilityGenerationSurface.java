package com.spindle.core.minecraft.resource;

public record MinecraftResourceVisibilityGenerationSurface(
    String boundaryId,
    String displayName,
    int order,
    MinecraftResourceVisibilityGenerationLane lane,
    boolean runtimeFacing,
    boolean offlineGenerationFacing,
    boolean availableInTarget19,
    boolean requiresRuntimeReloadTiming,
    boolean requiresRuntimeResourceVisibilityDesign,
    boolean requiresOfflineGenerationDesign,
    boolean requiresBindingRequirements,
    boolean requiresFutureSteelHookPrimitive,
    boolean implementationReady,
    MinecraftResourceReloadBoundaryStatus sourceBoundaryStatus,
    String notes) {}
