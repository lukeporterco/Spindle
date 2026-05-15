package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.MinecraftSide;
import java.util.List;

public record SteelHook02ContractGeneralizationAnalysis(
    int schema,
    String milestoneName,
    String target,
    String steelHookVersion,
    String minecraftVersion,
    MinecraftSide side,
    String sourcePatchPlanMilestone,
    String sourcePrimitiveBoundaryMilestone,
    boolean analysisOnly,
    boolean classLoadingOccurred,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean patchingOccurred,
    boolean bytecodeModified,
    boolean hookInstallationOccurred,
    boolean runtimeDispatchOccurred,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed,
    boolean contractGeneralizationOccurred,
    boolean contractGeneralizationReady,
    boolean minecraftRuntimeTransformReady,
    boolean eligibleForTarget25TransformerExtraction,
    boolean eligibleForTarget26RuntimeTransformation,
    boolean gatePassed,
    String gateFailureReason,
    SteelHook02ContractGeneralizationStatus status,
    SteelHook02ContractGeneralizationNextDirection nextDirection,
    String nextRecommendedAction,
    SteelHook02TargetDescriptor targetDescriptor,
    SteelHook02DispatcherDescriptor dispatcherDescriptor,
    SteelHook02PrimitiveContract primitiveContract,
    SteelHook02GeneralizedPatchPlan generalizedPatchPlan,
    List<SteelHook02ContractGeneralizationFinding> findings) {
  public SteelHook02ContractGeneralizationAnalysis {
    findings = List.copyOf(findings == null ? List.of() : findings);
  }
}
