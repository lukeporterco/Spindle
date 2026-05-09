package com.spindle.core.resolve;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.discovery.ModCandidate;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.metadata.ModMetadata;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DependencyResolver {
  public ResolvedModSet resolve(LaunchContext context, List<ModCandidate> candidates)
      throws LoaderException {
    Map<String, ModCandidate> candidateById = new LinkedHashMap<>();
    for (ModCandidate candidate : candidates) {
      ModMetadata metadata = requireMetadata(candidate);
      ModCandidate duplicate = candidateById.putIfAbsent(metadata.id(), candidate);
      if (duplicate != null) {
        throw new LoaderException(
            "Duplicate mod id discovered: "
                + metadata.id()
                + " at "
                + duplicate.normalizedRelativePath()
                + " and "
                + candidate.normalizedRelativePath());
      }
    }

    for (ModCandidate candidate : candidates) {
      validateDependencies(context, candidate, candidateById);
      validateIncompatibilities(candidate, candidateById);
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
              metadata.entrypoints(),
              metadata.depends(),
              metadata.breaks(),
              metadata.schema(),
              metadata.lifecycle(),
              metadata.permissions(),
              metadata.storage(),
              metadata.services()));
    }

    resolvedMods.sort(Comparator.comparing(ResolvedModSet.ResolvedMod::id));
    return new ResolvedModSet(resolvedMods);
  }

  private void validateDependencies(
      LaunchContext context, ModCandidate candidate, Map<String, ModCandidate> candidateById)
      throws LoaderException {
    ModMetadata metadata = requireMetadata(candidate);
    for (Map.Entry<String, String> dependency : metadata.depends().entrySet()) {
      String dependencyId = dependency.getKey();
      String requirement = dependency.getValue();

      switch (dependencyId) {
        case "loader" ->
            validateVersionRequirement(
                metadata.id(), dependencyId, context.loaderVersion(), requirement, true);
        case "java" ->
            validateVersionRequirement(
                metadata.id(),
                dependencyId,
                Integer.toString(context.javaMajorVersion()),
                requirement,
                true);
        case "minecraft" ->
            validateVersionRequirement(
                metadata.id(), dependencyId, context.targetMinecraftVersion(), requirement, true);
        default -> {
          ModCandidate dependencyCandidate = candidateById.get(dependencyId);
          if (dependencyCandidate == null) {
            throw new LoaderException(
                "Missing dependency "
                    + dependencyId
                    + " required by mod "
                    + metadata.id()
                    + " with requirement "
                    + requirement
                    + "; discovered mods: "
                    + discoveredModIds(candidateById));
          }
          ModMetadata dependencyMetadata = requireMetadata(dependencyCandidate);
          validateVersionRequirement(
              metadata.id(), dependencyId, dependencyMetadata.version(), requirement, false);
        }
      }
    }
  }

  private void validateIncompatibilities(
      ModCandidate candidate, Map<String, ModCandidate> candidateById) throws LoaderException {
    ModMetadata metadata = requireMetadata(candidate);
    for (Map.Entry<String, String> incompatibility : metadata.breaks().entrySet()) {
      String brokenModId = incompatibility.getKey();
      ModCandidate brokenCandidate = candidateById.get(brokenModId);
      if (brokenCandidate == null) {
        continue;
      }

      ModMetadata brokenMetadata = requireMetadata(brokenCandidate);
      VersionRequirement requirement =
          parseRequirement(metadata.id(), brokenModId, incompatibility.getValue());
      if (requirement.matches(brokenMetadata.version())) {
        throw new LoaderException(
            "Mod "
                + metadata.id()
                + " breaks "
                + brokenModId
                + " "
                + brokenMetadata.version()
                + " with requirement "
                + incompatibility.getValue());
      }
    }
  }

  private void validateVersionRequirement(
      String requestingModId,
      String dependencyId,
      String actualVersion,
      String requirementText,
      boolean builtinDependency)
      throws LoaderException {
    VersionRequirement requirement =
        parseRequirement(requestingModId, dependencyId, requirementText);
    if (!VersionRequirement.isSupportedVersion(actualVersion)) {
      throw new LoaderException(
          "Unsupported actual version for " + dependencyId + ": " + actualVersion);
    }
    if (requirement.matches(actualVersion)) {
      return;
    }

    if (builtinDependency) {
      throw new LoaderException(
          "Builtin dependency failure for mod "
              + requestingModId
              + ": "
              + dependencyId
              + " requires "
              + requirementText
              + " but actual version is "
              + actualVersion);
    }

    throw new LoaderException(
        "Dependency version failure for mod "
            + requestingModId
            + ": "
            + dependencyId
            + " requires "
            + requirementText
            + " but discovered version is "
            + actualVersion);
  }

  private VersionRequirement parseRequirement(
      String requestingModId, String dependencyId, String requirementText) throws LoaderException {
    try {
      return VersionRequirement.parse(requirementText);
    } catch (IllegalArgumentException exception) {
      throw new LoaderException(
          "Unsupported version requirement for dependency "
              + dependencyId
              + " in mod "
              + requestingModId
              + ": "
              + requirementText);
    }
  }

  private String discoveredModIds(Map<String, ModCandidate> candidateById) {
    return String.join(",", candidateById.keySet().stream().sorted().toList());
  }

  private ModMetadata requireMetadata(ModCandidate candidate) throws LoaderException {
    if (candidate.metadata() == null) {
      throw new LoaderException(
          "Metadata was not parsed for " + candidate.normalizedRelativePath());
    }
    return candidate.metadata();
  }
}
