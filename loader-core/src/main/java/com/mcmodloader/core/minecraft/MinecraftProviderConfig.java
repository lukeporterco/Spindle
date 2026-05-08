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
    boolean downloadServer,
    Path cacheDirectory,
    boolean offline,
    boolean cacheInspect,
    boolean cacheRepair,
    boolean cacheStrict,
    boolean forceRedownload,
    Path outputPlanPath,
    boolean launch,
    Path serverDirectory,
    boolean acceptEulaForTest,
    java.util.List<String> serverJvmArgs,
    java.util.List<String> serverArgs,
    int launchTimeoutSeconds,
    boolean stopAfterReady,
    int readyTimeoutSeconds,
    boolean baselineServer,
    String baselineVersion,
    Path baselineReportPath,
    boolean offlineReplay,
    boolean requireReady,
    boolean realSmoke,
    String manifestUrl,
    boolean runtimePlan,
    boolean planMods,
    boolean integrationPlan,
    boolean boundaryReport,
    boolean preflight,
    boolean offlinePreflight,
    boolean strictBoundary,
    boolean strictRuntimeConflicts,
    boolean strictSide,
    boolean strictClassVersions,
    boolean explainBoundary,
    boolean explainRuntime,
    boolean explainMods,
    boolean reproducibilityCheck
) {
    public MinecraftProviderConfig {
        side = side == null ? MinecraftSide.CLIENT : side;
        cacheDirectory = cacheDirectory == null ? Path.of("minecraft-cache") : cacheDirectory.normalize();
        outputPlanPath = outputPlanPath == null ? Path.of("minecraft-launch-plan.json") : outputPlanPath.normalize();
        baselineReportPath = baselineReportPath == null ? Path.of("minecraft-server-baseline.json") : baselineReportPath.normalize();
        serverJvmArgs = java.util.List.copyOf(serverJvmArgs == null ? java.util.List.of() : serverJvmArgs);
        serverArgs = java.util.List.copyOf(serverArgs == null ? java.util.List.of() : serverArgs);
    }

    public MinecraftProviderConfig(
        String requestedVersion,
        Path minecraftDirectory,
        Path explicitVersionJson,
        Path manifestJson,
        MinecraftSide side,
        boolean dryRun,
        boolean verifyFiles,
        boolean fetchMetadata,
        boolean downloadServer,
        Path cacheDirectory,
        boolean offline,
        boolean cacheInspect,
        boolean cacheRepair,
        boolean cacheStrict,
        boolean forceRedownload,
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
        this(
            requestedVersion,
            minecraftDirectory,
            explicitVersionJson,
            manifestJson,
            side,
            dryRun,
            verifyFiles,
            fetchMetadata,
            downloadServer,
            cacheDirectory,
            offline,
            cacheInspect,
            cacheRepair,
            cacheStrict,
            forceRedownload,
            outputPlanPath,
            launch,
            serverDirectory,
            acceptEulaForTest,
            serverJvmArgs,
            serverArgs,
            launchTimeoutSeconds,
            stopAfterReady,
            readyTimeoutSeconds,
            false,
            null,
            Path.of("minecraft-server-baseline.json"),
            false,
            false,
            false,
            MinecraftMetadataResolver.DEFAULT_MANIFEST_URL,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false
        );
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
            downloadServer,
            resolvePath(workingDirectory, cacheDirectory == null ? Path.of("minecraft-cache") : cacheDirectory),
            offline,
            cacheInspect,
            cacheRepair,
            cacheStrict,
            forceRedownload,
            resolvePath(workingDirectory, outputPlanPath),
            launch,
            resolveNullablePath(workingDirectory, serverDirectory),
            acceptEulaForTest,
            serverJvmArgs,
            serverArgs,
            launchTimeoutSeconds,
            stopAfterReady,
            readyTimeoutSeconds,
            baselineServer,
            baselineVersion,
            resolvePath(workingDirectory, baselineReportPath),
            offlineReplay,
            requireReady,
            realSmoke,
            manifestUrl,
            runtimePlan,
            planMods,
            integrationPlan,
            boundaryReport,
            preflight,
            offlinePreflight,
            strictBoundary,
            strictRuntimeConflicts,
            strictSide,
            strictClassVersions,
            explainBoundary,
            explainRuntime,
            explainMods,
            reproducibilityCheck
        );
    }

    public boolean prefersCacheOrDownload() {
        return downloadServer || cacheRepair || forceRedownload;
    }

    public boolean baselineServerEnabled() {
        return baselineServer;
    }

    public String requestedVersionOrBaseline() {
        if (baselineServer && baselineVersion != null && !baselineVersion.isBlank()) {
            return baselineVersion;
        }
        return requestedVersion;
    }

    public MinecraftProviderConfig withMinecraftDirectory(Path updatedMinecraftDirectory) {
        return new MinecraftProviderConfig(
            requestedVersion,
            updatedMinecraftDirectory,
            explicitVersionJson,
            manifestJson,
            side,
            dryRun,
            verifyFiles,
            fetchMetadata,
            downloadServer,
            cacheDirectory,
            offline,
            cacheInspect,
            cacheRepair,
            cacheStrict,
            forceRedownload,
            outputPlanPath,
            launch,
            serverDirectory,
            acceptEulaForTest,
            serverJvmArgs,
            serverArgs,
            launchTimeoutSeconds,
            stopAfterReady,
            readyTimeoutSeconds,
            baselineServer,
            baselineVersion,
            baselineReportPath,
            offlineReplay,
            requireReady,
            realSmoke,
            manifestUrl,
            runtimePlan,
            planMods,
            integrationPlan,
            boundaryReport,
            preflight,
            offlinePreflight,
            strictBoundary,
            strictRuntimeConflicts,
            strictSide,
            strictClassVersions,
            explainBoundary,
            explainRuntime,
            explainMods,
            reproducibilityCheck
        );
    }

    public MinecraftProviderConfig withRequestedVersion(String updatedRequestedVersion) {
        return new MinecraftProviderConfig(
            updatedRequestedVersion,
            minecraftDirectory,
            explicitVersionJson,
            manifestJson,
            side,
            dryRun,
            verifyFiles,
            fetchMetadata,
            downloadServer,
            cacheDirectory,
            offline,
            cacheInspect,
            cacheRepair,
            cacheStrict,
            forceRedownload,
            outputPlanPath,
            launch,
            serverDirectory,
            acceptEulaForTest,
            serverJvmArgs,
            serverArgs,
            launchTimeoutSeconds,
            stopAfterReady,
            readyTimeoutSeconds,
            baselineServer,
            baselineVersion,
            baselineReportPath,
            offlineReplay,
            requireReady,
            realSmoke,
            manifestUrl,
            runtimePlan,
            planMods,
            integrationPlan,
            boundaryReport,
            preflight,
            offlinePreflight,
            strictBoundary,
            strictRuntimeConflicts,
            strictSide,
            strictClassVersions,
            explainBoundary,
            explainRuntime,
            explainMods,
            reproducibilityCheck
        );
    }

    public MinecraftProviderConfig withServerDirectory(Path updatedServerDirectory) {
        return new MinecraftProviderConfig(
            requestedVersion,
            minecraftDirectory,
            explicitVersionJson,
            manifestJson,
            side,
            dryRun,
            verifyFiles,
            fetchMetadata,
            downloadServer,
            cacheDirectory,
            offline,
            cacheInspect,
            cacheRepair,
            cacheStrict,
            forceRedownload,
            outputPlanPath,
            launch,
            updatedServerDirectory,
            acceptEulaForTest,
            serverJvmArgs,
            serverArgs,
            launchTimeoutSeconds,
            stopAfterReady,
            readyTimeoutSeconds,
            baselineServer,
            baselineVersion,
            baselineReportPath,
            offlineReplay,
            requireReady,
            realSmoke,
            manifestUrl,
            runtimePlan,
            planMods,
            integrationPlan,
            boundaryReport,
            preflight,
            offlinePreflight,
            strictBoundary,
            strictRuntimeConflicts,
            strictSide,
            strictClassVersions,
            explainBoundary,
            explainRuntime,
            explainMods,
            reproducibilityCheck
        );
    }

    public MinecraftProviderConfig withBaselineVersion(String updatedBaselineVersion) {
        return new MinecraftProviderConfig(
            requestedVersion,
            minecraftDirectory,
            explicitVersionJson,
            manifestJson,
            side,
            dryRun,
            verifyFiles,
            fetchMetadata,
            downloadServer,
            cacheDirectory,
            offline,
            cacheInspect,
            cacheRepair,
            cacheStrict,
            forceRedownload,
            outputPlanPath,
            launch,
            serverDirectory,
            acceptEulaForTest,
            serverJvmArgs,
            serverArgs,
            launchTimeoutSeconds,
            stopAfterReady,
            readyTimeoutSeconds,
            baselineServer,
            updatedBaselineVersion,
            baselineReportPath,
            offlineReplay,
            requireReady,
            realSmoke,
            manifestUrl,
            runtimePlan,
            planMods,
            integrationPlan,
            boundaryReport,
            preflight,
            offlinePreflight,
            strictBoundary,
            strictRuntimeConflicts,
            strictSide,
            strictClassVersions,
            explainBoundary,
            explainRuntime,
            explainMods,
            reproducibilityCheck
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
