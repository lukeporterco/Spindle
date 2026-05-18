package com.spindle.core.minecraft.hook.verify;

import java.nio.file.Path;

public record SteelHook03CompletionInput(
    Path steelHook02ReportPath,
    Path target28ReportPath,
    Path target29ReportPath,
    Path target30ReportPath,
    Path hookInstallationResultPath,
    Path serverBootstrapResultPath,
    Path fixtureTransformationResultPath,
    Path hookBootstrapTransformationResultPath) {
  public static final String REPORT_FILE_NAME = "minecraft-steelhook-0-3-report.json";

  public static SteelHook03CompletionInput fromWorkingDirectory(Path workingDirectory) {
    return new SteelHook03CompletionInput(
        workingDirectory.resolve("minecraft-steelhook-0-2-report.json"),
        workingDirectory.resolve("minecraft-steelhook-0-3-framed-method-foundation.json"),
        workingDirectory.resolve("minecraft-steelhook-0-3-method-exit-static-dispatch.json"),
        workingDirectory.resolve(
            "minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json"),
        workingDirectory.resolve("minecraft-hook-installation-result.json"),
        workingDirectory.resolve("minecraft-server-bootstrap-result.json"),
        workingDirectory.resolve("minecraft-fixture-transformation-result.json"),
        workingDirectory.resolve("minecraft-hook-bootstrap-transformation-result.json"));
  }

  public static Path reportPath(Path workingDirectory) {
    return workingDirectory.resolve(REPORT_FILE_NAME);
  }
}
