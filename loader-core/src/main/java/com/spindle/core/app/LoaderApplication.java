package com.spindle.core.app;

import com.spindle.core.LoaderMain;
import com.spindle.core.cli.LaunchArguments;
import com.spindle.core.diagnostics.DiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.execution.StandardGameLaunchExecutor;
import com.spindle.core.game.GameProvider;
import com.spindle.core.game.GameProviderResolver;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.launch.LaunchPhase;
import com.spindle.core.minecraft.MinecraftGameProvider;
import com.spindle.core.minecraft.flow.MinecraftFlowRunner;
import com.spindle.core.pipeline.ModpackPlanningPipeline;
import com.spindle.core.pipeline.ModpackPlanningResult;
import com.spindle.core.report.DiagnosticMeasurements;
import com.spindle.core.report.DisplayPaths;
import com.spindle.core.runtime.CompiledModpackProfileBuilder;
import com.spindle.core.runtime.CompiledModpackProfileWriter;
import java.nio.file.Path;

public final class LoaderApplication {
  private final GameProviderResolver gameProviderResolver = new GameProviderResolver();
  private final ModpackPlanningPipeline modpackPlanningPipeline = new ModpackPlanningPipeline();
  private final CompiledModpackProfileBuilder compiledModpackProfileBuilder =
      new CompiledModpackProfileBuilder();
  private final CompiledModpackProfileWriter compiledModpackProfileWriter =
      new CompiledModpackProfileWriter();
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
    writeCompiledProfile(context, launchArguments, planningResult, diagnosticSink);
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

  private void writeCompiledProfile(
      LaunchContext context,
      LaunchArguments launchArguments,
      ModpackPlanningResult planningResult,
      DiagnosticSink diagnosticSink)
      throws LoaderException {
    Path outputPath = context.workingDirectory().resolve("spindle.profile.json");
    DiagnosticMeasurements.measure(
        diagnosticSink,
        "runtime.compiled_profile.write",
        LaunchPhase.COMPLETE,
        () ->
            compiledModpackProfileWriter.write(
                outputPath,
                compiledModpackProfileBuilder.build(
                    context, planningResult, resolveGameSide(launchArguments))),
        result ->
            DiagnosticMeasurements.details(
                "compiledProfileOutputPath",
                DisplayPaths.displayPath(context, result.outputPath()),
                "compiledProfileSchemaVersion",
                Integer.toString(result.schemaVersion()),
                "compiledProfileFingerprint",
                result.fingerprint()));
  }

  private static String resolveGameSide(LaunchArguments launchArguments) {
    if ("minecraft".equals(launchArguments.gameProviderId())) {
      return launchArguments.minecraftProviderConfig().side().id();
    }
    return "universal";
  }
}
