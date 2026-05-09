package com.spindle.core.minecraft.flow;

import com.spindle.core.artifact.MinecraftArtifactResolver;
import com.spindle.core.baseline.MinecraftServerBaseline;
import com.spindle.core.baseline.MinecraftServerBaselineMode;
import com.spindle.core.baseline.MinecraftServerBaselineResult;
import com.spindle.core.baseline.MinecraftServerBaselineWriter;
import com.spindle.core.diagnostics.DiagnosticEvent;
import com.spindle.core.diagnostics.DiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.launch.LaunchPhase;
import com.spindle.core.minecraft.MinecraftDryRunResult;
import com.spindle.core.minecraft.MinecraftProviderConfig;
import com.spindle.core.minecraft.MinecraftVersionSelection;
import com.spindle.core.process.MinecraftProcessResult;
import com.spindle.core.report.DiagnosticMeasurements;
import com.spindle.core.report.DisplayPaths;
import java.nio.file.Path;

public final class MinecraftBaselineFlow {
  private final MinecraftServerLaunchFlow minecraftServerLaunchFlow =
      new MinecraftServerLaunchFlow();

  public MinecraftServerBaselineResult complete(
      LaunchContext context,
      MinecraftProviderConfig config,
      MinecraftDryRunResult dryRunResult,
      DiagnosticSink diagnosticSink)
      throws LoaderException {
    MinecraftArtifactResolver.Resolution artifactResolution = dryRunResult.artifactResolution();
    MinecraftVersionSelection versionSelection = artifactResolution.versionSelection();
    if (versionSelection == null) {
      throw new LoaderException("Minecraft baseline flow requires a resolved version selection.");
    }

    diagnosticSink.record(
        new DiagnosticEvent(
            "minecraft.baseline.start",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            "Minecraft baseline flow started",
            baselineDetails(context, config, artifactResolution, null)));
    diagnosticSink.record(
        new DiagnosticEvent(
            "minecraft.version_select",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            "Minecraft version selection resolved",
            baselineDetails(context, config, artifactResolution, null)));
    diagnosticSink.record(
        new DiagnosticEvent(
            config.offlineReplay()
                ? "minecraft.baseline.offline_replay"
                : "minecraft.baseline.artifacts.resolve",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            config.offlineReplay()
                ? "Minecraft baseline offline replay using cached artifacts"
                : "Minecraft baseline artifacts resolved",
            baselineDetails(context, config, artifactResolution, null)));

    MinecraftProviderConfig launchConfig = config;
    if (config.serverDirectory() == null || serverDirectoryUsesRequestedSelector(context, config)) {
      launchConfig =
          config.withServerDirectory(
              context
                  .workingDirectory()
                  .resolve("minecraft-server-baseline")
                  .resolve(artifactResolution.metadata().id())
                  .toAbsolutePath()
                  .normalize());
    }

    MinecraftProcessResult processResult = null;
    if (launchConfig.launch()) {
      diagnosticSink.record(
          new DiagnosticEvent(
              "minecraft.baseline.launch.start",
              LaunchPhase.COMPLETE.name(),
              0L,
              "ok",
              "Minecraft baseline launch starting",
              baselineDetails(context, launchConfig, artifactResolution, null)));
      processResult =
          minecraftServerLaunchFlow.launch(context, launchConfig, dryRunResult, diagnosticSink);
      diagnosticSink.record(
          new DiagnosticEvent(
              "minecraft.baseline.launch.complete",
              LaunchPhase.COMPLETE.name(),
              0L,
              "ok",
              "Minecraft baseline launch complete",
              baselineDetails(context, launchConfig, artifactResolution, processResult)));
    }

    String manifestSha256 =
        artifactResolution.manifestRecord() == null
            ? null
            : artifactResolution.manifestRecord().sha256();
    String manifestPath =
        artifactResolution.manifestRecord() == null
                || !artifactResolution.manifestRecord().present()
            ? null
            : DisplayPaths.displayPath(context, artifactResolution.manifestRecord().path());
    String versionJsonSha256 =
        artifactResolution.versionRecord() == null
            ? null
            : artifactResolution.versionRecord().sha256();
    String versionJsonPath =
        artifactResolution.versionRecord() == null || !artifactResolution.versionRecord().present()
            ? null
            : DisplayPaths.displayPath(context, artifactResolution.versionRecord().path());
    String launchResultPath =
        processResult == null
            ? null
            : DisplayPaths.displayPath(
                context, context.workingDirectory().resolve("minecraft-server-launch-result.json"));
    boolean offlineReplaySucceeded =
        launchConfig.offlineReplay()
            && (processResult == null
                || !launchConfig.requireReady()
                || processResult.readyDetected());

    MinecraftServerBaseline baseline =
        new MinecraftServerBaseline(
            1,
            com.spindle.core.LoaderMain.TARGET_MINECRAFT_VERSION,
            artifactResolution.metadata().id(),
            versionSelection,
            new MinecraftServerBaseline.Metadata(
                manifestPath, versionJsonPath, manifestSha256, versionJsonSha256),
            new MinecraftServerBaseline.ServerArtifact(
                artifactResolution.serverRecord() == null
                    ? null
                    : DisplayPaths.displayPath(context, artifactResolution.serverRecord().path()),
                artifactResolution.serverRecord() == null
                    ? null
                    : artifactResolution.serverRecord().sourceUrl(),
                artifactResolution.serverRecord() == null
                    ? null
                    : artifactResolution.serverRecord().sha1(),
                artifactResolution.serverRecord() == null
                    ? null
                    : artifactResolution.serverRecord().sha256(),
                artifactResolution.serverRecord() == null
                    ? null
                    : artifactResolution.serverRecord().size(),
                artifactResolution.serverRecord() != null
                    && artifactResolution.serverRecord().verified()),
            new MinecraftServerBaseline.Launch(
                launchConfig.launch(),
                launchResultPath,
                processResult != null && processResult.started(),
                processResult == null ? null : processResult.readyDetected(),
                processResult == null ? null : processResult.exitCode(),
                processResult != null && processResult.timedOut()),
            new MinecraftServerBaseline.OfflineReplay(
                launchConfig.offlineReplay(),
                launchResultPath,
                offlineReplaySucceeded,
                artifactResolution.networkRequestCount()),
            new MinecraftServerBaseline.ModIntegration(false, false, false));

    new MinecraftServerBaselineWriter().write(launchConfig.baselineReportPath(), baseline);
    diagnosticSink.record(
        new DiagnosticEvent(
            "minecraft.baseline.write",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            "Minecraft baseline report written",
            baselineDetails(context, launchConfig, artifactResolution, processResult)));

    if (launchConfig.requireReady() && (processResult == null || !processResult.readyDetected())) {
      throw new LoaderException(
          "Minecraft baseline launch did not detect a ready line while --minecraft-require-ready was set.");
    }

    return new MinecraftServerBaselineResult(
        launchConfig.offlineReplay()
            ? MinecraftServerBaselineMode.OFFLINE_REPLAY
            : MinecraftServerBaselineMode.ACQUIRE,
        baseline,
        artifactResolution,
        dryRunResult,
        processResult,
        launchConfig.baselineReportPath());
  }

