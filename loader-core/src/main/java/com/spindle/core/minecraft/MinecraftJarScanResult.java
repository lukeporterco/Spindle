package com.spindle.core.minecraft;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record MinecraftJarScanResult(
    String jar,
    List<String> packages,
    List<String> classes,
    List<String> resources,
    Map<String, List<String>> duplicateEntries,
    Map<String, List<String>> serviceProviders,
    boolean moduleInfoPresent,
    String automaticModuleName,
    boolean multiRelease,
    List<String> nativeLibraries,
    List<String> suspiciousPaths,
    List<String> signatureFiles,
    List<String> manifestClasspathAttributes,
    List<Integer> classFileMajorVersions,
    List<String> unsupportedClassFiles) {
  public MinecraftJarScanResult {
    packages = List.copyOf(packages);
    classes = List.copyOf(classes);
    resources = List.copyOf(resources);
    duplicateEntries = immutableStringListMap(duplicateEntries);
    serviceProviders = immutableStringListMap(serviceProviders);
    nativeLibraries = List.copyOf(nativeLibraries);
    suspiciousPaths = List.copyOf(suspiciousPaths);
    signatureFiles = List.copyOf(signatureFiles);
    manifestClasspathAttributes = List.copyOf(manifestClasspathAttributes);
    classFileMajorVersions = List.copyOf(classFileMajorVersions);
    unsupportedClassFiles = List.copyOf(unsupportedClassFiles);
  }

  private static Map<String, List<String>> immutableStringListMap(Map<String, List<String>> input) {
    TreeMap<String, List<String>> sorted = new TreeMap<>();
    for (Map.Entry<String, List<String>> entry : input.entrySet()) {
      sorted.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    return java.util.Collections.unmodifiableMap(sorted);
  }
}
