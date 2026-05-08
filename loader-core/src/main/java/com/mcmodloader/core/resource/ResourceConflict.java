package com.mcmodloader.core.resource;

import java.util.List;

public record ResourceConflict(String resourcePath, List<String> modIds) {
    public ResourceConflict {
        modIds = List.copyOf(modIds);
    }
}
