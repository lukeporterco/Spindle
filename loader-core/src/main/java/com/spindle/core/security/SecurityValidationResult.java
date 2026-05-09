package com.spindle.core.security;

import java.util.List;

public record SecurityValidationResult(
    SecurityPolicyFingerprint securityPolicyFingerprint,
    List<String> validatedSurfaces,
    List<SecurityFinding> findings) {
  public SecurityValidationResult {
    validatedSurfaces = List.copyOf(validatedSurfaces);
    findings = findings.stream().sorted(SecurityFinding.ORDER).toList();
  }

  public boolean hasFatalFindings() {
    return findings.stream().anyMatch(SecurityFinding::isFatal);
  }

  public int fatalCount() {
    return (int) findings.stream().filter(SecurityFinding::isFatal).count();
  }

  public int warningCount() {
    return findings.size() - fatalCount();
  }

  public String state() {
    return hasFatalFindings() ? "blocked" : "validated";
  }
}
