package com.spindle.core.security;

import com.spindle.core.security.risk.StaticRiskSignal;
import com.spindle.core.security.risk.StaticRiskSummary;
import com.spindle.core.security.tool.RestrictedToolResult;
import com.spindle.core.security.trust.ArtifactTrustEntry;
import com.spindle.core.security.trust.ArtifactTrustSummary;
import java.util.List;

public record SecurityValidationResult(
    SecurityPolicyFingerprint securityPolicyFingerprint,
    RestrictedToolResult restrictedToolResult,
    List<String> validatedSurfaces,
    List<ArtifactTrustEntry> artifactTrustEntries,
    ArtifactTrustSummary artifactTrustSummary,
    StaticRiskSummary staticRiskSummary,
    List<StaticRiskSignal> staticRiskSignals,
    List<SecurityFinding> findings) {
  public SecurityValidationResult {
    restrictedToolResult =
        java.util.Objects.requireNonNull(restrictedToolResult, "restrictedToolResult");
    validatedSurfaces = List.copyOf(validatedSurfaces);
    artifactTrustEntries = List.copyOf(artifactTrustEntries);
    staticRiskSummary = staticRiskSummary == null ? StaticRiskSummary.EMPTY : staticRiskSummary;
    staticRiskSignals =
        staticRiskSignals == null
            ? List.of()
            : staticRiskSignals.stream().sorted(StaticRiskSignal.ORDER).toList();
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
