package com.spindle.core.mache;

import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class MacheReferenceScanner {
  public MacheReferenceReport scan(Path macheDirectory, String requestedVersion)
      throws LoaderException {
    Path normalizedDirectory = macheDirectory.toAbsolutePath().normalize();
    if (!Files.isDirectory(normalizedDirectory)) {
      throw new LoaderException("Mache directory does not exist: " + normalizedDirectory);
    }

    List<String> detectedVersionDirectories = detectVersionDirectories(normalizedDirectory);
    boolean hasRequestedVersionDirectory =
        requestedVersion != null && detectedVersionDirectories.contains(requestedVersion);
    List<String> warnings = new ArrayList<>();
    if (requestedVersion != null && !requestedVersion.isBlank() && !hasRequestedVersionDirectory) {
      warnings.add("Requested Mache version directory was not found: " + requestedVersion);
    }

    Path licensePath =
        firstExisting(
            normalizedDirectory.resolve("LICENSE"), normalizedDirectory.resolve("LICENSE.txt"));
    if (licensePath != null) {
      String licenseText = readString(licensePath).toUpperCase(Locale.ROOT);
      if (licenseText.contains("LGPL")) {
        warnings.add("Mache is reference-only. Do not copy code into this MIT project.");
      }
    }

    warnings.addAll(scanPotentialSourceLayouts(normalizedDirectory));

    return new MacheReferenceReport(
        1,
        normalizedDirectory.toString().replace('\\', '/'),
        requestedVersion,
        detectedVersionDirectories,
        hasRequestedVersionDirectory,
        requestedVersion == null || requestedVersion.isBlank()
            ? null
            : "release/" + requestedVersion,
        new MacheReferenceReport.FileFlags(
            firstExisting(
                    normalizedDirectory.resolve("settings.gradle"),
                    normalizedDirectory.resolve("settings.gradle.kts"))
                != null,
            Files.isRegularFile(normalizedDirectory.resolve("gradle.properties")),
            firstExisting(
                    normalizedDirectory.resolve("README.md"),
                    normalizedDirectory.resolve("readme.md"))
                != null,
            licensePath != null),
        warnings);
  }

  private List<String> detectVersionDirectories(Path macheDirectory) throws LoaderException {
    Path versionsDirectory = macheDirectory.resolve("versions");
    if (!Files.isDirectory(versionsDirectory)) {
      return List.of();
    }
    try (Stream<Path> stream = Files.list(versionsDirectory)) {
      return stream
          .filter(Files::isDirectory)
          .map(path -> path.getFileName().toString())
          .sorted()
          .toList();
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to scan Mache versions directory: " + versionsDirectory, exception);
    }
  }

  private List<String> scanPotentialSourceLayouts(Path macheDirectory) throws LoaderException {
    List<String> warnings = new ArrayList<>();
    Path versionsDirectory = macheDirectory.resolve("versions");
    if (!Files.isDirectory(versionsDirectory)) {
      return warnings;
    }
    try (Stream<Path> stream = Files.walk(versionsDirectory)) {
      stream
          .filter(Files::isDirectory)
          .filter(path -> path.toString().replace('\\', '/').contains("/src/main/java"))
          .sorted(Comparator.comparing(Path::toString))
          .forEach(
              path -> {
                try (Stream<Path> sources = Files.walk(path)) {
                  long javaFileCount =
                      sources
                          .filter(
                              candidate ->
                                  Files.isRegularFile(candidate)
                                      && candidate.toString().endsWith(".java"))
                          .count();
                  if (javaFileCount > 0) {
                    warnings.add(
                        "Detected potential decompiled source layout at "
                            + path.toString().replace('\\', '/')
                            + " ("
                            + javaFileCount
                            + " .java files)");
                  }
                } catch (IOException ignored) {
                }
              });
    } catch (IOException exception) {
      throw new LoaderException("Failed to scan Mache layout: " + macheDirectory, exception);
    }
    return warnings;
  }

  private Path firstExisting(Path first, Path second) {
    if (Files.isRegularFile(first)) {
      return first;
    }
    return Files.isRegularFile(second) ? second : null;
  }

  private String readString(Path path) throws LoaderException {
    try {
      return Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new LoaderException("Failed to read Mache file: " + path, exception);
    }
  }
}
