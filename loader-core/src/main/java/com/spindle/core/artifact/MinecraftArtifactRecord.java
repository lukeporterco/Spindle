package com.spindle.core.artifact;

import java.nio.file.Path;

public record MinecraftArtifactRecord(
    String id,
    ArtifactKind kind,
    Path path,
    String sourceUrl,
    String sha1,
    String sha256,
    Long size,
    boolean present,
    boolean downloaded,
    boolean verified,
    ArtifactStatus status) {
  public MinecraftArtifactRecord {
    path = path.toAbsolutePath().normalize();
  }

  public MinecraftArtifactRecord withVerification(
      String actualSha256,
      Long actualSize,
      boolean artifactVerified,
      ArtifactStatus artifactStatus) {
    return new MinecraftArtifactRecord(
        id,
        kind,
        path,
        sourceUrl,
        sha1,
        actualSha256,
        actualSize,
        present,
        downloaded,
        artifactVerified,
        artifactStatus);
  }

  public MinecraftArtifactRecord withPresence(
      boolean artifactPresent, ArtifactStatus artifactStatus) {
    return new MinecraftArtifactRecord(
        id,
        kind,
        path,
        sourceUrl,
        sha1,
        sha256,
        size,
        artifactPresent,
        downloaded,
        verified,
        artifactStatus);
  }

  public MinecraftArtifactRecord withDownload(boolean artifactDownloaded) {
    return new MinecraftArtifactRecord(
        id,
        kind,
        path,
        sourceUrl,
        sha1,
        sha256,
        size,
        present,
        artifactDownloaded,
        verified,
        status);
  }
}
