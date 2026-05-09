package com.spindle.core.security;

import com.spindle.core.runtime.CompiledModpackProfile;
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
    List<SecurityFinding> findings) {
  public static final int SCHEMA_VERSION = 1;
  public static final String REPORT_KIND = "security-validation";

  public SecurityValidationReport {
    validatedSurfaces = List.copyOf(validatedSurfaces);
    findings = List.copyOf(findings);
  }
}
