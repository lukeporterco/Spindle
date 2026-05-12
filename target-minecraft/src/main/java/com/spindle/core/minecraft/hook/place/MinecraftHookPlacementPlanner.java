package com.spindle.core.minecraft.hook.place;

import com.spindle.core.minecraft.MinecraftModExecutionPlan;
import com.spindle.core.minecraft.MinecraftRuntimeFile;
import com.spindle.core.minecraft.MinecraftServerRuntimeClasspath;
import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import com.spindle.core.minecraft.hook.MinecraftHookContractReport;
import com.spindle.core.minecraft.hook.MinecraftHookContractResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

public final class MinecraftHookPlacementPlanner {
  public static final String MILESTONE_NAME = "Target-5";
  public static final String SUPPORTED_PLACEMENT_ID =
      "target-5.minecraft.server.main.method-entry-placement";
  public static final String SUPPORTED_SOURCE_CONTRACT_ID =
      "minecraft.26_1_2.server.main.entrypoint";
  public static final String SUPPORTED_CATALOG_ID = "minecraft-26.1.2-server-known-symbols";
  public static final String SUPPORTED_OWNER_INTERNAL_NAME = "net/minecraft/server/Main";
  public static final String SUPPORTED_MEMBER_NAME = "main";
  public static final String SUPPORTED_DESCRIPTOR = "([Ljava/lang/String;)V";
  public static final String SUPPORTED_MAIN_CLASS = "net.minecraft.server.Main";
  private static final String TARGET = "minecraft";
  private static final String CLASS_ENTRY_NAME = SUPPORTED_OWNER_INTERNAL_NAME + ".class";

  private final MinecraftMethodCodeReader methodCodeReader = new MinecraftMethodCodeReader();

  public MinecraftHookPlacementPlan plan(
      MinecraftHookContractReport contractReport,
      MinecraftModExecutionPlan executionPlan,
      MinecraftServerRuntimePlan runtimePlan) {
    if (contractReport == null) {
      return failedPlan(
          null, executionPlan, runtimePlan, "Target-3 hook contract report is missing.");
    }
    if (runtimePlan == null) {
      return failedPlan(contractReport, executionPlan, null, "Minecraft runtime plan is missing.");
    }
    if (!SUPPORTED_CATALOG_ID.equals(contractReport.catalogId())) {
      return failedPlan(
          contractReport,
          executionPlan,
          runtimePlan,
          "Unsupported hook contract catalog: " + contractReport.catalogId());
    }
    if (!contractReport.validationPassed()) {
      return failedPlan(
          contractReport, executionPlan, runtimePlan, "Target-3 hook contract validation failed.");
    }
    if (contractReport.errorCount() != 0) {
      return failedPlan(
          contractReport,
          executionPlan,
          runtimePlan,
          "Target-3 hook contract report contains errors: " + contractReport.errorCount());
    }

    MinecraftHookContractResult entrypointContract =
        contractReport.contracts().stream()
            .filter(contract -> SUPPORTED_SOURCE_CONTRACT_ID.equals(contract.id()))
            .findFirst()
            .orElse(null);
    if (entrypointContract == null
        || !entrypointContract.valid()
        || !SUPPORTED_OWNER_INTERNAL_NAME.equals(entrypointContract.ownerInternalName())
        || !SUPPORTED_MEMBER_NAME.equals(entrypointContract.memberName())
        || !SUPPORTED_DESCRIPTOR.equals(entrypointContract.descriptor())) {
      return failedPlan(
          contractReport,
          executionPlan,
          runtimePlan,
          "Required Target-3 contract minecraft.26_1_2.server.main.entrypoint is missing or invalid.");
    }
    if (executionPlan == null || !SUPPORTED_MAIN_CLASS.equals(executionPlan.minecraftMainClass())) {
      return failedPlan(
          contractReport,
          executionPlan,
          runtimePlan,
          "Minecraft execution plan main class must be net.minecraft.server.Main.");
    }

    ArtifactClassBytes classBytes = locateClassBytes(runtimePlan);
    if (classBytes.failureReason() != null) {
      return failedPlan(contractReport, executionPlan, runtimePlan, classBytes.failureReason());
    }
    if (classBytes.classBytes() == null) {
      return failedPlan(
          contractReport,
          executionPlan,
          runtimePlan,
          "Minecraft runtime artifact does not contain net/minecraft/server/Main.class.");
    }

    MinecraftMethodCodeSummary methodCodeSummary;
    try {
      methodCodeSummary =
          methodCodeReader.read(
              classBytes.classBytes(),
              SUPPORTED_OWNER_INTERNAL_NAME,
              SUPPORTED_MEMBER_NAME,
              SUPPORTED_DESCRIPTOR);
    } catch (Exception exception) {
      return failedPlan(
          contractReport,
          executionPlan,
          runtimePlan,
          "Failed to read method Code attribute for net/minecraft/server/Main.main([Ljava/lang/String;)V.");
    }
    if (!methodCodeSummary.hasCodeAttribute()) {
      return failedPlan(
          contractReport,
          executionPlan,
          runtimePlan,
          methodCodeSummary.abstractOrNative()
              ? "Minecraft server entrypoint method is abstract or native and has no Code attribute."
              : "Minecraft server entrypoint method has no Code attribute.");
    }

    MinecraftPlannedHookPlacement placement =
        new MinecraftPlannedHookPlacement(
            SUPPORTED_PLACEMENT_ID,
            SUPPORTED_SOURCE_CONTRACT_ID,
            SUPPORTED_CATALOG_ID,
            MinecraftHookPlacementKind.METHOD_ENTRY,
            SUPPORTED_OWNER_INTERNAL_NAME,
            SUPPORTED_MEMBER_NAME,
            SUPPORTED_DESCRIPTOR,
            0,
            MinecraftHookPlacementMode.METHOD_ENTRY_ANALYSIS_ONLY,
            true,
            methodCodeSummary);
    return new MinecraftHookPlacementPlan(
        1,
        MILESTONE_NAME,
        TARGET,
        executionPlan.resolvedMinecraftVersion(),
        executionPlan.side(),
        contractReport.catalogId(),
        contractReport.validationPassed(),
        contractReport.errorCount(),
        executionPlan.minecraftMainClass(),
        true,
        null,
        true,
        1,
        List.of(placement),
        true,
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false);
  }

