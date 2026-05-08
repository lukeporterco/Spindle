package com.mcmodloader.core.minecraft;

import java.util.List;
import java.util.Map;

public record MinecraftRuntimeResourceIndex(Map<String, List<String>> resources) {
    public MinecraftRuntimeResourceIndex {
        resources = Map.copyOf(resources);
    }
}
