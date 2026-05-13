package com.spindle.core.minecraft.command;

public record MinecraftAnalyzedCommandRegistrationBoundary(
    String id,
    String boundaryId,
    String displayName,
    MinecraftCommandRegistrationBoundaryStatus status,
    MinecraftCommandRegistrationRepresentationKind representationKind,
    String upstreamConceptId,
    String upstreamDispatchId,
    String sourceLifecyclePhaseId,
    boolean minecraftSymbolKnown,
    String ownerInternalName,
    String memberName,
    String descriptor,
    boolean requiresFutureMinecraftSymbol,
    boolean requiresFutureSteelHookPrimitive,
    boolean implementedInThisPass,
    boolean analysisOnly,
    String notes) {}
