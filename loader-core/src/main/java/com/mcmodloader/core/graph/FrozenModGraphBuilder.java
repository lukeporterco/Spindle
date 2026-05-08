package com.mcmodloader.core.graph;

import com.mcmodloader.core.classpath.RuntimeClasspathPlan;
import com.mcmodloader.core.game.GameProvider;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.ownership.ClassOwnershipIndex;
import com.mcmodloader.core.ownership.PackageOwnershipIndex;
import com.mcmodloader.core.resource.ResourceConflictIndex;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FrozenModGraphBuilder {
    public FrozenModGraph build(
        LaunchContext context,
        GameProvider gameProvider,
        ResolvedModSet resolvedMods,
        RuntimeClasspathPlan classpathPlan,
        ClassOwnershipIndex classOwnershipIndex,
        PackageOwnershipIndex packageOwnershipIndex,
        ResourceConflictIndex resourceConflictIndex
    ) {
        Map<String, FrozenMod> modById = new LinkedHashMap<>();
        List<FrozenMod> frozenMods = new ArrayList<>(resolvedMods.mods().size());
        for (ResolvedModSet.ResolvedMod mod : resolvedMods.mods()) {
            FrozenMod frozenMod =
                new FrozenMod(
                    mod.id(),
                    mod.version(),
                    mod.normalizedRelativePath(),
                    mod.sha256(),
                    mod.entrypoints(),
                    mod.depends(),
                    mod.breaks(),
                    classOwnershipIndex.classCountByMod().getOrDefault(mod.id(), 0),
                    packageOwnershipIndex.packageCountByMod().getOrDefault(mod.id(), 0),
                    resourceConflictIndex.resourceCountByMod().getOrDefault(mod.id(), 0)
                );
            frozenMods.add(frozenMod);
            modById.put(frozenMod.id(), frozenMod);
        }

        List<BuiltinDependency> builtinDependencies =
            List.of(
                new BuiltinDependency("loader", context.loaderVersion()),
                new BuiltinDependency("java", Integer.toString(context.javaMajorVersion())),
                new BuiltinDependency("minecraft", context.targetMinecraftVersion())
            );

        Map<String, String> builtinVersions = new LinkedHashMap<>();
        for (BuiltinDependency builtin : builtinDependencies) {
            builtinVersions.put(builtin.id(), builtin.version());
        }

        List<DependencyEdge> dependencyEdges = new ArrayList<>();
        List<IncompatibilityEdge> incompatibilityEdges = new ArrayList<>();
        for (FrozenMod mod : frozenMods) {
            for (Map.Entry<String, String> dependency : mod.depends().entrySet()) {
                String targetId = dependency.getKey();
                String satisfiedBy = builtinVersions.get(targetId);
                if (satisfiedBy == null) {
                    FrozenMod targetMod = modById.get(targetId);
                    satisfiedBy = targetMod == null ? "" : targetMod.version();
                }
                dependencyEdges.add(new DependencyEdge(mod.id(), targetId, dependency.getValue(), satisfiedBy));
            }

            for (Map.Entry<String, String> incompatibility : mod.breaks().entrySet()) {
                FrozenMod targetMod = modById.get(incompatibility.getKey());
                if (targetMod != null) {
                    incompatibilityEdges.add(
                        new IncompatibilityEdge(mod.id(), targetMod.id(), incompatibility.getValue(), targetMod.version())
                    );
                }
            }
        }

        dependencyEdges.sort(Comparator.comparing(DependencyEdge::fromId).thenComparing(DependencyEdge::toId));
        incompatibilityEdges.sort(Comparator.comparing(IncompatibilityEdge::fromId).thenComparing(IncompatibilityEdge::toId));

        return new FrozenModGraph(
            frozenMods,
            builtinDependencies,
            gameProvider.id(),
            gameProvider.displayName(),
            gameProvider.version(),
            dependencyEdges,
            incompatibilityEdges,
            classOwnershipIndex,
            packageOwnershipIndex,
            resourceConflictIndex.conflicts(),
            classpathPlan
        );
    }
}
