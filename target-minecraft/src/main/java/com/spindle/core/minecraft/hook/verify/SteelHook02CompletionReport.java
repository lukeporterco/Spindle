package com.spindle.core.minecraft.hook.verify;

import java.util.List;

public record SteelHook02CompletionReport(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    SteelHook02CompletionStatus status,
    SteelHook02CompletionHandoffStatus handoffStatus,
    SteelHook02CompletionNextDirection nextDirection,
    boolean reportChainVerified,
    boolean completionReady,
    int stageFailureCount,
    int safetyInvariantFailureCount,
    int capabilityBoundaryCount,
    String targetBinaryName,
    String targetClassEntryName,
    String primitiveKind,
    boolean runtimeClassLoadingPathVerified,
    boolean runtimeClassDefinedVerified,
    boolean minecraftMainNotInvokedVerified,
    boolean minecraftServerNotLaunchedVerified,
    boolean hookInstallationNotOccurredVerified,
    boolean runtimeDispatchNotObservedVerified,
    boolean publicApiNotExposedVerified,
    boolean javaModExecutionSandboxingNotClaimedVerified,
    boolean unsupportedCapabilitiesRemainBlockedVerified,
    List<SteelHookStageVerification> stageVerifications,
    List<SteelHookSafetyInvariant> safetyInvariants,
    List<SteelHookCapabilityBoundary> capabilityBoundaries,
    String failureSummary) {
  public SteelHook02CompletionReport {
    stageVerifications = List.copyOf(stageVerifications == null ? List.of() : stageVerifications);
    safetyInvariants = List.copyOf(safetyInvariants == null ? List.of() : safetyInvariants);
    capabilityBoundaries =
        List.copyOf(capabilityBoundaries == null ? List.of() : capabilityBoundaries);
  }
}
