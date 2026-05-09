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
    boolean reproducibilityCheck,
    boolean executionPlan,
    boolean bootstrapClassloaderGraph,
    boolean bootstrapServer,
    boolean strictExecution,
    boolean denyLoaderInternals,
    boolean verifyPlanFingerprints,
    boolean bootstrapOffline,
    boolean bootstrapFakeServer
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
            false, // baselineServer
            null, // baselineVersion
            Path.of("minecraft-server-baseline.json"),
            false, // offlineReplay
            false, // requireReady
            false, // realSmoke
            MinecraftMetadataResolver.DEFAULT_MANIFEST_URL,
            false, // runtimePlan
            false, // planMods
            false, // integrationPlan
            false, // boundaryReport
            false, // preflight
            false, // offlinePreflight
            false, // strictBoundary
            false, // strictRuntimeConflicts
            false, // strictSide
            false, // strictClassVersions
            false, // explainBoundary
            false, // explainRuntime
            false, // explainMods
            false, // reproducibilityCheck
            false, // executionPlan
            false, // bootstrapClassloaderGraph
            false, // bootstrapServer
            false, // strictExecution
            false, // denyLoaderInternals
            false, // verifyPlanFingerprints
            false, // bootstrapOffline
            false // bootstrapFakeServer
        );
    }

    public MinecraftProviderConfig resolveAgainst(Path workingDirectory) {
        return copy(
            requestedVersion,
            resolvePath(workingDirectory, minecraftDirectory),
            resolveNullablePath(workingDirectory, explicitVersionJson),
            resolveNullablePath(workingDirectory, manifestJson),
            resolvePath(workingDirectory, cacheDirectory == null ? Path.of("minecraft-cache") : cacheDirectory),
            resolvePath(workingDirectory, outputPlanPath),
            resolveNullablePath(workingDirectory, serverDirectory),
            resolvePath(workingDirectory, baselineReportPath)
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
        return copy(requestedVersion, updatedMinecraftDirectory, explicitVersionJson, manifestJson, cacheDirectory, outputPlanPath, serverDirectory, baselineReportPath);
    }

    public MinecraftProviderConfig withRequestedVersion(String updatedRequestedVersion) {
        return copy(updatedRequestedVersion, minecraftDirectory, explicitVersionJson, manifestJson, cacheDirectory, outputPlanPath, serverDirectory, baselineReportPath);
    }

    public MinecraftProviderConfig withServerDirectory(Path updatedServerDirectory) {
        return copy(requestedVersion, minecraftDirectory, explicitVersionJson, manifestJson, cacheDirectory, outputPlanPath, updatedServerDirectory, baselineReportPath);
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
            reproducibilityCheck,
            executionPlan,
            bootstrapClassloaderGraph,
            bootstrapServer,
            strictExecution,
            denyLoaderInternals,
            verifyPlanFingerprints,
            bootstrapOffline,
            bootstrapFakeServer
        );
    }

    private MinecraftProviderConfig copy(
        String updatedRequestedVersion,
        Path updatedMinecraftDirectory,
        Path updatedExplicitVersionJson,
        Path updatedManifestJson,
        Path updatedCacheDirectory,
        Path updatedOutputPlanPath,
        Path updatedServerDirectory,
        Path updatedBaselineReportPath
    ) {
        return new MinecraftProviderConfig(
            updatedRequestedVersion,
            updatedMinecraftDirectory,
            updatedExplicitVersionJson,
            updatedManifestJson,
            side,
            dryRun,
            verifyFiles,
            fetchMetadata,
            downloadServer,
            updatedCacheDirectory,
            offline,
            cacheInspect,
            cacheRepair,
            cacheStrict,
            forceRedownload,
            updatedOutputPlanPath,
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
            updatedBaselineReportPath,
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
            reproducibilityCheck,
            executionPlan,
            bootstrapClassloaderGraph,
            bootstrapServer,
            strictExecution,
            denyLoaderInternals,
            verifyPlanFingerprints,
            bootstrapOffline,
            bootstrapFakeServer
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
