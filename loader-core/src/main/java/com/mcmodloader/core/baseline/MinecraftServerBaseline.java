package com.mcmodloader.core.baseline;

public record MinecraftServerBaseline(
    int schema,
    String projectTargetMinecraft,
    String baselineMinecraft,
    com.mcmodloader.core.minecraft.MinecraftVersionSelection versionSelection,
    Metadata metadata,
    ServerArtifact serverArtifact,
    Launch launch,
    OfflineReplay offlineReplay,
    ModIntegration modIntegration
) {
    public record Metadata(String manifestPath, String versionJsonPath, String manifestSha256, String versionJsonSha256) {
    }

    public record ServerArtifact(
        String path,
        String sourceUrl,
        String sha1,
        String sha256,
        Long size,
        boolean verified
    ) {
    }

    public record Launch(
        boolean attempted,
        String resultPath,
        boolean started,
        Boolean readyDetected,
        Integer exitCode,
        boolean timedOut
    ) {
    }

    public record OfflineReplay(boolean attempted, String resultPath, boolean succeeded, int networkCalls) {
    }

    public record ModIntegration(
        boolean modClassLoaderCreated,
        boolean entrypointsInvoked,
        boolean modJarsOnMinecraftClasspath
    ) {
    }
}
