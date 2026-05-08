package com.mcmodloader.core.baseline;

import com.mcmodloader.core.artifact.MinecraftArtifactResolver;
import com.mcmodloader.core.minecraft.MinecraftDryRunResult;
import com.mcmodloader.core.process.MinecraftProcessResult;
import java.nio.file.Path;

public record MinecraftServerBaselineResult(
    MinecraftServerBaselineMode mode,
    MinecraftServerBaseline baseline,
    MinecraftArtifactResolver.Resolution artifactResolution,
    MinecraftDryRunResult dryRunResult,
    MinecraftProcessResult processResult,
    Path baselineReportPath
) {
}
