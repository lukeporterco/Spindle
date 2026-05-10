package com.spindle.core.security.trust;

public enum ArtifactProvenanceState {
  NOT_PRESENT("not-present"),
  PRESENT("present"),
  CLAIM_INVALID("claim-invalid");

  private final String id;

  ArtifactProvenanceState(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
