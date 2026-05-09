package com.spindle.core.runtime.service;

import java.util.Comparator;
import java.util.List;

public record RuntimeServiceContract(
    int contractVersion,
    String scope,
    String providerInstantiation,
    List<RuntimeServiceModPlan> mods,
    List<RuntimeServiceBinding> bindings,
    RuntimeServiceSummary summary) {
  public static final int CONTRACT_VERSION = 1;
  public static final String SCOPE = "spindle-api-only";
  public static final String PROVIDER_INSTANTIATION = "lazy-singleton-after-security-gate";

  public RuntimeServiceContract {
    mods = mods.stream().sorted(Comparator.comparing(RuntimeServiceModPlan::modId)).toList();
    bindings =
        bindings.stream()
            .sorted(
                Comparator.comparing(RuntimeServiceBinding::id)
                    .thenComparing(RuntimeServiceBinding::consumerModId)
                    .thenComparing(binding -> binding.providerModId() == null ? "" : binding.providerModId()))
            .toList();
    summary = summary == null ? RuntimeServiceSummary.from(mods, bindings) : summary;
  }

  public static RuntimeServiceContract empty() {
    return new RuntimeServiceContract(
        0, SCOPE, PROVIDER_INSTANTIATION, List.of(), List.of(), RuntimeServiceSummary.empty());
  }
}
