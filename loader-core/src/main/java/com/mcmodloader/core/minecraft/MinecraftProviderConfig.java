package com.mcmodloader.core.minecraft;

import java.nio.file.Path;

public record MinecraftProviderConfig(
    String requestedVersion,
    Path minecraftDirectory,
    Path explicitVersionJson,
    Path manifestJson,
    MinecraftSide side,
    boolean dryRun,
    boolean verifyFiles,
    boolean fetchMetadata,
    Path outputPlanPath
) {
    public MinecraftProviderConfig {
        side = side == null ? MinecraftSide.CLIENT : side;
        outputPlanPath = outputPlanPath == null ? Path.of("minecraft-launch-plan.json") : outputPlanPath.normalize();
    }

    public MinecraftProviderConfig resolveAgainst(Path workingDirectory) {
        return new MinecraftProviderConfig(
            requestedVersion,
            resolvePath(workingDirectory, minecraftDirectory),
            resolveNullablePath(workingDirectory, explicitVersionJson),
            resolveNullablePath(workingDirectory, manifestJson),
            side,
            dryRun,
            verifyFiles,
            fetchMetadata,
            resolvePath(workingDirectory, outputPlanPath)
        );
    }

    private static Path resolveNullablePath(Path workingDirectory, Path path) {
        return path == null ? null : resolvePath(workingDirectory, path);
    }

    private static Path resolvePath(Path workingDirectory, Path path) {
        if (path == null) {
            return null;
        }
        if (path.isAbsolute()) {
            return path.toAbsolutePath().normalize();
        }
        return workingDirectory.resolve(path).toAbsolutePath().normalize();
    }
}
