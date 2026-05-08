package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.diagnostics.LoaderException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

public final class MinecraftRuntimeBoundaryBuilder {
    private static final String MILESTONE_NAME = "Mega-Milestone 7";
    private final MinecraftJarScanner scanner = new MinecraftJarScanner();

    public MinecraftRuntimeBoundary build(MinecraftServerRuntimePlan plan, List<Path> runtimeJars, Function<Path, String> displayPath)
        throws LoaderException {
        TreeMap<String, List<String>> packageOwners = new TreeMap<>();
        TreeMap<String, List<String>> resourceOwners = new TreeMap<>();
        TreeMap<String, List<String>> serviceOwners = new TreeMap<>();
        TreeSet<String> moduleInfoJars = new TreeSet<>();
        TreeSet<String> multiReleaseJars = new TreeSet<>();
        TreeSet<String> nativeLibraryJars = new TreeSet<>();

        for (Path jar : runtimeJars.stream().sorted((left, right) -> displayPath.apply(left).compareTo(displayPath.apply(right))).toList()) {
            String owner = displayPath.apply(jar);
            MinecraftJarScanResult scan = scanner.scan(jar, owner);
            for (String packageName : scan.packages()) {
                append(packageOwners, packageName, owner);
            }
            for (String resource : scan.resources()) {
                append(resourceOwners, resource, owner);
            }
            for (String service : scan.serviceProviders().keySet()) {
                append(serviceOwners, service, owner);
            }
            if (scan.moduleInfoPresent()) {
                moduleInfoJars.add(owner);
            }
            if (scan.multiRelease()) {
                multiReleaseJars.add(owner);
            }
            if (!scan.nativeLibraries().isEmpty()) {
                nativeLibraryJars.add(owner);
            }
        }

        List<String> duplicateResources = conflicts(resourceOwners);
        List<String> splitPackages = conflicts(packageOwners);
        List<MinecraftBoundaryViolation> violations = new ArrayList<>();
        for (String duplicate : duplicateResources) {
            violations.add(
                new MinecraftBoundaryViolation(
                    "duplicate-runtime-resource",
                    MinecraftBoundarySeverity.WARNING,
                    duplicate,
                    String.join(",", resourceOwners.get(duplicate)),
                    null,
                    false,
                    true,
                    "Runtime classpath contains a duplicate resource; future mods must not add another owner."
                )
            );
        }
        for (String splitPackage : splitPackages) {
            violations.add(
                new MinecraftBoundaryViolation(
                    "split-runtime-package",
                    MinecraftBoundarySeverity.WARNING,
                    splitPackage,
                    String.join(",", packageOwners.get(splitPackage)),
                    null,
                    false,
                    true,
                    "Runtime classpath contains a split package; future mods must not claim this package."
                )
            );
        }

        Map<String, String> classpathOwnershipByLayer = new LinkedHashMap<>();
        for (MinecraftServerRuntimeClasspath.Entry entry : plan.classpathEntries()) {
            classpathOwnershipByLayer.put(entry.path(), entry.ownership());
        }

        Map<String, String> severityPolicy = new LinkedHashMap<>();
        severityPolicy.put("duplicate-runtime-resource", "warning now but fatal before injection");
        severityPolicy.put("split-runtime-package", "warning now but fatal before injection");
        severityPolicy.put("mod-conflicts-loader", "fatal now");
        severityPolicy.put("strict-boundary", "strict-mode fatal");
        severityPolicy.put("analysis-only", "informational only");

        return new MinecraftRuntimeBoundary(
            1,
            MILESTONE_NAME,
            plan.resolvedMinecraftVersion(),
            plan.classpathEntries(),
            packageOwners,
            resourceOwners,
            serviceOwners,
            List.copyOf(moduleInfoJars),
            List.copyOf(multiReleaseJars),
            List.copyOf(nativeLibraryJars),
            duplicateResources,
            splitPackages,
            classpathOwnershipByLayer,
            List.of(
                "future mod jars are not placed on the Minecraft runtime classpath",
                "future mod classloader is not attached to Minecraft in Mega-Milestone 7",
                "mods must not define Minecraft, loader core, or loader API packages",
                "mods must not rely on runtime transformation, remapping, Mixin, or patching"
            ),
            violations,
            severityPolicy,
            true,
            "The boundary model is analysis-only in Mega-Milestone 7."
        );
    }

    private void append(TreeMap<String, List<String>> map, String key, String owner) {
        List<String> owners = new ArrayList<>(map.getOrDefault(key, List.of()));
        owners.add(owner);
        map.put(key, owners.stream().distinct().sorted().toList());
    }

    private List<String> conflicts(TreeMap<String, List<String>> map) {
        return map.entrySet().stream().filter(entry -> entry.getValue().size() > 1).map(Map.Entry::getKey).sorted().toList();
    }
}
