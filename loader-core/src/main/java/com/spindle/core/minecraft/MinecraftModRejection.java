package com.spindle.core.minecraft;

public record MinecraftModRejection(
    String candidate, String reason, MinecraftBoundarySeverity severity, boolean fatalNow) {}
