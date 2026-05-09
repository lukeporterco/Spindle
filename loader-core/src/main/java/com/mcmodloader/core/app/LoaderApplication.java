package com.mcmodloader.core.app;

import com.mcmodloader.core.LoaderMain;
import com.mcmodloader.core.cli.LaunchArguments;
import com.mcmodloader.core.diagnostics.DiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.execution.StandardGameLaunchExecutor;
import com.mcmodloader.core.game.GameProvider;
import com.mcmodloader.core.game.GameProviderResolver;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.launch.LaunchPhase;
import com.mcmodloader.core.minecraft.MinecraftGameProvider;
import com.mcmodloader.core.minecraft.flow.MinecraftFlowRunner;
import com.mcmodloader.core.pipeline.ModpackPlanningPipeline;
import com.mcmodloader.core.pipeline.ModpackPlanningResult;
import com.mcmodloader.core.report.DiagnosticMeasurements;
import java.nio.file.Path;

public final class LoaderApplication {
  private final GameProviderResolver gameProviderResolver = new GameProviderResolver();
  private final ModpackPlanningPipeline modpackPlanningPipeline = new ModpackPlanningPipeline();
  private final StandardGameLaunchExecutor standardGameLaunchExecutor =
      new StandardGameLaunchExecutor();
  private final MinecraftFlowRunner minecraftFlowRunner = new MinecraftFlowRunner();

  public void run(
      Path workingDirectory, LaunchArguments launchArguments, DiagnosticSink diagnosticSink)
      throws LoaderException {
    LaunchContext context = createLaunchContext(workingDirectory, launchArguments);
    GameProvider gameProvider =
        DiagnosticMeasurements.measure(
            diagnosticSink,
            "game_provider.resolve",
            LaunchPhase.GAME_PROVIDER_RESOLVE,
            () -> gameProviderResolver.resolve(context, launchArguments.minecraftProviderConfig()),
            provider ->
                DiagnosticMeasurements.details(
                    "gameProviderId",
                    provider.id(),
                    "gameProviderName",
                    provider.displayName(),
                    "gameProviderVersion",
                    provider.version()));

    ModpackPlanningResult planningResult =
        modpackPlanningPipeline.plan(context, gameProvider, diagnosticSink);
    if (gameProvider instanceof MinecraftGameProvider minecraftGameProvider) {
      minecraftFlowRunner.run(
          context, launchArguments, minecraftGameProvider, planningResult, diagnosticSink);
      return;
    }

    standardGameLaunchExecutor.execute(context, gameProvider, planningResult, diagnosticSink);
  }

  private static LaunchContext createLaunchContext(
      Path workingDirectory, LaunchArguments launchArguments) {
    return new LaunchContext(
        workingDirectory,
        workingDirectory.resolve("mods"),
        launchArguments.gameMainClass(),
        launchArguments.gameProviderId(),
        launchArguments.launchArguments(),
        launchArguments.validateOnly(),
        launchArguments.explain(),
        launchArguments.strictResources(),
        launchArguments.strictPackages(),
        LoaderMain.LOADER_VERSION,
        Runtime.version().feature(),
        LoaderMain.TARGET_MINECRAFT_VERSION);
  }
}
