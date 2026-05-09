package com.spindle.core.graph;

import com.spindle.core.classpath.RuntimeClasspathPlan;
import com.spindle.core.ownership.ClassOwnershipIndex;
import com.spindle.core.ownership.PackageOwnershipIndex;
import com.spindle.core.resource.ResourceConflict;
import java.util.List;

public record FrozenModGraph(
    List<FrozenMod> mods,
    List<BuiltinDependency> builtinDependencies,
    String gameProviderId,
    String gameProviderDisplayName,
    String gameProviderVersion,
    List<DependencyEdge> dependencyEdges,
    List<IncompatibilityEdge> incompatibilityEdges,
    ClassOwnershipIndex classOwnershipIndex,
    PackageOwnershipIndex packageOwnershipIndex,
    List<ResourceConflict> resourceConflicts,
    RuntimeClasspathPlan runtimeClasspathPlan) {
  public FrozenModGraph {
    mods = List.copyOf(mods);
    builtinDependencies = List.copyOf(builtinDependencies);
    dependencyEdges = List.copyOf(dependencyEdges);
    incompatibilityEdges = List.copyOf(incompatibilityEdges);
    resourceConflicts = List.copyOf(resourceConflicts);
  }
}
