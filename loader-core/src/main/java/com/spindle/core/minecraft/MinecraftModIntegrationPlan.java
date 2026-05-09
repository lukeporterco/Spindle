package com.spindle.core.minecraft;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record MinecraftModIntegrationPlan(
    int schema,
    String milestoneName,
    List<String> discoveredModCandidates,
    List<MinecraftModAcceptance> acceptedMods,
    List<MinecraftModRejection> rejectedMods,
    List<String> warnings,
    Map<String, List<String>> dependencyGraph,
    List<String> dependencyGraphTopologicalOrder,
    Map<String, String> optionalDependencies,
    List<String> missingOptionalDependencies,
    Map<String, Map<String, String>> breaksMetadata,
    Map<String, String> sideCompatibility,
    Map<String, String> loaderCompatibility,
    Map<String, String> javaCompatibility,
    Map<String, String> minecraftCompatibility,
    String resolvedMinecraftRuntimeVersion,
    MinecraftModClasspathPlan modClasspathPlan,
    MinecraftClasspathBoundaryPlan classpathBoundaryPlan,
    Map<String, List<String>> packageOwnershipByMod,
    Map<String, List<String>> resourceOwnershipByMod,
    Map<String, List<String>> serviceProviderOwnershipByMod,
    Map<String, String> moduleInfoByMod,
    Map<String, String> automaticModuleNameByMod,
    Map<String, String> multiReleaseByMod,
    Map<String, List<Integer>> classFileMajorVersionsByMod,
    Map<String, List<String>> nativeLibrariesByMod,
    List<String> duplicateResourcesBetweenMods,
    List<String> splitPackagesBetweenMods,
    List<MinecraftBoundaryViolation> conflictsAgainstMinecraft,
    List<MinecraftBoundaryViolation> conflictsAgainstLoader,
    List<MinecraftBoundaryViolation> issues,
    boolean analysisOnly,
    boolean injectionOccurred,
    boolean transformationOccurred,
    boolean remappingOccurred,
    boolean mixinOccurred,
    boolean modClassesLoaded,
    boolean entrypointsInvoked,
    boolean patchingOccurred,
    boolean modJarsOnMinecraftRuntimeClasspath) {
  public MinecraftModIntegrationPlan {
    discoveredModCandidates = List.copyOf(discoveredModCandidates);
    acceptedMods = List.copyOf(acceptedMods);
    rejectedMods = List.copyOf(rejectedMods);
    warnings = List.copyOf(warnings);
    dependencyGraph = immutableStringListMap(dependencyGraph);
    dependencyGraphTopologicalOrder = List.copyOf(dependencyGraphTopologicalOrder);
    optionalDependencies = immutableStringMap(optionalDependencies);
    missingOptionalDependencies = List.copyOf(missingOptionalDependencies);
    breaksMetadata = immutableNestedStringMap(breaksMetadata);
    sideCompatibility = immutableStringMap(sideCompatibility);
    loaderCompatibility = immutableStringMap(loaderCompatibility);
    javaCompatibility = immutableStringMap(javaCompatibility);
    minecraftCompatibility = immutableStringMap(minecraftCompatibility);
    packageOwnershipByMod = immutableStringListMap(packageOwnershipByMod);
    resourceOwnershipByMod = immutableStringListMap(resourceOwnershipByMod);
    serviceProviderOwnershipByMod = immutableStringListMap(serviceProviderOwnershipByMod);
    moduleInfoByMod = immutableStringMap(moduleInfoByMod);
    automaticModuleNameByMod = immutableNullableStringMap(automaticModuleNameByMod);
    multiReleaseByMod = immutableStringMap(multiReleaseByMod);
    classFileMajorVersionsByMod = immutableIntegerListMap(classFileMajorVersionsByMod);
    nativeLibrariesByMod = immutableStringListMap(nativeLibrariesByMod);
    duplicateResourcesBetweenMods = List.copyOf(duplicateResourcesBetweenMods);
    splitPackagesBetweenMods = List.copyOf(splitPackagesBetweenMods);
    conflictsAgainstMinecraft = List.copyOf(conflictsAgainstMinecraft);
    conflictsAgainstLoader = List.copyOf(conflictsAgainstLoader);
    issues = List.copyOf(issues);
  }

  private static Map<String, List<String>> immutableStringListMap(Map<String, List<String>> input) {
    TreeMap<String, List<String>> sorted = new TreeMap<>();
    for (Map.Entry<String, List<String>> entry : input.entrySet()) {
      sorted.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    return java.util.Collections.unmodifiableMap(sorted);
  }

  private static Map<String, List<Integer>> immutableIntegerListMap(
      Map<String, List<Integer>> input) {
    TreeMap<String, List<Integer>> sorted = new TreeMap<>();
    for (Map.Entry<String, List<Integer>> entry : input.entrySet()) {
      sorted.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    return java.util.Collections.unmodifiableMap(sorted);
  }

  private static Map<String, String> immutableStringMap(Map<String, String> input) {
    return java.util.Collections.unmodifiableMap(new TreeMap<>(input));
  }

  private static Map<String, String> immutableNullableStringMap(Map<String, String> input) {
    TreeMap<String, String> sorted = new TreeMap<>();
    sorted.putAll(input);
    return java.util.Collections.unmodifiableMap(sorted);
  }

  private static Map<String, Map<String, String>> immutableNestedStringMap(
      Map<String, Map<String, String>> input) {
    TreeMap<String, Map<String, String>> sorted = new TreeMap<>();
    for (Map.Entry<String, Map<String, String>> entry : input.entrySet()) {
      sorted.put(entry.getKey(), immutableStringMap(entry.getValue()));
    }
    return java.util.Collections.unmodifiableMap(sorted);
  }
}
