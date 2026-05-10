package com.spindle.core.minecraft;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class MinecraftLibrarySelector {
  public Selection select(MinecraftVersionMetadata metadata, Path librariesRoot) {
    return select(metadata, librariesRoot, OperatingSystem.detect());
  }

  public Selection select(
      MinecraftVersionMetadata metadata, Path librariesRoot, OperatingSystem operatingSystem) {
    List<SelectedLibrary> libraries = new ArrayList<>();
    List<SelectedLibrary> nativeLibraries = new ArrayList<>();

    for (MinecraftVersionMetadata.Library library : metadata.libraries()) {
      if (!allows(library.rules(), operatingSystem)) {
        continue;
      }

      if (library.artifact() != null
          && library.artifact().path() != null
          && !library.artifact().path().isBlank()) {
        libraries.add(
            new SelectedLibrary(
                library.name(),
                librariesRoot.resolve(library.artifact().path()).toAbsolutePath().normalize(),
                library.artifact().sha1(),
                library.artifact().size(),
                false));
      }

      String classifierKey = nativeClassifierKey(library, operatingSystem);
      if (classifierKey == null) {
        continue;
      }
      MinecraftVersionMetadata.Download classifierDownload =
          library.classifiers().get(classifierKey);
      if (classifierDownload != null
          && classifierDownload.path() != null
          && !classifierDownload.path().isBlank()) {
        nativeLibraries.add(
            new SelectedLibrary(
                library.name(),
                librariesRoot.resolve(classifierDownload.path()).toAbsolutePath().normalize(),
                classifierDownload.sha1(),
                classifierDownload.size(),
                true));
      }
    }

    libraries.sort(Comparator.comparing(selected -> selected.path().toString()));
    nativeLibraries.sort(Comparator.comparing(selected -> selected.path().toString()));
    return new Selection(libraries, nativeLibraries);
  }

  public static boolean allows(
      List<MinecraftVersionMetadata.Rule> rules, OperatingSystem operatingSystem) {
    if (rules == null || rules.isEmpty()) {
      return true;
    }
    boolean allowed = false;
    for (MinecraftVersionMetadata.Rule rule : rules) {
      if (!matches(rule, operatingSystem)) {
        continue;
      }
      if ("allow".equalsIgnoreCase(rule.action())) {
        allowed = true;
      } else if ("disallow".equalsIgnoreCase(rule.action())) {
        allowed = false;
      }
    }
    return allowed;
  }

  public static boolean matches(
      MinecraftVersionMetadata.Rule rule, OperatingSystem operatingSystem) {
    if (rule == null) {
      return false;
    }
    if (rule.osName() != null
        && !rule.osName().isBlank()
        && !rule.osName().equalsIgnoreCase(operatingSystem.name())) {
      return false;
    }
    if (rule.osArch() != null
        && !rule.osArch().isBlank()
        && !operatingSystem.arch().equalsIgnoreCase(rule.osArch())) {
      return false;
    }
    return rule.osVersion() == null
        || rule.osVersion().isBlank()
        || operatingSystem.version().matches(rule.osVersion());
  }

  private String nativeClassifierKey(
      MinecraftVersionMetadata.Library library, OperatingSystem operatingSystem) {
    String value = library.natives().get(operatingSystem.name());
    if (value == null || value.isBlank()) {
      return null;
    }
    if (value.contains("${arch}")) {
      return value.replace("${arch}", operatingSystem.nativeArchToken());
    }
    return value;
  }

  public record Selection(List<SelectedLibrary> libraries, List<SelectedLibrary> nativeLibraries) {
    public Selection {
      libraries = List.copyOf(libraries);
      nativeLibraries = List.copyOf(nativeLibraries);
    }
  }

  public record SelectedLibrary(
      String name, Path path, String sha1, long size, boolean nativeLibrary) {
    public SelectedLibrary {
      path = path.toAbsolutePath().normalize();
    }
  }

  public record OperatingSystem(String name, String arch, String version) {
    static OperatingSystem detect() {
      String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
      String name;
      if (osName.contains("win")) {
        name = "windows";
      } else if (osName.contains("mac")) {
        name = "osx";
      } else {
        name = "linux";
      }
      return new OperatingSystem(
          name, System.getProperty("os.arch", ""), System.getProperty("os.version", ""));
    }

    String nativeArchToken() {
      if (arch.contains("64")) {
        return "64";
      }
      String digits = arch.replaceAll("[^0-9]", "");
      return digits.isBlank() ? arch : digits;
    }
  }
}
