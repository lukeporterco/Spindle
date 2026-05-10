package com.spindle.core.security.trust;

public enum ArtifactTrustTier {
  LOCAL_ONLY("local-only"),
  HASH_LOCKED("hash-locked"),
  PUBLISHER_SIGNED("publisher-signed"),
  INVALID_CLAIM("invalid-claim");

  private final String id;

  ArtifactTrustTier(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
