package com.spindle.core.security.risk;

import com.spindle.core.security.SecurityLocation;
import java.util.Comparator;

public record StaticRiskSignal(
    StaticRiskRuleId ruleId,
    StaticRiskSeverity severity,
    String modId,
    SecurityLocation location,
    String evidence,
    String message,
    String fix) {
  public static final Comparator<StaticRiskSignal> ORDER =
      Comparator.comparingInt((StaticRiskSignal signal) -> signal.severity().sortOrder())
          .thenComparing(signal -> signal.ruleId().id())
          .thenComparing(signal -> valueOrEmpty(signal.modId()))
          .thenComparing(
              signal -> signal.location() == null ? "" : valueOrEmpty(signal.location().kind()))
          .thenComparing(
              signal -> signal.location() == null ? "" : valueOrEmpty(signal.location().value()))
          .thenComparing(signal -> valueOrEmpty(signal.evidence()))
          .thenComparing(signal -> valueOrEmpty(signal.message()));

  public StaticRiskSignal {
    modId = normalize(modId);
    evidence = normalize(evidence);
    message = normalize(message);
    fix = normalize(fix);
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
