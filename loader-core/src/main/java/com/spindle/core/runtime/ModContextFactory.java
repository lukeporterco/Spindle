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
      Path configDirectory =
          resolveOwnedPath(context.workingDirectory(), plan.modId(), "config", plan.configDirectory());
      Path dataDirectory =
          resolveOwnedPath(context.workingDirectory(), plan.modId(), "data", plan.dataDirectory());
      Path cacheDirectory =
          resolveOwnedPath(context.workingDirectory(), plan.modId(), "cache", plan.cacheDirectory());
      Path generatedDirectory =
          resolveOwnedPath(
              context.workingDirectory(), plan.modId(), "generated", plan.generatedDirectory());
      createDirectories(
          plan.modId(),
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
          "Mod `"
              + modId
              + "` declares "
              + directoryKind
              + " storage path `"
              + relativePath.replace('\\', '/')
              + "`, but owned storage paths must stay within the working directory.");
    }
    return resolved;
  }

  private void createDirectories(String modId, Path... directories) throws LoaderException {
    for (Path directory : directories) {
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
}
