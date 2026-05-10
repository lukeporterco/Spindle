package com.spindle.core.cli;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.minecraft.MinecraftMetadataResolver;
import com.spindle.core.minecraft.MinecraftProviderConfig;
import com.spindle.core.minecraft.MinecraftSide;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class LoaderCliParser {
  private static final String DEFAULT_GAME_PROVIDER_ID = "sample";

  public LaunchArguments parse(String[] args) throws LoaderException {
    String gameMainClass = null;
    String gameProviderId = DEFAULT_GAME_PROVIDER_ID;
    List<String> launchArguments = new ArrayList<>();
    boolean validateOnly = false;
    boolean explain = false;
    boolean strictResources = false;
    boolean strictPackages = false;
    String minecraftVersion = null;
    Path minecraftDirectory = null;
    Path minecraftVersionJson = null;
    Path minecraftManifestJson = null;
    MinecraftSide minecraftSide = MinecraftSide.CLIENT;
    boolean minecraftDryRun = false;
    boolean minecraftVerifyFiles = false;
    boolean minecraftFetchMetadata = false;
    boolean minecraftDownloadServer = false;
    Path minecraftCacheDirectory = Path.of("minecraft-cache");
    boolean minecraftOffline = false;
    boolean minecraftCacheInspect = false;
    boolean minecraftCacheRepair = false;
    boolean minecraftCacheStrict = false;
    boolean minecraftForceRedownload = false;
    Path minecraftOutputPlan = Path.of("minecraft-launch-plan.json");
    boolean minecraftLaunch = false;
    Path minecraftServerDirectory = null;
    boolean minecraftAcceptEulaForTest = false;
    List<String> minecraftServerJvmArgs = new ArrayList<>();
    List<String> minecraftServerArgs = new ArrayList<>();
    int minecraftLaunchTimeoutSeconds = 30;
    boolean minecraftStopAfterReady = false;
    int minecraftReadyTimeoutSeconds = 20;
    boolean minecraftBaselineServer = false;
    String minecraftBaselineVersion = null;
    Path minecraftBaselineReport = Path.of("minecraft-server-baseline.json");
    boolean minecraftOfflineReplay = false;
    boolean minecraftRequireReady = false;
    boolean minecraftRealSmoke = false;
    String minecraftManifestUrl = MinecraftMetadataResolver.DEFAULT_MANIFEST_URL;
    boolean minecraftRuntimePlan = false;
    boolean minecraftPlanMods = false;
    boolean minecraftIntegrationPlan = false;
    boolean minecraftBoundaryReport = false;
    boolean minecraftPreflight = false;
    boolean minecraftOfflinePreflight = false;
    boolean minecraftStrictBoundary = false;
    boolean minecraftStrictRuntimeConflicts = false;
    boolean minecraftStrictSide = false;
    boolean minecraftStrictClassVersions = false;
    boolean minecraftExplainBoundary = false;
    boolean minecraftExplainRuntime = false;
    boolean minecraftExplainMods = false;
    boolean minecraftReproducibilityCheck = false;
    boolean minecraftExecutionPlan = false;
    boolean minecraftBootstrapClassloaderGraph = false;
    boolean minecraftBootstrapServer = false;
    boolean minecraftStrictExecution = false;
    boolean minecraftDenyLoaderInternals = false;
    boolean minecraftVerifyPlanFingerprints = false;
    boolean minecraftBootstrapOffline = false;
    boolean minecraftBootstrapFakeServer = false;
    Path macheDirectory = null;
    String macheVersion = null;
    boolean macheReferenceScan = false;

    for (int index = 0; index < args.length; index++) {
      String argument = args[index];
      if ("--game-main".equals(argument)) {
        gameMainClass = CliParsing.requireValue(args, index, "--game-main");
        index++;
        continue;
      }
      if ("--game-provider".equals(argument)) {
        gameProviderId = CliParsing.requireValue(args, index, "--game-provider");
        index++;
        continue;
      }
      if ("--validate-only".equals(argument)) {
        validateOnly = true;
        continue;
      }
      if ("--explain".equals(argument)) {
        explain = true;
        continue;
      }
      if ("--strict-resources".equals(argument)) {
        strictResources = true;
        continue;
      }
      if ("--strict-packages".equals(argument)) {
        strictPackages = true;
        continue;
      }
      if ("--minecraft-version".equals(argument)) {
        minecraftVersion = CliParsing.requireValue(args, index, "--minecraft-version");
        index++;
        continue;
      }
      if ("--minecraft-dir".equals(argument)) {
        minecraftDirectory = Path.of(CliParsing.requireValue(args, index, "--minecraft-dir"));
        index++;
        continue;
      }
      if ("--minecraft-version-json".equals(argument)) {
        minecraftVersionJson =
            Path.of(CliParsing.requireValue(args, index, "--minecraft-version-json"));
        index++;
        continue;
      }
      if ("--minecraft-manifest-json".equals(argument)) {
        minecraftManifestJson =
            Path.of(CliParsing.requireValue(args, index, "--minecraft-manifest-json"));
        index++;
        continue;
      }
      if ("--minecraft-side".equals(argument)) {
        minecraftSide =
            MinecraftSide.fromCliValue(CliParsing.requireValue(args, index, "--minecraft-side"));
        index++;
        continue;
      }
      if ("--minecraft-dry-run".equals(argument)) {
        minecraftDryRun = true;
        continue;
      }
      if ("--minecraft-verify-files".equals(argument)) {
        minecraftVerifyFiles = true;
        continue;
      }
      if ("--minecraft-fetch-metadata".equals(argument)) {
        minecraftFetchMetadata = true;
        continue;
      }
      if ("--minecraft-download-server".equals(argument)) {
        minecraftDownloadServer = true;
        continue;
      }
      if ("--minecraft-cache-dir".equals(argument)) {
        minecraftCacheDirectory =
            Path.of(CliParsing.requireValue(args, index, "--minecraft-cache-dir"));
        index++;
        continue;
      }
      if ("--minecraft-offline".equals(argument)) {
        minecraftOffline = true;
        continue;
      }
      if ("--minecraft-cache-inspect".equals(argument)) {
        minecraftCacheInspect = true;
        continue;
      }
      if ("--minecraft-cache-repair".equals(argument)) {
        minecraftCacheRepair = true;
        continue;
      }
      if ("--minecraft-cache-strict".equals(argument)) {
        minecraftCacheStrict = true;
        continue;
      }
      if ("--minecraft-force-redownload".equals(argument)) {
        minecraftForceRedownload = true;
        continue;
      }
      if ("--minecraft-output-plan".equals(argument)) {
        minecraftOutputPlan =
            Path.of(CliParsing.requireValue(args, index, "--minecraft-output-plan"));
        index++;
        continue;
      }
      if ("--minecraft-launch".equals(argument)) {
        minecraftLaunch = true;
        continue;
      }
      if ("--minecraft-baseline-server".equals(argument)) {
        minecraftBaselineServer = true;
        continue;
      }
      if ("--minecraft-baseline-version".equals(argument)) {
        minecraftBaselineVersion =
            CliParsing.requireValue(args, index, "--minecraft-baseline-version");
        index++;
        continue;
      }
      if ("--minecraft-baseline-report".equals(argument)) {
        minecraftBaselineReport =
            Path.of(CliParsing.requireValue(args, index, "--minecraft-baseline-report"));
        index++;
        continue;
      }
      if ("--minecraft-offline-replay".equals(argument)) {
        minecraftOfflineReplay = true;
        continue;
      }
      if ("--minecraft-require-ready".equals(argument)) {
        minecraftRequireReady = true;
        continue;
      }
      if ("--minecraft-real-smoke".equals(argument)) {
        minecraftRealSmoke = true;
        continue;
      }
      if ("--minecraft-server-dir".equals(argument)) {
        minecraftServerDirectory =
            Path.of(CliParsing.requireValue(args, index, "--minecraft-server-dir"));
        index++;
        continue;
      }
      if ("--minecraft-accept-eula-for-test".equals(argument)) {
        minecraftAcceptEulaForTest = true;
        continue;
      }
      if ("--minecraft-server-jvm-arg".equals(argument)) {
        minecraftServerJvmArgs.add(
            CliParsing.requireValue(args, index, "--minecraft-server-jvm-arg"));
        index++;
        continue;
      }
      if ("--minecraft-server-arg".equals(argument)) {
        minecraftServerArgs.add(CliParsing.requireValue(args, index, "--minecraft-server-arg"));
        index++;
        continue;
      }
      if ("--minecraft-launch-timeout-seconds".equals(argument)) {
        minecraftLaunchTimeoutSeconds =
            CliParsing.parsePositiveInt(
                CliParsing.requireValue(args, index, "--minecraft-launch-timeout-seconds"),
                "--minecraft-launch-timeout-seconds");
        index++;
        continue;
      }
      if ("--minecraft-stop-after-ready".equals(argument)) {
        minecraftStopAfterReady = true;
        continue;
      }
      if ("--minecraft-ready-timeout-seconds".equals(argument)) {
        minecraftReadyTimeoutSeconds =
            CliParsing.parsePositiveInt(
                CliParsing.requireValue(args, index, "--minecraft-ready-timeout-seconds"),
                "--minecraft-ready-timeout-seconds");
        index++;
        continue;
      }
      if ("--mache-dir".equals(argument)) {
        macheDirectory = Path.of(CliParsing.requireValue(args, index, "--mache-dir"));
        index++;
        continue;
      }
      if ("--mache-version".equals(argument)) {
        macheVersion = CliParsing.requireValue(args, index, "--mache-version");
        index++;
        continue;
      }
      if ("--mache-reference-scan".equals(argument)) {
        macheReferenceScan = true;
        continue;
      }
      if ("--minecraft-runtime-plan".equals(argument)) {
        minecraftRuntimePlan = true;
        continue;
      }
      if ("--minecraft-plan-mods".equals(argument)) {
        minecraftPlanMods = true;
        continue;
      }
      if ("--minecraft-integration-plan".equals(argument)) {
        minecraftIntegrationPlan = true;
        continue;
      }
      if ("--minecraft-boundary-report".equals(argument)) {
        minecraftBoundaryReport = true;
        continue;
      }
      if ("--minecraft-preflight".equals(argument)) {
        minecraftPreflight = true;
        minecraftRuntimePlan = true;
        minecraftBoundaryReport = true;
        minecraftIntegrationPlan = true;
        continue;
      }
      if ("--minecraft-offline-preflight".equals(argument)) {
        minecraftOfflinePreflight = true;
        minecraftPreflight = true;
        minecraftRuntimePlan = true;
        minecraftBoundaryReport = true;
        minecraftIntegrationPlan = true;
        minecraftOffline = true;
        continue;
      }
      if ("--minecraft-strict-boundary".equals(argument)) {
        minecraftStrictBoundary = true;
        continue;
      }
      if ("--minecraft-strict-runtime-conflicts".equals(argument)) {
        minecraftStrictRuntimeConflicts = true;
        continue;
      }
      if ("--minecraft-strict-side".equals(argument)) {
        minecraftStrictSide = true;
        continue;
      }
      if ("--minecraft-strict-class-versions".equals(argument)) {
        minecraftStrictClassVersions = true;
        continue;
      }
      if ("--minecraft-explain-boundary".equals(argument)) {
        minecraftExplainBoundary = true;
        minecraftBoundaryReport = true;
        continue;
      }
      if ("--minecraft-explain-runtime".equals(argument)) {
        minecraftExplainRuntime = true;
        minecraftRuntimePlan = true;
        continue;
      }
      if ("--minecraft-explain-mods".equals(argument)) {
        minecraftExplainMods = true;
        minecraftIntegrationPlan = true;
        continue;
      }
      if ("--minecraft-reproducibility-check".equals(argument)) {
        minecraftReproducibilityCheck = true;
        minecraftRuntimePlan = true;
        minecraftBoundaryReport = true;
        minecraftIntegrationPlan = true;
        continue;
      }
      if ("--minecraft-execution-plan".equals(argument)) {
        minecraftExecutionPlan = true;
        minecraftRuntimePlan = true;
        minecraftBoundaryReport = true;
        minecraftIntegrationPlan = true;
        continue;
      }
      if ("--minecraft-bootstrap-classloader-graph".equals(argument)) {
        minecraftBootstrapClassloaderGraph = true;
        minecraftExecutionPlan = true;
        minecraftRuntimePlan = true;
        minecraftBoundaryReport = true;
        minecraftIntegrationPlan = true;
        continue;
      }
      if ("--minecraft-bootstrap".equals(argument)
          || "--minecraft-bootstrap-server".equals(argument)
          || "--minecraft-execute-mods".equals(argument)) {
        minecraftBootstrapServer = true;
        minecraftExecutionPlan = true;
        minecraftRuntimePlan = true;
        minecraftBoundaryReport = true;
        minecraftIntegrationPlan = true;
        continue;
      }
      if ("--minecraft-strict-execution".equals(argument)) {
        minecraftStrictExecution = true;
        continue;
      }
      if ("--minecraft-deny-loader-internals".equals(argument)) {
        minecraftDenyLoaderInternals = true;
        continue;
      }
      if ("--minecraft-verify-plan-fingerprints".equals(argument)) {
        minecraftVerifyPlanFingerprints = true;
        continue;
      }
      if ("--minecraft-bootstrap-offline".equals(argument)) {
        minecraftBootstrapOffline = true;
        minecraftBootstrapServer = true;
        minecraftExecutionPlan = true;
        minecraftRuntimePlan = true;
        minecraftBoundaryReport = true;
        minecraftIntegrationPlan = true;
        continue;
      }
      if ("--minecraft-bootstrap-fake-server".equals(argument)) {
        minecraftBootstrapFakeServer = true;
        minecraftBootstrapServer = true;
        minecraftExecutionPlan = true;
        minecraftRuntimePlan = true;
        minecraftBoundaryReport = true;
        minecraftIntegrationPlan = true;
        continue;
      }

      launchArguments.add(argument);
    }

    if (gameMainClass == null) {
      throw new LoaderException("Missing required argument --game-main");
    }

    MinecraftProviderConfig minecraftProviderConfig =
        new MinecraftProviderConfig(
            minecraftVersion,
            minecraftDirectory,
            minecraftVersionJson,
            minecraftManifestJson,
            minecraftSide,
            minecraftDryRun,
            minecraftVerifyFiles,
            minecraftFetchMetadata,
            minecraftDownloadServer,
            minecraftCacheDirectory,
            minecraftOffline,
            minecraftCacheInspect,
            minecraftCacheRepair,
            minecraftCacheStrict,
            minecraftForceRedownload,
            minecraftOutputPlan,
            minecraftLaunch,
            minecraftServerDirectory,
            minecraftAcceptEulaForTest,
            minecraftServerJvmArgs,
            minecraftServerArgs,
            minecraftLaunchTimeoutSeconds,
            minecraftStopAfterReady,
            minecraftReadyTimeoutSeconds,
            minecraftBaselineServer,
            minecraftBaselineVersion,
            minecraftBaselineReport,
            minecraftOfflineReplay,
            minecraftRequireReady,
            minecraftRealSmoke,
            minecraftManifestUrl,
            minecraftRuntimePlan,
            minecraftPlanMods,
            minecraftIntegrationPlan,
            minecraftBoundaryReport,
            minecraftPreflight,
            minecraftOfflinePreflight,
            minecraftStrictBoundary,
            minecraftStrictRuntimeConflicts,
            minecraftStrictSide,
            minecraftStrictClassVersions,
            minecraftExplainBoundary,
            minecraftExplainRuntime,
            minecraftExplainMods,
            minecraftReproducibilityCheck,
            minecraftExecutionPlan,
            minecraftBootstrapClassloaderGraph,
            minecraftBootstrapServer,
            minecraftStrictExecution,
            minecraftDenyLoaderInternals,
            minecraftVerifyPlanFingerprints,
            minecraftBootstrapOffline,
            minecraftBootstrapFakeServer);

    return new LaunchArguments(
        gameMainClass,
        gameProviderId,
        launchArguments,
        validateOnly,
        explain,
        strictResources,
        strictPackages,
        minecraftProviderConfig,
        macheDirectory,
        macheVersion,
        macheReferenceScan);
  }
}
