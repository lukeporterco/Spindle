package com.spindle.core.minecraft;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record MinecraftRuntimeBoundary(
    int schema,
    String milestoneName,
    String minecraftVersion,
    List<MinecraftServerRuntimeClasspath.Entry> runtimeClasspathEntries,
    Map<String, List<String>> packageOwnership,
    Map<String, List<String>> resourceOwnership,
    Map<String, List<String>> serviceProviderOwnership,
    List<String> moduleInfoJars,
    List<String> multiReleaseJars,
    List<String> nativeLibraryJars,
    List<String> duplicateRuntimeResources,
    List<String> splitRuntimePackages,
    Map<String, String> classpathOwnershipByLayer,
    List<String> futureModBoundaries,
    List<MinecraftBoundaryViolation> violations,
    Map<String, String> severityPolicy,
    boolean analysisOnly,
    String analysisOnlyStatement,
    boolean modJarsOnMinecraftRuntimeClasspath,
    boolean modClassesLoaded,
    boolean entrypointsInvoked,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean remappingOccurred,
    boolean mixinOccurred,
    boolean patchingOccurred) {
  public MinecraftRuntimeBoundary {
    runtimeClasspathEntries = List.copyOf(runtimeClasspathEntries);
    packageOwnership = immutableStringListMap(packageOwnership);
    resourceOwnership = immutableStringListMap(resourceOwnership);
    serviceProviderOwnership = immutableStringListMap(serviceProviderOwnership);
    moduleInfoJars = List.copyOf(moduleInfoJars);
    multiReleaseJars = List.copyOf(multiReleaseJars);
    nativeLibraryJars = List.copyOf(nativeLibraryJars);
    duplicateRuntimeResources = List.copyOf(duplicateRuntimeResources);
    splitRuntimePackages = List.copyOf(splitRuntimePackages);
    classpathOwnershipByLayer = immutableStringMap(classpathOwnershipByLayer);
    futureModBoundaries = List.copyOf(futureModBoundaries);
    violations = List.copyOf(violations);
    severityPolicy = immutableStringMap(severityPolicy);
  }

  private static Map<String, List<String>> immutableStringListMap(Map<String, List<String>> input) {
    TreeMap<String, List<String>> sorted = new TreeMap<>();
    for (Map.Entry<String, List<String>> entry : input.entrySet()) {
      sorted.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    return java.util.Collections.unmodifiableMap(sorted);
  }

  private static Map<String, String> immutableStringMap(Map<String, String> input) {
    return java.util.Collections.unmodifiableMap(new TreeMap<>(input));
  }
}
