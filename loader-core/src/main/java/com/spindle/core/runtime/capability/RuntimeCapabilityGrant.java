package com.spindle.core.runtime.capability;

import java.util.List;

public record RuntimeCapabilityGrant(
    String capability,
    String state,
    List<String> sources,
    String reason,
    String controls,
    String fix) {
  public RuntimeCapabilityGrant {
    sources = sources.stream().sorted(RuntimeCapabilityCatalog.sourceComparator()).toList();
  }
}
