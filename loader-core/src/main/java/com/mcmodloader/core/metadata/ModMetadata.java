package com.mcmodloader.core.metadata;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public record ModMetadata(
    int schema,
    String id,
    String version,
    String side,
    Map<String, List<String>> entrypoints,
    Map<String, String> depends,
    Map<String, String> breaks
) {
    public ModMetadata {
        entrypoints =
            Map.copyOf(
                entrypoints
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> List.copyOf(entry.getValue()), (left, right) -> left, TreeMap::new))
            );
        depends = Map.copyOf(new TreeMap<>(depends));
        breaks = Map.copyOf(new TreeMap<>(breaks));
    }

    public List<String> mainEntrypoints() {
        return entrypoints.get("main");
    }
}
