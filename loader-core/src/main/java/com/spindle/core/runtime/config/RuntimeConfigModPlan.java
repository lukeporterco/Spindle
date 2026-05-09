package com.spindle.core.runtime.config;

import java.util.Comparator;
import java.util.List;

public record RuntimeConfigModPlan(
    String modId,
    String path,
    boolean runtimeWrites,
    String state,
    List<RuntimeConfigEntryPlan> entries,
    List<String> unknownKeys,
    RuntimeConfigSummary summary,
    String findingCode) {
  public RuntimeConfigModPlan {
    entries = entries.stream().sorted(Comparator.comparing(RuntimeConfigEntryPlan::key)).toList();
    unknownKeys = unknownKeys.stream().sorted().toList();
    summary =
        summary == null
            ? RuntimeConfigSummary.modSummary(entries, unknownKeys.size(), 0, 0)
            : summary;
  }
}
