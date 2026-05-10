package com.spindle.core.security.tool;

import com.spindle.core.security.SecurityFinding;
import com.spindle.core.security.risk.StaticRiskAnalyzer;
import java.util.List;

public record RestrictedToolResult(
    RestrictedToolExecutionMode mode,
    String worker,
    String status,
    String outputPath,
    StaticRiskAnalyzer.Analysis analysis,
    List<SecurityFinding> findings) {
  public static final String STATUS_PASSED = "passed";
  public static final String STATUS_FAILED = "failed";

  public RestrictedToolResult {
    analysis = analysis == null ? StaticRiskAnalyzer.Analysis.EMPTY : analysis;
    findings =
        findings == null ? List.of() : findings.stream().sorted(SecurityFinding.ORDER).toList();
  }
}
