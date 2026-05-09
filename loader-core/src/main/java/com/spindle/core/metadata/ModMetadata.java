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
    Map<String, String> breaks,
    Map<String, List<String>> lifecycle,
    List<String> permissions,
    Storage storage) {
  public ModMetadata(
      int schema,
      String id,
      String version,
      String side,
      Map<String, List<String>> entrypoints,
      Map<String, String> depends,
      Map<String, String> breaks) {
    this(
        schema,
        id,
        version,
        side,
        entrypoints,
        depends,
        breaks,
        Map.of(),
        List.of(),
        Storage.disabled());
  }

  public ModMetadata {
    TreeMap<String, List<String>> sortedEntrypoints = new TreeMap<>();
    for (Map.Entry<String, List<String>> entry : entrypoints.entrySet()) {
      sortedEntrypoints.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    entrypoints = java.util.Collections.unmodifiableMap(sortedEntrypoints);
    TreeMap<String, List<String>> sortedLifecycle = new TreeMap<>();
    for (Map.Entry<String, List<String>> entry : lifecycle.entrySet()) {
      sortedLifecycle.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    lifecycle = java.util.Collections.unmodifiableMap(sortedLifecycle);
    depends = java.util.Collections.unmodifiableMap(new TreeMap<>(depends));
    breaks = java.util.Collections.unmodifiableMap(new TreeMap<>(breaks));
    permissions = List.copyOf(permissions);
    storage = storage == null ? Storage.disabled() : storage;
  }

  public List<String> mainEntrypoints() {
    return entrypoints.getOrDefault("main", List.of());
  }

  public List<String> minecraftServerEntrypoints() {
    return entrypoints.getOrDefault("minecraftServer", List.of());
  }

  public boolean usesRuntimeLifecycle() {
    return !lifecycle.isEmpty();
  }

  public record Storage(boolean config, boolean data, boolean cache, boolean generated) {
    public static Storage disabled() {
      return new Storage(false, false, false, false);
    }
  }
}
