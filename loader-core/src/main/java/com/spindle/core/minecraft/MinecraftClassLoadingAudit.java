package com.spindle.core.minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public final class MinecraftClassLoadingAudit {
  private final TreeMap<String, Integer> attemptCountsByLoader = new TreeMap<>();
  private final TreeMap<String, Integer> definedClassCountsByLoader = new TreeMap<>();
  private final List<String> deniedClassLoads = new ArrayList<>();

  public synchronized void recordAttempt(String loaderId) {
    attemptCountsByLoader.merge(loaderId, 1, Integer::sum);
  }

  public synchronized void recordDefined(String loaderId) {
    definedClassCountsByLoader.merge(loaderId, 1, Integer::sum);
  }

  public synchronized void recordDenied(String loaderId, String className) {
    deniedClassLoads.add(loaderId + ":" + className);
  }

  public synchronized Summary summary() {
    return new Summary(
        java.util.Collections.unmodifiableMap(new TreeMap<>(attemptCountsByLoader)),
        java.util.Collections.unmodifiableMap(new TreeMap<>(definedClassCountsByLoader)),
        List.copyOf(deniedClassLoads));
  }

  public record Summary(
      java.util.Map<String, Integer> attemptedClassLoadsByLoader,
      java.util.Map<String, Integer> definedClassLoadsByLoader,
      List<String> deniedClassLoads) {
    public Summary {
      deniedClassLoads = List.copyOf(deniedClassLoads);
    }
  }
}
