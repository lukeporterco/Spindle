package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.LoaderMain;
import com.mcmodloader.core.discovery.ModCandidate;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.metadata.ModMetadata;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

public final class MinecraftModIntegrationPlanner {
    private static final String MILESTONE_NAME = "Mega-Milestone 7";
    private final MinecraftJarScanner jarScanner = new MinecraftJarScanner();
    private final MinecraftModBoundaryScanner boundaryScanner = new MinecraftModBoundaryScanner();

    public MinecraftModIntegrationPlan plan(
        LaunchContext context,
        List<ModCandidate> parsedCandidates,
        ResolvedModSet resolvedMods,
        MinecraftRuntimeBoundary runtimeBoundary,
        String resolvedMinecraftVersion,
        boolean strictSide,
        boolean strictClassVersions,
        boolean strictRuntimeConflicts,
        Function<java.nio.file.Path, String> displayPath
    ) throws com.mcmodloader.core.diagnostics.LoaderException {
        List<String> discovered = parsedCandidates.stream().map(ModCandidate::normalizedRelativePath).sorted().toList();
        List<MinecraftModAcceptance> accepted = new ArrayList<>();
        List<MinecraftModRejection> rejected = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, List<String>> dependencyGraph = new TreeMap<>();
        Map<String, Map<String, String>> breaksMetadata = new TreeMap<>();
        Map<String, String> sideCompatibility = new TreeMap<>();
        Map<String, String> loaderCompatibility = new TreeMap<>();
        Map<String, String> javaCompatibility = new TreeMap<>();
        Map<String, String> minecraftCompatibility = new TreeMap<>();
        Map<String, List<String>> packagesByMod = new TreeMap<>();
        Map<String, List<String>> resourcesByMod = new TreeMap<>();
        Map<String, List<String>> servicesByMod = new TreeMap<>();
        Map<String, String> moduleInfoByMod = new TreeMap<>();
        Map<String, String> automaticModuleNameByMod = new TreeMap<>();
        Map<String, String> multiReleaseByMod = new TreeMap<>();
        Map<String, List<Integer>> classVersionsByMod = new TreeMap<>();
        Map<String, List<String>> nativesByMod = new TreeMap<>();
        List<MinecraftBoundaryViolation> minecraftConflicts = new ArrayList<>();
        List<MinecraftBoundaryViolation> loaderConflicts = new ArrayList<>();
        List<MinecraftBoundaryViolation> issues = new ArrayList<>();

        for (ModCandidate candidate : parsedCandidates.stream().sorted(java.util.Comparator.comparing(ModCandidate::normalizedRelativePath)).toList()) {
            ModMetadata metadata = candidate.metadata();
            String modId = metadata.id();
            MinecraftJarScanResult scan = jarScanner.scan(candidate.jarPath(), candidate.normalizedRelativePath());
            packagesByMod.put(modId, scan.packages());
            resourcesByMod.put(modId, scan.resources());
            servicesByMod.put(modId, scan.serviceProviders().keySet().stream().sorted().toList());
            moduleInfoByMod.put(modId, Boolean.toString(scan.moduleInfoPresent()));
            automaticModuleNameByMod.put(modId, scan.automaticModuleName());
            multiReleaseByMod.put(modId, Boolean.toString(scan.multiRelease()));
            classVersionsByMod.put(modId, scan.classFileMajorVersions());
            nativesByMod.put(modId, scan.nativeLibraries());
            breaksMetadata.put(modId, metadata.breaks());
            dependencyGraph.put(modId, metadata.depends().keySet().stream().filter(key -> !isBuiltin(key)).sorted().toList());

            boolean sideOk = "server".equals(metadata.side()) || "universal".equals(metadata.side());
            sideCompatibility.put(modId, sideOk ? "accepted for server" : "client-only mod is not server-compatible");
            boolean loaderOk = requirementMatches(metadata.depends().get("loader"), LoaderMain.LOADER_VERSION);
            boolean javaOk = requirementMatches(metadata.depends().get("java"), Integer.toString(context.javaMajorVersion()));
            boolean minecraftOk = requirementMatches(metadata.depends().get("minecraft"), resolvedMinecraftVersion);
            loaderCompatibility.put(modId, loaderOk ? "ok" : "requires " + metadata.depends().get("loader"));
            javaCompatibility.put(modId, javaOk ? "ok" : "requires " + metadata.depends().get("java"));
            minecraftCompatibility.put(modId, minecraftOk ? "ok" : "requires " + metadata.depends().get("minecraft"));

            List<MinecraftBoundaryViolation> boundaryViolations =
                boundaryScanner.scan(modId, scan, runtimeBoundary, strictRuntimeConflicts);
            issues.addAll(boundaryViolations);
            minecraftConflicts.addAll(
                boundaryViolations.stream().filter(violation -> violation.type().contains("runtime")).toList()
            );
            loaderConflicts.addAll(
                boundaryViolations.stream().filter(violation -> violation.type().contains("loader")).toList()
            );

            boolean classVersionOk = scan.unsupportedClassFiles().isEmpty();
            if (!classVersionOk) {
                MinecraftBoundaryViolation violation =
                    new MinecraftBoundaryViolation(
                        "mod-class-version-too-new",
                        strictClassVersions ? MinecraftBoundarySeverity.FATAL : MinecraftBoundarySeverity.WARNING,
                        String.join(",", scan.unsupportedClassFiles()),
                        modId,
                        null,
                        strictClassVersions,
                        true,
                        "Class file targets a newer Java major than the project baseline."
                    );
                issues.add(violation);
                warnings.add(violation.reason());
            }

            if (!sideOk) {
                issues.add(
                    new MinecraftBoundaryViolation(
                        "mod-side-mismatch",
                        strictSide ? MinecraftBoundarySeverity.FATAL : MinecraftBoundarySeverity.ERROR,
                        metadata.side(),
                        modId,
                        "server",
                        true,
                        true,
                        strictSide
                            ? "Mod side is incompatible with server preflight and strict side validation is enabled."
                            : "Mod side is incompatible with server preflight."
                    )
                );
            }
            if (!loaderOk) {
                issues.add(
                    new MinecraftBoundaryViolation(
                        "mod-loader-version-mismatch",
                        MinecraftBoundarySeverity.ERROR,
                        metadata.depends().get("loader"),
                        modId,
                        LoaderMain.LOADER_VERSION,
                        true,
                        true,
                        "Mod loader dependency does not match the current loader version."
                    )
                );
            }
            if (!javaOk) {
                issues.add(
                    new MinecraftBoundaryViolation(
                        "mod-java-version-mismatch",
                        MinecraftBoundarySeverity.ERROR,
                        metadata.depends().get("java"),
                        modId,
                        Integer.toString(context.javaMajorVersion()),
                        true,
                        true,
                        "Mod Java dependency does not match the current Java runtime."
                    )
                );
            }
            if (!minecraftOk) {
                issues.add(
                    new MinecraftBoundaryViolation(
                        "mod-minecraft-version-mismatch",
                        MinecraftBoundarySeverity.ERROR,
                        metadata.depends().get("minecraft"),
                        modId,
                        resolvedMinecraftVersion,
                        true,
                        true,
                        "Mod Minecraft dependency does not match the resolved Minecraft runtime."
                    )
                );
            }

            boolean fatalBoundaryViolation = boundaryViolations.stream().anyMatch(violation -> violation.severity() == MinecraftBoundarySeverity.FATAL);
            if (!sideOk || !loaderOk || !javaOk || !minecraftOk || (strictClassVersions && !classVersionOk) || fatalBoundaryViolation) {
                String reason =
                    !sideOk
                        ? "side mismatch"
                        : !loaderOk
                            ? "loader dependency mismatch"
                            : !javaOk
                                ? "java dependency mismatch"
                                : !minecraftOk
                                    ? "minecraft dependency mismatch"
                                    : (strictClassVersions && !classVersionOk) ? "class version mismatch" : "fatal boundary violation";
                MinecraftBoundarySeverity rejectionSeverity =
                    (strictSide && !sideOk) || fatalBoundaryViolation ? MinecraftBoundarySeverity.FATAL : MinecraftBoundarySeverity.ERROR;
                rejected.add(new MinecraftModRejection(modId, reason, rejectionSeverity, true));
            } else {
                accepted.add(new MinecraftModAcceptance(modId, metadata.version(), metadata.side(), "metadata and analysis checks passed"));
            }
        }

        List<String> duplicateResources = conflicts(resourcesByMod);
        List<String> splitPackages = conflicts(packagesByMod);
        for (String duplicate : duplicateResources) {
            issues.add(new MinecraftBoundaryViolation("mod-duplicate-resource", MinecraftBoundarySeverity.WARNING, duplicate, "mods", null, false, true, "Resource is owned by multiple mods."));
        }
        for (String splitPackage : splitPackages) {
            issues.add(new MinecraftBoundaryViolation("mod-split-package", MinecraftBoundarySeverity.WARNING, splitPackage, "mods", null, false, true, "Package is owned by multiple mods."));
        }

        List<String> topologicalOrder =
            resolvedMods.mods().stream().map(ResolvedModSet.ResolvedMod::id).sorted().filter(id -> accepted.stream().anyMatch(mod -> mod.modId().equals(id))).toList();
        List<String> plannedModClasspath =
            accepted.stream().map(MinecraftModAcceptance::modId).sorted().map(id -> "future-mod-classloader:" + id).toList();

        return new MinecraftModIntegrationPlan(
            1,
            MILESTONE_NAME,
            discovered,
            List.copyOf(accepted),
            List.copyOf(rejected),
            List.copyOf(warnings),
            dependencyGraph,
            topologicalOrder,
            Map.of(),
            List.of(),
            breaksMetadata,
            sideCompatibility,
            loaderCompatibility,
            javaCompatibility,
            minecraftCompatibility,
            resolvedMinecraftVersion,
            new MinecraftModClasspathPlan(plannedModClasspath, false),
            new MinecraftClasspathBoundaryPlan("future child-first mod classloader is planned but not created", "loader and Minecraft remain parents; no attachment occurs"),
            packagesByMod,
            resourcesByMod,
            servicesByMod,
            moduleInfoByMod,
            automaticModuleNameByMod,
            multiReleaseByMod,
            classVersionsByMod,
            nativesByMod,
            duplicateResources,
            splitPackages,
            List.copyOf(minecraftConflicts),
            List.copyOf(loaderConflicts),
            List.copyOf(issues),
            true,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false
        );
    }

