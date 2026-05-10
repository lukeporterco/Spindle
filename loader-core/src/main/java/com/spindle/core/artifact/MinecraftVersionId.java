package com.spindle.core.artifact;

import com.spindle.core.diagnostics.LoaderException;
import java.util.regex.Pattern;

public final class MinecraftVersionId {
  private static final Pattern SAFE_VERSION_PATTERN = Pattern.compile("[A-Za-z0-9._-]{1,80}");

  private MinecraftVersionId() {}

  public static String requireSafe(String version) throws LoaderException {
    if (version == null) {
      throw invalid(version);
    }
    String normalized = version.trim();
    if (normalized.isEmpty()
        || ".".equals(normalized)
        || "..".equals(normalized)
        || normalized.contains("..")
        || normalized.indexOf('/') >= 0
        || normalized.indexOf('\\') >= 0
        || normalized.indexOf(':') >= 0
        || normalized.chars().anyMatch(Character::isISOControl)
        || !SAFE_VERSION_PATTERN.matcher(normalized).matches()) {
      throw invalid(version);
    }
    return normalized;
  }

  private static LoaderException invalid(String version) {
    return new LoaderException(
        "Minecraft version id `"
            + String.valueOf(version)
            + "` is not a safe path component. Expected `[A-Za-z0-9._-]{1,80}`.");
  }
}
