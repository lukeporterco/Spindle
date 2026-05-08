package com.mcmodloader.core.discovery;

import com.mcmodloader.core.metadata.ModMetadata;
import java.nio.file.Path;

public record ModCandidate(Path jarPath, Path relativePath, String sha256, ModMetadata metadata) {
    public ModCandidate(Path jarPath, Path relativePath, String sha256) {
        this(jarPath, relativePath, sha256, null);
    }

    public ModCandidate withMetadata(ModMetadata modMetadata) {
        return new ModCandidate(jarPath, relativePath, sha256, modMetadata);
    }

    public String normalizedRelativePath() {
        return relativePath.toString().replace('\\', '/');
    }
}
