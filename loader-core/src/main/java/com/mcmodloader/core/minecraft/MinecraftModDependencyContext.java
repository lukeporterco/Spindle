package com.mcmodloader.core.minecraft;

import java.util.Map;

public record MinecraftModDependencyContext(String loaderVersion, String javaVersion, String minecraftVersion, Map<String, String> availableMods) {
    public MinecraftModDependencyContext {
        availableMods = Map.copyOf(availableMods);
    }
}
