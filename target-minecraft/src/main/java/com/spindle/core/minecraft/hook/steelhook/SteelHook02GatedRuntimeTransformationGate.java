package com.spindle.core.minecraft.hook.steelhook;

public record SteelHook02GatedRuntimeTransformationGate(
    boolean passed,
    String failureReason,
    boolean target25GatePassed,
    boolean target25Transformed,
    boolean eligibleForTarget26GatedRuntimeTransformation,
    boolean runtimeClassLoadingPathPreviouslyDisabled,
    boolean runtimeClassLoadingPathEnabledForTarget26,
    boolean targetDescriptorPresent,
    boolean dispatcherDescriptorPresent,
    boolean primitiveContractPresent,
    boolean generalizedPatchPlanPresent,
    boolean targetClassBytesMetadataPresent,
    boolean runtimeClasspathUrlsPresent,
    boolean minecraftRuntimeTransformReadyBeforeTarget26,
    boolean minecraftMainInvocationAllowed) {}
