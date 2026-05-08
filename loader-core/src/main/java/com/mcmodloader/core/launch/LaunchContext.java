package com.mcmodloader.core.launch;

import java.nio.file.Path;

public record LaunchContext(
    Path workingDirectory,
    Path modsDirectory,
    String gameMainClass,
    String loaderVersion,
    int javaMajorVersion,
    String targetMinecraftVersion
) {
}
