package com.mcmodloader.core.minecraft;

import java.util.List;
import java.util.Map;

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
    String analysisOnlyStatement
) {
    public MinecraftRuntimeBoundary {
        runtimeClasspathEntries = List.copyOf(runtimeClasspathEntries);
        packageOwnership = Map.copyOf(packageOwnership);
        resourceOwnership = Map.copyOf(resourceOwnership);
        serviceProviderOwnership = Map.copyOf(serviceProviderOwnership);
        moduleInfoJars = List.copyOf(moduleInfoJars);
        multiReleaseJars = List.copyOf(multiReleaseJars);
        nativeLibraryJars = List.copyOf(nativeLibraryJars);
        duplicateRuntimeResources = List.copyOf(duplicateRuntimeResources);
        splitRuntimePackages = List.copyOf(splitRuntimePackages);
        classpathOwnershipByLayer = Map.copyOf(classpathOwnershipByLayer);
        futureModBoundaries = List.copyOf(futureModBoundaries);
        violations = List.copyOf(violations);
        severityPolicy = Map.copyOf(severityPolicy);
    }
}
