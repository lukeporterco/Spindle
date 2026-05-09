package com.spindle.core.classpath;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.resolve.ResolvedModSet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RuntimeClasspathPlanner {
  public RuntimeClasspathPlan plan(LaunchContext context, ResolvedModSet resolvedMods)
      throws LoaderException {
    List<Path> modJars = new ArrayList<>(resolvedMods.mods().size());
    for (ResolvedModSet.ResolvedMod mod : resolvedMods.mods()) {
      Path jarPath = mod.jarPath().toAbsolutePath().normalize();
      if (!Files.isRegularFile(jarPath)) {
        throw new LoaderException("Resolved mod jar does not exist: " + jarPath);
      }
      modJars.add(jarPath);
    }

    return new RuntimeClasspathPlan(modJars, List.of(), List.of());
  }
}