    private boolean isBuiltin(String dependencyId) {
        return "loader".equals(dependencyId) || "java".equals(dependencyId) || "minecraft".equals(dependencyId);
    }

    private boolean requirementMatches(String requirement, String actualVersion) {
        if (requirement == null || requirement.isBlank()) {
            return true;
        }
        if (!requirement.startsWith(">=")) {
            return requirement.equals(actualVersion);
        }
        return compareVersions(actualVersion, requirement.substring(2).trim()) >= 0;
    }

    private int compareVersions(String left, String right) {
        String[] leftParts = left.split("\\.");
        String[] rightParts = right.split("\\.");
        int size = Math.max(leftParts.length, rightParts.length);
        for (int index = 0; index < size; index++) {
            int leftValue = parsePart(leftParts, index);
            int rightValue = parsePart(rightParts, index);
            int result = Integer.compare(leftValue, rightValue);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private int parsePart(String[] parts, int index) {
        if (index >= parts.length) {
            return 0;
        }
        String digits = parts[index].chars().takeWhile(Character::isDigit).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
        return digits.isBlank() ? 0 : Integer.parseInt(digits);
    }

    private List<String> conflicts(Map<String, List<String>> ownershipByOwner) {
        Map<String, List<String>> ownerBySubject = new TreeMap<>();
        for (Map.Entry<String, List<String>> owner : ownershipByOwner.entrySet()) {
            for (String subject : owner.getValue()) {
                List<String> owners = new ArrayList<>(ownerBySubject.getOrDefault(subject, List.of()));
                owners.add(owner.getKey());
                ownerBySubject.put(subject, owners.stream().distinct().sorted().toList());
            }
        }
        return ownerBySubject.entrySet().stream().filter(entry -> entry.getValue().size() > 1).map(Map.Entry::getKey).sorted().toList();
    }
}
