package com.spindle.core.minecraft.hook.place;

import java.util.List;

public record MinecraftHookPlacementPlan(
    int schema,
    String milestoneName,
    String target,
    String minecraftVersion,
    String side,
    String catalogId,
    boolean sourceContractValidationPassed,
    int sourceContractErrorCount,
    String minecraftMainClass,
    boolean gatePassed,
    String gateFailureReason,
    boolean placementPlanned,
    int plannedPlacementCount,
    List<MinecraftPlannedHookPlacement> plannedPlacements,
    boolean codeInspectionOccurred,
    boolean codeAttributeParsed,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean bytecodeModified,
    boolean javaAgentUsed,
    boolean mixinUsed,
    boolean remappingOccurred,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed,
    boolean instructionInspectionOccurred,
    boolean callsiteInspectionOccurred) {
  public MinecraftHookPlacementPlan {
    plannedPlacements = List.copyOf(plannedPlacements == null ? List.of() : plannedPlacements);
  }
}
