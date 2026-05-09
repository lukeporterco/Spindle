package com.spindle.core.runtime;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.pipeline.ModpackPlanningResult;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CompiledModpackProfileCache {
  private final CompiledModpackProfileReader reader = new CompiledModpackProfileReader();
  private final CompiledModpackProfileWriter writer = new CompiledModpackProfileWriter();
  private final CompiledModpackProfileFingerprint fingerprint =
      new CompiledModpackProfileFingerprint();

  public CacheLookup lookup(
      LaunchContext context,
      ModpackPlanningResult planningResult,
      String gameSide,
      String inputFingerprint,
      String runtimePolicyFingerprint) {
    Path cachePath = cachePath(context, inputFingerprint);
    if (!Files.exists(cachePath)) {
      return new CacheLookup(false, "missing profile", cachePath, null);
    }
    try {
      CompiledModpackProfile profile = reader.read(cachePath);
      if (profile.schemaVersion() != CompiledModpackProfile.SCHEMA_VERSION) {
        return new CacheLookup(false, "schema mismatch", cachePath, null);
      }
      if (!CompiledModpackProfile.PROFILE_KIND.equals(profile.profileKind())) {
        return new CacheLookup(false, "profile kind mismatch", cachePath, null);
      }
      if (!CompiledModpackProfile.LOADER_ID.equals(profile.loader().id())
          || !context.loaderVersion().equals(profile.loader().version())) {
        return new CacheLookup(false, "loader mismatch", cachePath, null);
      }
      if (!planningResult.frozenModGraph().gameProviderId().equals(profile.game().id())
          || !planningResult.frozenModGraph().gameProviderVersion().equals(profile.game().version())
          || !gameSide.equals(profile.game().side())) {
        return new CacheLookup(false, "game mismatch", cachePath, null);
      }
      if (!inputFingerprint.equals(profile.inputFingerprint())) {
        return new CacheLookup(false, "input fingerprint mismatch", cachePath, null);
      }
      if (!runtimePolicyFingerprint.equals(profile.runtimePolicyFingerprint())) {
        return new CacheLookup(false, "runtime policy fingerprint mismatch", cachePath, null);
      }
      String actualFingerprint = fingerprint.compute(profile);
      if (!actualFingerprint.equals(profile.fingerprint())) {
        return new CacheLookup(false, "profile fingerprint mismatch", cachePath, null);
      }
      return new CacheLookup(true, "cache hit", cachePath, profile);
    } catch (LoaderException exception) {
      return new CacheLookup(false, "unreadable profile", cachePath, null);
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
