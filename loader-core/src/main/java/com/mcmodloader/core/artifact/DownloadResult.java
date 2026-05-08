package com.mcmodloader.core.artifact;

public record DownloadResult(
    String sourceUrl,
    long bytesWritten,
    long durationMs,
    boolean downloaded,
    String sha256,
    long size,
    boolean verified
) {
}
