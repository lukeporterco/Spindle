package com.mcmodloader.core.minecraft.flow;

import com.mcmodloader.core.cli.LaunchArguments;
import com.mcmodloader.core.diagnostics.DiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.minecraft.MinecraftDryRunResult;
import com.mcmodloader.core.minecraft.MinecraftGameProvider;
import com.mcmodloader.core.minecraft.bootstrap.MinecraftBootstrapExitCode;
import com.mcmodloader.core.minecraft.bootstrap.MinecraftBootstrapResult;
import com.mcmodloader.core.pipeline.ModpackPlanningResult;
import com.mcmodloader.core.process.MinecraftProcessResult;
import com.mcmodloader.core.report.StartupProfileSupport;

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
      System.out.println("[loader] minecraft cache inspection complete");
      return;
    }
    if (minecraftGameProvider.config().baselineServerEnabled()) {
      minecraftBaselineFlow.complete(
          context, minecraftGameProvider.config(), dryRunResult, diagnosticSink);
      StartupProfileSupport.writeStartupProfile(context, diagnosticSink);
      System.out.println(
          minecraftGameProvider.config().offlineReplay()
              ? "[loader] minecraft baseline offline replay complete"
              : "[loader] minecraft server baseline complete");
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
      System.out.println("[loader] minecraft server bootstrap complete");
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
      System.out.println("[loader] minecraft server launch complete");
      return;
    }
    StartupProfileSupport.writeStartupProfile(context, diagnosticSink);
    System.out.println("[loader] minecraft dry run complete");
  }
}
