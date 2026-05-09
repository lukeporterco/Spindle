package com.spindle.core.process;

import java.nio.file.Path;
import java.util.List;

public record MinecraftProcessConfig(
    Path serverDirectory,
    Path serverJar,
    Path javaExecutable,
    List<String> jvmArgs,
    List<String> serverArgs,
    int timeoutSeconds,
    boolean stopAfterReady,
    int readyTimeoutSeconds,
    boolean acceptEulaForTest) {
  public MinecraftProcessConfig {
    jvmArgs = List.copyOf(jvmArgs);
    serverArgs = List.copyOf(serverArgs);
  }
}
