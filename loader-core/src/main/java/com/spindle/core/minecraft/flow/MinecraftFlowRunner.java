package com.spindle.core.minecraft.flow;

import com.spindle.core.cli.LaunchArguments;
import com.spindle.core.diagnostics.DiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.minecraft.MinecraftDryRunResult;
import com.spindle.core.minecraft.MinecraftGameProvider;
import com.spindle.core.minecraft.bootstrap.MinecraftBootstrapExitCode;
import com.spindle.core.minecraft.bootstrap.MinecraftBootstrapResult;
import com.spindle.core.pipeline.ModpackPlanningResult;
import com.spindle.core.process.MinecraftProcessResult;
import com.spindle.core.report.StartupProfileSupport;

public final class MinecraftFlowRunner {
  private final MinecraftDryRunFlow minecraftDryRunFlow = new MinecraftDryRunFlow();
  private final MinecraftBaselineFlow minecraftBaselineFlow = new MinecraftBaselineFlow();
  private final MinecraftBootstrapFlow minecraftBootstrapFlow = new MinecraftBootstrapFlow();
  private final MinecraftServerLaunchFlow minecraftServerLaunchFlow =
      new MinecraftServerLaunchFlow();

  public void run(
      LaunchContext context,
      LaunchArguments launchArguments,
      MinecraftGameProvider minecraftGameProvider,
      ModpackPlanningResult planningResult,
      DiagnosticSink diagnosticSink)
      throws LoaderException {
    MinecraftDryRunResult dryRunResult =
        minecraftDryRunFlow.run(
            context, launchArguments, minecraftGameProvider, planningResult, diagnosticSink);
    if (minecraftGameProvider.config().cacheInspect()) {
      StartupProfileSupport.writeStartupProfile(context, diagnosticSink);
      System.out.println("[spindle] minecraft cache inspection complete");
      return;
    }
    if (minecraftGameProvider.config().baselineServerEnabled()) {
      minecraftBaselineFlow.complete(
          context, minecraftGameProvider.config(), dryRunResult, diagnosticSink);
      StartupProfileSupport.writeStartupProfile(context, diagnosticSink);
      System.out.println(
          minecraftGameProvider.config().offlineReplay()
              ? "[spindle] minecraft baseline offline replay complete"
              : "[spindle] minecraft server baseline complete");
      return;
    }
    if (minecraftGameProvider.config().bootstrapServer()) {
      MinecraftBootstrapResult bootstrapResult =
          minecraftBootstrapFlow.launch(
              context, minecraftGameProvider.config(), dryRunResult, diagnosticSink);
      if (bootstrapResult.exitCode() != MinecraftBootstrapExitCode.SUCCESS.code()) {
        throw new LoaderException(
            "Minecraft bootstrap failed. See minecraft-server-bootstrap-result.json for details.");
      }
      StartupProfileSupport.writeStartupProfile(context, diagnosticSink);
      System.out.println("[spindle] minecraft server bootstrap complete");
      return;
    }
    if (minecraftGameProvider.config().launch()) {
      MinecraftProcessResult processResult =
          minecraftServerLaunchFlow.launch(
              context, minecraftGameProvider.config(), dryRunResult, diagnosticSink);
      if (minecraftGameProvider.config().requireReady() && !processResult.readyDetected()) {
        throw new LoaderException(
            "Minecraft server launch did not detect a ready line while --minecraft-require-ready was set.");
      }
      StartupProfileSupport.writeStartupProfile(context, diagnosticSink);
      System.out.println("[spindle] minecraft server launch complete");
      return;
    }
    StartupProfileSupport.writeStartupProfile(context, diagnosticSink);
    System.out.println("[spindle] minecraft dry run complete");
  }
}
