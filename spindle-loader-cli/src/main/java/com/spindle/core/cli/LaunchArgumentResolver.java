package com.spindle.core.cli;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.MinecraftInstallLocator;
import com.spindle.core.minecraft.MinecraftProviderConfig;
import com.spindle.core.minecraft.MinecraftSide;
import java.nio.file.Path;

public final class LaunchArgumentResolver {
  public LaunchArguments resolve(Path workingDirectory, LaunchArguments launchArguments)
      throws LoaderException {
    MinecraftProviderConfig resolvedMinecraftProviderConfig =
        launchArguments.minecraftProviderConfig().resolveAgainst(workingDirectory);
    if (!"minecraft".equals(launchArguments.gameProviderId())
        && resolvedMinecraftProviderConfig.realSmoke()) {
      throw new LoaderException("--minecraft-real-smoke requires --game-provider minecraft");
    }
    if ("minecraft".equals(launchArguments.gameProviderId())
        && resolvedMinecraftProviderConfig.minecraftDirectory() == null
        && resolvedMinecraftProviderConfig.side() == MinecraftSide.CLIENT
        && !resolvedMinecraftProviderConfig.cacheInspect()) {
      resolvedMinecraftProviderConfig =
          resolvedMinecraftProviderConfig
              .withMinecraftDirectory(
                  new MinecraftInstallLocator().defaultMinecraftDirectory().orElse(null))
              .resolveAgainst(workingDirectory);
    }

    if ("minecraft".equals(launchArguments.gameProviderId())
        && !resolvedMinecraftProviderConfig.dryRun()) {
      throw new LoaderException(
          "Minecraft provider requires --minecraft-dry-run until managed Minecraft runtime ownership is explicitly requested.");
    }
    if ("minecraft".equals(launchArguments.gameProviderId())
        && resolvedMinecraftProviderConfig.launch()) {
      if (resolvedMinecraftProviderConfig.side() != MinecraftSide.SERVER) {
        throw new LoaderException("Minecraft server launch requires --minecraft-side server");
      }
      if (!resolvedMinecraftProviderConfig.verifyFiles()) {
        throw new LoaderException("Minecraft server launch requires --minecraft-verify-files");
      }
      if (resolvedMinecraftProviderConfig.serverDirectory() == null
          && resolvedMinecraftProviderConfig.requestedVersion() != null
          && !resolvedMinecraftProviderConfig.requestedVersion().isBlank()) {
        resolvedMinecraftProviderConfig =
            resolvedMinecraftProviderConfig.withServerDirectory(
                workingDirectory
                    .resolve("minecraft-server")
                    .resolve(resolvedMinecraftProviderConfig.requestedVersion()));
      }
    }

    return launchArguments
        .withMinecraftProviderConfig(resolvedMinecraftProviderConfig)
        .withMacheDirectory(
            CliParsing.resolveOptionalPath(workingDirectory, launchArguments.macheDirectory()));
  }
}
