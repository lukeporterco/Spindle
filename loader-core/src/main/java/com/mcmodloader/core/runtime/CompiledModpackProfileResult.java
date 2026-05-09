package com.mcmodloader.core.runtime;

import java.nio.file.Path;

public record CompiledModpackProfileResult(
    Path outputPath, int schemaVersion, String profileKind, String fingerprint) {
  public CompiledModpackProfileResult {
    outputPath = outputPath.toAbsolutePath().normalize();
  }
}
