package com.spindle.core.baseline;

import com.spindle.core.artifact.MinecraftArtifactResolver;
import com.spindle.core.minecraft.MinecraftDryRunResult;
import com.spindle.core.process.MinecraftProcessResult;
import java.nio.file.Path;

public record MinecraftServerBaselineResult(
    MinecraftServerBaselineMode mode,
    MinecraftServerBaseline baseline,
    MinecraftArtifactResolver.Resolution artifactResolution,
    MinecraftDryRunResult dryRunResult,
    MinecraftProcessResult processResult,
    Path baselineReportPath) {}
