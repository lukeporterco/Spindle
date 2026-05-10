package com.spindle.core.runtime.capability;

public enum RuntimeCapabilityState {
  GRANTED("granted"),
  DENIED("denied"),
  UNAVAILABLE("unavailable"),
  UNKNOWN("unknown"),
  VISIBILITY_ONLY("visibility-only");

  private final String id;

  RuntimeCapabilityState(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
