package com.mcmodloader.core.resolve;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public record ResolvedModSet(List<ResolvedMod> mods) {
    public ResolvedModSet {
        mods = List.copyOf(mods);
    }

    public record ResolvedMod(
        String id,
        String version,
        Path relativePath,
        Path jarPath,
        String sha256,
        Map<String, List<String>> entrypoints,
        Map<String, String> depends,
        Map<String, String> breaks
    ) {
        public ResolvedMod {
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

        public String normalizedRelativePath() {
            return relativePath.toString().replace('\\', '/');
        }
    }
}
