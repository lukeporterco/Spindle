package com.spindle.core.ownership;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.resolve.ResolvedModSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarFile;

public final class PackageOwnershipIndex {
  private final Map<String, List<String>> packageOwners;
  private final List<SplitPackage> splitPackages;
  private final Map<String, Integer> packageCountByMod;

  private PackageOwnershipIndex(
      Map<String, List<String>> packageOwners,
      List<SplitPackage> splitPackages,
      Map<String, Integer> packageCountByMod) {
    this.packageOwners = Collections.unmodifiableMap(new TreeMap<>(packageOwners));
    this.splitPackages = List.copyOf(splitPackages);
    this.packageCountByMod = Collections.unmodifiableMap(new TreeMap<>(packageCountByMod));
  }

  public static PackageOwnershipIndex build(ResolvedModSet resolvedMods) throws LoaderException {
    Map<String, Set<String>> packageOwners = new TreeMap<>();
    Map<String, Set<String>> packagesByMod = new TreeMap<>();

    for (ResolvedModSet.ResolvedMod mod : resolvedMods.mods()) {
      Set<String> modPackages = new TreeSet<>();
      try (JarFile jarFile = new JarFile(mod.jarPath().toFile())) {
        jarFile.stream()
            .filter(entry -> !entry.isDirectory())
            .map(entry -> entry.getName())
            .filter(name -> name.endsWith(".class"))
            .filter(name -> !"module-info.class".equals(name))
            .map(PackageOwnershipIndex::toPackageName)
            .forEach(
                packageName -> {
                  modPackages.add(packageName);
                  packageOwners
                      .computeIfAbsent(packageName, ignored -> new TreeSet<>())
                      .add(mod.id());
                });
      } catch (IOException exception) {
        throw new LoaderException("Failed to index packages for mod " + mod.id(), exception);
      }
      packagesByMod.put(mod.id(), modPackages);
    }

    List<SplitPackage> splitPackages = new ArrayList<>();
    Map<String, List<String>> normalizedOwners = new TreeMap<>();
    for (Map.Entry<String, Set<String>> entry : packageOwners.entrySet()) {
      List<String> owners = List.copyOf(entry.getValue());
      normalizedOwners.put(entry.getKey(), owners);
      if (owners.size() > 1) {
        splitPackages.add(new SplitPackage(entry.getKey(), owners));
      }
    }

    Map<String, Integer> packageCountByMod = new TreeMap<>();
    for (Map.Entry<String, Set<String>> entry : packagesByMod.entrySet()) {
      packageCountByMod.put(entry.getKey(), entry.getValue().size());
    }

    return new PackageOwnershipIndex(normalizedOwners, splitPackages, packageCountByMod);
  }

  public Map<String, List<String>> packageOwners() {
    return packageOwners;
  }

  public List<SplitPackage> splitPackages() {
    return splitPackages;
  }

  public Map<String, Integer> packageCountByMod() {
    return packageCountByMod;
  }

  private static String toPackageName(String classEntryName) {
    String binaryClassName =
        classEntryName.substring(0, classEntryName.length() - ".class".length()).replace('/', '.');
    int lastDot = binaryClassName.lastIndexOf('.');
    return lastDot >= 0 ? binaryClassName.substring(0, lastDot) : "";
  }

  public record SplitPackage(String packageName, List<String> modIds) {
    public SplitPackage {
      modIds = List.copyOf(modIds);
    }
  }
}
