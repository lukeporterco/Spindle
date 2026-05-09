package com.spindle.core.minecraft.flow;

import com.spindle.core.diagnostics.DiagnosticEvent;
import com.spindle.core.diagnostics.DiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.launch.LaunchPhase;
import com.spindle.core.minecraft.MinecraftDryRunResult;
import com.spindle.core.minecraft.MinecraftProviderConfig;
import com.spindle.core.minecraft.MinecraftServerLaunchCommand;
import com.spindle.core.process.JavaExecutableResolver;
import com.spindle.core.process.MinecraftProcessConfig;
import com.spindle.core.process.MinecraftProcessResult;
import com.spindle.core.process.MinecraftProcessResultWriter;
import com.spindle.core.process.MinecraftServerProcessLauncher;
import com.spindle.core.report.DiagnosticMeasurements;
import com.spindle.core.report.DisplayPaths;
import java.nio.file.Path;

public final class MinecraftServerLaunchFlow {
  public MinecraftProcessResult launch(
      LaunchContext context,
      MinecraftProviderConfig config,
      MinecraftDryRunResult dryRunResult,
      DiagnosticSink diagnosticSink)
      throws LoaderException {
    Path serverJarPath = dryRunResult.serverJarPath();
    if (serverJarPath == null) {
      throw new LoaderException("Minecraft server launch requires a resolved server jar");
    }

    Path javaExecutable = new JavaExecutableResolver().resolve();
    MinecraftServerLaunchCommand launchCommand =
        dryRunResult.plannedRuntime() == null
            ? MinecraftServerLaunchCommand.simpleJar(
                javaExecutable,
                serverJarPath,
                config.serverJvmArgs(),
                config.serverArgs(),
                path -> DisplayPaths.displayPath(context, path))
            : dryRunResult.plannedRuntime().command();
    Path resultOutputPath =
        context
            .workingDirectory()
            .resolve("minecraft-server-launch-result.json")
            .toAbsolutePath()
            .normalize();
    diagnosticSink.record(
        new DiagnosticEvent(
            "minecraft.server_launch.preflight",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            "Minecraft server launch preflight complete",
            minecraftServerLaunchDetails(
                context, config, serverJarPath, javaExecutable, null, resultOutputPath)));
    diagnosticSink.record(
        new DiagnosticEvent(
            "minecraft.server_launch.start",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            "Minecraft server launch starting",
            minecraftServerLaunchDetails(
                context, config, serverJarPath, javaExecutable, null, resultOutputPath)));

    MinecraftProcessResult result =
        new MinecraftServerProcessLauncher()
            .launch(
                dryRunResult.artifactResolution().metadata().id(),
                new MinecraftProcessConfig(
                    config.serverDirectory(),
                    serverJarPath,
                    javaExecutable,
                    config.serverJvmArgs(),
                    config.serverArgs(),
                    config.launchTimeoutSeconds(),
                    config.stopAfterReady(),
                    config.readyTimeoutSeconds(),
                    config.acceptEulaForTest()),
                launchCommand,
                path -> DisplayPaths.displayPath(context, path));

    if (result.readyDetected()) {
      diagnosticSink.record(
          new DiagnosticEvent(
              "minecraft.server_launch.ready",
              LaunchPhase.COMPLETE.name(),
              0L,
              "ok",
              "Minecraft server readiness detected",
              minecraftServerLaunchDetails(
                  context, config, serverJarPath, javaExecutable, result, resultOutputPath)));
    }
    if (result.stopRequested()) {
      diagnosticSink.record(
          new DiagnosticEvent(
              "minecraft.server_launch.stop_request",
              LaunchPhase.COMPLETE.name(),
              0L,
              "ok",
              "Minecraft server stop requested",
              minecraftServerLaunchDetails(
                  context, config, serverJarPath, javaExecutable, result, resultOutputPath)));
    }
    if (result.timedOut()) {
      diagnosticSink.record(
          new DiagnosticEvent(
              "minecraft.server_launch.timeout",
              LaunchPhase.COMPLETE.name(),
              0L,
              "ok",
              "Minecraft server launch timed out",
              minecraftServerLaunchDetails(
                  context, config, serverJarPath, javaExecutable, result, resultOutputPath)));
    }

    new MinecraftProcessResultWriter().write(resultOutputPath, result);
    diagnosticSink.record(
        new DiagnosticEvent(
            "minecraft.server_launch_result.write",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            "Minecraft server launch result written",
            minecraftServerLaunchDetails(
                context, config, serverJarPath, javaExecutable, result, resultOutputPath)));
    diagnosticSink.record(
        new DiagnosticEvent(
            "minecraft.server_launch.complete",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            "Minecraft server launch complete",
            minecraftServerLaunchDetails(
                context, config, serverJarPath, javaExecutable, result, resultOutputPath)));
    return result;
  }

  private static java.util.Map<String, String> minecraftServerLaunchDetails(
      LaunchContext context,
      MinecraftProviderConfig config,
      Path serverJarPath,
      Path javaExecutable,
      MinecraftProcessResult result,
      Path resultOutputPath) {
    return DiagnosticMeasurements.details(
        "minecraftVersion",
        config.requestedVersion(),
        "serverDirectory",
        result == null
            ? DisplayPaths.displayPath(context, config.serverDirectory())
            : result.serverDirectory(),
        "serverJar",
        result == null ? DisplayPaths.displayPath(context, serverJarPath) : result.serverJar(),
        "javaExecutable",
        result == null
            ? DisplayPaths.displayPath(context, javaExecutable)
            : result.javaExecutable(),
        "timeoutSeconds",
        Integer.toString(config.launchTimeoutSeconds()),
        "readyTimeoutSeconds",
        Integer.toString(config.readyTimeoutSeconds()),
        "stopAfterReady",
        Boolean.toString(config.stopAfterReady()),
        "started",
        result == null ? null : Boolean.toString(result.started()),
        "readyDetected",
        result == null ? null : Boolean.toString(result.readyDetected()),
        "stopRequested",
        result == null ? null : Boolean.toString(result.stopRequested()),
        "exitCode",
        result == null || result.exitCode() == null ? null : Integer.toString(result.exitCode()),
        "timedOut",
        result == null ? null : Boolean.toString(result.timedOut()),
        "launchResultOutputPath",
        DisplayPaths.displayPath(context, resultOutputPath));
  }
}