  private ArtifactClassBytes locateClassBytes(MinecraftServerRuntimePlan runtimePlan) {
    for (Path artifactPath : artifactCandidates(runtimePlan)) {
      if (!Files.isRegularFile(artifactPath)) {
        continue;
      }
      try (JarFile jarFile = new JarFile(artifactPath.toFile())) {
        var entry = jarFile.getJarEntry(CLASS_ENTRY_NAME);
        if (entry == null) {
          continue;
        }
        try (var inputStream = jarFile.getInputStream(entry)) {
          return new ArtifactClassBytes(inputStream.readAllBytes(), null);
        }
      } catch (IOException exception) {
        return new ArtifactClassBytes(
            null,
            "Failed to inspect runtime artifact " + artifactPath.toString().replace('\\', '/'));
      }
    }
    return new ArtifactClassBytes(null, null);
  }

  private List<Path> artifactCandidates(MinecraftServerRuntimePlan runtimePlan) {
    Set<Path> candidates = new LinkedHashSet<>();
    addCandidate(candidates, runtimePlan.serverJarPath());
    for (MinecraftServerRuntimeClasspath.Entry entry : runtimePlan.classpathEntries()) {
      addCandidate(candidates, entry.path());
    }
    for (MinecraftRuntimeFile file : runtimePlan.bundledRuntimeFiles()) {
      if (file.path() != null) {
        candidates.add(file.path().toAbsolutePath().normalize());
      }
    }
    return List.copyOf(candidates);
  }

  private void addCandidate(Set<Path> candidates, String pathValue) {
    if (pathValue == null || pathValue.isBlank()) {
      return;
    }
    candidates.add(Path.of(pathValue).toAbsolutePath().normalize());
  }

  private MinecraftHookPlacementPlan failedPlan(
      MinecraftHookContractReport contractReport,
      MinecraftModExecutionPlan executionPlan,
      MinecraftServerRuntimePlan runtimePlan,
      String reason) {
    return new MinecraftHookPlacementPlan(
        1,
        MILESTONE_NAME,
        TARGET,
        executionPlan != null
            ? executionPlan.resolvedMinecraftVersion()
            : runtimePlan == null ? null : runtimePlan.resolvedMinecraftVersion(),
        executionPlan != null
            ? executionPlan.side()
            : contractReport == null ? null : contractReport.side(),
        contractReport == null ? null : contractReport.catalogId(),
        contractReport != null && contractReport.validationPassed(),
        contractReport == null ? 0 : contractReport.errorCount(),
        executionPlan == null ? null : executionPlan.minecraftMainClass(),
        false,
        reason,
        false,
        0,
        List.of(),
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false);
  }

  private record ArtifactClassBytes(byte[] classBytes, String failureReason) {}
}
