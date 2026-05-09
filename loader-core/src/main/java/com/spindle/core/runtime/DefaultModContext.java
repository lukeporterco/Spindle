package com.spindle.core.runtime;

import com.spindle.api.ModContext;
import java.nio.file.Path;

public record DefaultModContext(
    String modId,
    String modVersion,
    String loaderVersion,
    String gameId,
    String gameVersion,
    String side,
    Path workingDirectory,
    Path configDirectory,
    Path dataDirectory,
    Path cacheDirectory,
    Path generatedDirectory)
    implements ModContext {}
