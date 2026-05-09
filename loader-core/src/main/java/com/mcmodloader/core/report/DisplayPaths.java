package com.mcmodloader.core.report;

import com.mcmodloader.core.launch.LaunchContext;
import java.nio.file.Path;

public final class DisplayPaths {
  private DisplayPaths() {}

  public static String displayPath(LaunchContext context, Path path) {
    if (path == null) {
      return null;
    }

    Path normalizedWorkingDirectory = context.workingDirectory().toAbsolutePath().normalize();
    Path normalizedPath = path.toAbsolutePath().normalize();
    try {
      return normalizedWorkingDirectory.relativize(normalizedPath).toString().replace('\\', '/');
    } catch (IllegalArgumentException exception) {
      return normalizedPath.toString().replace('\\', '/');
    }
  }
}
