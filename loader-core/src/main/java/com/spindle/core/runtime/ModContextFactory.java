package com.spindle.core.runtime;

import com.spindle.api.ModContext;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModContextFactory {
  public Map<String, ModContext> createContexts(
      LaunchContext context, CompiledModpackProfile profile) throws LoaderException {
    Map<String, String> modVersionById = new LinkedHashMap<>();
    for (CompiledModpackProfile.Mod mod : profile.mods()) {
      modVersionById.put(mod.id(), mod.version());
    }

    Map<String, ModContext> contexts = new LinkedHashMap<>();
    for (CompiledModpackProfile.ModContextPlan plan : profile.contexts().mods()) {
      Path configDirectory = resolveOwnedPath(context.workingDirectory(), plan.configDirectory());
      Path dataDirectory = resolveOwnedPath(context.workingDirectory(), plan.dataDirectory());
      Path cacheDirectory = resolveOwnedPath(context.workingDirectory(), plan.cacheDirectory());
      Path generatedDirectory =
          resolveOwnedPath(context.workingDirectory(), plan.generatedDirectory());
      createDirectories(configDirectory, dataDirectory, cacheDirectory, generatedDirectory);
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
              configDirectory,
              dataDirectory,
              cacheDirectory,
              generatedDirectory));
    }
    return Map.copyOf(contexts);
  }

  private Path resolveOwnedPath(Path workingDirectory, String relativePath) throws LoaderException {
    Path resolved = workingDirectory.resolve(relativePath).normalize();
    if (!resolved.startsWith(workingDirectory)) {
      throw new LoaderException(
          "Owned storage path escapes working directory: " + relativePath.replace('\\', '/'));
    }
    return resolved;
  }

  private void createDirectories(Path... directories) throws LoaderException {
    for (Path directory : directories) {
      try {
        Files.createDirectories(directory);
      } catch (IOException exception) {
        throw new LoaderException(
            "Failed to create owned storage directory " + directory.toString().replace('\\', '/'),
            exception);
      }
    }
  }
}
