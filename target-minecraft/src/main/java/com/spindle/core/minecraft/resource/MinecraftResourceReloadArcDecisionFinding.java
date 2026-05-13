package com.spindle.core.minecraft.resource;

public record MinecraftResourceReloadArcDecisionFinding(
    String id,
    String sourceMilestoneName,
    String summary,
    boolean implementationReady,
    boolean recommendedForImmediateImplementation,
    boolean requiresFutureWork,
    String notes) {}
