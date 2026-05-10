package com.spindle.core.runtime;

import com.spindle.api.ModContext;
import com.spindle.api.config.ModConfig;
import com.spindle.api.exception.CapabilityDeniedException;
import com.spindle.api.service.ServiceRegistry;
import com.spindle.core.runtime.capability.RuntimeCapabilityCatalog;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public record DefaultModContext(
    String modId,
    String modVersion,
    String loaderVersion,
    String gameId,
    String gameVersion,
    String side,
    Path workingDirectory,
    Set<String> grantedCapabilities,
    ModConfig config,
    ServiceRegistry services,
    Path configDirectory,
    Path dataDirectory,
    Path cacheDirectory,
    Path generatedDirectory)
    implements ModContext {
  public DefaultModContext {
    TreeSet<String> sortedCapabilities = new TreeSet<>(grantedCapabilities);
    grantedCapabilities = Collections.unmodifiableSet(new LinkedHashSet<>(sortedCapabilities));
    config = config == null ? ModConfig.empty() : config;
    services = services == null ? ServiceRegistry.empty() : services;
  }

  @Override
  public Path configDirectory() {
    return requireStorageCapability(
        "configDirectory()",
        RuntimeCapabilityCatalog.STORAGE_CONFIG,
        "storage.config",
        configDirectory);
  }

  @Override
  public Path dataDirectory() {
    return requireStorageCapability(
        "dataDirectory()",
        RuntimeCapabilityCatalog.STORAGE_DATA,
        "storage.data",
        dataDirectory);
  }

  @Override
  public Path cacheDirectory() {
    return requireStorageCapability(
        "cacheDirectory()",
        RuntimeCapabilityCatalog.STORAGE_CACHE,
        "storage.cache",
        cacheDirectory);
  }

  @Override
  public Path generatedDirectory() {
    return requireStorageCapability(
        "generatedDirectory()",
        RuntimeCapabilityCatalog.STORAGE_GENERATED,
        "storage.generated",
        generatedDirectory);
  }

  private Path requireStorageCapability(
      String methodName, String capability, String storageFlag, Path directory) {
    if (hasCapability(capability)) {
      return directory;
    }
    throw new CapabilityDeniedException(
        modId,
        capability,
        methodName,
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
