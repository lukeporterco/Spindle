package com.mcmodloader.core.launch;

import java.nio.file.Path;
import java.util.List;

public record LaunchContext(
    Path workingDirectory,
    Path modsDirectory,
    String gameMainClass,
    String gameProviderId,
    List<String> launchArguments,
    String loaderVersion,
    int javaMajorVersion,
    String targetMinecraftVersion
) {
    public LaunchContext {
        workingDirectory = workingDirectory.toAbsolutePath().normalize();
        modsDirectory = modsDirectory.toAbsolutePath().normalize();
        launchArguments = List.copyOf(launchArguments);
    }
}
