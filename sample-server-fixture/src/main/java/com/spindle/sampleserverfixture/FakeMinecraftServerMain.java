package com.spindle.sampleserverfixture;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FakeMinecraftServerMain {
  private FakeMinecraftServerMain() {}

  public static void main(String[] args) throws Exception {
    boolean skipReady = false;
    boolean writeStderr = false;
    int sleepSeconds = 0;
    String bootstrapMarker = null;

    for (int index = 0; index < args.length; index++) {
      String argument = args[index];
      if ("--skip-ready".equals(argument)) {
        skipReady = true;
        continue;
      }
      if ("--write-stderr".equals(argument)) {
        writeStderr = true;
        continue;
      }
      if ("--sleep-seconds".equals(argument) && index + 1 < args.length) {
        sleepSeconds = Integer.parseInt(args[++index]);
        continue;
      }
      if ("--bootstrap-marker".equals(argument) && index + 1 < args.length) {
        bootstrapMarker = args[++index];
      }
    }

    System.out.println("Starting fake Minecraft server");
    if (writeStderr) {
      System.err.println("Fake server stderr line");
    }
    if (!skipReady) {
      System.out.println("Done (0.1s)! For help, type \"help\"");
    }
    if (sleepSeconds > 0) {
      Thread.sleep(sleepSeconds * 1_000L);
    }
    if (bootstrapMarker != null && !bootstrapMarker.isBlank()) {
      Path markerPath = Path.of(bootstrapMarker);
      Path parent = markerPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      Files.writeString(markerPath, "fake-server-main-invoked\n", StandardCharsets.UTF_8);
      return;
    }

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if ("stop".equals(line.trim())) {
          System.out.println("Stopping fake Minecraft server");
          return;
        }
      }
    }
  }
}
