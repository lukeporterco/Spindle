package com.mcmodloader.core.state;

import com.google.gson.annotations.SerializedName;
import com.mcmodloader.core.graph.FrozenModGraph;
import com.mcmodloader.core.ownership.PackageOwnershipIndex;
import com.mcmodloader.core.resource.ResourceConflict;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record ModpackState(
    int schema,
    String loader,
    @SerializedName("java")
    int javaMajorVersion,
    String minecraft,
    GameProviderState gameProvider,
    List<ModState> mods,
    ClassOwnershipState classOwnership,
    PackageOwnershipState packageOwnership,
    List<ResourceConflictState> resourceConflicts,
    ClasspathState classpath
) {
    public static ModpackState from(FrozenModGraph graph, Path workingDirectory) {
        List<ModState> mods = new ArrayList<>(graph.mods().size());
        graph
            .mods()
            .forEach(mod -> mods.add(
                new ModState(
                    mod.id(),
                    mod.version(),
                    mod.path(),
                    mod.sha256(),
                    mod.entrypoints(),
                    mod.depends(),
                    mod.breaks(),
                    mod.classCount(),
                    mod.packageCount(),
                    mod.resourceCount()
                )
            ));

        List<PackageState> packages = new ArrayList<>();
        graph
            .packageOwnershipIndex()
            .packageOwners()
            .forEach((packageName, modIds) -> packages.add(new PackageState(packageName, modIds)));

        List<SplitPackageState> splitPackages = new ArrayList<>();
        for (PackageOwnershipIndex.SplitPackage splitPackage : graph.packageOwnershipIndex().splitPackages()) {
            splitPackages.add(new SplitPackageState(splitPackage.packageName(), splitPackage.modIds()));
        }

        List<ResourceConflictState> resourceConflicts = new ArrayList<>();
        for (ResourceConflict conflict : graph.resourceConflicts()) {
            resourceConflicts.add(new ResourceConflictState(conflict.resourcePath(), conflict.modIds()));
        }

        return new ModpackState(
            1,
            graph.builtinDependencies().stream().filter(builtin -> "loader".equals(builtin.id())).findFirst().orElseThrow().version(),
            Integer.parseInt(graph.builtinDependencies().stream().filter(builtin -> "java".equals(builtin.id())).findFirst().orElseThrow().version()),
            graph.builtinDependencies().stream().filter(builtin -> "minecraft".equals(builtin.id())).findFirst().orElseThrow().version(),
            new GameProviderState(graph.gameProviderId(), graph.gameProviderDisplayName(), graph.gameProviderVersion()),
            mods,
            new ClassOwnershipState(graph.classOwnershipIndex().totalClasses(), List.of()),
            new PackageOwnershipState(packages, splitPackages),
            resourceConflicts,
            new ClasspathState(graph.runtimeClasspathPlan().modJarDisplayPaths(workingDirectory))
        );
    }

    public record GameProviderState(String id, String displayName, String version) {
    }

    public record ModState(
        String id,
        String version,
        String path,
        String sha256,
        Map<String, List<String>> entrypoints,
        Map<String, String> depends,
        Map<String, String> breaks,
        int classCount,
        int packageCount,
        int resourceCount
    ) {
        public ModState {
            entrypoints = Map.copyOf(new TreeMap<>(entrypoints));
            depends = Map.copyOf(new TreeMap<>(depends));
            breaks = Map.copyOf(new TreeMap<>(breaks));
        }
    }

    public record ClassOwnershipState(int totalClasses, List<String> duplicateClasses) {
        public ClassOwnershipState {
            duplicateClasses = List.copyOf(duplicateClasses);
        }
    }

    public record PackageOwnershipState(List<PackageState> packages, List<SplitPackageState> splitPackages) {
        public PackageOwnershipState {
            packages = List.copyOf(packages);
            splitPackages = List.copyOf(splitPackages);
        }
    }

    public record PackageState(String name, List<String> mods) {
        public PackageState {
            mods = List.copyOf(mods);
        }
    }

    public record SplitPackageState(String name, List<String> mods) {
        public SplitPackageState {
            mods = List.copyOf(mods);
        }
    }

    public record ResourceConflictState(String path, List<String> mods) {
        public ResourceConflictState {
            mods = List.copyOf(mods);
        }
    }

    public record ClasspathState(List<String> modJars) {
        public ClasspathState {
            modJars = List.copyOf(modJars);
        }
    }
}
