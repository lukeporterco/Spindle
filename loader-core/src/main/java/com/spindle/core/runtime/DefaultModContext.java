package com.spindle.core.runtime;

import com.spindle.api.ModContext;
import com.spindle.core.runtime.capability.RuntimeCapabilityCatalog;
import java.nio.file.Path;
import java.util.Set;

public record DefaultModContext(
    String modId,
    String modVersion,
    String loaderVersion,
    String gameId,
    String gameVersion,
    String side,
    Path workingDirectory,
    Set<String> grantedCapabilities,
    Path configDirectory,
    Path dataDirectory,
    Path cacheDirectory,
    Path generatedDirectory)
    implements ModContext {
  public DefaultModContext {
    grantedCapabilities = Set.copyOf(grantedCapabilities);
  }

  @Override
  public Path configDirectory() {
    return requireCapability(
        "configDirectory()",
        RuntimeCapabilityCatalog.STORAGE_CONFIG,
        "storage.config",
        configDirectory);
  }

  @Override
  public Path dataDirectory() {
    return requireCapability(
        "dataDirectory()",
        RuntimeCapabilityCatalog.STORAGE_DATA,
        "storage.data",
        dataDirectory);
  }

  @Override
  public Path cacheDirectory() {
    return requireCapability(
        "cacheDirectory()",
        RuntimeCapabilityCatalog.STORAGE_CACHE,
        "storage.cache",
        cacheDirectory);
  }

  @Override
  public Path generatedDirectory() {
    return requireCapability(
        "generatedDirectory()",
        RuntimeCapabilityCatalog.STORAGE_GENERATED,
        "storage.generated",
        generatedDirectory);
  }

  private Path requireCapability(
      String methodName, String capability, String storageFlag, Path directory) {
    if (hasCapability(capability)) {
      return directory;
    }
    throw new IllegalStateException(
        "Mod `"
            + modId
            + "` cannot access "
            + methodName
            + " because capability `"
            + capability
            + "` was not granted. Enable "
            + storageFlag
            + " in loader.mod.json.");
  }
}
