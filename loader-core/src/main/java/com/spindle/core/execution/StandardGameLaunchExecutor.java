package com.spindle.core.execution;

import com.spindle.core.classpath.ModClassLoader;
import com.spindle.core.diagnostics.DiagnosticEvent;
import com.spindle.core.diagnostics.DiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.game.GameProvider;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.launch.LaunchPhase;
import com.spindle.core.lifecycle.LifecycleExecutionReport;
import com.spindle.core.lifecycle.LifecycleExecutionReportWriter;
import com.spindle.core.lifecycle.LifecycleExecutor;
import com.spindle.core.pipeline.ModpackPlanningResult;
import com.spindle.core.report.DiagnosticMeasurements;
import com.spindle.core.report.DisplayPaths;
import com.spindle.core.report.StartupProfileSupport;
import com.spindle.core.runtime.CompiledModpackProfile;
import com.spindle.core.runtime.ModContextFactory;
import com.spindle.core.runtime.service.RuntimeServiceContractGate;
import com.spindle.core.runtime.service.RuntimeServiceRegistryFactory;
import com.spindle.core.security.SecurityGate;
import com.spindle.core.security.SecurityValidationResult;
import java.io.IOException;

public final class StandardGameLaunchExecutor {
  private final LifecycleExecutor lifecycleExecutor = new LifecycleExecutor();
  private final LifecycleExecutionReportWriter lifecycleExecutionReportWriter =
      new LifecycleExecutionReportWriter();
  private final ModContextFactory modContextFactory = new ModContextFactory();
  private final SecurityGate securityGate = new SecurityGate();
  private final RuntimeServiceContractGate runtimeServiceContractGate =
      new RuntimeServiceContractGate();
  private final RuntimeServiceRegistryFactory runtimeServiceRegistryFactory =
      new RuntimeServiceRegistryFactory();

  public void execute(
      LaunchContext context,
      GameProvider gameProvider,
      ModpackPlanningResult planningResult,
      CompiledModpackProfile compiledProfile,
      SecurityValidationResult securityValidationResult,
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
    securityGate.ensureLifecycleExecutionAllowed(securityValidationResult);
    runtimeServiceContractGate.ensureLifecycleExecutionAllowed(compiledProfile);

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
      LifecycleExecutionReport lifecycleReport =
          DiagnosticMeasurements.measure(
              diagnosticSink,
              "lifecycle.execute",
              LaunchPhase.ENTRYPOINT_INVOKE,
              () ->
                  lifecycleExecutor.execute(
                      compiledProfile,
                      modClassLoader,
                      modContextFactory.createContexts(
                          context,
                          compiledProfile,
                          runtimeServiceRegistryFactory.create(compiledProfile, modClassLoader))),
              report ->
                  DiagnosticMeasurements.details(
                      "handlerCount",
                      Integer.toString(report.plannedHandlers().size()),
                      "phaseOrder",
                      String.join(",", report.phaseOrder())));
      lifecycleExecutionReportWriter.write(
          context.workingDirectory().resolve("spindle.lifecycle-report.json"), lifecycleReport);
      diagnosticSink.record(
          new DiagnosticEvent(
              "entrypoint.invoke",
              LaunchPhase.ENTRYPOINT_INVOKE.name(),
              0L,
              "ok",
              "Lifecycle execution compatibility alias",
              DiagnosticMeasurements.details(
                  "handlerCount",
                  Integer.toString(lifecycleReport.plannedHandlers().size()),
                  "phaseOrder",
                  String.join(",", lifecycleReport.phaseOrder()))));
      if (lifecycleReport.plannedHandlers().isEmpty()) {
        diagnosticSink.record(
            new DiagnosticEvent(
                "lifecycle.execute",
                LaunchPhase.ENTRYPOINT_INVOKE.name(),
                0L,
                "ok",
                "No lifecycle handlers to execute",
                DiagnosticMeasurements.details("handlerCount", "0")));
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
}
