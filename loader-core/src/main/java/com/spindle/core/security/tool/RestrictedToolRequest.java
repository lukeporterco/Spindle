package com.spindle.core.security.tool;

import com.spindle.core.resolve.ResolvedModSet;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public record RestrictedToolRequest(
    String worker, Path workingDirectory, Path outputPath, List<ModInput> mods) {
  public static final String STATIC_RISK_SCAN_WORKER = "static-risk-scan";
  private static final String DEFAULT_OUTPUT_PATH =
      ".spindle/security-tools/static-risk-scan/output.json";

  public RestrictedToolRequest {
    worker = normalizeText(worker);
    workingDirectory = workingDirectory.toAbsolutePath().normalize();
    outputPath = outputPath.toAbsolutePath().normalize();
    mods = mods.stream().sorted(Comparator.comparing(ModInput::relativePath)).toList();
  }

  public static RestrictedToolRequest staticRiskScan(
      Path workingDirectory, List<ResolvedModSet.ResolvedMod> mods) {
    Path normalizedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
    return new RestrictedToolRequest(
        STATIC_RISK_SCAN_WORKER,
        normalizedWorkingDirectory,
        normalizedWorkingDirectory.resolve(DEFAULT_OUTPUT_PATH),
        mods.stream().map(ModInput::fromResolvedMod).toList());
  }

  public String relativeOutputPath() {
    return displayPath(workingDirectory, outputPath);
  }

  public List<com.spindle.core.security.risk.StaticRiskAnalyzer.TargetMod> targetMods() {
    return mods.stream().map(ModInput::toTargetMod).toList();
  }

  public static String displayPath(Path workingDirectory, Path path) {
    Path normalizedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
    Path normalizedPath = path.toAbsolutePath().normalize();
    try {
      return normalizedWorkingDirectory.relativize(normalizedPath).toString().replace('\\', '/');
    } catch (IllegalArgumentException exception) {
      return normalizedPath.toString().replace('\\', '/');
    }
  }

  private static String normalizeText(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  public record ModInput(String modId, String relativePath, String sha256, Path jarPath) {
    public ModInput {
      modId = normalizeText(modId);
      relativePath = normalizeRelativePath(relativePath);
      sha256 = normalizeText(sha256);
      jarPath = jarPath.toAbsolutePath().normalize();
    }

    public static ModInput fromResolvedMod(ResolvedModSet.ResolvedMod mod) {
      return new ModInput(mod.id(), mod.normalizedRelativePath(), mod.sha256(), mod.jarPath());
    }

    public com.spindle.core.security.risk.StaticRiskAnalyzer.TargetMod toTargetMod() {
      return new com.spindle.core.security.risk.StaticRiskAnalyzer.TargetMod(
          modId, relativePath, jarPath);
    }

    private static String normalizeRelativePath(String value) {
      String normalized = normalizeText(value);
      return normalized == null ? null : normalized.replace('\\', '/');
    }
  }
}
