package com.mcmodloader.core.minecraft;

import java.nio.file.Path;

public record MinecraftRuntimeFile(
    String id,
    Path path,
    String relativeCachePath,
    String origin,
    String sha1,
    String sha256,
    long size,
    boolean present,
    boolean verified,
    String verificationStatus
) {
    public MinecraftRuntimeFile {
        path = path == null ? null : path.toAbsolutePath().normalize();
    }
}
