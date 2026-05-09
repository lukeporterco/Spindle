package com.spindle.core.runtime.service;

import java.util.List;

public record RuntimeServiceSummary(
    int providers,
    int consumers,
    int bindings,
    int availableProviders,
    int conflictingProviders,
    int missingImplementations,
    int implementationOwnershipViolations,
    int requiredUnbound,
    int optionalUnbound,
    int typeMismatches,
    int fatalCount,
    int warningCount) {
  static RuntimeServiceSummary from(
      List<RuntimeServiceModPlan> mods, List<RuntimeServiceBinding> bindings) {
    int providers = 0;
    int consumers = 0;
    int availableProviders = 0;
    int conflictingProviders = 0;
    int missingImplementations = 0;
    int implementationOwnershipViolations = 0;
    for (RuntimeServiceModPlan mod : mods) {
      providers += mod.provides().size();
      consumers += mod.consumes().size();
      for (RuntimeServiceProviderPlan provider : mod.provides()) {
        switch (provider.state()) {
          case RuntimeServiceStates.AVAILABLE -> availableProviders++;
          case RuntimeServiceStates.CONFLICT -> conflictingProviders++;
          case RuntimeServiceStates.IMPLEMENTATION_MISSING -> missingImplementations++;
          case RuntimeServiceStates.IMPLEMENTATION_NOT_OWNED ->
              implementationOwnershipViolations++;
          default -> throw new IllegalArgumentException("Unsupported provider state " + provider.state());
        }
      }
    }

    int requiredUnbound = 0;
    int optionalUnbound = 0;
    int typeMismatches = 0;
    for (RuntimeServiceBinding binding : bindings) {
      switch (binding.state()) {
        case RuntimeServiceStates.BOUND, RuntimeServiceStates.PROVIDER_CONFLICT -> {}
        case RuntimeServiceStates.REQUIRED_UNBOUND -> requiredUnbound++;
        case RuntimeServiceStates.OPTIONAL_UNBOUND -> optionalUnbound++;
        case RuntimeServiceStates.TYPE_MISMATCH -> typeMismatches++;
        default -> throw new IllegalArgumentException("Unsupported binding state " + binding.state());
      }
    }

    int fatalCount =
        conflictingProviders
            + missingImplementations
            + implementationOwnershipViolations
            + requiredUnbound
            + typeMismatches;
    return new RuntimeServiceSummary(
        providers,
        consumers,
        bindings.size(),
        availableProviders,
        conflictingProviders,
        missingImplementations,
        implementationOwnershipViolations,
        requiredUnbound,
        optionalUnbound,
        typeMismatches,
        fatalCount,
        optionalUnbound);
  }

  public static RuntimeServiceSummary empty() {
    return new RuntimeServiceSummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
  }
}
