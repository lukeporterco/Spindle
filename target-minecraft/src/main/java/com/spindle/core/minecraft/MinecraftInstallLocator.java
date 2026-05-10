package com.spindle.core.minecraft;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

public final class MinecraftInstallLocator {
  public Optional<Path> defaultMinecraftDirectory() {
    String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    if (osName.contains("win")) {
      String appData = System.getenv("APPDATA");
      if (appData == null || appData.isBlank()) {
        return Optional.empty();
      }
      return Optional.of(Path.of(appData).resolve(".minecraft").toAbsolutePath().normalize());
    }
    if (osName.contains("mac")) {
      return Optional.of(
          Path.of(System.getProperty("user.home"))
              .resolve("Library/Application Support/minecraft")
              .toAbsolutePath()
              .normalize());
    }
    if (osName.contains("nux")
        || osName.contains("nix")
        || osName.contains("aix")
        || osName.contains("linux")) {
      return Optional.of(
          Path.of(System.getProperty("user.home"))
              .resolve(".minecraft")
              .toAbsolutePath()
              .normalize());
    }
    return Optional.empty();
  }

  public Path versionJsonPath(Path minecraftDirectory, String version) {
    return versionDirectory(minecraftDirectory, version)
        .resolve(version + ".json")
        .toAbsolutePath()
        .normalize();
  }

  public Path clientJarPath(Path minecraftDirectory, String version) {
    return versionDirectory(minecraftDirectory, version)
        .resolve(version + ".jar")
        .toAbsolutePath()
        .normalize();
  }

  public Path primaryServerJarPath(Path minecraftDirectory, String version) {
    return versionDirectory(minecraftDirectory, version)
        .resolve(version + "-server.jar")
        .toAbsolutePath()
        .normalize();
  }

  public Path alternateServerJarPath(Path minecraftDirectory, String version) {
    return versionDirectory(minecraftDirectory, version)
        .resolve("server.jar")
        .toAbsolutePath()
        .normalize();
  }

  public Path librariesRoot(Path minecraftDirectory) {
    return minecraftDirectory.resolve("libraries").toAbsolutePath().normalize();
  }

  public Path assetsRoot(Path minecraftDirectory) {
    return minecraftDirectory.resolve("assets").toAbsolutePath().normalize();
  }

  public Path assetIndexPath(Path minecraftDirectory, String assetIndexId) {
    return assetsRoot(minecraftDirectory)
        .resolve("indexes")
        .resolve(assetIndexId + ".json")
        .toAbsolutePath()
        .normalize();
  }

  public Path nativesDirectory(Path workingDirectory, String version, MinecraftSide side) {
    return workingDirectory
        .resolve("natives")
        .resolve(version)
        .resolve(side.id())
        .toAbsolutePath()
        .normalize();
  }

  private Path versionDirectory(Path minecraftDirectory, String version) {
    return minecraftDirectory.resolve("versions").resolve(version).toAbsolutePath().normalize();
  }
}
