package com.spindle.core.minecraft.hook.install;

import com.spindle.core.minecraft.MinecraftModExecutionPlan;
import com.spindle.core.minecraft.hook.MinecraftHookContractReport;
import com.spindle.core.minecraft.hook.MinecraftHookContractResult;
import java.util.List;

public final class MinecraftHookInstallationPlanner {
  public static final String SUPPORTED_HOOK_ID = "target-4.minecraft.server.main.launch-boundary";
  public static final String SUPPORTED_SOURCE_CONTRACT_ID =
      "minecraft.26_1_2.server.main.entrypoint";
  public static final String SUPPORTED_CATALOG_ID = "minecraft-26.1.2-server-known-symbols";
  public static final String SUPPORTED_KIND = "LAUNCH_BOUNDARY_MAIN";
  public static final String SUPPORTED_OWNER_INTERNAL_NAME = "net/minecraft/server/Main";
  public static final String SUPPORTED_MEMBER_NAME = "main";
  public static final String SUPPORTED_DESCRIPTOR = "([Ljava/lang/String;)V";
  public static final String SUPPORTED_MAIN_CLASS = "net.minecraft.server.Main";
  public static final String MILESTONE_NAME = "Target-4";

  public MinecraftHookInstallationPlan plan(
      MinecraftHookContractReport contractReport, MinecraftModExecutionPlan executionPlan) {
    if (contractReport == null) {
      return failedPlan(null, executionPlan, "Target-3 hook contract report is missing.");
    }
    if (!SUPPORTED_CATALOG_ID.equals(contractReport.catalogId())) {
      return failedPlan(
          contractReport,
          executionPlan,
          "Unsupported hook contract catalog: " + contractReport.catalogId());
    }
    if (!contractReport.validationPassed()) {
      return failedPlan(contractReport, executionPlan, "Target-3 hook contract validation failed.");
    }
    if (contractReport.errorCount() != 0) {
      return failedPlan(
          contractReport,
          executionPlan,
          "Target-3 hook contract report contains errors: " + contractReport.errorCount());
    }
    MinecraftHookContractResult entrypointContract =
        contractReport.contracts().stream()
            .filter(contract -> SUPPORTED_SOURCE_CONTRACT_ID.equals(contract.id()))
            .findFirst()
            .orElse(null);
    if (entrypointContract == null
        || !entrypointContract.valid()
        || !SUPPORTED_OWNER_INTERNAL_NAME.equals(entrypointContract.ownerInternalName())
        || !SUPPORTED_MEMBER_NAME.equals(entrypointContract.memberName())
        || !SUPPORTED_DESCRIPTOR.equals(entrypointContract.descriptor())) {
      return failedPlan(
          contractReport,
          executionPlan,
          "Required Target-3 contract minecraft.26_1_2.server.main.entrypoint is missing or invalid.");
    }
    if (executionPlan == null || !SUPPORTED_MAIN_CLASS.equals(executionPlan.minecraftMainClass())) {
      return failedPlan(
          contractReport,
          executionPlan,
          "Minecraft execution plan main class must be net.minecraft.server.Main.");
    }

    MinecraftPlannedHookInstallation plannedHook =
        new MinecraftPlannedHookInstallation(
            SUPPORTED_HOOK_ID,
            SUPPORTED_SOURCE_CONTRACT_ID,
            SUPPORTED_CATALOG_ID,
            SUPPORTED_KIND,
            SUPPORTED_OWNER_INTERNAL_NAME,
            SUPPORTED_MEMBER_NAME,
            SUPPORTED_DESCRIPTOR,
            true,
            MinecraftHookInstallationMode.LAUNCH_BOUNDARY_MAIN_WRAPPER);
    return new MinecraftHookInstallationPlan(
        1,
        MILESTONE_NAME,
        "minecraft",
        executionPlan.resolvedMinecraftVersion(),
        executionPlan.side(),
        contractReport.catalogId(),
        contractReport.validationPassed(),
        contractReport.errorCount(),
        executionPlan.minecraftMainClass(),
        true,
        null,
        true,
        MinecraftHookInstallationMode.LAUNCH_BOUNDARY_MAIN_WRAPPER,
        1,
        List.of(plannedHook),
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false);
  }

  private MinecraftHookInstallationPlan failedPlan(
      MinecraftHookContractReport contractReport,
      MinecraftModExecutionPlan executionPlan,
      String reason) {
    return new MinecraftHookInstallationPlan(
        1,
        MILESTONE_NAME,
        "minecraft",
        executionPlan == null ? null : executionPlan.resolvedMinecraftVersion(),
        executionPlan == null ? null : executionPlan.side(),
        contractReport == null ? null : contractReport.catalogId(),
        contractReport != null && contractReport.validationPassed(),
        contractReport == null ? 0 : contractReport.errorCount(),
        executionPlan == null ? null : executionPlan.minecraftMainClass(),
        false,
        reason,
        false,
        MinecraftHookInstallationMode.LAUNCH_BOUNDARY_MAIN_WRAPPER,
        0,
        List.of(),
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false);
  }
}
