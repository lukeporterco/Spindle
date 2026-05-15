package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.MinecraftRuntimeFile;
import com.spindle.core.minecraft.MinecraftServerRuntimeClasspath;
import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class SteelHook02RuntimeClasspathUrlBuilder {
  public SteelHook02RuntimeClasspathUrls build(MinecraftServerRuntimePlan runtimePlan)
      throws LoaderException {
    if (runtimePlan == null) {
      throw new LoaderException("Target-26 requires a Minecraft runtime plan.");
    }
    List<Path> orderedPaths = new ArrayList<>();
    LinkedHashSet<String> seen = new LinkedHashSet<>();
    addRequiredPath(orderedPaths, seen, runtimePlan.serverJarPath(), "server jar");
    for (MinecraftServerRuntimeClasspath.Entry entry : runtimePlan.classpathEntries()) {
      addRequiredPath(orderedPaths, seen, entry.path(), "classpath entry");
    }
    for (MinecraftRuntimeFile file : runtimePlan.bundledRuntimeFiles()) {
      if (file.path() != null) {
        addRequiredPath(orderedPaths, seen, file.path().toString(), "bundled runtime file");
      }
    }
    List<java.net.URL> urls = new ArrayList<>(orderedPaths.size());
    List<String> normalizedPaths = new ArrayList<>(orderedPaths.size());
    for (Path path : orderedPaths) {
      normalizedPaths.add(path.toString());
      try {
        urls.add(path.toUri().toURL());
      } catch (MalformedURLException exception) {
        throw new LoaderException(
            "Target-26 could not convert runtime classpath path to URL: " + path, exception);
      }
    }
    return new SteelHook02RuntimeClasspathUrls(urls, normalizedPaths);
  }

  private void addRequiredPath(
      List<Path> orderedPaths, LinkedHashSet<String> seen, String rawPath, String label)
      throws LoaderException {
    if (rawPath == null || rawPath.isBlank()) {
      throw new LoaderException("Target-26 requires a " + label + " path in the runtime plan.");
    }
    Path normalized = Path.of(rawPath).toAbsolutePath().normalize();
    if (!Files.isRegularFile(normalized)) {
      throw new LoaderException(
          "Target-26 requires readable runtime classpath file " + normalized + ".");
    }
    if (!Files.isReadable(normalized)) {
      throw new LoaderException(
          "Target-26 requires readable runtime classpath file " + normalized + ".");
    }
    if (seen.add(normalized.toString())) {
      orderedPaths.add(normalized);
    }
  }
}
