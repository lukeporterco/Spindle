package com.spindle.core.minecraft;

import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

public final class MinecraftBundledServerResolver {
  private final MinecraftRuntimeFileVerifier verifier = new MinecraftRuntimeFileVerifier();

  public ResolvedBundledServer resolve(
      Path serverJar,
      MinecraftBundledServerInspector.Inspection inspection,
      MinecraftRuntimeCacheLayout layout,
      boolean strict)
      throws LoaderException {
    if (inspection == null || !inspection.bundled()) {
      return new ResolvedBundledServer(List.of(), List.of());
    }

    Map<String, String> expectedShaByRelativePath = new LinkedHashMap<>();
    List<MinecraftRuntimeFile> extracted = new ArrayList<>();
    List<Path> classpath = new ArrayList<>();
    try (JarFile jarFile = new JarFile(serverJar.toFile())) {
      List<MinecraftBundledServerInspector.BundledEntry> entries = new ArrayList<>();
      entries.addAll(inspection.versions());
      entries.addAll(inspection.libraries());
      entries.sort(
          Comparator.comparingInt(
                  (MinecraftBundledServerInspector.BundledEntry entry) ->
                      "version".equals(entry.kind()) ? 0 : 1)
              .thenComparing(MinecraftBundledServerInspector.BundledEntry::path));

      for (MinecraftBundledServerInspector.BundledEntry entry : entries) {
        Path target =
            "library".equals(entry.kind())
                ? layout.libraryPath(entry.path())
                : layout.versionPath(entry.path());
        if (!layout.owns(target)) {
          throw new LoaderException(
              "Bundled runtime entry escapes runtime cache layout: " + entry.path());
        }

        String relativeCachePath =
            layout.runtimeDirectory().relativize(target).toString().replace('\\', '/');
        String previousSha = expectedShaByRelativePath.putIfAbsent(relativeCachePath, entry.sha1());
        if (previousSha != null && !previousSha.equalsIgnoreCase(entry.sha1())) {
          throw new LoaderException(
              "Duplicate bundled runtime entry has conflicting hash: " + relativeCachePath);
        }

        if (!Files.isRegularFile(target) || strict) {
          Files.createDirectories(target.getParent());
          try (var inputStream = jarFile.getInputStream(jarFile.getEntry(entry.jarEntryName()))) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
          }
        }

        MinecraftRuntimeFileVerifier.Verification verification =
            verifier.verify(target, entry.sha1());
        MinecraftRuntimeFile runtimeFile =
            new MinecraftRuntimeFile(
                entry.id(),
                target,
                relativeCachePath,
                serverJar.getFileName().toString() + "!" + entry.jarEntryName(),
                verification.sha1(),
                verification.sha256(),
                verification.size(),
                verification.present(),
                verification.verified(),
                verification.status());
        if (strict && !runtimeFile.verified()) {
          throw new LoaderException(
              "Strict bundled runtime verification failed for "
                  + relativeCachePath
                  + ": "
                  + runtimeFile.verificationStatus());
        }
        extracted.add(runtimeFile);
        classpath.add(target);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to materialize bundled Minecraft server runtime", exception);
    }
    return new ResolvedBundledServer(List.copyOf(extracted), List.copyOf(classpath));
  }

  public record ResolvedBundledServer(
      List<MinecraftRuntimeFile> extractedFiles, List<Path> classpath) {
    public ResolvedBundledServer {
      extractedFiles = List.copyOf(extractedFiles);
      classpath = List.copyOf(classpath);
    }
  }
}
