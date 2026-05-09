package com.spindle.core.resolve;

import java.nio.file.Path;
import java.util.Collections;
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
      Map<String, String> breaks,
      int metadataSchema,
      Map<String, List<String>> lifecycle,
      List<String> permissions,
      com.spindle.core.metadata.ModMetadata.Storage storage,
      com.spindle.core.metadata.ModMetadata.Services services) {
    public ResolvedMod(
        String id,
        String version,
        Path relativePath,
        Path jarPath,
        String sha256,
        Map<String, List<String>> entrypoints,
        Map<String, String> depends,
        Map<String, String> breaks) {
      this(
          id,
          version,
          relativePath,
          jarPath,
          sha256,
          entrypoints,
          depends,
          breaks,
          1,
          Map.of(),
          List.of(),
          com.spindle.core.metadata.ModMetadata.Storage.disabled(),
          com.spindle.core.metadata.ModMetadata.Services.empty());
    }

    public ResolvedMod {
      entrypoints =
          Collections.unmodifiableMap(
              entrypoints.entrySet().stream()
                  .collect(
                      Collectors.toMap(
                          Map.Entry::getKey,
                          entry -> List.copyOf(entry.getValue()),
                          (left, right) -> left,
                          TreeMap::new)));
      lifecycle =
          Collections.unmodifiableMap(
              lifecycle.entrySet().stream()
                  .collect(
                      Collectors.toMap(
                          Map.Entry::getKey,
                          entry -> List.copyOf(entry.getValue()),
                          (left, right) -> left,
                          TreeMap::new)));
      depends = Collections.unmodifiableMap(new TreeMap<>(depends));
      breaks = Collections.unmodifiableMap(new TreeMap<>(breaks));
      permissions = List.copyOf(permissions);
      storage =
          storage == null ? com.spindle.core.metadata.ModMetadata.Storage.disabled() : storage;
      services =
          services == null ? com.spindle.core.metadata.ModMetadata.Services.empty() : services;
    }

    public String normalizedRelativePath() {
      return relativePath.toString().replace('\\', '/');
    }
  }
}
