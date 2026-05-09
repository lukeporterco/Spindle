package com.spindle.core.security.trust;

import java.util.List;

public record ArtifactTrustSummary(
    int localUnsignedCount,
    int lockedHashCount,
    int signedArtifactCount,
    int invalidSignatureCount) {
  public static ArtifactTrustSummary from(List<ArtifactTrustEntry> entries) {
    int localUnsignedCount = 0;
    int lockedHashCount = 0;
    int signedArtifactCount = 0;
    int invalidSignatureCount = 0;
    for (ArtifactTrustEntry entry : entries) {
      switch (entry.trustState()) {
        case LOCAL_UNSIGNED -> localUnsignedCount++;
        case LOCKED_HASH -> lockedHashCount++;
        case SIGNED_ARTIFACT -> signedArtifactCount++;
        case SIGNATURE_SIDECAR_INVALID, SIGNATURE_ARTIFACT_HASH_MISMATCH, SIGNATURE_INVALID ->
            invalidSignatureCount++;
      }
    }
    return new ArtifactTrustSummary(
        localUnsignedCount, lockedHashCount, signedArtifactCount, invalidSignatureCount);
  }
}
