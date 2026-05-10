package com.spindle.api;

import com.spindle.api.config.ModConfig;
import com.spindle.api.exception.CapabilityDeniedException;
import com.spindle.api.service.ServiceRegistry;
import java.nio.file.Path;
import java.util.Set;

/**
 * Stable Runtime API-0 lifecycle context for a loaded Spindle mod.
 *
 * <p>This interface exposes runtime identity, capability inspection, owned storage access, config
 * access, and service access. It does not imply sandboxing, target integration, or access to
 * unavailable capability surfaces.
 */
public interface ModContext {
  /** Returns the current mod id. */
  String modId();

  /** Returns the current mod version. */
  String modVersion();

  /** Returns the current Spindle loader version. */
  String loaderVersion();

  /** Returns the logical game id selected for the runtime. */
  String gameId();

  /** Returns the logical game version selected for the runtime. */
  String gameVersion();

  /** Returns the active side identifier. */
  String side();

  /** Returns the loader working directory for the active runtime. */
  Path workingDirectory();

  /** Returns granted capabilities in deterministic sorted order when provided by Spindle. */
  default Set<String> grantedCapabilities() {
    return Set.of();
  }

  /** Returns whether the named capability was granted to this mod. */
  default boolean hasCapability(String capability) {
    return grantedCapabilities().contains(capability);
  }

  /**
   * Requires a granted capability for the current context.
   *
   * @throws CapabilityDeniedException if the capability was not granted
   */
  default void requireCapability(String capability) {
    if (!hasCapability(capability)) {
      throw new CapabilityDeniedException(
          modId(),
          capability,
          "requireCapability",
          "Mod `"
              + modId()
              + "` requires capability `"
              + capability
              + "`, but it was not granted.");
    }
  }

  /** Returns the runtime service registry for declared service consumers. */
  default ServiceRegistry services() {
    return ServiceRegistry.empty();
  }

  /** Returns the runtime config view for declared schema-2 config entries. */
  default ModConfig config() {
    return ModConfig.empty();
  }

  /**
   * Returns the owned config directory for this mod.
   *
   * @throws CapabilityDeniedException in Spindle runtime contexts when {@code storage.config} was
   *     not granted
   */
  Path configDirectory();

  /**
   * Returns the owned data directory for this mod.
   *
   * @throws CapabilityDeniedException in Spindle runtime contexts when {@code storage.data} was not
   *     granted
   */
  Path dataDirectory();

  /**
   * Returns the owned cache directory for this mod.
   *
   * @throws CapabilityDeniedException in Spindle runtime contexts when {@code storage.cache} was
   *     not granted
   */
  Path cacheDirectory();

  /**
   * Returns the owned generated-output directory for this mod.
   *
   * @throws CapabilityDeniedException in Spindle runtime contexts when {@code storage.generated}
   *     was not granted
   */
  Path generatedDirectory();
}
