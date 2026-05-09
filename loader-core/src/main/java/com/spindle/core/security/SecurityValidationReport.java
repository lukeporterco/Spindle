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
    String runtimeExecutionIsolationMode,
    boolean sandboxed,
    boolean runtimeSandboxed,
    String sandboxClaim,
    ToolIsolation toolIsolation,
    int fatalCount,
    int warningCount,
    List<String> validatedSurfaces,
    List<ArtifactTrustEntry> artifactTrustEntries,
    ArtifactTrustSummary artifactTrustSummary,
    StaticRiskSummary staticRiskSummary,
    List<StaticRiskSignal> staticRiskSignals,
    List<SecurityFinding> findings) {
  public static final int SCHEMA_VERSION = 3;
  public static final String REPORT_KIND = "security-validation";

  public SecurityValidationReport {
    toolIsolation = toolIsolation == null ? ToolIsolation.failed() : toolIsolation;
    validatedSurfaces = List.copyOf(validatedSurfaces);
    artifactTrustEntries = List.copyOf(artifactTrustEntries);
    staticRiskSummary = staticRiskSummary == null ? StaticRiskSummary.EMPTY : staticRiskSummary;
    staticRiskSignals = staticRiskSignals == null ? List.of() : List.copyOf(staticRiskSignals);
    findings = List.copyOf(findings);
  }

  public record ToolIsolation(String mode, String worker, String status, String outputPath) {
    public ToolIsolation {
      mode = normalize(mode);
      worker = normalize(worker);
      status = normalize(status);
      outputPath = normalize(outputPath);
    }

    public static ToolIsolation failed() {
      return new ToolIsolation("restricted-child-jvm", "static-risk-scan", "failed", null);
    }

    private static String normalize(String value) {
      if (value == null) {
        return null;
      }
      String normalized = value.trim().replace('\\', '/');
      return normalized.isEmpty() ? null : normalized;
    }
  }
}
