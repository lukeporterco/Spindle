package com.spindle.core.minecraft.hook.steelhook;

public record SteelHook02MethodEntryTransformerGate(
    boolean passed,
    String failureReason,
    boolean contractGeneralizationGatePassed,
    boolean contractGeneralizationReady,
    boolean eligibleForTarget25TransformerExtraction,
    boolean targetDescriptorPresent,
    boolean dispatcherDescriptorPresent,
    boolean primitiveContractPresent,
    boolean generalizedPatchPlanPresent,
    boolean targetClassBytesPresent,
    boolean minecraftRuntimeTransformReady,
    boolean runtimeClassLoadingPathEnabled) {}