  private static java.util.Map<String, String> baselineDetails(
      LaunchContext context,
      MinecraftProviderConfig config,
      MinecraftArtifactResolver.Resolution artifactResolution,
      MinecraftProcessResult processResult) {
    return DiagnosticMeasurements.details(
        "projectTargetMinecraft",
        com.spindle.core.LoaderMain.TARGET_MINECRAFT_VERSION,
        "requestedBaselineVersion",
        config.requestedVersionOrBaseline(),
        "resolvedBaselineVersion",
        artifactResolution == null || artifactResolution.versionSelection() == null
            ? null
            : artifactResolution.versionSelection().resolved(),
        "versionSelectionSource",
        artifactResolution == null || artifactResolution.versionSelection() == null
            ? null
            : artifactResolution.versionSelection().source(),
        "cacheDirectory",
        config.cacheDirectory() == null
            ? null
            : DisplayPaths.displayPath(context, config.cacheDirectory()),
        "serverJar",
        artifactResolution == null || artifactResolution.serverJarPath() == null
            ? null
            : DisplayPaths.displayPath(context, artifactResolution.serverJarPath()),
        "serverJarSource",
        artifactResolution == null ? null : artifactResolution.serverJarSource(),
        "networkRequests",
        artifactResolution == null
            ? null
            : Integer.toString(artifactResolution.networkRequestCount()),
        "offline",
        Boolean.toString(config.offline()),
        "launchAttempted",
        Boolean.toString(config.launch()),
        "readyDetected",
        processResult == null ? null : Boolean.toString(processResult.readyDetected()),
        "requireReady",
        Boolean.toString(config.requireReady()),
        "baselineReportPath",
        DisplayPaths.displayPath(context, config.baselineReportPath()));
  }

  private static boolean serverDirectoryUsesRequestedSelector(
      LaunchContext context, MinecraftProviderConfig config) {
    if (config.serverDirectory() == null) {
      return false;
    }
    String requested = config.requestedVersionOrBaseline();
    if (requested == null || requested.isBlank()) {
      return false;
    }
    Path expected =
        context
            .workingDirectory()
            .resolve("minecraft-server-baseline")
            .resolve(requested)
            .toAbsolutePath()
            .normalize();
    return expected.equals(config.serverDirectory());
  }
}
