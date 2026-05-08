package com.mcmodloader.core.graph;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record FrozenMod(
    String id,
    String version,
    String path,
    String sha256,
    Map<String, List<String>> entrypoints,
    Map<String, String> depends,
    Map<String, String> breaks,
    int classCount,
    int packageCount,
    int resourceCount
) {
    public FrozenMod {
        entrypoints =
            Map.copyOf(
                entrypoints
                    .entrySet()
                    .stream()
                    .collect(
                        java.util.stream.Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> List.copyOf(entry.getValue()),
                            (left, right) -> left,
                            TreeMap::new
                        )
                    )
            );
        depends = Map.copyOf(new TreeMap<>(depends));
        breaks = Map.copyOf(new TreeMap<>(breaks));
    }
}
