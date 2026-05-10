package com.spindle.core.security.tool;

public enum RestrictedToolExecutionMode {
  RESTRICTED_CHILD_JVM("restricted-child-jvm");

  private final String id;

  RestrictedToolExecutionMode(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }

  public static RestrictedToolExecutionMode fromId(String id) {
    for (RestrictedToolExecutionMode value : values()) {
      if (value.id.equals(id)) {
        return value;
      }
    }
    throw new IllegalArgumentException("Unknown restricted tool execution mode: " + id);
  }
}
