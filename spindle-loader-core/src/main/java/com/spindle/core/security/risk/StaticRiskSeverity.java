package com.spindle.core.security.risk;

public enum StaticRiskSeverity {
  WARNING("warning", 0);

  private final String id;
  private final int sortOrder;

  StaticRiskSeverity(String id, int sortOrder) {
    this.id = id;
    this.sortOrder = sortOrder;
  }

  public String id() {
    return id;
  }

  public int sortOrder() {
    return sortOrder;
  }

  public static StaticRiskSeverity fromId(String id) {
    for (StaticRiskSeverity value : values()) {
      if (value.id.equals(id)) {
        return value;
      }
    }
    throw new IllegalArgumentException("Unknown static risk severity id: " + id);
  }
}
