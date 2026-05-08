package com.mcmodloader.core.resource;

import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarFile;

public final class ResourceConflictIndex {
    private final List<ResourceConflict> conflicts;
    private final Map<String, Integer> resourceCountByMod;

    private ResourceConflictIndex(List<ResourceConflict> conflicts, Map<String, Integer> resourceCountByMod) {
        this.conflicts = List.copyOf(conflicts);
        this.resourceCountByMod = Map.copyOf(new TreeMap<>(resourceCountByMod));
    }

    public static ResourceConflictIndex build(ResolvedModSet resolvedMods) throws LoaderException {
        Map<String, Set<String>> resourceOwners = new TreeMap<>();
        Map<String, Integer> resourceCountByMod = new TreeMap<>();

        for (ResolvedModSet.ResolvedMod mod : resolvedMods.mods()) {
            int resourceCount = 0;
            try (JarFile jarFile = new JarFile(mod.jarPath().toFile())) {
                var entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    String name = entry.getName();
                    if (isIgnoredResource(name)) {
                        continue;
                    }
                    resourceCount++;
                    resourceOwners.computeIfAbsent(name, ignored -> new TreeSet<>()).add(mod.id());
                }
            } catch (IOException exception) {
                throw new LoaderException("Failed to index resources for mod " + mod.id(), exception);
            }
            resourceCountByMod.put(mod.id(), resourceCount);
        }

        List<ResourceConflict> conflicts = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : resourceOwners.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflicts.add(new ResourceConflict(entry.getKey(), List.copyOf(entry.getValue())));
            }
        }
        return new ResourceConflictIndex(conflicts, resourceCountByMod);
    }

    public List<ResourceConflict> conflicts() {
        return conflicts;
    }

    public Map<String, Integer> resourceCountByMod() {
        return resourceCountByMod;
    }

    private static boolean isIgnoredResource(String entryName) {
        return entryName.endsWith(".class") || "META-INF/MANIFEST.MF".equals(entryName) || "loader.mod.json".equals(entryName);
    }
}
