package com.spindle.core.runtime;

import com.spindle.api.ModContext;
import com.spindle.api.service.ServiceRegistry;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.runtime.capability.RuntimeCapabilityCatalog;
import com.spindle.core.runtime.capability.RuntimeCapabilityModPlan;
import com.spindle.core.security.SecurityRuleId;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ModContextFactory {
  public Map<String, ModContext> createContexts(
      LaunchContext context, CompiledModpackProfile profile) throws LoaderException {
    return createContexts(context, profile, Map.of());
  }

  public Map<String, ModContext> createContexts(
      LaunchContext context,
      CompiledModpackProfile profile,
      Map<String, ServiceRegistry> serviceRegistries)
      throws LoaderException {
    Map<String, String> modVersionById = new LinkedHashMap<>();
    for (CompiledModpackProfile.Mod mod : profile.mods()) {
      modVersionById.put(mod.id(), mod.version());
    }

    Map<String, ModContext> contexts = new LinkedHashMap<>();
    Map<String, Set<String>> grantedCapabilitiesByModId = grantedCapabilitiesByModId(profile);
    for (CompiledModpackProfile.ModContextPlan plan : profile.contexts().mods()) {
      Set<String> grantedCapabilities =
          grantedCapabilitiesByModId.getOrDefault(plan.modId(), Set.of());
      Path configDirectory =
          resolveOwnedPath(context.workingDirectory(), plan.modId(), "config", plan.configDirectory());
      Path dataDirectory =
          resolveOwnedPath(context.workingDirectory(), plan.modId(), "data", plan.dataDirectory());
      Path cacheDirectory =
          resolveOwnedPath(context.workingDirectory(), plan.modId(), "cache", plan.cacheDirectory());
      Path generatedDirectory =
          resolveOwnedPath(
              context.workingDirectory(), plan.modId(), "generated", plan.generatedDirectory());
      createGrantedDirectories(
          plan.modId(),
          grantedCapabilities,
          configDirectory,
          dataDirectory,
          cacheDirectory,
          generatedDirectory);
      contexts.put(
          plan.modId(),
          new DefaultModContext(
              plan.modId(),
              modVersionById.getOrDefault(plan.modId(), ""),
              profile.loader().version(),
              profile.game().id(),
              profile.game().version(),
              profile.game().side(),
              context.workingDirectory(),
              grantedCapabilities,
              serviceRegistries.getOrDefault(plan.modId(), ServiceRegistry.empty()),
              configDirectory,
              dataDirectory,
              cacheDirectory,
              generatedDirectory));
    }
    return Map.copyOf(contexts);
  }

  private Path resolveOwnedPath(
      Path workingDirectory, String modId, String directoryKind, String relativePath)
      throws LoaderException {
    Path resolved = workingDirectory.resolve(relativePath).normalize();
    if (!resolved.startsWith(workingDirectory)) {
      throw new LoaderException(
          "["
              + SecurityRuleId.SEC_PATH_001.id()
              + "] Mod `"
              + modId
              + "` declares "
              + directoryKind
              + " storage path `"
              + relativePath.replace('\\', '/')
              + "`, but owned storage paths must stay within the working directory.");
    }
    return resolved;
  }

  private void createGrantedDirectories(
      String modId,
      Set<String> grantedCapabilities,
      Path configDirectory,
      Path dataDirectory,
      Path cacheDirectory,
      Path generatedDirectory)
      throws LoaderException {
    Map<String, Path> directories =
        Map.of(
            RuntimeCapabilityCatalog.STORAGE_CONFIG, configDirectory,
            RuntimeCapabilityCatalog.STORAGE_DATA, dataDirectory,
            RuntimeCapabilityCatalog.STORAGE_CACHE, cacheDirectory,
            RuntimeCapabilityCatalog.STORAGE_GENERATED, generatedDirectory);
    for (Map.Entry<String, Path> entry : directories.entrySet()) {
      if (!grantedCapabilities.contains(entry.getKey())) {
        continue;
      }
      Path directory = entry.getValue();
      try {
        Files.createDirectories(directory);
      } catch (IOException exception) {
        throw new LoaderException(
            "Failed to create owned storage directory `"
                + directory.toString().replace('\\', '/')
                + "` for mod `"
                + modId
                + "`.",
            exception);
      }
    }
  }

  private Map<String, Set<String>> grantedCapabilitiesByModId(CompiledModpackProfile profile) {
    Map<String, Set<String>> grantedCapabilitiesByModId = new LinkedHashMap<>();
    for (RuntimeCapabilityModPlan modPlan : profile.permissions().mods()) {
      Set<String> grantedCapabilities = new LinkedHashSet<>();
      modPlan.grants().stream()
          .filter(grant -> "granted".equals(grant.state()))
          .map(grant -> grant.capability())
          .forEach(grantedCapabilities::add);
      grantedCapabilitiesByModId.put(modPlan.modId(), Set.copyOf(grantedCapabilities));
    }
    return grantedCapabilitiesByModId;
  }
}
