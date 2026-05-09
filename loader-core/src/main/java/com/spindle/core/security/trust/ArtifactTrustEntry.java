package com.spindle.core.security.trust;

public record ArtifactTrustEntry(
    String modId,
    String version,
    String path,
    String sha256,
    ArtifactTrustState trustState,
    String signerId,
    String signatureKind) {
  public ArtifactTrustEntry {
    modId = normalize(modId);
    version = normalize(version);
    path = normalize(path);
    sha256 = normalize(sha256);
    signerId = normalize(signerId);
    signatureKind = normalize(signatureKind);
  }

  public ArtifactTrustTier trustTier() {
    return trustState.trustTier();
  }

  public ArtifactProvenanceState provenanceState() {
    return trustState.provenanceState();
  }

  private static String normalize(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim().replace('\\', '/');
    return normalized.isEmpty() ? null : normalized;
  }
}
