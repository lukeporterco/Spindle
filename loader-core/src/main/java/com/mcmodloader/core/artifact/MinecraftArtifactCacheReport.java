package com.mcmodloader.core.artifact;

import java.util.List;

public record MinecraftArtifactCacheReport(
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
    public MinecraftArtifactCacheReport {
        artifacts = List.copyOf(artifacts);
        warnings = List.copyOf(warnings);
    }
}
