package com.spindle.core.minecraft;

public record MinecraftExecutionPolicy(
    boolean strictExecution,
    boolean denyLoaderInternals,
    boolean verifyPlanFingerprints,
    boolean offlineBootstrap,
    boolean bootstrapFakeServer) {}
