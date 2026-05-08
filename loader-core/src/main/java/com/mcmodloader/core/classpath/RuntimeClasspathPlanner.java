package com.mcmodloader.core.classpath;

import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RuntimeClasspathPlanner {
    public RuntimeClasspathPlan plan(LaunchContext context, ResolvedModSet resolvedMods) throws LoaderException {
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
