package com.spindle.core.security.trust;

public enum ArtifactTrustState {
  LOCAL_UNSIGNED(
      "local-unsigned", ArtifactTrustTier.LOCAL_ONLY, ArtifactProvenanceState.NOT_PRESENT),
  LOCKED_HASH("locked-hash", ArtifactTrustTier.HASH_LOCKED, ArtifactProvenanceState.NOT_PRESENT),
  SIGNED_ARTIFACT(
      "signed-artifact", ArtifactTrustTier.PUBLISHER_SIGNED, ArtifactProvenanceState.PRESENT),
  SIGNATURE_SIDECAR_INVALID(
      "signature-sidecar-invalid",
      ArtifactTrustTier.INVALID_CLAIM,
      ArtifactProvenanceState.CLAIM_INVALID),
  SIGNATURE_ARTIFACT_HASH_MISMATCH(
      "signature-artifact-hash-mismatch",
      ArtifactTrustTier.INVALID_CLAIM,
      ArtifactProvenanceState.CLAIM_INVALID),
  SIGNATURE_INVALID(
      "signature-invalid", ArtifactTrustTier.INVALID_CLAIM, ArtifactProvenanceState.CLAIM_INVALID);

  private final String id;
  private final ArtifactTrustTier trustTier;
  private final ArtifactProvenanceState provenanceState;

  ArtifactTrustState(
      String id, ArtifactTrustTier trustTier, ArtifactProvenanceState provenanceState) {
    this.id = id;
    this.trustTier = trustTier;
    this.provenanceState = provenanceState;
  }

  public String id() {
    return id;
  }

  public ArtifactTrustTier trustTier() {
    return trustTier;
  }

  public ArtifactProvenanceState provenanceState() {
    return provenanceState;
  }

  public boolean isInvalidClaim() {
    return trustTier == ArtifactTrustTier.INVALID_CLAIM;
  }
}
