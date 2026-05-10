package com.spindle.core.minecraft.bootstrap;

import com.spindle.api.minecraft.MinecraftServerModContext;
import java.nio.file.Path;

final class DefaultMinecraftServerModContext implements MinecraftServerModContext {
  private final String modId;
  private final String modVersion;
  private final String minecraftVersion;
  private final String loaderVersion;
  private final String side;
  private final Path gameDirectory;
  private final Path configDirectory;
  private final Path modDataDirectory;

  DefaultMinecraftServerModContext(
      String modId,
      String modVersion,
      String minecraftVersion,
      String loaderVersion,
      String side,
      Path gameDirectory,
      Path configDirectory,
      Path modDataDirectory) {
    this.modId = modId;
    this.modVersion = modVersion;
    this.minecraftVersion = minecraftVersion;
    this.loaderVersion = loaderVersion;
    this.side = side;
    this.gameDirectory = gameDirectory;
    this.configDirectory = configDirectory;
    this.modDataDirectory = modDataDirectory;
  }

  @Override
  public String modId() {
    return modId;
  }

  @Override
  public String modVersion() {
    return modVersion;
  }

  @Override
  public String minecraftVersion() {
    return minecraftVersion;
  }

  @Override
  public String loaderVersion() {
    return loaderVersion;
  }

  @Override
  public String side() {
    return side;
  }

  @Override
  public Path gameDirectory() {
    return gameDirectory;
  }

  @Override
  public Path configDirectory() {
    return configDirectory;
  }

  @Override
  public Path modDataDirectory() {
    return modDataDirectory;
  }
}
