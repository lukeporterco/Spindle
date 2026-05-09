package com.spindle.core.runtime.service;

import com.spindle.core.metadata.ModMetadata;
import com.spindle.core.ownership.ClassOwnershipIndex;
import com.spindle.core.resolve.ResolvedModSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RuntimeServicePlanner {
  public RuntimeServiceContract plan(
      ResolvedModSet resolvedMods, ClassOwnershipIndex classOwnershipIndex) {
    List<DeclaredProvider> declaredProviders = new ArrayList<>();
    Map<String, List<DeclaredProvider>> providersById = new LinkedHashMap<>();
    for (ResolvedModSet.ResolvedMod mod : resolvedMods.mods()) {
      for (ModMetadata.ServiceProvider provider : mod.services().provides()) {
        DeclaredProvider declaredProvider = new DeclaredProvider(mod.id(), provider);
        declaredProviders.add(declaredProvider);
        providersById.computeIfAbsent(provider.id(), ignored -> new ArrayList<>()).add(declaredProvider);
      }
    }

    Map<DeclaredProvider, RuntimeServiceProviderPlan> providerPlans = new HashMap<>();
    for (DeclaredProvider provider : declaredProviders) {
      providerPlans.put(provider, planProvider(provider, providersById.get(provider.id()), classOwnershipIndex));
    }

    List<RuntimeServiceModPlan> mods = new ArrayList<>();
    List<RuntimeServiceBinding> bindings = new ArrayList<>();
    for (ResolvedModSet.ResolvedMod mod : resolvedMods.mods()) {
      List<RuntimeServiceProviderPlan> provides = new ArrayList<>();
      for (ModMetadata.ServiceProvider provider : mod.services().provides()) {
        provides.add(providerPlans.get(new DeclaredProvider(mod.id(), provider)));
      }

      List<RuntimeServiceConsumerPlan> consumes = new ArrayList<>();
      for (ModMetadata.ServiceConsumer consumer : mod.services().consumes()) {
        ConsumerBinding consumerBinding =
            planConsumer(mod.id(), consumer, providersById.getOrDefault(consumer.id(), List.of()), providerPlans);
        consumes.add(consumerBinding.consumerPlan());
        bindings.add(consumerBinding.binding());
      }
      mods.add(new RuntimeServiceModPlan(mod.id(), provides, consumes));
    }

    return new RuntimeServiceContract(
        RuntimeServiceContract.CONTRACT_VERSION,
        RuntimeServiceContract.SCOPE,
        RuntimeServiceContract.PROVIDER_INSTANTIATION,
        mods,
        bindings,
        null);
  }

  private RuntimeServiceProviderPlan planProvider(
      DeclaredProvider provider,
      List<DeclaredProvider> sameServiceIdProviders,
      ClassOwnershipIndex classOwnershipIndex) {
    if (sameServiceIdProviders.size() > 1) {
      return new RuntimeServiceProviderPlan(
          provider.id(),
          provider.type(),
          provider.implementation(),
          RuntimeServiceStates.CONFLICT,
          "Multiple mods provide service `"
              + provider.id()
              + "`, so Runtime-3 does not select a provider.");
    }
    String implementationOwner =
        classOwnershipIndex.ownerOfClass(provider.implementation()).orElse(null);
    if (implementationOwner == null) {
      return new RuntimeServiceProviderPlan(
          provider.id(),
          provider.type(),
          provider.implementation(),
          RuntimeServiceStates.IMPLEMENTATION_MISSING,
          "Provider implementation `"
              + provider.implementation()
              + "` is not present in the class ownership index.");
    }
    if (!provider.modId().equals(implementationOwner)) {
      return new RuntimeServiceProviderPlan(
          provider.id(),
          provider.type(),
          provider.implementation(),
          RuntimeServiceStates.IMPLEMENTATION_NOT_OWNED,
          "Provider implementation `"
              + provider.implementation()
              + "` is owned by mod `"
              + implementationOwner
              + "`, not declaring mod `"
              + provider.modId()
              + "`.");
    }
    return new RuntimeServiceProviderPlan(
        provider.id(),
        provider.type(),
        provider.implementation(),
        RuntimeServiceStates.AVAILABLE,
        "Provider implementation is owned by the declaring mod.");
  }

  private ConsumerBinding planConsumer(
      String consumerModId,
      ModMetadata.ServiceConsumer consumer,
      List<DeclaredProvider> sameServiceIdProviders,
      Map<DeclaredProvider, RuntimeServiceProviderPlan> providerPlans) {
    if (sameServiceIdProviders.size() > 1) {
      return new ConsumerBinding(
          new RuntimeServiceConsumerPlan(
              consumer.id(),
              consumer.type(),
              consumer.required(),
              RuntimeServiceStates.PROVIDER_CONFLICT,
              null,
              "Multiple providers declare service `"
                  + consumer.id()
                  + "`, so Runtime-3 does not bind a winner."),
          new RuntimeServiceBinding(
              consumer.id(),
              consumerModId,
              null,
              consumer.type(),
              null,
              consumer.required(),
              RuntimeServiceStates.PROVIDER_CONFLICT));
    }
    if (sameServiceIdProviders.isEmpty()) {
      return unboundConsumer(consumerModId, consumer, null, null);
    }

    DeclaredProvider provider = sameServiceIdProviders.get(0);
    RuntimeServiceProviderPlan providerPlan = providerPlans.get(provider);
    if (providerPlan == null || !RuntimeServiceStates.AVAILABLE.equals(providerPlan.state())) {
      return unboundConsumer(consumerModId, consumer, provider.modId(), providerPlan);
    }
    if (!provider.type().equals(consumer.type())) {
      return new ConsumerBinding(
          new RuntimeServiceConsumerPlan(
              consumer.id(),
              consumer.type(),
              consumer.required(),
              RuntimeServiceStates.TYPE_MISMATCH,
              provider.modId(),
              "Consumer type `"
                  + consumer.type()
                  + "` does not match provider type `"
                  + provider.type()
                  + "` for service `"
                  + consumer.id()
                  + "`."),
          new RuntimeServiceBinding(
              consumer.id(),
              consumerModId,
              provider.modId(),
              consumer.type(),
              provider.implementation(),
              consumer.required(),
              RuntimeServiceStates.TYPE_MISMATCH));
    }
    return new ConsumerBinding(
        new RuntimeServiceConsumerPlan(
            consumer.id(),
            consumer.type(),
            consumer.required(),
            RuntimeServiceStates.BOUND,
            provider.modId(),
            "Required service is bound to provider."),
        new RuntimeServiceBinding(
            consumer.id(),
            consumerModId,
            provider.modId(),
            consumer.type(),
            provider.implementation(),
            consumer.required(),
            RuntimeServiceStates.BOUND));
  }

  private ConsumerBinding unboundConsumer(
      String consumerModId,
      ModMetadata.ServiceConsumer consumer,
      String providerModId,
      RuntimeServiceProviderPlan providerPlan) {
    String state =
        consumer.required()
            ? RuntimeServiceStates.REQUIRED_UNBOUND
            : RuntimeServiceStates.OPTIONAL_UNBOUND;
    String reason;
    if (providerPlan == null) {
      reason =
          consumer.required()
              ? "Required service has no usable provider."
              : "Optional service has no usable provider.";
    } else {
      reason =
          "No usable provider is available for service `"
              + consumer.id()
              + "` because provider mod `"
              + providerModId
              + "` is `"
              + providerPlan.state()
              + "`.";
    }
    return new ConsumerBinding(
        new RuntimeServiceConsumerPlan(
            consumer.id(),
            consumer.type(),
            consumer.required(),
            state,
            null,
            reason),
        new RuntimeServiceBinding(
            consumer.id(),
            consumerModId,
            null,
            consumer.type(),
            null,
            consumer.required(),
            state));
  }

  private record DeclaredProvider(String modId, ModMetadata.ServiceProvider provider) {
    private String id() {
      return provider.id();
    }

    private String type() {
      return provider.type();
    }

    private String implementation() {
      return provider.implementation();
    }
  }

  private record ConsumerBinding(
      RuntimeServiceConsumerPlan consumerPlan, RuntimeServiceBinding binding) {}
}
