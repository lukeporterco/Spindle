package com.spindle.core.execution;

import com.spindle.core.classpath.ModClassLoader;
import com.spindle.core.diagnostics.DiagnosticEvent;
import com.spindle.core.diagnostics.DiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.entrypoint.EntrypointInvoker;
import com.spindle.core.game.GameProvider;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.launch.LaunchPhase;
import com.spindle.core.pipeline.ModpackPlanningResult;
import com.spindle.core.report.DiagnosticMeasurements;
import com.spindle.core.report.DisplayPaths;
import com.spindle.core.report.StartupProfileSupport;
import java.io.IOException;
import java.util.List;

public final class StandardGameLaunchExecutor {
  private final EntrypointInvoker entrypointInvoker = new EntrypointInvoker();

  public void execute(
      LaunchContext context,
      GameProvider gameProvider,
      ModpackPlanningResult planningResult,
      DiagnosticSink diagnosticSink)
      throws LoaderException {
    if (context.validateOnly()) {
      diagnosticSink.record(
          new DiagnosticEvent(
              "validation.complete",
              LaunchPhase.COMPLETE.name(),
              0L,
              "ok",
              "Validation complete",
              DiagnosticMeasurements.details(
                  "resolvedModCount",
                  Integer.toString(planningResult.frozenModGraph().mods().size()),
                  "modpackStateOutputPath",
                  DisplayPaths.displayPath(context, planningResult.modpackStatePath()),
                  "dependencyGraphOutputPath",
                  DisplayPaths.displayPath(context, planningResult.dependencyGraphPath()))));
      StartupProfileSupport.writeStartupProfile(context, diagnosticSink);
      System.out.println("[spindle] validation complete");
      return;
    }

    try (ModClassLoader modClassLoader =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "classpath.create",
            LaunchPhase.CLASSLOADER_CREATE,
            () ->
                ModClassLoader.create(
                    planningResult.classpathPlan(),
                    StandardGameLaunchExecutor.class.getClassLoader()),
            ignored ->
                DiagnosticMeasurements.details(
                    "modJarCount",
                    Integer.toString(planningResult.classpathPlan().modJars().size())))) {
      List<EntrypointInvoker.EntrypointInvocation> invocations =
          DiagnosticMeasurements.measure(
              diagnosticSink,
              "entrypoint.invoke",
              LaunchPhase.ENTRYPOINT_INVOKE,
              () ->
                  entrypointInvoker.invoke(
                      planningResult.frozenModGraph(),
                      modClassLoader,
                      planningResult.classOwnershipIndex()),
              results ->
                  DiagnosticMeasurements.details(
                      "entrypointCount",
                      Integer.toString(results.size()),
                      "ownerModIds",
                      joinOwners(results),
                      "entrypointClasses",
                      joinEntrypoints(results)));
      if (invocations.isEmpty()) {
        diagnosticSink.record(
            new DiagnosticEvent(
                "entrypoint.invoke",
                LaunchPhase.ENTRYPOINT_INVOKE.name(),
                0L,
                "ok",
                "No entrypoints to invoke",
                DiagnosticMeasurements.details("entrypointCount", "0")));
      }

      DiagnosticMeasurements.measure(
          diagnosticSink,
          "game.launch",
          LaunchPhase.GAME_LAUNCH,
          () -> {
            gameProvider.launch(context, modClassLoader);
            return null;
          },
          ignored ->
              DiagnosticMeasurements.details(
                  "gameProviderId",
                  gameProvider.id(),
                  "gameProviderVersion",
                  gameProvider.version(),
                  "gameMainClass",
                  context.gameMainClass()));
    } catch (IOException exception) {
      throw new LoaderException("Failed to close mod class loader", exception);
    }

    System.out.println("[spindle] startup complete");
    diagnosticSink.record(
        new DiagnosticEvent(
            "startup.complete",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            "Startup complete",
            DiagnosticMeasurements.details(
                "resolvedModCount",
                Integer.toString(planningResult.frozenModGraph().mods().size()))));
    StartupProfileSupport.writeStartupProfile(context, diagnosticSink);
  }

  private static String joinEntrypoints(List<EntrypointInvoker.EntrypointInvocation> invocations) {
    return invocations.stream()
        .map(EntrypointInvoker.EntrypointInvocation::entrypointClassName)
        .sorted()
        .reduce((left, right) -> left + "," + right)
        .orElse("");
  }

  private static String joinOwners(List<EntrypointInvoker.EntrypointInvocation> invocations) {
    return invocations.stream()
        .map(EntrypointInvoker.EntrypointInvocation::ownerModId)
        .distinct()
        .sorted()
        .reduce((left, right) -> left + "," + right)
        .orElse("");
  }
}
