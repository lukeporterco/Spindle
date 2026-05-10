package com.spindle.core.ownership;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.resolve.ResolvedModSet;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.jar.JarFile;

public final class ClassOwnershipIndex {
  private final Map<String, String> classOwners;
  private final Map<String, Integer> classCountByMod;

  private ClassOwnershipIndex(
      Map<String, String> classOwners, Map<String, Integer> classCountByMod) {
    this.classOwners = Collections.unmodifiableMap(new TreeMap<>(classOwners));
    this.classCountByMod = Collections.unmodifiableMap(new TreeMap<>(classCountByMod));
  }

  public static ClassOwnershipIndex build(ResolvedModSet resolvedMods) throws LoaderException {
    Map<String, String> classOwners = new LinkedHashMap<>();
    Map<String, Integer> classCountByMod = new TreeMap<>();
    for (ResolvedModSet.ResolvedMod mod : resolvedMods.mods()) {
      int modClassCount = 0;
      try (JarFile jarFile = new JarFile(mod.jarPath().toFile())) {
        for (var entries = jarFile.entries(); entries.hasMoreElements(); ) {
          var entry = entries.nextElement();
          if (entry.isDirectory()) {
            continue;
          }
          String name = entry.getName();
          if (!name.endsWith(".class") || "module-info.class".equals(name)) {
            continue;
          }
          modClassCount++;
          String className = toBinaryClassName(name);
          classOwners.compute(
              className,
              (ignored, existingOwner) -> {
                if (existingOwner != null && !existingOwner.equals(mod.id())) {
                  throw new DuplicateClassOwnershipException(existingOwner, mod.id(), className);
                }
                return mod.id();
              });
        }
      } catch (DuplicateClassOwnershipException exception) {
        throw new LoaderException(
            "Duplicate class "
                + exception.className
                + " found in mods "
                + exception.leftModId
                + " and "
                + exception.rightModId);
      } catch (IOException exception) {
        throw new LoaderException("Failed to index classes for mod " + mod.id(), exception);
      }
      classCountByMod.put(mod.id(), modClassCount);
    }

    return new ClassOwnershipIndex(classOwners, classCountByMod);
  }

  public Optional<String> ownerOfClass(String binaryClassName) {
    return Optional.ofNullable(classOwners.get(binaryClassName));
  }

  public Map<String, String> classOwners() {
    return classOwners;
  }

  public Map<String, Integer> classCountByMod() {
    return classCountByMod;
  }

  public int totalClasses() {
    return classOwners.size();
  }

  private static String toBinaryClassName(String entryName) {
    return entryName.substring(0, entryName.length() - ".class".length()).replace('/', '.');
  }

  private static final class DuplicateClassOwnershipException extends RuntimeException {
    private final String leftModId;
    private final String rightModId;
    private final String className;

    private DuplicateClassOwnershipException(
        String leftModId, String rightModId, String className) {
      this.leftModId = leftModId;
      this.rightModId = rightModId;
      this.className = className;
    }
  }
}
