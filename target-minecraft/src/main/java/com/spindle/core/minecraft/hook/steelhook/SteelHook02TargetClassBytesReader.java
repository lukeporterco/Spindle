package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.MinecraftRuntimeFile;
import com.spindle.core.minecraft.MinecraftServerRuntimeClasspath;
import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.jar.JarFile;

public final class SteelHook02TargetClassBytesReader {
  public SteelHook02TargetClassBytes read(
      MinecraftServerRuntimePlan runtimePlan, SteelHook02TargetDescriptor targetDescriptor) {
    if (runtimePlan == null) {
      return new SteelHook02TargetClassBytes(
          targetDescriptor == null ? null : targetDescriptor.classEntryName(),
          null,
          null,
          null,
          null,
          false,
          false,
          "Minecraft runtime plan is required for Target-25 class-byte inspection.");
    }
    if (targetDescriptor == null) {
      return new SteelHook02TargetClassBytes(
          null,
          null,
          null,
          null,
          null,
          false,
          false,
          "Target-25 requires a Target-24 target descriptor.");
    }
    List<Candidate> candidates = candidates(runtimePlan);
    String firstReadFailure = null;
    for (Candidate candidate : candidates) {
      Path path = candidate.path();
      if (path == null) {
        continue;
      }
      if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
        if (firstReadFailure == null) {
          firstReadFailure = "Target-25 could not read runtime artifact candidate " + path + ".";
        }
        continue;
      }
      try (JarFile jarFile = new JarFile(path.toFile())) {
        var entry = jarFile.getJarEntry(targetDescriptor.classEntryName());
        if (entry == null) {
          continue;
        }
        byte[] classBytes = jarFile.getInputStream(entry).readAllBytes();
        return new SteelHook02TargetClassBytes(
            targetDescriptor.classEntryName(),
            path.toString(),
            candidate.kind(),
            sha256Hex(classBytes),
            classBytes,
            true,
            true,
            null);
      } catch (IOException exception) {
        if (firstReadFailure == null) {
          firstReadFailure = "Target-25 could not inspect runtime artifact candidate " + path + ".";
        }
      }
    }
    return new SteelHook02TargetClassBytes(
        targetDescriptor.classEntryName(),
        null,
        null,
        null,
        null,
        false,
        false,
        firstReadFailure == null
            ? "Target-25 could not locate "
                + targetDescriptor.classEntryName()
                + " in the resolved runtime artifacts."
            : firstReadFailure);
  }

  private List<Candidate> candidates(MinecraftServerRuntimePlan runtimePlan) {
    List<Candidate> candidates = new ArrayList<>();
    LinkedHashSet<String> seen = new LinkedHashSet<>();
    addCandidate(candidates, seen, runtimePlan.serverJarPath(), "server-jar");
    for (MinecraftServerRuntimeClasspath.Entry entry : runtimePlan.classpathEntries()) {
      addCandidate(candidates, seen, entry.path(), "classpath-entry");
    }
    for (MinecraftRuntimeFile file : runtimePlan.bundledRuntimeFiles()) {
      addCandidate(
          candidates, seen, file.path() == null ? null : file.path().toString(), "bundled-runtime");
    }
    return candidates;
  }

  private void addCandidate(
      List<Candidate> candidates, LinkedHashSet<String> seen, String pathValue, String kind) {
    if (pathValue == null || pathValue.isBlank()) {
      return;
    }
    Path path = Path.of(pathValue).toAbsolutePath().normalize();
    if (seen.add(path.toString())) {
      candidates.add(new Candidate(path, kind));
    }
  }

  private String sha256Hex(byte[] bytes) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(bytes);
      StringBuilder builder = new StringBuilder(hash.length * 2);
      for (byte value : hash) {
        builder.append(Character.forDigit((value >>> 4) & 0xF, 16));
        builder.append(Character.forDigit(value & 0xF, 16));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(
          "SHA-256 is unavailable for Target-25 class-byte reading.", exception);
    }
  }

  private record Candidate(Path path, String kind) {}
}
