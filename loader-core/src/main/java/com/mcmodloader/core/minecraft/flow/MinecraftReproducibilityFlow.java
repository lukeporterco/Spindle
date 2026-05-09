package com.mcmodloader.core.minecraft.flow;

import com.mcmodloader.core.artifact.MinecraftArtifactCache;
import com.mcmodloader.core.artifact.MinecraftArtifactResolver;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.discovery.ModCandidate;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.minecraft.MinecraftBootstrapClassLoaderGraph;
import com.mcmodloader.core.minecraft.MinecraftBootstrapClassLoaderGraphBuilder;
import com.mcmodloader.core.minecraft.MinecraftBootstrapClassLoaderGraphWriter;
import com.mcmodloader.core.minecraft.MinecraftModExecutionPlan;
import com.mcmodloader.core.minecraft.MinecraftModExecutionPlanWriter;
import com.mcmodloader.core.minecraft.MinecraftModExecutionPlanner;
import com.mcmodloader.core.minecraft.MinecraftModIntegrationPlan;
import com.mcmodloader.core.minecraft.MinecraftModIntegrationPlanWriter;
import com.mcmodloader.core.minecraft.MinecraftModIntegrationPlanner;
import com.mcmodloader.core.minecraft.MinecraftPlanFingerprint;
import com.mcmodloader.core.minecraft.MinecraftPreflightResultWriter;
import com.mcmodloader.core.minecraft.MinecraftProviderConfig;
import com.mcmodloader.core.minecraft.MinecraftReproducibilityCheck;
import com.mcmodloader.core.minecraft.MinecraftReproducibilityChecker;
import com.mcmodloader.core.minecraft.MinecraftRuntimeBoundary;
import com.mcmodloader.core.minecraft.MinecraftRuntimeBoundaryBuilder;
import com.mcmodloader.core.minecraft.MinecraftRuntimeBoundaryWriter;
import com.mcmodloader.core.minecraft.MinecraftServerRuntimeClasspath;
import com.mcmodloader.core.minecraft.MinecraftServerRuntimePlan;
import com.mcmodloader.core.minecraft.MinecraftServerRuntimePlanWriter;
import com.mcmodloader.core.minecraft.MinecraftServerRuntimePlanner;
import com.mcmodloader.core.report.DisplayPaths;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftReproducibilityFlow {
  private final MinecraftPreflightFlow minecraftPreflightFlow = new MinecraftPreflightFlow();

  public MinecraftReproducibilityCheck createCheck(
      LaunchContext context,
      MinecraftProviderConfig config,
      MinecraftArtifactCache artifactCache,
      MinecraftArtifactResolver.Resolution artifactResolution,
      List<ModCandidate> parsedMods,
      ResolvedModSet resolvedMods,
      String resolvedMinecraftVersion,
      MinecraftServerRuntimePlan firstRuntimePlan,
      MinecraftRuntimeBoundary firstRuntimeBoundary,
      MinecraftModIntegrationPlan firstIntegrationPlan,
      MinecraftModExecutionPlan firstExecutionPlan,
      List<String> reportsWritten)
      throws LoaderException {
    Path snapshotDirectory =
        context.workingDirectory().resolve(".minecraft-reproducibility").resolve("second-run");
    try {
      Files.createDirectories(snapshotDirectory);
    } catch (IOException exception) {
      throw new LoaderException("Failed to create reproducibility snapshot directory", exception);
    }

    MinecraftServerRuntimePlanner.PlannedRuntime secondRuntime =
        new MinecraftServerRuntimePlanner()
            .plan(
                context.workingDirectory(),
                config,
                artifactCache,
                artifactResolution,
                path -> DisplayPaths.displayPath(context, path));
    Path secondRuntimePlanPath = snapshotDirectory.resolve("minecraft-server-runtime-plan.json");
    new MinecraftServerRuntimePlanWriter().write(secondRuntimePlanPath, secondRuntime.plan());

    MinecraftRuntimeBoundary secondBoundary = null;
    Path secondBoundaryPath = snapshotDirectory.resolve("minecraft-runtime-boundary.json");
    if (firstRuntimeBoundary != null) {
      secondBoundary =
          new MinecraftRuntimeBoundaryBuilder()
              .build(
                  secondRuntime.plan(),
                  runtimeJarsForPlan(
                      context, artifactResolution.serverJarPath(), secondRuntime.plan()),
                  path -> DisplayPaths.displayPath(context, path),
                  config.strictBoundary(),
                  config.strictRuntimeConflicts());
      new MinecraftRuntimeBoundaryWriter().write(secondBoundaryPath, secondBoundary);
    }

    MinecraftModIntegrationPlan secondIntegrationPlan = null;
    Path secondIntegrationPlanPath =
        snapshotDirectory.resolve("minecraft-mod-integration-plan.json");
    if (firstIntegrationPlan != null && secondBoundary != null) {
      secondIntegrationPlan =
          new MinecraftModIntegrationPlanner()
              .plan(
                  context,
                  parsedMods,
                  resolvedMods,
                  secondBoundary,
                  resolvedMinecraftVersion,
                  config.strictSide(),
                  config.strictClassVersions(),
                  config.strictRuntimeConflicts(),
                  path -> DisplayPaths.displayPath(context, path));
      new MinecraftModIntegrationPlanWriter()
          .write(secondIntegrationPlanPath, secondIntegrationPlan);
    }

    MinecraftModExecutionPlan secondExecutionPlan = null;
    Path secondExecutionPlanPath = snapshotDirectory.resolve("minecraft-mod-execution-plan.json");
    Path secondGraphPath = snapshotDirectory.resolve("minecraft-bootstrap-classloader-graph.json");
    if (firstExecutionPlan != null && secondBoundary != null && secondIntegrationPlan != null) {
      MinecraftPlanFingerprint secondRuntimeFingerprint =
          MinecraftPlanFingerprint.fromFile("runtime-plan", secondRuntimePlanPath);
      MinecraftPlanFingerprint secondBoundaryFingerprint =
          MinecraftPlanFingerprint.fromFile("runtime-boundary", secondBoundaryPath);
      MinecraftPlanFingerprint secondIntegrationFingerprint =
          MinecraftPlanFingerprint.fromFile("integration-plan", secondIntegrationPlanPath);
      secondExecutionPlan =
          new MinecraftModExecutionPlanner()
              .plan(
                  context,
                  config,
                  parsedMods,
                  secondRuntime.plan(),
                  secondBoundary,
                  secondIntegrationPlan,
                  secondRuntimeFingerprint,
                  secondBoundaryFingerprint,
                  secondIntegrationFingerprint);
      new MinecraftModExecutionPlanWriter().write(secondExecutionPlanPath, secondExecutionPlan);
      MinecraftBootstrapClassLoaderGraph secondGraph =
          new MinecraftBootstrapClassLoaderGraphBuilder().build(secondExecutionPlan);
      new MinecraftBootstrapClassLoaderGraphWriter().write(secondGraphPath, secondGraph);
    }

    Path secondPreflightPath = snapshotDirectory.resolve("minecraft-preflight-result.json");
    if (config.preflight() && secondBoundary != null) {
      new MinecraftPreflightResultWriter()
          .write(
              secondPreflightPath,
              minecraftPreflightFlow.buildResult(
                  resolvedMinecraftVersion, reportsWritten, secondBoundary, secondIntegrationPlan));
    }

    List<MinecraftReproducibilityChecker.ReportPair> pairs = new ArrayList<>();
    pairs.add(
        new MinecraftReproducibilityChecker.ReportPair(
            "minecraft-server-runtime-plan.json",
            context.workingDirectory().resolve("minecraft-server-runtime-plan.json"),
            secondRuntimePlanPath));
    if (firstRuntimeBoundary != null) {
      pairs.add(
          new MinecraftReproducibilityChecker.ReportPair(
              "minecraft-runtime-boundary.json",
              context.workingDirectory().resolve("minecraft-runtime-boundary.json"),
              secondBoundaryPath));
    }
    if (firstIntegrationPlan != null) {
      pairs.add(
          new MinecraftReproducibilityChecker.ReportPair(
              "minecraft-mod-integration-plan.json",
              context.workingDirectory().resolve("minecraft-mod-integration-plan.json"),
              secondIntegrationPlanPath));
    }
    if (firstExecutionPlan != null) {
      pairs.add(
          new MinecraftReproducibilityChecker.ReportPair(
              "minecraft-mod-execution-plan.json",
              context.workingDirectory().resolve("minecraft-mod-execution-plan.json"),
              secondExecutionPlanPath));
      if (Files.isRegularFile(
          context.workingDirectory().resolve("minecraft-bootstrap-classloader-graph.json"))) {
        pairs.add(
            new MinecraftReproducibilityChecker.ReportPair(
                "minecraft-bootstrap-classloader-graph.json",
                context.workingDirectory().resolve("minecraft-bootstrap-classloader-graph.json"),
                secondGraphPath));
      }
    }
    if (config.preflight()
        && Files.isRegularFile(
            context.workingDirectory().resolve("minecraft-preflight-result.json"))) {
      pairs.add(
          new MinecraftReproducibilityChecker.ReportPair(
              "minecraft-preflight-result.json",
              context.workingDirectory().resolve("minecraft-preflight-result.json"),
              secondPreflightPath));
    }

    return new MinecraftReproducibilityChecker()
        .check(
            "Mega-Milestone 7",
            pairs,
            config.offline() && artifactResolution.networkRequestCount() > 0);
  }

  private static List<Path> runtimeJarsForPlan(
      LaunchContext context, Path serverJarPath, MinecraftServerRuntimePlan runtimePlan) {
    List<Path> runtimeJars = new ArrayList<>();
    runtimeJars.add(serverJarPath);
    for (MinecraftServerRuntimeClasspath.Entry entry : runtimePlan.classpathEntries()) {
      Path path = Path.of(entry.path());
      runtimeJars.add(path.isAbsolute() ? path : context.workingDirectory().resolve(path));
    }
    return runtimeJars;
  }
}
