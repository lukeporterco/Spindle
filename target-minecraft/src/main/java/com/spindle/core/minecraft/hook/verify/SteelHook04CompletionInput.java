package com.spindle.core.minecraft.hook.verify;

import java.nio.file.Path;

public record SteelHook04CompletionInput(
    Path target32ReportPath,
    Path target33ReportPath,
    Path target34ReportPath,
    Path target35ReportPath,
    Path hookInstallationResultPath,
    Path serverBootstrapResultPath,
    Path fixtureTransformationResultPath,
    Path hookBootstrapTransformationResultPath) {
  public static final String REPORT_FILE_NAME = "minecraft-steelhook-0-4-report.json";

  public static SteelHook04CompletionInput fromWorkingDirectory(Path workingDirectory) {
    return new SteelHook04CompletionInput(
        workingDirectory.resolve("minecraft-steelhook-0-4-primitive-boundary.json"),
        workingDirectory.resolve(
            "minecraft-steelhook-0-4-return-value-intercept-offline-proof.json"),
        workingDirectory.resolve("minecraft-steelhook-0-4-invoke-redirect-wrap-offline-proof.json"),
        workingDirectory.resolve("minecraft-steelhook-0-4-gated-runtime-proof.json"),
        workingDirectory.resolve("minecraft-hook-installation-result.json"),
        workingDirectory.resolve("minecraft-server-bootstrap-result.json"),
        workingDirectory.resolve("minecraft-fixture-transformation-result.json"),
        workingDirectory.resolve("minecraft-hook-bootstrap-transformation-result.json"));
  }

  public static Path reportPath(Path workingDirectory) {
    return workingDirectory.resolve(REPORT_FILE_NAME);
  }
}
