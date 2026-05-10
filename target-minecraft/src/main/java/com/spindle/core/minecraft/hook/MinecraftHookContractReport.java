package com.spindle.core.minecraft.hook;

import java.util.List;

public record MinecraftHookContractReport(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    String side,
    boolean analysisOnly,
    boolean classLoadingOccurred,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean hookInstallationOccurred,
    int artifactInterpretationSchema,
    String artifactInterpretationMilestone,
    int contractCount,
    int validContractCount,
    int invalidContractCount,
    int requiredContractCount,
    int optionalContractCount,
    int warningCount,
    int errorCount,
    boolean validationPassed,
    List<MinecraftHookContractResult> contracts,
    List<MinecraftHookContractDiagnostic> diagnostics) {
  public MinecraftHookContractReport {
    contracts = List.copyOf(contracts == null ? List.of() : contracts);
    diagnostics = List.copyOf(diagnostics == null ? List.of() : diagnostics);
  }
}
