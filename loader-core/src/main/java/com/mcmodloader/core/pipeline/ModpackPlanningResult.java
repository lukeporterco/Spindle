package com.mcmodloader.core.pipeline;

import com.mcmodloader.core.classpath.RuntimeClasspathPlan;
import com.mcmodloader.core.discovery.ModCandidate;
import com.mcmodloader.core.graph.FrozenModGraph;
import com.mcmodloader.core.ownership.ClassOwnershipIndex;
import com.mcmodloader.core.ownership.PackageOwnershipIndex;
import com.mcmodloader.core.resolve.ResolvedModSet;
import com.mcmodloader.core.resource.ResourceConflictIndex;
import java.nio.file.Path;
import java.util.List;

public record ModpackPlanningResult(
    List<ModCandidate> discoveredMods,
    List<ModCandidate> parsedMods,
    ResolvedModSet resolvedMods,
    Path lockfilePath,
    String lockfileAction,
    RuntimeClasspathPlan classpathPlan,
    ClassOwnershipIndex classOwnershipIndex,
    PackageOwnershipIndex packageOwnershipIndex,
    ResourceConflictIndex resourceConflictIndex,
    FrozenModGraph frozenModGraph,
    Path modpackStatePath,
    Path dependencyGraphPath) {}
