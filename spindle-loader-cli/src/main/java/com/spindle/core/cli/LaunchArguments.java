package com.spindle.core.cli;

import com.spindle.core.minecraft.MinecraftProviderConfig;
import java.nio.file.Path;
import java.util.List;

public record LaunchArguments(
    String gameMainClass,
    String gameProviderId,
    List<String> launchArguments,
    boolean validateOnly,
    boolean explain,
    boolean strictResources,
    boolean strictPackages,
    MinecraftProviderConfig minecraftProviderConfig,
    Path macheDirectory,
    String macheVersion,
    boolean macheReferenceScan) {
  public LaunchArguments {
    launchArguments = List.copyOf(launchArguments);
  }

  public LaunchArguments withMacheDirectory(Path updatedMacheDirectory) {
    return new LaunchArguments(
        gameMainClass,
        gameProviderId,
        launchArguments,
        validateOnly,
        explain,
        strictResources,
        strictPackages,
        minecraftProviderConfig,
        updatedMacheDirectory,
        macheVersion,
        macheReferenceScan);
  }

  public LaunchArguments withMinecraftProviderConfig(
      MinecraftProviderConfig updatedMinecraftProviderConfig) {
    return new LaunchArguments(
        gameMainClass,
        gameProviderId,
        launchArguments,
        validateOnly,
        explain,
        strictResources,
        strictPackages,
        updatedMinecraftProviderConfig,
        macheDirectory,
        macheVersion,
        macheReferenceScan);
  }
}
