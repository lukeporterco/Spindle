package com.mcmodloader.core.minecraft;

import java.util.List;
import java.util.Optional;

public record MinecraftVersionManifest(String latestRelease, String latestSnapshot, List<VersionEntry> versions) {
    public MinecraftVersionManifest {
        versions = List.copyOf(versions);
    }

    public Optional<VersionEntry> findVersion(String id) {
        return versions.stream().filter(version -> version.id().equals(id)).findFirst();
    }

    public record VersionEntry(String id, String type, String url, String sha1, String releaseTime, String time) {
    }
}
