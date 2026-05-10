package com.spindle.core.runtime.capability;

import java.util.List;

public record RuntimeCapabilitySummary(
    int granted, int denied, int unavailable, int unknown, int visibilityOnly) {
  public static RuntimeCapabilitySummary fromGrants(List<RuntimeCapabilityGrant> grants) {
    int granted = 0;
    int denied = 0;
    int unavailable = 0;
    int unknown = 0;
    int visibilityOnly = 0;
    for (RuntimeCapabilityGrant grant : grants) {
      switch (grant.state()) {
        case "granted" -> granted++;
        case "denied" -> denied++;
        case "unavailable" -> unavailable++;
        case "unknown" -> unknown++;
        case "visibility-only" -> visibilityOnly++;
        default -> throw new IllegalArgumentException("Unsupported capability state " + grant.state());
      }
    }
    return new RuntimeCapabilitySummary(granted, denied, unavailable, unknown, visibilityOnly);
  }

  public RuntimeCapabilitySummary plus(RuntimeCapabilitySummary other) {
    return new RuntimeCapabilitySummary(
        granted + other.granted,
        denied + other.denied,
        unavailable + other.unavailable,
        unknown + other.unknown,
        visibilityOnly + other.visibilityOnly);
  }

  public static RuntimeCapabilitySummary empty() {
    return new RuntimeCapabilitySummary(0, 0, 0, 0, 0);
  }
}
