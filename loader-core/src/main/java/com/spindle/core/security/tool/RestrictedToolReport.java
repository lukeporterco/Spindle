package com.spindle.core.security.tool;

import com.spindle.core.security.risk.StaticRiskSignal;
import com.spindle.core.security.risk.StaticRiskSummary;
import java.util.List;

public record RestrictedToolReport(
    int schemaVersion,
    String reportKind,
    String worker,
    RestrictedToolExecutionMode mode,
    StaticRiskSummary staticRiskSummary,
    List<StaticRiskSignal> staticRiskSignals) {
  public static final int SCHEMA_VERSION = 1;
  public static final String REPORT_KIND = "restricted-tool-output";

  public RestrictedToolReport {
    staticRiskSummary = staticRiskSummary == null ? StaticRiskSummary.EMPTY : staticRiskSummary;
    staticRiskSignals =
        staticRiskSignals == null
            ? List.of()
            : staticRiskSignals.stream().sorted(StaticRiskSignal.ORDER).toList();
  }
}
