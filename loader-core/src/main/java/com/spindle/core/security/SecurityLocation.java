package com.spindle.core.security;

public record SecurityLocation(String kind, String value) {
  public SecurityLocation {
    kind = normalize(kind);
    value = normalize(value);
  }

  public static SecurityLocation of(String kind, String value) {
    return new SecurityLocation(kind, value);
  }

  private static String normalize(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim().replace('\\', '/');
    return normalized.isEmpty() ? null : normalized;
  }
}
