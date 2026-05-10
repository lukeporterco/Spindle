package com.spindle.core.runtime.service;

import com.spindle.api.exception.ServiceAccessException;
import com.spindle.api.service.ServiceRegistry;
import com.spindle.core.classpath.ModClassLoader;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RuntimeServiceRegistryFactory {
  public Map<String, ServiceRegistry> create(
      com.spindle.core.runtime.CompiledModpackProfile profile, ModClassLoader classLoader) {
    Map<String, RuntimeServiceBinding> bindingsByConsumerAndId = new LinkedHashMap<>();
    for (RuntimeServiceBinding binding : profile.services().bindings()) {
      bindingsByConsumerAndId.put(binding.consumerModId() + "\u0000" + binding.id(), binding);
    }

    Map<String, Object> singletonInstances = new LinkedHashMap<>();
    ProviderResolver providerResolver =
        new ProviderResolver() {
          @Override
          public <T> T resolve(
              String consumerModId, RuntimeServiceBinding binding, Class<T> requestedType) {
            return resolveProvider(
                consumerModId, binding, requestedType, classLoader, singletonInstances);
          }
        };

    Map<String, RuntimeServiceModPlan> servicesByModId = new LinkedHashMap<>();
    for (RuntimeServiceModPlan modPlan : profile.services().mods()) {
      servicesByModId.put(modPlan.modId(), modPlan);
    }

    Map<String, ServiceRegistry> registries = new LinkedHashMap<>();
    for (com.spindle.core.runtime.CompiledModpackProfile.Mod mod : profile.mods()) {
      RuntimeServiceModPlan modPlan =
          servicesByModId.getOrDefault(mod.id(), new RuntimeServiceModPlan(mod.id(), java.util.List.of(), java.util.List.of()));
      Map<String, RuntimeServiceConsumerPlan> consumersById = new LinkedHashMap<>();
      Map<String, RuntimeServiceBinding> bindingsById = new LinkedHashMap<>();
      for (RuntimeServiceConsumerPlan consumer : modPlan.consumes()) {
        consumersById.put(consumer.id(), consumer);
        RuntimeServiceBinding binding = bindingsByConsumerAndId.get(mod.id() + "\u0000" + consumer.id());
        if (binding != null) {
          bindingsById.put(consumer.id(), binding);
        }
      }
      registries.put(
          mod.id(),
          new RuntimeServiceRegistry(mod.id(), consumersById, bindingsById, providerResolver));
    }
    return Map.copyOf(registries);
  }

  private <T> T resolveProvider(
      String consumerModId,
      RuntimeServiceBinding binding,
      Class<T> requestedType,
      ClassLoader classLoader,
      Map<String, Object> singletonInstances) {
    String providerModId = binding.providerModId();
    String implementationClassName = binding.implementation();
    String declaredTypeName = binding.type();
    String singletonKey = providerModId + "\u0000" + binding.id();

    Object instance = singletonInstances.get(singletonKey);
    if (instance == null) {
      instance =
          instantiateProvider(
              consumerModId, binding.id(), providerModId, implementationClassName, declaredTypeName, classLoader);
      singletonInstances.put(singletonKey, instance);
    }
    if (!requestedType.isInstance(instance)) {
      throw new ServiceAccessException(
          consumerModId,
          binding.id(),
          "Service `"
              + binding.id()
              + "` for consumer mod `"
              + consumerModId
              + "` was provided by mod `"
              + providerModId
              + "` using implementation `"
              + implementationClassName
              + "`, but the instance is not assignable to requested type `"
              + requestedType.getName()
              + "` and declared type `"
              + declaredTypeName
              + "`.");
    }
    return requestedType.cast(instance);
  }

  private Object instantiateProvider(
      String consumerModId,
      String serviceId,
      String providerModId,
      String implementationClassName,
      String declaredTypeName,
      ClassLoader classLoader) {
    try {
      Class<?> declaredType = classLoader.loadClass(declaredTypeName);
      Class<?> implementationClass = classLoader.loadClass(implementationClassName);
      if (!declaredType.isAssignableFrom(implementationClass)) {
        throw new ServiceAccessException(
            consumerModId,
            serviceId,
            "Service `"
                + serviceId
                + "` for consumer mod `"
                + consumerModId
                + "` declares type `"
                + declaredTypeName
                + "`, but provider mod `"
                + providerModId
                + "` implementation `"
                + implementationClassName
                + "` is not assignable to that type.");
      }
      return implementationClass.getConstructor().newInstance();
    } catch (ClassNotFoundException exception) {
      throw new ServiceAccessException(
          consumerModId,
          serviceId,
          "Service `"
              + serviceId
              + "` for consumer mod `"
              + consumerModId
              + "` could not load provider mod `"
              + providerModId
              + "` implementation `"
              + implementationClassName
              + "` or declared type `"
              + declaredTypeName
              + "`.",
          exception);
    } catch (NoSuchMethodException exception) {
      throw new ServiceAccessException(
          consumerModId,
          serviceId,
          "Service `"
              + serviceId
              + "` for consumer mod `"
              + consumerModId
              + "` requires provider mod `"
              + providerModId
              + "` implementation `"
              + implementationClassName
              + "` to expose a public no-arg constructor for declared type `"
              + declaredTypeName
              + "`.",
          exception);
    } catch (ServiceAccessException exception) {
      throw exception;
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
      throw new ServiceAccessException(
          consumerModId,
          serviceId,
          "Service `"
              + serviceId
              + "` for consumer mod `"
              + consumerModId
              + "` failed to instantiate provider mod `"
              + providerModId
              + "` implementation `"
              + implementationClassName
              + "` for declared type `"
              + declaredTypeName
              + "`.",
          exception);
    }
  }

  @FunctionalInterface
  interface ProviderResolver {
    <T> T resolve(String consumerModId, RuntimeServiceBinding binding, Class<T> requestedType);
  }
}
