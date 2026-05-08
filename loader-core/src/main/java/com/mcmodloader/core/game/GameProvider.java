package com.mcmodloader.core.game;

import com.mcmodloader.core.launch.LaunchContext;

public interface GameProvider {
    String id();

    String displayName();

    String version();

    void validate(LaunchContext context) throws Exception;

    void launch(LaunchContext context, ClassLoader classLoader) throws Exception;
}
