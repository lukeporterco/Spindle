package com.spindle.core.metadata;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record ModMetadata(
    int schema,
    String id,
    String version,
    String side,
    Map<String, List<String>> entrypoints,
    Map<String, String> depends,
    Map<String, String> breaks) {
  public ModMetadata {
    TreeMap<String, List<String>> sortedEntrypoints = new TreeMap<>();
    for (Map.Entry<String, List<String>> entry : entrypoints.entrySet()) {
      sortedEntrypoints.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    entrypoints = java.util.Collections.unmodifiableMap(sortedEntrypoints);
    depends = java.util.Collections.unmodifiableMap(new TreeMap<>(depends));
    breaks = java.util.Collections.unmodifiableMap(new TreeMap<>(breaks));
  }

  public List<String> mainEntrypoints() {
    return entrypoints.getOrDefault("main", List.of());
  }

  public List<String> minecraftServerEntrypoints() {
    return entrypoints.getOrDefault("minecraftServer", List.of());
  }
}
