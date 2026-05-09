package com.spindle.api;

import java.nio.file.Path;

public interface ModContext {
  String modId();

  String modVersion();

  String loaderVersion();

  String gameId();

  String gameVersion();

  String side();

  Path workingDirectory();

  Path configDirectory();

  Path dataDirectory();

  Path cacheDirectory();

  Path generatedDirectory();
}
