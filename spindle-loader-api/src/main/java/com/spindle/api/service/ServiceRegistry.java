package com.spindle.api.service;

import com.spindle.api.exception.ServiceAccessException;
import java.util.Optional;
import java.util.Set;

/**
 * Stable Runtime API-0 registry for declared runtime service consumers.
 *
 * <p>This interface exposes only services compiled into the current runtime plan. It does not imply
 * general dependency injection or sandboxing. Callers should expect {@link ServiceAccessException}
 * for undeclared or unavailable required service access.
 */
public interface ServiceRegistry {
  /** Returns a bound declared service or throws if the access is not allowed. */
  <T> T require(String serviceId, Class<T> type);

  /** Returns a bound declared service, or {@link Optional#empty()} for optional unbound access. */
  <T> Optional<T> find(String serviceId, Class<T> type);

  /** Returns whether the named declared service is currently bound. */
  boolean hasService(String serviceId);

  /** Returns bound declared service ids in deterministic sorted order when provided by Spindle. */
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
    throw new ServiceAccessException(
        "unavailable",
        serviceId,
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
