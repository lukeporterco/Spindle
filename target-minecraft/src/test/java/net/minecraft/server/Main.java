package net.minecraft.server;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {
  private Main() {}

  public static void main(String[] args) {
    try {
      writeMarker(args);
      failIfRequested(args);
    } catch (RuntimeException exception) {
      throw exception;
    }
  }

  private static void writeMarker(String[] args) {
    try {
      if (args.length > 0 && args[0] != null && !args[0].isBlank()) {
        Path markerPath = Path.of(args[0]);
        Path parent = markerPath.getParent();
        if (parent != null) {
          Files.createDirectories(parent);
        }
        Files.writeString(markerPath, "hook-installed-main", StandardCharsets.UTF_8);
      }
    } catch (Exception exception) {
      throw new RuntimeException("failed to write main marker", exception);
    }
  }

  private static void failIfRequested(String[] args) {
    if (args.length > 1 && "--fail".equals(args[1])) {
      throw new IllegalStateException("hook-installed main failed intentionally");
    }
  }
}
