package com.spindle.core.minecraft.hook.steelhook;

import java.util.List;

public record SteelHook04PrimitiveBoundaryReport(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    String sourceSteelHook03Milestone,
    String sourceSteelHook03Status,
    boolean sourceSteelHook03CompletionReady,
    String sourceSteelHook03HandoffStatus,
    boolean gatePassed,
    String gateFailureReason,
    SteelHook04PrimitiveBoundaryStatus boundaryStatus,
    SteelHook04PrimitiveBoundaryNextDirection nextDirection,
    String nextRecommendedAction,
    boolean analysisOnly,
    boolean bytecodeModified,
    boolean transformedClassBytesProduced,
    boolean runtimeClassLoadingPathEnabled,
    boolean classLoadingOccurred,
    boolean serverLaunchOccurred,
    boolean minecraftMainInvoked,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean publicApiExposed,
    boolean javaAgentUsed,
    boolean mixinUsed,
    boolean javaModExecutionSandboxed,
    int approvedPrimitiveCount,
    List<SteelHook04PrimitiveCandidate> candidates,
    List<SteelHook04FixtureShape> allowedFixtureShapes,
    List<SteelHook04FixtureShape> unsupportedFixtureShapes,
    List<SteelHook04RejectionReason> rejectionTaxonomy,
    List<SteelHook04EvidenceRequirement> evidenceRequirements,
    List<SteelHook04PrimitiveFinding> findings) {
  public SteelHook04PrimitiveBoundaryReport {
    candidates = List.copyOf(candidates == null ? List.of() : candidates);
    allowedFixtureShapes =
        List.copyOf(allowedFixtureShapes == null ? List.of() : allowedFixtureShapes);
    unsupportedFixtureShapes =
        List.copyOf(unsupportedFixtureShapes == null ? List.of() : unsupportedFixtureShapes);
    rejectionTaxonomy = List.copyOf(rejectionTaxonomy == null ? List.of() : rejectionTaxonomy);
    evidenceRequirements =
        List.copyOf(evidenceRequirements == null ? List.of() : evidenceRequirements);
    findings = List.copyOf(findings == null ? List.of() : findings);
  }
}
