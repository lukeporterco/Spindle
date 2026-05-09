package com.spindle.api.service;

import java.util.Optional;
import java.util.Set;

public interface ServiceRegistry {
  <T> T require(String serviceId, Class<T> type);

  <T> Optional<T> find(String serviceId, Class<T> type);

  boolean hasService(String serviceId);

  Set<String> availableServiceIds();

  static ServiceRegistry empty() {
    return EmptyServiceRegistry.INSTANCE;
  }
}

final class EmptyServiceRegistry implements ServiceRegistry {
  static final EmptyServiceRegistry INSTANCE = new EmptyServiceRegistry();

  private EmptyServiceRegistry() {}

  @Override
  public <T> T require(String serviceId, Class<T> type) {
    throw new IllegalStateException(
        "Service `"
            + serviceId
            + "` is not available because no runtime service registry was configured.");
  }

  @Override
  public <T> Optional<T> find(String serviceId, Class<T> type) {
    return Optional.empty();
  }

  @Override
  public boolean hasService(String serviceId) {
    return false;
  }

  @Override
  public Set<String> availableServiceIds() {
    return Set.of();
  }
}
