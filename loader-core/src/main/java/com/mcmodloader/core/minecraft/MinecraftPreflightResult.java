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
    boolean modJarsOnMinecraftRuntimeClasspath,
    List<String> reportsWritten,
    List<MinecraftModRejection> rejectedMods,
    List<MinecraftBoundaryViolation> issues,
    int acceptedModCount,
    int rejectedModCount,
    int warningCount,
    int fatalCount,
    List<String> failureReasons,
    boolean succeeded
) {
    public MinecraftPreflightResult {
        reportsWritten = List.copyOf(reportsWritten);
        rejectedMods = List.copyOf(rejectedMods);
        issues = List.copyOf(issues);
        failureReasons = List.copyOf(failureReasons);
    }
}
