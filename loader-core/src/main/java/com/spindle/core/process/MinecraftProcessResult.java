package com.spindle.core.process;

import java.util.List;

public record MinecraftProcessResult(
    int schema,
    String minecraftVersion,
    String serverDirectory,
    String serverJar,
    String javaExecutable,
    List<String> jvmArgs,
    List<String> serverArgs,
    List<String> commandPreview,
    boolean started,
    boolean readyDetected,
    boolean stopRequested,
    Integer exitCode,
    boolean timedOut,
    long durationMs,
    String stdoutTail,
    String stderrTail) {
  public MinecraftProcessResult {
    jvmArgs = List.copyOf(jvmArgs);
    serverArgs = List.copyOf(serverArgs);
    commandPreview = List.copyOf(commandPreview);
    stdoutTail = stdoutTail == null ? "" : stdoutTail;
    stderrTail = stderrTail == null ? "" : stderrTail;
  }
}
