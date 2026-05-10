package com.spindle.core.minecraft;

import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class MinecraftJarScanner {
  private static final int JAVA_25_CLASS_MAJOR = 69;

  public MinecraftJarScanResult scan(Path jarPath, String displayPath) throws LoaderException {
    TreeSet<String> packages = new TreeSet<>();
    TreeSet<String> classes = new TreeSet<>();
    TreeSet<String> resources = new TreeSet<>();
    TreeMap<String, List<String>> serviceProviders = new TreeMap<>();
    TreeSet<String> nativeLibraries = new TreeSet<>();
    TreeSet<String> suspiciousPaths = new TreeSet<>();
    TreeSet<String> signatureFiles = new TreeSet<>();
    TreeSet<Integer> classMajors = new TreeSet<>();
    TreeSet<String> unsupportedClassFiles = new TreeSet<>();
    TreeMap<String, List<String>> duplicates = duplicateEntries(jarPath);
    boolean moduleInfoPresent = false;
    String automaticModuleName = null;
    boolean multiRelease = false;
    List<String> manifestClasspath = new ArrayList<>();

    try (JarFile jarFile = new JarFile(jarPath.toFile())) {
      Manifest manifest = jarFile.getManifest();
      if (manifest != null) {
        Attributes attributes = manifest.getMainAttributes();
        automaticModuleName = attributes.getValue("Automatic-Module-Name");
        multiRelease = "true".equalsIgnoreCase(attributes.getValue("Multi-Release"));
        String classPath = attributes.getValue(Attributes.Name.CLASS_PATH);
        if (classPath != null && !classPath.isBlank()) {
          manifestClasspath.add(classPath);
        }
      }

      List<? extends ZipEntry> entries =
          jarFile.stream().sorted(Comparator.comparing(ZipEntry::getName)).toList();
      for (ZipEntry entry : entries) {
        if (entry.isDirectory()) {
          continue;
        }
        String name = entry.getName().replace('\\', '/');
        if (name.startsWith("/") || name.contains("../") || name.contains("/..")) {
          suspiciousPaths.add(name);
        }
        if (name.equals("module-info.class") || name.endsWith("/module-info.class")) {
          moduleInfoPresent = true;
        }
        if (isSignature(name)) {
          signatureFiles.add(name);
        }
        if (isNative(name)) {
          nativeLibraries.add(name);
        }
        if (name.startsWith("META-INF/services/") && !name.equals("META-INF/services/")) {
          serviceProviders.put(
              name.substring("META-INF/services/".length()), serviceLines(jarFile, entry));
        }
        if (name.endsWith(".class")) {
          classes.add(name.substring(0, name.length() - ".class".length()).replace('/', '.'));
          String packageName = packageName(name);
          if (!packageName.isBlank()) {
            packages.add(packageName);
          }
          int major = classMajor(jarFile, entry);
          if (major > 0) {
            classMajors.add(major);
            if (major > JAVA_25_CLASS_MAJOR) {
              unsupportedClassFiles.add(name + ":" + major);
            }
          }
        } else if (!name.equals("META-INF/MANIFEST.MF")) {
          resources.add(name);
        }
      }
    } catch (IOException exception) {
      throw new LoaderException("Failed to scan jar without class loading: " + jarPath, exception);
    }

    return new MinecraftJarScanResult(
        displayPath,
        List.copyOf(packages),
        List.copyOf(classes),
        List.copyOf(resources),
        duplicates,
        serviceProviders,
        moduleInfoPresent,
        automaticModuleName,
        multiRelease,
        List.copyOf(nativeLibraries),
        List.copyOf(suspiciousPaths),
        List.copyOf(signatureFiles),
        List.copyOf(manifestClasspath),
        List.copyOf(classMajors),
        List.copyOf(unsupportedClassFiles));
  }

  private TreeMap<String, List<String>> duplicateEntries(Path jarPath) throws LoaderException {
    TreeMap<String, Integer> counts = new TreeMap<>();
    try (ZipInputStream inputStream =
        new ZipInputStream(java.nio.file.Files.newInputStream(jarPath))) {
      ZipEntry entry;
      while ((entry = inputStream.getNextEntry()) != null) {
        if (!entry.isDirectory()) {
          counts.merge(entry.getName().replace('\\', '/'), 1, Integer::sum);
        }
      }
    } catch (IOException exception) {
      throw new LoaderException("Failed to inspect duplicate jar entries: " + jarPath, exception);
    }
    TreeMap<String, List<String>> duplicates = new TreeMap<>();
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      if (entry.getValue() > 1) {
        duplicates.put(entry.getKey(), List.of("count=" + entry.getValue()));
      }
    }
    return duplicates;
  }

  private List<String> serviceLines(JarFile jarFile, ZipEntry entry) throws IOException {
    try (var inputStream = jarFile.getInputStream(entry)) {
      String content =
          new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
      return content
          .lines()
          .map(String::trim)
          .filter(line -> !line.isEmpty() && !line.startsWith("#"))
          .sorted()
          .toList();
    }
  }

  private int classMajor(JarFile jarFile, ZipEntry entry) {
    try (var inputStream = jarFile.getInputStream(entry)) {
      byte[] header = inputStream.readNBytes(8);
      if (header.length < 8
          || header[0] != (byte) 0xCA
          || header[1] != (byte) 0xFE
          || header[2] != (byte) 0xBA
          || header[3] != (byte) 0xBE) {
        return -1;
      }
      return ((header[6] & 0xff) << 8) | (header[7] & 0xff);
    } catch (IOException ignored) {
      return -1;
    }
  }

  private String packageName(String classEntry) {
    int slash = classEntry.lastIndexOf('/');
    return slash <= 0 ? "" : classEntry.substring(0, slash).replace('/', '.');
  }

  private boolean isNative(String name) {
    String lower = name.toLowerCase(java.util.Locale.ROOT);
    return lower.endsWith(".dll")
        || lower.endsWith(".so")
        || lower.endsWith(".dylib")
        || lower.endsWith(".jnilib");
  }

  private boolean isSignature(String name) {
    String upper = name.toUpperCase(java.util.Locale.ROOT);
    return upper.startsWith("META-INF/")
        && (upper.endsWith(".SF")
            || upper.endsWith(".DSA")
            || upper.endsWith(".RSA")
            || upper.endsWith(".EC"));
  }
}
