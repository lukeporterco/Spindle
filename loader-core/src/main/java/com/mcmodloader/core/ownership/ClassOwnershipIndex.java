package com.mcmodloader.core.ownership;

import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.jar.JarFile;

public final class ClassOwnershipIndex {
    private final Map<String, String> classOwners;

    private ClassOwnershipIndex(Map<String, String> classOwners) {
        this.classOwners = Map.copyOf(new TreeMap<>(classOwners));
    }

    public static ClassOwnershipIndex build(ResolvedModSet resolvedMods) throws LoaderException {
        Map<String, String> classOwners = new LinkedHashMap<>();
        for (ResolvedModSet.ResolvedMod mod : resolvedMods.mods()) {
            try (JarFile jarFile = new JarFile(mod.jarPath().toFile())) {
                jarFile
                    .stream()
                    .filter(entry -> !entry.isDirectory())
                    .map(entry -> entry.getName())
                    .filter(name -> name.endsWith(".class"))
                    .filter(name -> !"module-info.class".equals(name))
                    .map(ClassOwnershipIndex::toBinaryClassName)
                    .forEach(className -> classOwners.compute(className, (ignored, existingOwner) -> {
                        if (existingOwner != null && !existingOwner.equals(mod.id())) {
                            throw new DuplicateClassOwnershipException(existingOwner, mod.id(), className);
                        }
                        return mod.id();
                    }));
            } catch (DuplicateClassOwnershipException exception) {
                throw new LoaderException(
                    "Duplicate class " +
                    exception.className +
                    " found in mods " +
                    exception.leftModId +
                    " and " +
                    exception.rightModId
                );
            } catch (IOException exception) {
                throw new LoaderException("Failed to index classes for mod " + mod.id(), exception);
            }
        }

        return new ClassOwnershipIndex(classOwners);
    }

    public Optional<String> ownerOfClass(String binaryClassName) {
        return Optional.ofNullable(classOwners.get(binaryClassName));
    }

    public Map<String, String> classOwners() {
        return classOwners;
    }

    private static String toBinaryClassName(String entryName) {
        return entryName.substring(0, entryName.length() - ".class".length()).replace('/', '.');
    }

    private static final class DuplicateClassOwnershipException extends RuntimeException {
        private final String leftModId;
        private final String rightModId;
        private final String className;

        private DuplicateClassOwnershipException(String leftModId, String rightModId, String className) {
            this.leftModId = leftModId;
            this.rightModId = rightModId;
            this.className = className;
        }
    }
}
