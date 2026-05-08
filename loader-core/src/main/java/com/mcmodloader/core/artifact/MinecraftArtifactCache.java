package com.mcmodloader.core.artifact;

import java.nio.file.Path;

public final class MinecraftArtifactCache {
    private final Path workingDirectory;
    private final Path cacheDirectory;

    public MinecraftArtifactCache(Path workingDirectory, Path cacheDirectory) {
        this.workingDirectory = workingDirectory.toAbsolutePath().normalize();
        this.cacheDirectory = cacheDirectory.toAbsolutePath().normalize();
    }

    public Path workingDirectory() {
        return workingDirectory;
    }

    public Path cacheDirectory() {
        return cacheDirectory;
    }

    public Path manifestPath() {
        return cacheDirectory.resolve("metadata/version-manifest.json").toAbsolutePath().normalize();
    }

    public Path versionJsonPath(String version) {
        return cacheDirectory.resolve("metadata/versions").resolve(version + ".json").toAbsolutePath().normalize();
    }

    public Path serverJarPath(String version) {
        return cacheDirectory.resolve("versions").resolve(version).resolve(version + "-server.jar").toAbsolutePath().normalize();
    }

    public Path tmpDirectory() {
        return cacheDirectory.resolve("tmp").toAbsolutePath().normalize();
    }

    public Path artifactLockPath(String version) {
        return cacheDirectory.resolve("versions").resolve(version).resolve("server-artifacts.lock.json").toAbsolutePath().normalize();
    }

    public Path artifactReportPath() {
        return workingDirectory.resolve("minecraft-artifacts.json").toAbsolutePath().normalize();
    }

    public String displayPath(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        try {
            return workingDirectory.relativize(normalized).toString().replace('\\', '/');
        } catch (IllegalArgumentException ignored) {
            return normalized.toString().replace('\\', '/');
        }
    }
}
