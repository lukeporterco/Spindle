package com.spindle.core;

import com.spindle.core.app.LoaderApplication;
import com.spindle.core.cli.LaunchArgumentResolver;
import com.spindle.core.cli.LaunchArguments;
import com.spindle.core.cli.LoaderCliParser;
import com.spindle.core.diagnostics.DiagnosticSink;
import com.spindle.core.diagnostics.JsonDiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchPhase;
import com.spindle.core.report.DiagnosticMeasurements;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class LoaderMain {
  public static final String LOADER_VERSION = LoaderRuntimeInfo.LOADER_VERSION;
  public static final String TARGET_MINECRAFT_VERSION = LoaderRuntimeInfo.TARGET_MINECRAFT_VERSION;

  private LoaderMain() {}

  public static void main(String[] args) {
    Path workingDirectory = Paths.get("").toAbsolutePath().normalize();
    int exitCode = 0;
    try {
      Files.createDirectories(workingDirectory);
    } catch (IOException exception) {
      System.err.println("[spindle] error: failed to create working directory");
      System.exit(1);
      return;
    }

    JsonDiagnosticSink diagnosticSink =
        new JsonDiagnosticSink(workingDirectory.resolve("diagnostics/startup-trace.json"));
    try {
      execute(workingDirectory, args, diagnosticSink);
    } catch (LoaderException exception) {
      System.err.println("[spindle] error: " + exception.getMessage());
      exitCode = 1;
    } catch (Exception exception) {
      System.err.println("[spindle] error: unexpected failure");
      exception.printStackTrace(System.err);
      exitCode = 1;
    } finally {
      try {
        diagnosticSink.write();
      } catch (IOException exception) {
        System.err.println("[spindle] error: failed to write diagnostics");
        exitCode = 1;
      }
    }

    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }

  static void execute(Path workingDirectory, String[] args, DiagnosticSink diagnosticSink)
      throws LoaderException {
    Path resolvedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
    LaunchArguments launchArguments =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "argument.parse",
            LaunchPhase.ARGUMENT_PARSE,
            () ->
                new LaunchArgumentResolver()
                    .resolve(resolvedWorkingDirectory, new LoaderCliParser().parse(args)),
            parsedArguments ->
                DiagnosticMeasurements.details(
                    "gameMainClass",
                    parsedArguments.gameMainClass(),
                    "gameProviderId",
                    parsedArguments.gameProviderId(),
                    "launchArgumentCount",
                    Integer.toString(parsedArguments.launchArguments().size()),
                    "validateOnly",
                    Boolean.toString(parsedArguments.validateOnly()),
                    "explain",
                    Boolean.toString(parsedArguments.explain()),
                    "minecraftVersion",
                    parsedArguments.minecraftProviderConfig() == null
                        ? null
                        : parsedArguments.minecraftProviderConfig().requestedVersion(),
                    "minecraftSide",
                    parsedArguments.minecraftProviderConfig() == null
                        ? null
                        : parsedArguments.minecraftProviderConfig().side().id(),
                    "macheReferenceScan",
                    Boolean.toString(parsedArguments.macheReferenceScan())));

    new LoaderApplication().run(resolvedWorkingDirectory, launchArguments, diagnosticSink);
  }

  static LaunchArguments parseArguments(String[] args) throws LoaderException {
    return new LoaderCliParser().parse(args);
  }
}
