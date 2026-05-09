package com.spindle.core.minecraft;

public record MinecraftEntrypointPlan(
    String modId,
    String entrypointKey,
    String entrypointClassName,
    String interfaceType,
    String methodName,
    String modJarPath,
    String modJarSha256,
    String plannedModClassLoaderId,
    String plannedParentClassLoaderId) {}
