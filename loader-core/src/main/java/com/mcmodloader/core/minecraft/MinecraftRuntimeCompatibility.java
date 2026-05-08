package com.mcmodloader.core.minecraft;

public record MinecraftRuntimeCompatibility(
    boolean loaderCompatible,
    boolean javaCompatible,
    boolean minecraftCompatible,
    boolean sideCompatible,
    String reason
) {
}
