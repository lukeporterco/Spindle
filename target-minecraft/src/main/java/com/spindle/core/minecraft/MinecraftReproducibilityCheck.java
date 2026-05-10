package com.spindle.core.minecraft;

import java.util.List;

public record MinecraftReproducibilityCheck(
    int schema,
    String milestoneName,
    List<ComparedReport> comparedReports,
    boolean byteForByteEqual,
    boolean timestampLeakageDetected,
    boolean nondeterministicOrderingDetected,
    boolean pathInstabilityDetected,
    boolean offlineNetworkUseDetected,
    List<String> failures) {
  public MinecraftReproducibilityCheck {
    comparedReports = List.copyOf(comparedReports);
    failures = List.copyOf(failures);
  }

  public record ComparedReport(
      String reportName,
      String firstPath,
      String secondPath,
      String firstSha256,
      String secondSha256,
      boolean byteForByteEqual,
      boolean semanticallyEqual,
      List<String> failureReasons) {
    public ComparedReport {
      failureReasons = List.copyOf(failureReasons);
    }
  }
}
