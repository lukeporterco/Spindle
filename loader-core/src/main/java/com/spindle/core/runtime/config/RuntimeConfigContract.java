package com.spindle.core.runtime.config;

import java.util.Comparator;
import java.util.List;

public record RuntimeConfigContract(
    int contractVersion,
    String scope,
    String format,
    List<RuntimeConfigModPlan> mods,
    RuntimeConfigSummary summary) {
  public static final int CONTRACT_VERSION = 1;
  public static final String SCOPE = "spindle-api-only";
  public static final String FORMAT = "flat-json-object";

  public RuntimeConfigContract {
    mods = mods.stream().sorted(Comparator.comparing(RuntimeConfigModPlan::modId)).toList();
    summary = summary == null ? RuntimeConfigSummary.fromMods(mods) : summary;
  }

  public static RuntimeConfigContract empty() {
    return new RuntimeConfigContract(0, SCOPE, FORMAT, List.of(), RuntimeConfigSummary.empty());
  }
}
