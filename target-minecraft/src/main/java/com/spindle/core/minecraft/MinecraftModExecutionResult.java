package com.spindle.core.minecraft;

import java.util.List;
import java.util.Map;

public record MinecraftModExecutionResult(
    int schema,
    String milestoneName,
    String startMode,
    String resolvedMinecraftVersion,
    List<String> executableMods,
    List<String> entrypointsAttempted,
    List<String> entrypointsSucceeded,
    List<String> entrypointsFailed,
    List<String> entrypointInvocationOrder,
    List<String> failureReasons,
    Map<String, String> markerOutputs,
    MinecraftClassLoadingAudit.Summary classloadingAuditSummary,
    MinecraftExecutionProof proof,
    boolean minecraftMainInvoked,
    String minecraftMainClass,
    List<String> minecraftMainArgs,
    Integer processOutcome) {
  public MinecraftModExecutionResult {
    executableMods = List.copyOf(executableMods);
    entrypointsAttempted = List.copyOf(entrypointsAttempted);
    entrypointsSucceeded = List.copyOf(entrypointsSucceeded);
    entrypointsFailed = List.copyOf(entrypointsFailed);
    entrypointInvocationOrder = List.copyOf(entrypointInvocationOrder);
    failureReasons = List.copyOf(failureReasons);
    markerOutputs = java.util.Collections.unmodifiableMap(new java.util.TreeMap<>(markerOutputs));
    minecraftMainArgs = List.copyOf(minecraftMainArgs);
  }
}
