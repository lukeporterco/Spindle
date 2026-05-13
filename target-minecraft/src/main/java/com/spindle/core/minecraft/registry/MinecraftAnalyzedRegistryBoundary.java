package com.spindle.core.minecraft.registry;

public record MinecraftAnalyzedRegistryBoundary(
    String boundaryId,
    String displayName,
    int order,
    MinecraftRegistryBoundaryStatus status,
    MinecraftRegistryRepresentationKind representationKind,
    String notes) {}
