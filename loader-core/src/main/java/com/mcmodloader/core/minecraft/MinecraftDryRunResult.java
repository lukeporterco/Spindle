package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.mache.MacheReferenceReport;
import java.nio.file.Path;

public record MinecraftDryRunResult(
    MinecraftLaunchPlan launchPlan,
    MacheReferenceReport macheReferenceReport,
    Path serverJarPath,
    String serverJarSource
) {
}
