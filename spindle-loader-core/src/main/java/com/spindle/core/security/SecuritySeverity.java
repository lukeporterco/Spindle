package com.spindle.core.security;

public enum SecuritySeverity {
  FATAL("fatal", 0),
  WARNING("warning", 1);

  private final String id;
  private final int sortOrder;

  SecuritySeverity(String id, int sortOrder) {
    this.id = id;
    this.sortOrder = sortOrder;
  }

  public String id() {
    return id;
  }

  public int sortOrder() {
    return sortOrder;
  }
}
