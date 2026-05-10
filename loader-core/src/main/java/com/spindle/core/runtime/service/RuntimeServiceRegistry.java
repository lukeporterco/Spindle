package com.spindle.core.runtime.service;

import com.spindle.api.exception.ServiceAccessException;
import com.spindle.api.service.ServiceRegistry;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public final class RuntimeServiceRegistry implements ServiceRegistry {
  private final String modId;
  private final Map<String, RuntimeServiceConsumerPlan> consumersById;
  private final Map<String, RuntimeServiceBinding> bindingsById;
  private final RuntimeServiceRegistryFactory.ProviderResolver providerResolver;
  private final Set<String> availableServiceIds;

  RuntimeServiceRegistry(
      String modId,
      Map<String, RuntimeServiceConsumerPlan> consumersById,
      Map<String, RuntimeServiceBinding> bindingsById,
      RuntimeServiceRegistryFactory.ProviderResolver providerResolver) {
    this.modId = modId;
    this.consumersById = Map.copyOf(consumersById);
    this.bindingsById = Map.copyOf(bindingsById);
    this.providerResolver = providerResolver;
    TreeSet<String> available = new TreeSet<>();
    for (Map.Entry<String, RuntimeServiceBinding> entry : bindingsById.entrySet()) {
      if (RuntimeServiceStates.BOUND.equals(entry.getValue().state())) {
        available.add(entry.getKey());
      }
    }
    this.availableServiceIds = Collections.unmodifiableSet(new LinkedHashSet<>(available));
  }

  @Override
  public <T> T require(String serviceId, Class<T> type) {
    RuntimeServiceConsumerPlan consumer = declaredConsumer(serviceId);
    if (RuntimeServiceStates.BOUND.equals(consumer.state())) {
      return providerResolver.resolve(modId, binding(serviceId), type);
    }
    throw new ServiceAccessException(modId, serviceId, unavailableMessage(serviceId, consumer));
  }

  @Override
  public <T> Optional<T> find(String serviceId, Class<T> type) {
    RuntimeServiceConsumerPlan consumer = declaredConsumer(serviceId);
    if (RuntimeServiceStates.BOUND.equals(consumer.state())) {
      return Optional.of(providerResolver.resolve(modId, binding(serviceId), type));
    }
    if (RuntimeServiceStates.OPTIONAL_UNBOUND.equals(consumer.state())) {
      return Optional.empty();
    }
    throw new ServiceAccessException(modId, serviceId, unavailableMessage(serviceId, consumer));
  }

  @Override
  public boolean hasService(String serviceId) {
    RuntimeServiceConsumerPlan consumer = consumersById.get(serviceId);
    return consumer != null && RuntimeServiceStates.BOUND.equals(consumer.state());
  }

  @Override
  public Set<String> availableServiceIds() {
    return availableServiceIds;
  }

  private RuntimeServiceConsumerPlan declaredConsumer(String serviceId) {
    RuntimeServiceConsumerPlan consumer = consumersById.get(serviceId);
    if (consumer != null) {
      return consumer;
    }
    throw new ServiceAccessException(
        modId,
        serviceId,
        "Mod `"
            + modId
            + "` cannot access service `"
            + serviceId
            + "` because it was not declared in services.consumes.");
  }

  private RuntimeServiceBinding binding(String serviceId) {
    RuntimeServiceBinding binding = bindingsById.get(serviceId);
    if (binding == null) {
      throw new ServiceAccessException(
          modId,
          serviceId,
          "Missing runtime service binding for mod `"
              + modId
              + "` and service `"
              + serviceId
              + "`.");
    }
    return binding;
  }

  private String unavailableMessage(String serviceId, RuntimeServiceConsumerPlan consumer) {
    return switch (consumer.state()) {
      case RuntimeServiceStates.OPTIONAL_UNBOUND ->
          "Mod `"
              + modId
              + "` declared optional service `"
              + serviceId
              + "`, but no provider was bound. Use find(...) to handle the empty result.";
      case RuntimeServiceStates.REQUIRED_UNBOUND ->
          "Mod `"
              + modId
              + "` declared required service `"
              + serviceId
              + "`, but no provider was bound. "
              + consumer.reason();
      case RuntimeServiceStates.PROVIDER_CONFLICT ->
          "Mod `"
              + modId
              + "` cannot access service `"
              + serviceId
              + "` because multiple providers were declared. "
              + consumer.reason();
      case RuntimeServiceStates.TYPE_MISMATCH ->
          "Mod `"
              + modId
              + "` cannot access service `"
              + serviceId
              + "` because the bound provider type does not match declared consumer type `"
              + consumer.type()
              + "`. "
              + consumer.reason();
      default -> throw new IllegalArgumentException("Unsupported service state " + consumer.state());
    };
  }
}
