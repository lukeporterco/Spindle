package com.mcmodloader.core.artifact;

import java.util.List;

public record MinecraftArtifactCacheReport(
    int schema,
    String projectTargetMinecraft,
    String minecraftVersion,
    String baselineMinecraft,
    String cacheDirectory,
    boolean offline,
    boolean inspectOnly,
    boolean repair,
    boolean forceRedownload,
    int networkRequestCount,
    List<MinecraftArtifactRecord> artifacts,
    List<String> warnings
) {
    public MinecraftArtifactCacheReport {
        artifacts = List.copyOf(artifacts);
        warnings = List.copyOf(warnings);
    }

    public MinecraftArtifactCacheReport(
        int schema,
        String minecraftVersion,
        String cacheDirectory,
        boolean offline,
        boolean inspectOnly,
        boolean repair,
        boolean forceRedownload,
        List<MinecraftArtifactRecord> artifacts,
        List<String> warnings
    ) {
        this(schema, "26.1.2", minecraftVersion, null, cacheDirectory, offline, inspectOnly, repair, forceRedownload, 0, artifacts, warnings);
    }
}
