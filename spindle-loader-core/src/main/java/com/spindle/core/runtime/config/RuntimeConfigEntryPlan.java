package com.spindle.core.runtime.config;

public record RuntimeConfigEntryPlan(
    String key,
    String type,
    String defaultValue,
    String value,
    String state,
    String reason,
    String min,
    String max,
    java.util.List<String> allowed,
    String findingCode) {
  public RuntimeConfigEntryPlan {
    allowed = allowed == null ? java.util.List.of() : java.util.List.copyOf(allowed);
  }
}
