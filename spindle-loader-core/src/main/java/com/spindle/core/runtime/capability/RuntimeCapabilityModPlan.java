package com.spindle.core.runtime.capability;

import java.util.Comparator;
import java.util.List;

public record RuntimeCapabilityModPlan(
    String modId,
    List<String> requested,
    List<RuntimeCapabilityGrant> grants,
    RuntimeCapabilitySummary summary) {
  public RuntimeCapabilityModPlan {
    requested = List.copyOf(requested);
    grants =
        grants.stream()
            .sorted(
                Comparator.comparing(RuntimeCapabilityGrant::capability)
                    .thenComparing(grant -> String.join("\u0000", grant.sources())))
            .toList();
    summary = summary == null ? RuntimeCapabilitySummary.fromGrants(grants) : summary;
  }
}
