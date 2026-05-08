package com.mcmodloader.core.graph;

import java.util.List;
import java.util.Map;
import java.util.Collections;
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
            Collections.unmodifiableMap(
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
        depends = Collections.unmodifiableMap(new TreeMap<>(depends));
        breaks = Collections.unmodifiableMap(new TreeMap<>(breaks));
    }
}
