package com.mcmodloader.core.minecraft;

import java.nio.file.Path;

public final class MinecraftRuntimeCacheLayout {
    private final Path cacheDirectory;
    private final String version;

    public MinecraftRuntimeCacheLayout(Path cacheDirectory, String version) {
        this.cacheDirectory = cacheDirectory.toAbsolutePath().normalize();
        this.version = version;
    }

    public Path runtimeDirectory() {
        return cacheDirectory.resolve("versions").resolve(version).resolve("server-runtime").toAbsolutePath().normalize();
    }

    public Path libraryPath(String relativePath) {
        return runtimeDirectory().resolve("libraries").resolve(relativePath).toAbsolutePath().normalize();
    }

    public Path versionPath(String relativePath) {
        return runtimeDirectory().resolve("versions").resolve(relativePath).toAbsolutePath().normalize();
    }

    public boolean owns(Path path) {
        return path.toAbsolutePath().normalize().startsWith(runtimeDirectory());
    }
}
