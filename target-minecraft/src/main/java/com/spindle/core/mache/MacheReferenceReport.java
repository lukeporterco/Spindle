package com.spindle.core.mache;

import java.util.List;

public record MacheReferenceReport(
    int schema,
    String macheDirectory,
    String requestedVersion,
    List<String> detectedVersionDirectories,
    boolean hasRequestedVersionDirectory,
    String branchHint,
    FileFlags files,
    List<String> warnings) {
  public MacheReferenceReport {
    detectedVersionDirectories = List.copyOf(detectedVersionDirectories);
    warnings = List.copyOf(warnings);
  }

  public record FileFlags(
      boolean settingsGradle, boolean gradleProperties, boolean readme, boolean license) {}
}
