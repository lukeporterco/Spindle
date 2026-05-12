package com.spindle.core.minecraft.hook.verify;

import java.util.List;

public record SteelHookCompletionReport(
    int schema,
    String milestoneName,
    String steelHookVersion,
    SteelHookCompletionStatus status,
    boolean reportChainVerified,
    int stageFailureCount,
    int safetyInvariantFailureCount,
    List<SteelHookStageVerification> stageVerifications,
    List<SteelHookSafetyInvariant> safetyInvariants,
    List<SteelHookCapabilityBoundary> capabilityBoundaries) {
  public SteelHookCompletionReport {
    stageVerifications = List.copyOf(stageVerifications == null ? List.of() : stageVerifications);
    safetyInvariants = List.copyOf(safetyInvariants == null ? List.of() : safetyInvariants);
    capabilityBoundaries =
        List.copyOf(capabilityBoundaries == null ? List.of() : capabilityBoundaries);
  }
}
