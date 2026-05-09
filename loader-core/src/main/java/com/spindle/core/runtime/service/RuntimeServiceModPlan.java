package com.spindle.core.runtime.service;

import java.util.Comparator;
import java.util.List;

public record RuntimeServiceModPlan(
    String modId, List<RuntimeServiceProviderPlan> provides, List<RuntimeServiceConsumerPlan> consumes) {
  public RuntimeServiceModPlan {
    provides =
        provides.stream()
            .sorted(
                Comparator.comparing(RuntimeServiceProviderPlan::id)
                    .thenComparing(RuntimeServiceProviderPlan::type)
                    .thenComparing(RuntimeServiceProviderPlan::implementation))
            .toList();
    consumes =
        consumes.stream()
            .sorted(
                Comparator.comparing(RuntimeServiceConsumerPlan::id)
                    .thenComparing(RuntimeServiceConsumerPlan::type)
                    .thenComparing(RuntimeServiceConsumerPlan::required))
            .toList();
  }
}
