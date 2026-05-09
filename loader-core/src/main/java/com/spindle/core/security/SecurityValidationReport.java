package com.spindle.core.security;

import com.spindle.core.runtime.CompiledModpackProfile;
import com.spindle.core.security.risk.StaticRiskSignal;
import com.spindle.core.security.risk.StaticRiskSummary;
import com.spindle.core.security.trust.ArtifactTrustEntry;
import com.spindle.core.security.trust.ArtifactTrustSummary;
import java.util.List;

public record SecurityValidationReport(
    int schemaVersion,
    String reportKind,
    String state,
    CompiledModpackProfile.Loader loader,
    CompiledModpackProfile.Game game,
    String profileFingerprint,
    String inputFingerprint,
    String runtimePolicyFingerprint,
    String securityPolicyFingerprint,
    String executionIsolationMode,
    boolean sandboxed,
    String sandboxClaim,
    int fatalCount,
    int warningCount,
    List<String> validatedSurfaces,
    List<ArtifactTrustEntry> artifactTrustEntries,
    ArtifactTrustSummary artifactTrustSummary,
    StaticRiskSummary staticRiskSummary,
    List<StaticRiskSignal> staticRiskSignals,
    List<SecurityFinding> findings) {
  public static final int SCHEMA_VERSION = 2;
  public static final String REPORT_KIND = "security-validation";

  public SecurityValidationReport {
    validatedSurfaces = List.copyOf(validatedSurfaces);
    artifactTrustEntries = List.copyOf(artifactTrustEntries);
    staticRiskSummary = staticRiskSummary == null ? StaticRiskSummary.EMPTY : staticRiskSummary;
    staticRiskSignals = staticRiskSignals == null ? List.of() : List.copyOf(staticRiskSignals);
    findings = List.copyOf(findings);
  }
}
