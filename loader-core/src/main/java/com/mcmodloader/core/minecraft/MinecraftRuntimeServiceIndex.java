package com.mcmodloader.core.minecraft;

import java.util.List;
import java.util.Map;

public record MinecraftRuntimeServiceIndex(Map<String, List<String>> services) {
    public MinecraftRuntimeServiceIndex {
        services = Map.copyOf(services);
    }
}
