package com.spindle.core.minecraft;

import com.spindle.core.discovery.ModCandidate;
import com.spindle.core.metadata.ModMetadata;

public record MinecraftModCandidate(
    ModCandidate source, ModMetadata metadata, MinecraftJarScanResult scan) {
  public String id() {
    return metadata == null ? source.normalizedRelativePath() : metadata.id();
  }
}
