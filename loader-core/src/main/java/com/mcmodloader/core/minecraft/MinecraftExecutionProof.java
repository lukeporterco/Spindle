package com.mcmodloader.core.minecraft;

public record MinecraftExecutionProof(
    boolean minecraftModExecutionAllowed,
    boolean minecraftModClassesLoadedOnlyInBootstrapExecution,
    boolean minecraftEntrypointsInvokedOnlyInBootstrapExecution,
    boolean modJarsOnMinecraftRuntimeClasspath,
    boolean mixinUsed,
    boolean remappingUsed,
    boolean bytecodeTransformationUsed,
    boolean minecraftPatchingUsed,
    boolean accessWidenersUsed,
    boolean fabricCompatibilityUsed,
    boolean forgeCompatibilityUsed
) {
}
