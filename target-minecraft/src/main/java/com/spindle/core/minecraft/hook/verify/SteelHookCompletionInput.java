package com.spindle.core.minecraft.hook.verify;

import java.nio.file.Path;

public record SteelHookCompletionInput(
    Path hookContractsReportPath,
    Path hookPlacementPlanPath,
    Path hookBytecodeAnalysisReportPath,
    Path hookPatchPlanPath,
    Path bootstrapTransformationResultPath,
    Path serverBootstrapResultPath,
    Path modExecutionResultPath,
    Path hookInstallationResultPath) {
  public static final String REPORT_FILE_NAME = "minecraft-steelhook-0.1-report.json";

  public static SteelHookCompletionInput fromWorkingDirectory(Path workingDirectory) {
    return new SteelHookCompletionInput(
        workingDirectory.resolve("minecraft-hook-contracts.json"),
        workingDirectory.resolve("minecraft-hook-placement-plan.json"),
        workingDirectory.resolve("minecraft-hook-bytecode-analysis.json"),
        workingDirectory.resolve("minecraft-hook-patch-plan.json"),
        workingDirectory.resolve("minecraft-hook-bootstrap-transformation-result.json"),
        workingDirectory.resolve("minecraft-server-bootstrap-result.json"),
        workingDirectory.resolve("minecraft-mod-execution-result.json"),
        workingDirectory.resolve("minecraft-hook-installation-result.json"));
  }

  public static Path reportPath(Path workingDirectory) {
    return workingDirectory.resolve(REPORT_FILE_NAME);
  }
}
