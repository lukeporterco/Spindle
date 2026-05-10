package com.spindle.core.security;

import java.util.Comparator;

public record SecurityFinding(
    RuleId ruleId,
    SecuritySeverity severity,
    String modId,
    SecurityLocation location,
    String message,
    String fix) {
  public static final Comparator<SecurityFinding> ORDER =
      Comparator.comparingInt((SecurityFinding finding) -> finding.severity().sortOrder())
          .thenComparing(finding -> finding.ruleId().id())
          .thenComparing(finding -> valueOrEmpty(finding.modId()))
          .thenComparing(
              finding -> finding.location() == null ? "" : valueOrEmpty(finding.location().kind()))
          .thenComparing(
              finding -> finding.location() == null ? "" : valueOrEmpty(finding.location().value()))
          .thenComparing(finding -> valueOrEmpty(finding.message()));

  public SecurityFinding {
    modId = normalize(modId);
    message = normalize(message);
    fix = normalize(fix);
  }

  public boolean isFatal() {
    return severity == SecuritySeverity.FATAL;
  }

  private static String normalize(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private static String valueOrEmpty(String value) {
    return value == null ? "" : value;
  }
}
