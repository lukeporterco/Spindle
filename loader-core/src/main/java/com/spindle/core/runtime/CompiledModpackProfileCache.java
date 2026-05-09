package com.spindle.core.runtime;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CompiledModpackProfileCache {
  private final CompiledModpackProfileReader reader = new CompiledModpackProfileReader();
  private final CompiledModpackProfileWriter writer = new CompiledModpackProfileWriter();

  public CacheLookup lookup(LaunchContext context, String inputFingerprint) {
    Path cachePath = cachePath(context, inputFingerprint);
    if (!Files.exists(cachePath)) {
      return new CacheLookup(false, "profile not found", cachePath, null);
    }
    try {
      CompiledModpackProfile profile = reader.read(cachePath);
      if (!inputFingerprint.equals(profile.inputFingerprint())) {
        return new CacheLookup(false, "input fingerprint mismatch", cachePath, null);
      }
      return new CacheLookup(true, "input fingerprint matched", cachePath, profile);
    } catch (LoaderException exception) {
      return new CacheLookup(false, "cache read failed", cachePath, null);
    }
  }

  public void store(LaunchContext context, String inputFingerprint, CompiledModpackProfile profile)
      throws LoaderException {
    writer.write(cachePath(context, inputFingerprint), profile);
  }

  public Path cachePath(LaunchContext context, String inputFingerprint) {
    return context
        .workingDirectory()
        .resolve(".spindle")
        .resolve("profile-cache")
        .resolve(inputFingerprint)
        .resolve("spindle.profile.json");
  }

  public record CacheLookup(
      boolean hit, String reason, Path cachePath, CompiledModpackProfile profile) {}
}
