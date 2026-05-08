package com.mcmodloader.core.metadata;

import java.util.List;
import java.util.Map;

public record ModMetadata(
    int schema,
    String id,
    String version,
    String side,
    Map<String, List<String>> entrypoints,
    Map<String, String> depends
) {
    public List<String> mainEntrypoints() {
        return entrypoints.get("main");
    }
}
