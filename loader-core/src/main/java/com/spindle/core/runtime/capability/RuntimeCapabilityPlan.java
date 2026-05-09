package com.spindle.core.runtime.capability;

import java.util.Comparator;
import java.util.List;

public record RuntimeCapabilityPlan(
    int catalogVersion,
    String scope,
    String runtimeExecutionIsolationMode,
    boolean sandboxed,
    List<RuntimeCapabilityModPlan> mods,
    RuntimeCapabilitySummary summary) {
  public RuntimeCapabilityPlan {
    mods = mods.stream().sorted(Comparator.comparing(RuntimeCapabilityModPlan::modId)).toList();
    summary =
        summary == null
            ? mods.stream()
                .map(RuntimeCapabilityModPlan::summary)
                .reduce(RuntimeCapabilitySummary.empty(), RuntimeCapabilitySummary::plus)
            : summary;
  }
}
