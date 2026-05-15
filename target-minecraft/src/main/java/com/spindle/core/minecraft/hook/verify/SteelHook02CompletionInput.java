package com.spindle.core.minecraft.hook.verify;

import java.nio.file.Path;

public record SteelHook02CompletionInput(
    Path hookContractsReportPath,
    Path hookPlacementPlanPath,
    Path hookBytecodeAnalysisReportPath,
    Path hookPatchPlanPath,
    Path primitiveBoundaryReportPath,
    Path contractGeneralizationReportPath,
    Path methodEntryTransformerReportPath,
    Path gatedRuntimeTransformationReportPath,
    Path hookInstallationResultPath,
    Path fixtureTransformationResultPath,
    Path hookBootstrapTransformationResultPath,
    Path serverBootstrapResultPath) {
  public static final String REPORT_FILE_NAME = "minecraft-steelhook-0-2-report.json";

  public static SteelHook02CompletionInput fromWorkingDirectory(Path workingDirectory) {
    return new SteelHook02CompletionInput(
        workingDirectory.resolve("minecraft-hook-contracts.json"),
        workingDirectory.resolve("minecraft-hook-placement-plan.json"),
        workingDirectory.resolve("minecraft-hook-bytecode-analysis.json"),
        workingDirectory.resolve("minecraft-hook-patch-plan.json"),
        workingDirectory.resolve("minecraft-steelhook-0-2-primitive-boundary.json"),
        workingDirectory.resolve("minecraft-steelhook-0-2-contract-generalization.json"),
        workingDirectory.resolve("minecraft-steelhook-0-2-method-entry-transformer-result.json"),
        workingDirectory.resolve(
            "minecraft-steelhook-0-2-gated-runtime-transformation-result.json"),
        workingDirectory.resolve("minecraft-hook-installation-result.json"),
        workingDirectory.resolve("minecraft-fixture-transformation-result.json"),
        workingDirectory.resolve("minecraft-hook-bootstrap-transformation-result.json"),
        workingDirectory.resolve("minecraft-server-bootstrap-result.json"));
  }

  public static Path reportPath(Path workingDirectory) {
    return workingDirectory.resolve(REPORT_FILE_NAME);
  }
}
