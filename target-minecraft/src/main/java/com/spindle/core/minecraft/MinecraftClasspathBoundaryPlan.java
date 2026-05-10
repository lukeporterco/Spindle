package com.spindle.core.minecraft;

public record MinecraftClasspathBoundaryPlan(
    String futureClassloaderBoundary, String parentDelegationPolicy) {}
