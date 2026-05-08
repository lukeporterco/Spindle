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
    Path outputPlanPath,
    boolean launch,
    Path serverDirectory,
    boolean acceptEulaForTest,
    java.util.List<String> serverJvmArgs,
    java.util.List<String> serverArgs,
    int launchTimeoutSeconds,
    boolean stopAfterReady,
    int readyTimeoutSeconds
) {
    public MinecraftProviderConfig {
        side = side == null ? MinecraftSide.CLIENT : side;
        outputPlanPath = outputPlanPath == null ? Path.of("minecraft-launch-plan.json") : outputPlanPath.normalize();
        serverJvmArgs = java.util.List.copyOf(serverJvmArgs == null ? java.util.List.of() : serverJvmArgs);
        serverArgs = java.util.List.copyOf(serverArgs == null ? java.util.List.of() : serverArgs);
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
            resolvePath(workingDirectory, outputPlanPath),
            launch,
            resolveNullablePath(workingDirectory, serverDirectory),
            acceptEulaForTest,
            serverJvmArgs,
            serverArgs,
            launchTimeoutSeconds,
            stopAfterReady,
            readyTimeoutSeconds
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
