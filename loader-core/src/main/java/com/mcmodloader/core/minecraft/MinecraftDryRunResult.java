package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.artifact.MinecraftArtifactResolver;
import com.mcmodloader.core.mache.MacheReferenceReport;
import java.nio.file.Path;

public record MinecraftDryRunResult(
    MinecraftLaunchPlan launchPlan,
    MacheReferenceReport macheReferenceReport,
    Path serverJarPath,
    String serverJarSource,
    MinecraftArtifactResolver.Resolution artifactResolution,
    MinecraftServerRuntimePlanner.PlannedRuntime plannedRuntime,
    MinecraftRuntimeBoundary runtimeBoundary,
    MinecraftModIntegrationPlan integrationPlan
) {
    public MinecraftDryRunResult(
        MinecraftLaunchPlan launchPlan,
        MacheReferenceReport macheReferenceReport,
        Path serverJarPath,
        String serverJarSource,
        MinecraftArtifactResolver.Resolution artifactResolution
    ) {
        this(launchPlan, macheReferenceReport, serverJarPath, serverJarSource, artifactResolution, null, null, null);
    }
}
