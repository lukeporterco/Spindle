package com.mcmodloader.core.minecraft;

import java.util.List;

public record MinecraftPreflightResult(
    int schema,
    String milestoneName,
    String minecraftVersion,
    boolean runtimePlanWritten,
    boolean boundaryReportWritten,
    boolean integrationPlanWritten,
    boolean launchAttempted,
    boolean modClassesLoaded,
    boolean entrypointsInvoked,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean remappingOccurred,
    boolean mixinOccurred,
    boolean patchingOccurred,
    List<String> reportsWritten,
    List<MinecraftBoundaryViolation> issues,
    boolean succeeded
) {
    public MinecraftPreflightResult {
        reportsWritten = List.copyOf(reportsWritten);
        issues = List.copyOf(issues);
    }
}
