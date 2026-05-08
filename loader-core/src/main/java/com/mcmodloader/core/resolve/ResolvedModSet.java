package com.mcmodloader.core.resolve;

import java.nio.file.Path;
import java.util.List;

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
        List<String> entrypoints
    ) {
        public ResolvedMod {
            entrypoints = List.copyOf(entrypoints);
        }

        public String normalizedRelativePath() {
            return relativePath.toString().replace('\\', '/');
        }
    }
}
