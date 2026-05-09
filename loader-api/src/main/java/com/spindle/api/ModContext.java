package com.spindle.api;

import com.spindle.api.config.ModConfig;
import com.spindle.api.service.ServiceRegistry;
import java.nio.file.Path;
import java.util.Set;

public interface ModContext {
  String modId();

  String modVersion();

  String loaderVersion();

  String gameId();

  String gameVersion();

  String side();

  Path workingDirectory();

  default Set<String> grantedCapabilities() {
    return Set.of();
  }

  default boolean hasCapability(String capability) {
    return grantedCapabilities().contains(capability);
  }

  default ServiceRegistry services() {
    return ServiceRegistry.empty();
  }

  default ModConfig config() {
    return ModConfig.empty();
  }

  Path configDirectory();

  Path dataDirectory();

  Path cacheDirectory();

  Path generatedDirectory();
}
