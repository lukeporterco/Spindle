package com.spindle.core.minecraft.resource;

public record MinecraftAnalyzedResourceReloadBoundary(
    String boundaryId,
    String displayName,
    int order,
    MinecraftResourceReloadBoundaryStatus status,
    MinecraftResourceReloadRepresentationKind representationKind,
    boolean available,
    String sourceLifecyclePhaseId,
    String sourceLifecycleDispatchId,
    boolean requiresSymbolDiscovery,
    boolean requiresBindingStrategyAnalysis,
    boolean requiresRuntimeResourceAccess,
    boolean requiresOfflineGenerationDesign,
    String notes) {}
