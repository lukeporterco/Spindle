package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.discovery.ModCandidate;
import com.mcmodloader.core.metadata.ModMetadata;

public record MinecraftModCandidate(ModCandidate source, ModMetadata metadata, MinecraftJarScanResult scan) {
    public String id() {
        return metadata == null ? source.normalizedRelativePath() : metadata.id();
    }
}
