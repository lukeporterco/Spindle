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
    Storage storage,
    Services services,
    Config config) {
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
        Storage.disabled(),
        Services.empty(),
        Config.empty());
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
    services = services == null ? Services.empty() : services;
    config = config == null ? Config.empty() : config;
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

  public record Services(List<ServiceProvider> provides, List<ServiceConsumer> consumes) {
    public Services {
      provides =
          provides.stream()
              .sorted(
                  java.util.Comparator.comparing(ServiceProvider::id)
                      .thenComparing(ServiceProvider::type)
                      .thenComparing(ServiceProvider::implementation))
              .toList();
      consumes =
          consumes.stream()
              .sorted(
                  java.util.Comparator.comparing(ServiceConsumer::id)
                      .thenComparing(ServiceConsumer::type)
                      .thenComparing(ServiceConsumer::required))
              .toList();
    }

    public static Services empty() {
      return new Services(List.of(), List.of());
    }

    public boolean isEmpty() {
      return provides.isEmpty() && consumes.isEmpty();
    }
  }

  public record ServiceProvider(String id, String type, String implementation) {}

  public record ServiceConsumer(String id, String type, boolean required) {}

  public record Storage(boolean config, boolean data, boolean cache, boolean generated) {
    public static Storage disabled() {
      return new Storage(false, false, false, false);
    }
  }

  public record Config(boolean runtimeWrites, List<ConfigEntry> entries) {
    public Config {
      entries = entries.stream().sorted(java.util.Comparator.comparing(ConfigEntry::key)).toList();
    }

    public static Config empty() {
      return new Config(false, List.of());
    }

    public boolean isEmpty() {
      return entries.isEmpty();
    }
  }

  public record ConfigEntry(
      String key, String type, String defaultValue, String min, String max, List<String> allowed) {
    public ConfigEntry {
      allowed = allowed == null ? List.of() : allowed.stream().sorted().toList();
    }
  }
}
