package com.mcmodloader.core.minecraft;

public record MinecraftBoundaryViolation(
    String type,
    MinecraftBoundarySeverity severity,
    String subject,
    String owner,
    String conflictingOwner,
    boolean fatalNow,
    boolean fatalBeforeInjection,
    String reason
) {
}
