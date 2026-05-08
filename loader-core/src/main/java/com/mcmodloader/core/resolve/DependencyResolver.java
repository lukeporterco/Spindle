package com.mcmodloader.core.resolve;

import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.discovery.ModCandidate;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.metadata.ModMetadata;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DependencyResolver {
    public ResolvedModSet resolve(LaunchContext context, List<ModCandidate> candidates) throws LoaderException {
        Map<String, ModCandidate> candidateById = new HashMap<>();
        for (ModCandidate candidate : candidates) {
            ModMetadata metadata = requireMetadata(candidate);
            ModCandidate duplicate = candidateById.putIfAbsent(metadata.id(), candidate);
            if (duplicate != null) {
                throw new LoaderException("Duplicate mod id discovered: " + metadata.id());
            }
        }

        for (ModCandidate candidate : candidates) {
            validateDependencies(context, candidate, candidateById);
        }

        List<ResolvedModSet.ResolvedMod> resolvedMods = new ArrayList<>(candidates.size());
        for (ModCandidate candidate : candidates) {
            ModMetadata metadata = requireMetadata(candidate);
            resolvedMods.add(
                new ResolvedModSet.ResolvedMod(
                    metadata.id(),
                    metadata.version(),
                    candidate.relativePath(),
                    candidate.jarPath(),
                    candidate.sha256(),
                    metadata.mainEntrypoints()
                )
            );
        }

        resolvedMods.sort(Comparator.comparing(ResolvedModSet.ResolvedMod::id));
        return new ResolvedModSet(resolvedMods);
    }

    private void validateDependencies(LaunchContext context, ModCandidate candidate, Map<String, ModCandidate> candidateById)
        throws LoaderException {
        ModMetadata metadata = requireMetadata(candidate);
        for (Map.Entry<String, String> dependency : metadata.depends().entrySet()) {
            String dependencyId = dependency.getKey();
            String requirement = dependency.getValue();

            switch (dependencyId) {
                case "loader" -> validateVersionRequirement("loader", context.loaderVersion(), requirement, metadata.id());
                case "java" -> validateVersionRequirement(
                    "java",
                    Integer.toString(context.javaMajorVersion()),
                    requirement,
                    metadata.id()
                );
                case "minecraft" -> validateVersionRequirement(
                    "minecraft",
                    context.targetMinecraftVersion(),
                    requirement,
                    metadata.id()
                );
                default -> {
                    ModCandidate dependencyCandidate = candidateById.get(dependencyId);
                    if (dependencyCandidate == null) {
                        throw new LoaderException("Missing dependency " + dependencyId + " required by " + metadata.id());
                    }
                    ModMetadata dependencyMetadata = requireMetadata(dependencyCandidate);
                    validateVersionRequirement(dependencyId, dependencyMetadata.version(), requirement, metadata.id());
                }
            }
        }
    }

    private void validateVersionRequirement(String dependencyId, String actualVersion, String requirement, String modId)
        throws LoaderException {
        if (!requirement.startsWith(">=")) {
            throw new LoaderException("Unsupported version requirement for " + dependencyId + " in " + modId + ": " + requirement);
        }

        String minimumVersion = requirement.substring(2).trim();
        if (!isSupportedVersion(minimumVersion)) {
            throw new LoaderException("Unsupported version requirement for " + dependencyId + " in " + modId + ": " + requirement);
        }
        if (!isSupportedVersion(actualVersion)) {
            throw new LoaderException("Unsupported actual version for " + dependencyId + ": " + actualVersion);
        }

        if (compareVersions(actualVersion, minimumVersion) < 0) {
            throw new LoaderException(
                "Dependency " + dependencyId + " does not satisfy " + requirement + " for mod " + modId + " (found " + actualVersion + ")"
            );
        }
    }

    private boolean isSupportedVersion(String version) {
        String[] segments = version.split("\\.");
        if (segments.length < 1 || segments.length > 3) {
            return false;
        }
        for (String segment : segments) {
            if (segment.isEmpty() || !segment.chars().allMatch(Character::isDigit)) {
                return false;
            }
        }
        return true;
    }

    private int compareVersions(String left, String right) {
        String[] leftParts = left.split("\\.");
        String[] rightParts = right.split("\\.");
        int size = Math.max(leftParts.length, rightParts.length);
        for (int index = 0; index < size; index++) {
            int leftValue = index < leftParts.length ? Integer.parseInt(leftParts[index]) : 0;
            int rightValue = index < rightParts.length ? Integer.parseInt(rightParts[index]) : 0;
            int result = Integer.compare(leftValue, rightValue);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private ModMetadata requireMetadata(ModCandidate candidate) throws LoaderException {
        if (candidate.metadata() == null) {
            throw new LoaderException("Metadata was not parsed for " + candidate.normalizedRelativePath());
        }
        return candidate.metadata();
    }
}
