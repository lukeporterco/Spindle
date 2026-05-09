package com.spindle.core.pipeline;

import com.spindle.core.classpath.RuntimeClasspathPlan;
import com.spindle.core.discovery.ModCandidate;
import com.spindle.core.graph.FrozenModGraph;
import com.spindle.core.ownership.ClassOwnershipIndex;
import com.spindle.core.ownership.PackageOwnershipIndex;
import com.spindle.core.resolve.ResolvedModSet;
import com.spindle.core.resource.ResourceConflictIndex;
import com.spindle.core.runtime.ProtectedPackageViolation;
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
    List<ProtectedPackageViolation> protectedPackageViolations,
    ResourceConflictIndex resourceConflictIndex,
    FrozenModGraph frozenModGraph,
    Path modpackStatePath,
    Path dependencyGraphPath) {}
