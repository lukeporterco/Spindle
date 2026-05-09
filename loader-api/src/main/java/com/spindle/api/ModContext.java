package com.spindle.api;

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

  Set<String> grantedCapabilities();

  default boolean hasCapability(String capability) {
    return grantedCapabilities().contains(capability);
  }

  Path configDirectory();

  Path dataDirectory();

  Path cacheDirectory();

  Path generatedDirectory();
}
