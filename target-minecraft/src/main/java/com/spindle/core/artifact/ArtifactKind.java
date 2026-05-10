package com.spindle.core.artifact;

public enum ArtifactKind {
  METADATA("metadata"),
  SERVER("server");

  private final String id;

  ArtifactKind(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
