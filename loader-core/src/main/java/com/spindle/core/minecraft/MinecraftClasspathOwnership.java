package com.spindle.core.minecraft;

public enum MinecraftClasspathOwnership {
  LOADER_CORE("loader-core"),
  LOADER_API("loader-api"),
  GAME_PROVIDER("game-provider"),
  MINECRAFT_SERVER_JAR("minecraft-server-jar"),
  MINECRAFT_BUNDLED_LIBRARY("minecraft-bundled-library"),
  FUTURE_MOD_JAR("future-mod-jar"),
  FUTURE_MOD_CLASSLOADER("future-mod-classloader"),
  GENERATED_REPORT("generated-report"),
  CACHE_RUNTIME_FILE("cache-owned-runtime-file");

  private final String id;

  MinecraftClasspathOwnership(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
