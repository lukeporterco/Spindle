package com.mcmodloader.core.minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MinecraftModBoundaryScanner {
    public List<MinecraftBoundaryViolation> scan(
        String modId,
        MinecraftJarScanResult modScan,
        MinecraftRuntimeBoundary runtimeBoundary,
        boolean strictRuntimeConflicts
    ) {
        List<MinecraftBoundaryViolation> violations = new ArrayList<>();
        for (String packageName : modScan.packages()) {
            List<String> owners = runtimeBoundary.packageOwnership().getOrDefault(packageName, List.of());
            if (!owners.isEmpty()) {
                violations.add(
                    new MinecraftBoundaryViolation(
                        "mod-runtime-split-package",
                        strictRuntimeConflicts ? MinecraftBoundarySeverity.FATAL : MinecraftBoundarySeverity.WARNING,
                        packageName,
                        modId,
                        String.join(",", owners),
                        strictRuntimeConflicts,
                        true,
                        "Mod package collides with Minecraft runtime package."
                    )
                );
            }
            if (packageName.startsWith("com.mcmodloader.core") || packageName.startsWith("com.mcmodloader.api")) {
                violations.add(
                    new MinecraftBoundaryViolation(
                        "mod-loader-package",
                        MinecraftBoundarySeverity.FATAL,
                        packageName,
                        modId,
                        "loader",
                        true,
                        true,
                        "Mod must not define loader core or loader API packages."
                    )
                );
            }
        }
        for (String resource : modScan.resources()) {
            List<String> owners = runtimeBoundary.resourceOwnership().getOrDefault(resource, List.of());
            if (!owners.isEmpty()) {
                violations.add(
                    new MinecraftBoundaryViolation(
                        "mod-runtime-duplicate-resource",
                        strictRuntimeConflicts ? MinecraftBoundarySeverity.FATAL : MinecraftBoundarySeverity.WARNING,
                        resource,
                        modId,
                        String.join(",", owners),
                        strictRuntimeConflicts,
                        true,
                        "Mod resource duplicates a Minecraft runtime resource."
                    )
                );
            }
        }
        for (Map.Entry<String, List<String>> duplicate : modScan.duplicateEntries().entrySet()) {
            violations.add(
                new MinecraftBoundaryViolation(
                    "mod-duplicate-jar-entry",
                    MinecraftBoundarySeverity.ERROR,
                    duplicate.getKey(),
                    modId,
                    null,
                    true,
                    true,
                    "Mod jar contains duplicate entries."
                )
            );
        }
        for (String path : modScan.suspiciousPaths()) {
            violations.add(
                new MinecraftBoundaryViolation(
                    "mod-suspicious-path",
                    MinecraftBoundarySeverity.FATAL,
                    path,
                    modId,
                    null,
                    true,
                    true,
                    "Mod jar contains an unsafe absolute or traversal path."
                )
            );
        }
        return List.copyOf(violations);
    }
}
