package com.mcmodloader.core.minecraft;

import java.util.List;
import java.util.Map;

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
    List<String> unsupportedClassFiles
) {
    public MinecraftJarScanResult {
        packages = List.copyOf(packages);
        classes = List.copyOf(classes);
        resources = List.copyOf(resources);
        duplicateEntries = Map.copyOf(duplicateEntries);
        serviceProviders = Map.copyOf(serviceProviders);
        nativeLibraries = List.copyOf(nativeLibraries);
        suspiciousPaths = List.copyOf(suspiciousPaths);
        signatureFiles = List.copyOf(signatureFiles);
        manifestClasspathAttributes = List.copyOf(manifestClasspathAttributes);
        classFileMajorVersions = List.copyOf(classFileMajorVersions);
        unsupportedClassFiles = List.copyOf(unsupportedClassFiles);
    }
}
