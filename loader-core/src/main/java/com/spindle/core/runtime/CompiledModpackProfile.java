package com.spindle.core.runtime;

import java.nio.file.Path;
import java.util.List;

public record CompiledModpackProfile(
    int schemaVersion,
    String profileKind,
    String fingerprint,
    Loader loader,
    Game game,
    List<Mod> mods,
    List<String> resolvedOrder,
    List<ClasspathEntry> classpath,
    Ownership ownership,
    Lockfile lockfile) {
  public static final int SCHEMA_VERSION = 1;
  public static final String PROFILE_KIND = "compiled-modpack";
  public static final String LOADER_ID = "spindle";

  public CompiledModpackProfile {
    mods = List.copyOf(mods);
    resolvedOrder = List.copyOf(resolvedOrder);
    classpath = List.copyOf(classpath);
  }

  public record Loader(String id, String version) {}

  public record Game(String id, String version, String side) {}

  public record Mod(String id, String version, String path, String hash) {}

  public record ClasspathEntry(String path, String owner) {}

  public record Ownership(Count classes, Count packages, Resources resources) {}

  public record Count(int count) {}

  public record Resources(int duplicates) {}

  public record Lockfile(String mode, String path, String fingerprint) {}
}
