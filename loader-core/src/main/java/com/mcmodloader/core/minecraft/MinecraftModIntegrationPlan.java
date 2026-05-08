package com.mcmodloader.core.minecraft;

import java.util.List;
import java.util.Map;

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
    boolean entrypointsInvoked
) {
}
