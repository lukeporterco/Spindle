package com.mcmodloader.core;

import com.mcmodloader.core.classpath.ModClassLoader;
import com.mcmodloader.core.classpath.RuntimeClasspathPlan;
import com.mcmodloader.core.classpath.RuntimeClasspathPlanner;
import com.mcmodloader.core.artifact.MinecraftArtifactCache;
import com.mcmodloader.core.artifact.MinecraftArtifactInspector;
import com.mcmodloader.core.artifact.MinecraftArtifactResolver;
import com.mcmodloader.core.baseline.MinecraftServerBaseline;
import com.mcmodloader.core.baseline.MinecraftServerBaselineMode;
import com.mcmodloader.core.baseline.MinecraftServerBaselineResult;
import com.mcmodloader.core.baseline.MinecraftServerBaselineWriter;
import com.mcmodloader.core.diagnostics.DiagnosticEvent;
import com.mcmodloader.core.diagnostics.DiagnosticSink;
import com.mcmodloader.core.diagnostics.JsonDiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.discovery.ModCandidate;
import com.mcmodloader.core.discovery.ModDiscoverer;
import com.mcmodloader.core.entrypoint.EntrypointInvoker;
import com.mcmodloader.core.game.GameProvider;
import com.mcmodloader.core.game.GameProviderResolver;
import com.mcmodloader.core.graph.DependencyGraphWriter;
import com.mcmodloader.core.graph.FrozenModGraph;
import com.mcmodloader.core.graph.FrozenModGraphBuilder;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.launch.LaunchPhase;
import com.mcmodloader.core.lockfile.LockfileVerifier;
import com.mcmodloader.core.lockfile.LockfileWriter;
import com.mcmodloader.core.mache.MacheReferenceReport;
import com.mcmodloader.core.mache.MacheReferenceScanner;
import com.mcmodloader.core.mache.MacheReferenceWriter;
import com.mcmodloader.core.metadata.ModMetadataParser;
import com.mcmodloader.core.minecraft.MinecraftArgumentResolver;
import com.mcmodloader.core.minecraft.MinecraftDryRunResult;
import com.mcmodloader.core.minecraft.MinecraftFileVerifier;
import com.mcmodloader.core.minecraft.MinecraftGameProvider;
import com.mcmodloader.core.minecraft.MinecraftInstallLocator;
import com.mcmodloader.core.minecraft.MinecraftLaunchPlan;
import com.mcmodloader.core.minecraft.MinecraftLaunchPlanBuilder;
import com.mcmodloader.core.minecraft.MinecraftLaunchPlanWriter;
import com.mcmodloader.core.minecraft.MinecraftLibrarySelector;
import com.mcmodloader.core.minecraft.MinecraftMetadataResolver;
import com.mcmodloader.core.minecraft.MinecraftModIntegrationPlan;
import com.mcmodloader.core.minecraft.MinecraftModIntegrationPlanner;
import com.mcmodloader.core.minecraft.MinecraftModIntegrationPlanWriter;
import com.mcmodloader.core.minecraft.MinecraftModRejection;
import com.mcmodloader.core.minecraft.MinecraftProviderConfig;
import com.mcmodloader.core.minecraft.MinecraftPreflightResult;
import com.mcmodloader.core.minecraft.MinecraftPreflightResultWriter;
import com.mcmodloader.core.minecraft.MinecraftReproducibilityCheck;
import com.mcmodloader.core.minecraft.MinecraftReproducibilityChecker;
import com.mcmodloader.core.minecraft.MinecraftReproducibilityCheckWriter;
import com.mcmodloader.core.minecraft.MinecraftRuntimeBoundary;
import com.mcmodloader.core.minecraft.MinecraftRuntimeBoundaryBuilder;
import com.mcmodloader.core.minecraft.MinecraftRuntimeBoundaryWriter;
import com.mcmodloader.core.minecraft.MinecraftRuntimeProvenanceWriter;
import com.mcmodloader.core.minecraft.MinecraftSide;
import com.mcmodloader.core.minecraft.MinecraftServerLaunchCommand;
import com.mcmodloader.core.minecraft.MinecraftServerRuntimeClasspath;
import com.mcmodloader.core.minecraft.MinecraftServerRuntimePlan;
import com.mcmodloader.core.minecraft.MinecraftServerRuntimePlanner;
import com.mcmodloader.core.minecraft.MinecraftServerRuntimePlanWriter;
import com.mcmodloader.core.minecraft.MinecraftVersionSelection;
import com.mcmodloader.core.minecraft.MinecraftVersionMetadata;
import com.mcmodloader.core.minecraft.MinecraftVersionMetadataParser;
import com.mcmodloader.core.ownership.ClassOwnershipIndex;
import com.mcmodloader.core.ownership.PackageOwnershipIndex;
import com.mcmodloader.core.profile.StartupProfile;
import com.mcmodloader.core.profile.StartupProfileWriter;
import com.mcmodloader.core.process.JavaExecutableResolver;
import com.mcmodloader.core.process.MinecraftProcessConfig;
import com.mcmodloader.core.process.MinecraftProcessResult;
import com.mcmodloader.core.process.MinecraftProcessResultWriter;
import com.mcmodloader.core.process.MinecraftServerProcessLauncher;
import com.mcmodloader.core.resource.ResourceConflict;
import com.mcmodloader.core.resource.ResourceConflictIndex;
import com.mcmodloader.core.resolve.DependencyResolver;
import com.mcmodloader.core.resolve.ResolvedModSet;
import com.mcmodloader.core.state.ModpackState;
import com.mcmodloader.core.state.ModpackStateWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class LoaderMain {
    public static final String LOADER_VERSION = "0.1.0";
    public static final String TARGET_MINECRAFT_VERSION = "26.1.2";
    private static final String DEFAULT_GAME_PROVIDER_ID = "sample";

    private LoaderMain() {
    }

    public static void main(String[] args) {
        Path workingDirectory = Paths.get("").toAbsolutePath().normalize();
        JsonDiagnosticSink diagnosticSink = new JsonDiagnosticSink(workingDirectory.resolve("diagnostics/startup-trace.json"));
        int exitCode = 0;

        try {
            execute(workingDirectory, args, diagnosticSink);
        } catch (LoaderException exception) {
            System.err.println("[loader] error: " + exception.getMessage());
            exitCode = 1;
        } catch (Exception exception) {
            System.err.println("[loader] error: unexpected failure");
            exception.printStackTrace(System.err);
            exitCode = 1;
        } finally {
            try {
                diagnosticSink.write();
            } catch (IOException exception) {
                System.err.println("[loader] error: failed to write diagnostics");
                exitCode = 1;
            }
        }

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static void execute(Path workingDirectory, String[] args, DiagnosticSink diagnosticSink) throws LoaderException {
        LaunchArguments launchArguments =
            measure(
                diagnosticSink,
                "argument.parse",
                LaunchPhase.ARGUMENT_PARSE,
                () -> resolveLaunchArguments(workingDirectory, parseArguments(args)),
                parsedArguments -> details(
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
                    parsedArguments.minecraftProviderConfig() == null ? null : parsedArguments.minecraftProviderConfig().requestedVersion(),
                    "minecraftSide",
                    parsedArguments.minecraftProviderConfig() == null ? null : parsedArguments.minecraftProviderConfig().side().id(),
                    "macheReferenceScan",
                    Boolean.toString(parsedArguments.macheReferenceScan())
                )
            );

        LaunchContext context = createLaunchContext(workingDirectory, launchArguments);
        GameProvider gameProvider =
            measure(
                diagnosticSink,
                "game_provider.resolve",
                LaunchPhase.GAME_PROVIDER_RESOLVE,
                () -> new GameProviderResolver().resolve(context, launchArguments.minecraftProviderConfig()),
                provider -> details(
                    "gameProviderId",
                    provider.id(),
                    "gameProviderName",
                    provider.displayName(),
                    "gameProviderVersion",
                    provider.version()
                )
        );

        run(context, gameProvider, launchArguments, diagnosticSink);
    }

    static LaunchArguments parseArguments(String[] args) throws LoaderException {
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
        Path macheDirectory = null;
        String macheVersion = null;
        boolean macheReferenceScan = false;

        for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            if ("--game-main".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --game-main");
                }
                gameMainClass = args[++index];
                continue;
            }

            if ("--game-provider".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --game-provider");
                }
                gameProviderId = args[++index];
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
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-version");
                }
                minecraftVersion = args[++index];
                continue;
            }

            if ("--minecraft-dir".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-dir");
                }
                minecraftDirectory = Path.of(args[++index]);
                continue;
            }

            if ("--minecraft-version-json".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-version-json");
                }
                minecraftVersionJson = Path.of(args[++index]);
                continue;
            }

            if ("--minecraft-manifest-json".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-manifest-json");
                }
                minecraftManifestJson = Path.of(args[++index]);
                continue;
            }

            if ("--minecraft-side".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-side");
                }
                minecraftSide = MinecraftSide.fromCliValue(args[++index]);
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
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-cache-dir");
                }
                minecraftCacheDirectory = Path.of(args[++index]);
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
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-output-plan");
                }
                minecraftOutputPlan = Path.of(args[++index]);
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
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-baseline-version");
                }
                minecraftBaselineVersion = args[++index];
                continue;
            }

            if ("--minecraft-baseline-report".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-baseline-report");
                }
                minecraftBaselineReport = Path.of(args[++index]);
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
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-server-dir");
                }
                minecraftServerDirectory = Path.of(args[++index]);
                continue;
            }

            if ("--minecraft-accept-eula-for-test".equals(argument)) {
                minecraftAcceptEulaForTest = true;
                continue;
            }

            if ("--minecraft-server-jvm-arg".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-server-jvm-arg");
                }
                minecraftServerJvmArgs.add(args[++index]);
                continue;
            }

            if ("--minecraft-server-arg".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-server-arg");
                }
                minecraftServerArgs.add(args[++index]);
                continue;
            }

            if ("--minecraft-launch-timeout-seconds".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-launch-timeout-seconds");
                }
                minecraftLaunchTimeoutSeconds = parsePositiveInt(args[++index], "--minecraft-launch-timeout-seconds");
                continue;
            }

            if ("--minecraft-stop-after-ready".equals(argument)) {
                minecraftStopAfterReady = true;
                continue;
            }

            if ("--minecraft-ready-timeout-seconds".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-ready-timeout-seconds");
                }
                minecraftReadyTimeoutSeconds = parsePositiveInt(args[++index], "--minecraft-ready-timeout-seconds");
                continue;
            }

            if ("--mache-dir".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --mache-dir");
                }
                macheDirectory = Path.of(args[++index]);
                continue;
            }

            if ("--mache-version".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --mache-version");
                }
                macheVersion = args[++index];
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
                minecraftReproducibilityCheck
            );

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
            macheReferenceScan
        );
    }

    private static void run(LaunchContext context, GameProvider gameProvider, LaunchArguments launchArguments, DiagnosticSink diagnosticSink)
        throws LoaderException {
        ModDiscoverer modDiscoverer = new ModDiscoverer();
        ModMetadataParser metadataParser = new ModMetadataParser();
        DependencyResolver dependencyResolver = new DependencyResolver();
        LockfileWriter lockfileWriter = new LockfileWriter();
        LockfileVerifier lockfileVerifier = new LockfileVerifier();
        RuntimeClasspathPlanner classpathPlanner = new RuntimeClasspathPlanner();
        FrozenModGraphBuilder frozenModGraphBuilder = new FrozenModGraphBuilder();
        ModpackStateWriter modpackStateWriter = new ModpackStateWriter();
        DependencyGraphWriter dependencyGraphWriter = new DependencyGraphWriter();
        StartupProfileWriter startupProfileWriter = new StartupProfileWriter();
        EntrypointInvoker entrypointInvoker = new EntrypointInvoker();

        List<ModCandidate> discoveredMods =
            measure(
                diagnosticSink,
                "mod.discovery",
                LaunchPhase.MOD_DISCOVERY,
                () -> modDiscoverer.discover(context),
                candidates -> details("discoveredModCount", Integer.toString(candidates.size()))
            );
        System.out.println("[loader] discovered " + discoveredMods.size() + " " + pluralize(discoveredMods.size()));

        List<ModCandidate> parsedMods =
            measure(
                diagnosticSink,
                "metadata.parse",
                LaunchPhase.METADATA_PARSE,
                () -> parseMetadata(discoveredMods, metadataParser),
                candidates -> details("parsedModCount", Integer.toString(candidates.size()))
            );

        ResolvedModSet resolvedMods =
            measure(
                diagnosticSink,
                "dependency.resolution",
                LaunchPhase.DEPENDENCY_RESOLUTION,
                () -> dependencyResolver.resolve(context, parsedMods),
                resolvedModSet -> details("resolvedModCount", Integer.toString(resolvedModSet.mods().size()))
            );
        System.out.println("[loader] resolved " + resolvedMods.mods().size() + " " + pluralize(resolvedMods.mods().size()));

        Path lockfilePath = context.workingDirectory().resolve("loader.lock.json");
        String lockfileAction =
            measure(
                diagnosticSink,
                "lockfile.verify_or_write",
                LaunchPhase.LOCKFILE,
                () -> verifyOrWriteLockfile(lockfilePath, context, resolvedMods, lockfileWriter, lockfileVerifier),
                action -> details("lockfileAction", action)
            );
        System.out.println("wrote".equals(lockfileAction) ? "[loader] wrote loader.lock.json" : "[loader] verified loader.lock.json");

        RuntimeClasspathPlan classpathPlan =
            measure(
                diagnosticSink,
                "classpath.plan",
                LaunchPhase.CLASSPATH_PLAN,
                () -> classpathPlanner.plan(context, resolvedMods),
                plan -> details(
                    "modJarCount",
                    Integer.toString(plan.modJars().size()),
                    "modJars",
                    String.join(",", plan.modJarDisplayPaths(context.workingDirectory()))
                )
            );

        ClassOwnershipIndex ownershipIndex =
            measure(
                diagnosticSink,
                "ownership.index",
                LaunchPhase.CLASSPATH_PLAN,
                () -> ClassOwnershipIndex.build(resolvedMods),
                index -> details("classOwnershipCount", Integer.toString(index.totalClasses()))
            );

        PackageOwnershipIndex packageOwnershipIndex =
            measure(
                diagnosticSink,
                "package.index",
                LaunchPhase.CLASSPATH_PLAN,
                () -> PackageOwnershipIndex.build(resolvedMods),
                index -> details("splitPackageCount", Integer.toString(index.splitPackages().size()))
            );
        recordSplitPackageDiagnostics(diagnosticSink, packageOwnershipIndex);
        enforceStrictPackages(context, packageOwnershipIndex);

        ResourceConflictIndex resourceConflictIndex =
            measure(
                diagnosticSink,
                "resource.index",
                LaunchPhase.CLASSPATH_PLAN,
                () -> ResourceConflictIndex.build(resolvedMods),
                index -> details("duplicateResourceCount", Integer.toString(index.conflicts().size()))
            );
        recordResourceDiagnostics(diagnosticSink, resourceConflictIndex);
        enforceStrictResources(context, resourceConflictIndex);

        FrozenModGraph frozenModGraph =
            measure(
                diagnosticSink,
                "frozen_mod_graph.create",
                LaunchPhase.FROZEN_MOD_GRAPH,
                () -> frozenModGraphBuilder.build(
                    context,
                    gameProvider,
                    resolvedMods,
                    classpathPlan,
                    ownershipIndex,
                    packageOwnershipIndex,
                    resourceConflictIndex
                ),
                graph -> details(
                    "frozenModCount",
                    Integer.toString(graph.mods().size()),
                    "gameProviderId",
                    graph.gameProviderId(),
                    "gameProviderVersion",
                    graph.gameProviderVersion()
                )
            );

        if (context.explain()) {
            printExplain(context, frozenModGraph, discoveredMods.size(), lockfileAction);
            diagnosticSink.record(
                new DiagnosticEvent(
                    "explain.print",
                    LaunchPhase.COMPLETE.name(),
                    0L,
                    "ok",
                    "Explain summary printed",
                    details(
                        "dependencyGraphOutputPath",
                        "dependency-graph.json",
                        "modpackStateOutputPath",
                        "modpack-state.json"
                    )
                )
            );
        }

        Path modpackStatePath = context.workingDirectory().resolve("modpack-state.json");
        ModpackState modpackState = modpackStateWriter.create(frozenModGraph, context.workingDirectory());
        measure(
            diagnosticSink,
            "modpack_state.write",
            LaunchPhase.COMPLETE,
            () -> {
                modpackStateWriter.write(modpackStatePath, modpackState);
                return modpackStatePath;
            },
            outputPath -> details("modpackStateOutputPath", displayPath(context, outputPath))
        );

        Path dependencyGraphPath = context.workingDirectory().resolve("dependency-graph.json");
        measure(
            diagnosticSink,
            "dependency_graph.write",
            LaunchPhase.COMPLETE,
            () -> {
                dependencyGraphWriter.write(dependencyGraphPath, frozenModGraph);
                return dependencyGraphPath;
            },
            outputPath -> details("dependencyGraphOutputPath", displayPath(context, outputPath))
        );

        if (gameProvider instanceof MinecraftGameProvider minecraftGameProvider) {
            MinecraftDryRunResult dryRunResult =
                runMinecraftDryRun(context, launchArguments, minecraftGameProvider, parsedMods, resolvedMods, diagnosticSink);
            if (minecraftGameProvider.config().cacheInspect()) {
                writeStartupProfile(context, diagnosticSink, startupProfileWriter);
                System.out.println("[loader] minecraft cache inspection complete");
                return;
            }
            if (minecraftGameProvider.config().baselineServerEnabled()) {
                completeMinecraftBaseline(context, minecraftGameProvider.config(), dryRunResult, diagnosticSink);
                writeStartupProfile(context, diagnosticSink, startupProfileWriter);
                System.out.println(
                    minecraftGameProvider.config().offlineReplay()
                        ? "[loader] minecraft baseline offline replay complete"
                        : "[loader] minecraft server baseline complete"
                );
                return;
            }
            if (minecraftGameProvider.config().launch()) {
                MinecraftProcessResult processResult =
                    launchMinecraftServer(context, minecraftGameProvider.config(), dryRunResult, diagnosticSink);
                if (minecraftGameProvider.config().requireReady() && !processResult.readyDetected()) {
                    throw new LoaderException("Minecraft server launch did not detect a ready line while --minecraft-require-ready was set.");
                }
                writeStartupProfile(context, diagnosticSink, startupProfileWriter);
                System.out.println("[loader] minecraft server launch complete");
                return;
            }
            writeStartupProfile(context, diagnosticSink, startupProfileWriter);
            System.out.println("[loader] minecraft dry run complete");
            return;
        }

        if (context.validateOnly()) {
            diagnosticSink.record(
                new DiagnosticEvent(
                    "validation.complete",
                    LaunchPhase.COMPLETE.name(),
                    0L,
                    "ok",
                    "Validation complete",
                    details(
                        "resolvedModCount",
                        Integer.toString(frozenModGraph.mods().size()),
                        "modpackStateOutputPath",
                        displayPath(context, modpackStatePath),
                        "dependencyGraphOutputPath",
                        displayPath(context, dependencyGraphPath)
                    )
                )
            );
            writeStartupProfile(context, diagnosticSink, startupProfileWriter);
            System.out.println("[loader] validation complete");
            return;
        }

        try (
            ModClassLoader modClassLoader =
                measure(
                    diagnosticSink,
                    "classpath.create",
                    LaunchPhase.CLASSLOADER_CREATE,
                    () -> ModClassLoader.create(classpathPlan, LoaderMain.class.getClassLoader()),
                    ignored -> details("modJarCount", Integer.toString(classpathPlan.modJars().size()))
                )
        ) {
            List<EntrypointInvoker.EntrypointInvocation> invocations =
                measure(
                    diagnosticSink,
                    "entrypoint.invoke",
                    LaunchPhase.ENTRYPOINT_INVOKE,
                    () -> entrypointInvoker.invoke(frozenModGraph, modClassLoader, ownershipIndex),
                    results -> details(
                        "entrypointCount",
                        Integer.toString(results.size()),
                        "ownerModIds",
                        joinOwners(results),
                        "entrypointClasses",
                        joinEntrypoints(results)
                    )
                );
            if (invocations.isEmpty()) {
                diagnosticSink.record(
                    new DiagnosticEvent(
                        "entrypoint.invoke",
                        LaunchPhase.ENTRYPOINT_INVOKE.name(),
                        0L,
                        "ok",
                        "No entrypoints to invoke",
                        details("entrypointCount", "0")
                    )
                );
            }

            measure(
                diagnosticSink,
                "game.launch",
                LaunchPhase.GAME_LAUNCH,
                () -> {
                    gameProvider.launch(context, modClassLoader);
                    return null;
                },
                ignored -> details(
                    "gameProviderId",
                    gameProvider.id(),
                    "gameProviderVersion",
                    gameProvider.version(),
                    "gameMainClass",
                    context.gameMainClass()
                )
            );
        } catch (IOException exception) {
            throw new LoaderException("Failed to close mod class loader", exception);
        }

        System.out.println("[loader] startup complete");
        diagnosticSink.record(
            new DiagnosticEvent(
                "startup.complete",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Startup complete",
                details("resolvedModCount", Integer.toString(frozenModGraph.mods().size()))
            )
        );
        writeStartupProfile(context, diagnosticSink, startupProfileWriter);
    }

    private static MinecraftDryRunResult runMinecraftDryRun(
        LaunchContext context,
        LaunchArguments launchArguments,
        MinecraftGameProvider minecraftGameProvider,
        List<ModCandidate> parsedMods,
        ResolvedModSet resolvedMods,
        DiagnosticSink diagnosticSink
    ) throws LoaderException {
        MinecraftProviderConfig config = minecraftGameProvider.config();
        MinecraftArtifactCache artifactCache = new MinecraftArtifactCache(context.workingDirectory(), config.cacheDirectory());
        MinecraftArtifactResolver artifactResolver = new MinecraftArtifactResolver(artifactCache);
        MinecraftInstallLocator installLocator = new MinecraftInstallLocator();
        MinecraftLibrarySelector librarySelector = new MinecraftLibrarySelector();
        MinecraftArgumentResolver argumentResolver = new MinecraftArgumentResolver();
        MinecraftLaunchPlanBuilder launchPlanBuilder = new MinecraftLaunchPlanBuilder();
        MinecraftLaunchPlanWriter launchPlanWriter = new MinecraftLaunchPlanWriter();
        MinecraftFileVerifier fileVerifier = new MinecraftFileVerifier();
        MacheReferenceScanner macheReferenceScanner = new MacheReferenceScanner();
        MacheReferenceWriter macheReferenceWriter = new MacheReferenceWriter();

        if (config.cacheInspect()) {
            measure(
                diagnosticSink,
                "minecraft.metadata.resolve",
                LaunchPhase.COMPLETE,
                () -> {
                    new MinecraftArtifactInspector(artifactCache).inspect(config, diagnosticSink);
                    return config.requestedVersion();
                },
                ignored -> details(
                    "minecraftVersion",
                    config.requestedVersion(),
                    "minecraftSide",
                    config.side().id(),
                    "artifactReportOutputPath",
                    displayPath(context, artifactCache.artifactReportPath())
                )
            );
            return new MinecraftDryRunResult(null, null, null, "missing", null);
        }

        MinecraftArtifactResolver.Resolution artifactResolution =
            measure(
                diagnosticSink,
                "minecraft.metadata.resolve",
                LaunchPhase.COMPLETE,
                () -> artifactResolver.resolve(context.workingDirectory(), config, diagnosticSink),
                resolved -> details(
                    "minecraftVersion",
                    resolved.metadata().id(),
                    "minecraftSide",
                    config.side().id(),
                    "versionJsonPath",
                    displayPath(context, resolved.resolvedVersionJson().versionJsonPath()),
                    "metadataSource",
                    resolved.resolvedVersionJson().metadataSource()
                )
            );
        MinecraftMetadataResolver.ResolvedVersionJson resolvedVersionJson = artifactResolution.resolvedVersionJson();
        MinecraftVersionMetadata metadata = artifactResolution.metadata();

        MinecraftLibrarySelector.Selection selection =
            measure(
                diagnosticSink,
                "minecraft.library.select",
                LaunchPhase.COMPLETE,
                () ->
                    config.side() == MinecraftSide.SERVER
                        ? new MinecraftLibrarySelector.Selection(List.of(), List.of())
                        : librarySelector.select(metadata, installLocator.librariesRoot(config.minecraftDirectory())),
                selected -> details(
                    "minecraftVersion",
                    metadata.id(),
                    "minecraftSide",
                    config.side().id(),
                    "selectedLibraryCount",
                    Integer.toString(selected.libraries().size()),
                    "nativeLibraryCount",
                    Integer.toString(selected.nativeLibraries().size())
                )
            );

        List<Path> launchClasspath = new ArrayList<>();
        selection.libraries().forEach(library -> launchClasspath.add(library.path()));
        if (config.side() == MinecraftSide.CLIENT) {
            launchClasspath.add(installLocator.clientJarPath(config.minecraftDirectory(), metadata.id()));
        } else if (artifactResolution.serverJarPath() != null) {
            launchClasspath.add(artifactResolution.serverJarPath());
        }

        MinecraftArgumentResolver.ResolvedArguments resolvedArguments =
            measure(
                diagnosticSink,
                "minecraft.arguments.resolve",
                LaunchPhase.COMPLETE,
                () -> argumentResolver.resolve(
                    config,
                    metadata,
                    config.minecraftDirectory(),
                    config.minecraftDirectory() == null ? null : installLocator.assetsRoot(config.minecraftDirectory()),
                    installLocator.nativesDirectory(context.workingDirectory(), metadata.id(), config.side()),
                    launchClasspath
                ),
                arguments -> details(
                    "minecraftVersion",
                    metadata.id(),
                    "minecraftSide",
                    config.side().id(),
                    "jvmArgumentCount",
                    Integer.toString(arguments.jvmArguments().size()),
                    "gameArgumentCount",
                    Integer.toString(arguments.gameArguments().size())
                )
            );

        MinecraftLaunchPlan launchPlan =
            launchPlanBuilder.build(
                context.workingDirectory(),
                config,
                resolvedVersionJson,
                metadata,
                artifactResolution.serverJarPath(),
                artifactResolution.serverJarSource(),
                selection,
                resolvedArguments,
                installLocator
            );

        List<Path> missingFiles =
            config.verifyFiles()
                ? measure(
                    diagnosticSink,
                    "minecraft.file_verify",
                    LaunchPhase.COMPLETE,
                    () -> fileVerifier.verify(config, resolvedVersionJson, metadata, artifactResolution.serverJarPath(), selection, installLocator),
                    missing -> details(
                        "minecraftVersion",
                        metadata.id(),
                        "minecraftSide",
                        config.side().id(),
                        "missingFileCount",
                        Integer.toString(missing.size())
                    )
                )
                : fileVerifier.verify(config, resolvedVersionJson, metadata, artifactResolution.serverJarPath(), selection, installLocator);

        launchPlan =
            launchPlan.withMissingFiles(
                missingFiles.stream().map(path -> displayPath(context, path)).toList()
            );

        MinecraftLaunchPlan finalLaunchPlan = launchPlan;
        measure(
            diagnosticSink,
            "minecraft.launch_plan.write",
            LaunchPhase.COMPLETE,
            () -> {
                launchPlanWriter.write(config.outputPlanPath(), finalLaunchPlan);
                return config.outputPlanPath();
            },
            outputPath -> details(
                "minecraftVersion",
                metadata.id(),
                "minecraftSide",
                config.side().id(),
                "missingFileCount",
                Integer.toString(finalLaunchPlan.missingFiles().size()),
                "launchPlanOutputPath",
                displayPath(context, outputPath)
            )
        );

        MacheReferenceReport macheReferenceReport = null;
        if (launchArguments.macheReferenceScan() && launchArguments.macheDirectory() != null) {
            Path reportPath = context.workingDirectory().resolve("mache-reference-report.json");
            String requestedMacheVersion =
                launchArguments.macheVersion() == null || launchArguments.macheVersion().isBlank()
                    ? metadata.id()
                    : launchArguments.macheVersion();
            macheReferenceReport =
                measure(
                    diagnosticSink,
                    "mache.reference.scan",
                    LaunchPhase.COMPLETE,
                    () -> macheReferenceScanner.scan(launchArguments.macheDirectory(), requestedMacheVersion),
                    report -> details(
                        "minecraftVersion",
                        metadata.id(),
                        "macheReferenceScan",
                        "true",
                        "macheReportOutputPath",
                        displayPath(context, reportPath)
                    )
                );
            MacheReferenceReport finalMacheReferenceReport = macheReferenceReport;
            measure(
                diagnosticSink,
                "mache.reference_report.write",
                LaunchPhase.COMPLETE,
                () -> {
                    macheReferenceWriter.write(reportPath, finalMacheReferenceReport);
                    return reportPath;
                },
                outputPath -> details("macheReportOutputPath", displayPath(context, outputPath))
            );
        }

        MinecraftServerRuntimePlanner.PlannedRuntime plannedRuntime = null;
        MinecraftRuntimeBoundary runtimeBoundary = null;
        MinecraftModIntegrationPlan integrationPlan = null;
        List<String> megaMilestoneReports = new ArrayList<>();
        boolean needsRuntimePlanning =
            config.side() == MinecraftSide.SERVER &&
            artifactResolution.serverJarPath() != null &&
            (config.runtimePlan() || config.planMods() || config.boundaryReport() || config.integrationPlan() || config.preflight() || config.reproducibilityCheck() || config.launch());
        if (needsRuntimePlanning) {
            MinecraftArtifactCache finalArtifactCache = artifactCache;
            plannedRuntime =
                measure(
                    diagnosticSink,
                    "minecraft.runtime.plan",
                    LaunchPhase.COMPLETE,
                    () ->
                        new MinecraftServerRuntimePlanner()
                            .plan(context.workingDirectory(), config, finalArtifactCache, artifactResolution, path -> displayPath(context, path)),
                    planned -> details(
                        "minecraftVersion",
                        metadata.id(),
                        "launchMode",
                        planned.plan().launchMode(),
                        "runtimePlanOutputPath",
                        displayPath(context, context.workingDirectory().resolve("minecraft-server-runtime-plan.json"))
                    )
                );
            MinecraftServerRuntimePlanner.PlannedRuntime finalPlannedRuntime = plannedRuntime;
            measure(
                diagnosticSink,
                "minecraft.runtime_plan.write",
                LaunchPhase.COMPLETE,
                () -> {
                    Path outputPath = context.workingDirectory().resolve("minecraft-server-runtime-plan.json");
                    new MinecraftServerRuntimePlanWriter().write(outputPath, finalPlannedRuntime.plan());
                    return outputPath;
                },
                outputPath -> details("runtimePlanOutputPath", displayPath(context, outputPath))
            );
            measure(
                diagnosticSink,
                "minecraft.runtime_provenance.write",
                LaunchPhase.COMPLETE,
                () -> {
                    Path outputPath = context.workingDirectory().resolve("minecraft-runtime-provenance.json");
                    new MinecraftRuntimeProvenanceWriter().write(outputPath, finalPlannedRuntime.plan().provenance());
                    return outputPath;
                },
                outputPath -> details("runtimeProvenanceOutputPath", displayPath(context, outputPath))
            );
            megaMilestoneReports.add("minecraft-server-runtime-plan.json");
            megaMilestoneReports.add("minecraft-runtime-provenance.json");
            if (config.explainRuntime()) {
                printMinecraftRuntimeExplain(finalPlannedRuntime.plan());
            }

            if (config.boundaryReport() || config.planMods() || config.integrationPlan() || config.preflight() || config.reproducibilityCheck()) {
                List<Path> runtimeJars = runtimeJarsForPlan(context, artifactResolution.serverJarPath(), finalPlannedRuntime.plan());
                runtimeBoundary =
                    measure(
                        diagnosticSink,
                        "minecraft.runtime_boundary.create",
                        LaunchPhase.COMPLETE,
                        () ->
                            new MinecraftRuntimeBoundaryBuilder()
                                .build(
                                    finalPlannedRuntime.plan(),
                                    runtimeJars,
                                    path -> displayPath(context, path),
                                    config.strictBoundary(),
                                    config.strictRuntimeConflicts()
                                ),
                        boundary -> details(
                            "packageCount",
                            Integer.toString(boundary.packageOwnership().size()),
                            "resourceCount",
                            Integer.toString(boundary.resourceOwnership().size())
                        )
                    );
                MinecraftRuntimeBoundary finalRuntimeBoundary = runtimeBoundary;
                measure(
                    diagnosticSink,
                    "minecraft.runtime_boundary.write",
                    LaunchPhase.COMPLETE,
                    () -> {
                        Path outputPath = context.workingDirectory().resolve("minecraft-runtime-boundary.json");
                        new MinecraftRuntimeBoundaryWriter().write(outputPath, finalRuntimeBoundary);
                        return outputPath;
                    },
                    outputPath -> details("runtimeBoundaryOutputPath", displayPath(context, outputPath))
                );
                megaMilestoneReports.add("minecraft-runtime-boundary.json");
                if (config.explainBoundary()) {
                    printMinecraftBoundaryExplain(finalRuntimeBoundary);
                }
            }

            if ((config.integrationPlan() || config.planMods() || config.preflight() || config.reproducibilityCheck()) && runtimeBoundary != null) {
                MinecraftRuntimeBoundary finalRuntimeBoundary = runtimeBoundary;
                integrationPlan =
                    measure(
                        diagnosticSink,
                        "minecraft.mod_integration.plan",
                        LaunchPhase.COMPLETE,
                        () ->
                            new MinecraftModIntegrationPlanner()
                                .plan(
                                    context,
                                    parsedMods,
                                    resolvedMods,
                                    finalRuntimeBoundary,
                                    metadata.id(),
                                    config.strictSide(),
                                    config.strictClassVersions(),
                                    config.strictRuntimeConflicts(),
                                    path -> displayPath(context, path)
                                ),
                        plan -> details(
                            "acceptedModCount",
                            Integer.toString(plan.acceptedMods().size()),
                            "rejectedModCount",
                            Integer.toString(plan.rejectedMods().size())
                        )
                    );
                MinecraftModIntegrationPlan finalIntegrationPlan = integrationPlan;
                measure(
                    diagnosticSink,
                    "minecraft.mod_integration.write",
                    LaunchPhase.COMPLETE,
                    () -> {
                        Path outputPath = context.workingDirectory().resolve("minecraft-mod-integration-plan.json");
                        new MinecraftModIntegrationPlanWriter().write(outputPath, finalIntegrationPlan);
                        return outputPath;
                    },
                    outputPath -> details("modIntegrationOutputPath", displayPath(context, outputPath))
                );
                megaMilestoneReports.add("minecraft-mod-integration-plan.json");
                if (config.explainMods()) {
                    printMinecraftModsExplain(finalIntegrationPlan);
                }
            }

            if (config.preflight() && runtimeBoundary != null) {
                List<com.mcmodloader.core.minecraft.MinecraftBoundaryViolation> issues = new ArrayList<>(runtimeBoundary.violations());
                if (integrationPlan != null) {
                    issues.addAll(integrationPlan.issues());
                }
                List<MinecraftModRejection> rejectedMods = integrationPlan == null ? List.of() : integrationPlan.rejectedMods();
                int warningCount =
                    (int)
                        issues
                            .stream()
                            .filter(issue -> issue.severity() == com.mcmodloader.core.minecraft.MinecraftBoundarySeverity.WARNING)
                            .count();
                int fatalCount =
                    (int)
                        issues
                            .stream()
                            .filter(issue -> issue.fatalNow() || issue.severity() == com.mcmodloader.core.minecraft.MinecraftBoundarySeverity.FATAL)
                            .count();
                List<String> failureReasons = new ArrayList<>();
                for (com.mcmodloader.core.minecraft.MinecraftBoundaryViolation issue : issues) {
                    if (issue.fatalNow() || issue.severity() == com.mcmodloader.core.minecraft.MinecraftBoundarySeverity.FATAL) {
                        failureReasons.add(issue.type() + ": " + issue.reason());
                    }
                }
                for (MinecraftModRejection rejection : rejectedMods) {
                    failureReasons.add("rejected-mod " + rejection.candidate() + ": " + rejection.reason());
                }
                boolean preflightSucceeded = failureReasons.isEmpty();
                MinecraftPreflightResult preflightResult =
                    new MinecraftPreflightResult(
                        1,
                        "Mega-Milestone 7",
                        metadata.id(),
                        true,
                        runtimeBoundary != null,
                        integrationPlan != null,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        List.copyOf(megaMilestoneReports),
                        rejectedMods,
                        issues,
                        integrationPlan == null ? 0 : integrationPlan.acceptedMods().size(),
                        rejectedMods.size(),
                        warningCount,
                        fatalCount,
                        failureReasons,
                        preflightSucceeded
                    );
                measure(
                    diagnosticSink,
                    "minecraft.preflight.write",
                    LaunchPhase.COMPLETE,
                    () -> {
                        Path outputPath = context.workingDirectory().resolve("minecraft-preflight-result.json");
                        new MinecraftPreflightResultWriter().write(outputPath, preflightResult);
                        return outputPath;
                    },
                    outputPath -> details("preflightOutputPath", displayPath(context, outputPath))
                );
                megaMilestoneReports.add("minecraft-preflight-result.json");
                if (!preflightSucceeded) {
                    throw new LoaderException("Minecraft preflight failed. See minecraft-preflight-result.json for failure reasons.");
                }
            }

            if (config.reproducibilityCheck()) {
                MinecraftReproducibilityCheck check =
                    createReproducibilityCheck(
                        context,
                        config,
                        artifactCache,
                        artifactResolution,
                        parsedMods,
                        resolvedMods,
                        metadata.id(),
                        finalPlannedRuntime.plan(),
                        runtimeBoundary,
                        integrationPlan,
                        megaMilestoneReports
                    );
                measure(
                    diagnosticSink,
                    "minecraft.reproducibility.write",
                    LaunchPhase.COMPLETE,
                    () -> {
                        Path outputPath = context.workingDirectory().resolve("minecraft-reproducibility-check.json");
                        new MinecraftReproducibilityCheckWriter().write(outputPath, check);
                        return outputPath;
                    },
                    outputPath -> details("reproducibilityOutputPath", displayPath(context, outputPath))
                );
                if (!check.byteForByteEqual() || !check.failures().isEmpty()) {
                    throw new LoaderException("Minecraft reproducibility check failed. See minecraft-reproducibility-check.json for details.");
                }
            }
        }

        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.dry_run.complete",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Minecraft dry run complete",
                details(
                    "minecraftVersion",
                    metadata.id(),
                    "minecraftSide",
                    config.side().id(),
                    "selectedLibraryCount",
                    Integer.toString(selection.libraries().size()),
                    "nativeLibraryCount",
                    Integer.toString(selection.nativeLibraries().size()),
                    "missingFileCount",
                    Integer.toString(launchPlan.missingFiles().size()),
                    "launchPlanOutputPath",
                    displayPath(context, config.outputPlanPath()),
                    "versionJsonPath",
                    displayPath(context, resolvedVersionJson.versionJsonPath()),
                    "metadataSource",
                    resolvedVersionJson.metadataSource(),
                    "serverJarSource",
                    artifactResolution.serverJarSource(),
                    "macheReferenceScan",
                    Boolean.toString(launchArguments.macheReferenceScan() && launchArguments.macheDirectory() != null),
                    "macheReportOutputPath",
                    launchArguments.macheReferenceScan() && launchArguments.macheDirectory() != null
                        ? displayPath(context, context.workingDirectory().resolve("mache-reference-report.json"))
                        : null
                )
            )
        );

        return new MinecraftDryRunResult(
            launchPlan,
            macheReferenceReport,
            artifactResolution.serverJarPath(),
            artifactResolution.serverJarSource(),
            artifactResolution,
            plannedRuntime,
            runtimeBoundary,
            integrationPlan
        );
    }

    private static LaunchContext createLaunchContext(Path workingDirectory, LaunchArguments launchArguments) {
        String targetMinecraftVersion = TARGET_MINECRAFT_VERSION;
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
            LOADER_VERSION,
            Runtime.version().feature(),
            targetMinecraftVersion
        );
    }

    private static LaunchArguments resolveLaunchArguments(Path workingDirectory, LaunchArguments launchArguments) throws LoaderException {
        MinecraftProviderConfig resolvedMinecraftProviderConfig = launchArguments.minecraftProviderConfig().resolveAgainst(workingDirectory);
        if (!"minecraft".equals(launchArguments.gameProviderId()) && resolvedMinecraftProviderConfig.realSmoke()) {
            throw new LoaderException("--minecraft-real-smoke requires --game-provider minecraft");
        }
        if ("minecraft".equals(launchArguments.gameProviderId())
            && resolvedMinecraftProviderConfig.minecraftDirectory() == null
            && resolvedMinecraftProviderConfig.side() == MinecraftSide.CLIENT
            && !resolvedMinecraftProviderConfig.cacheInspect()) {
            resolvedMinecraftProviderConfig =
                resolvedMinecraftProviderConfig.withMinecraftDirectory(new MinecraftInstallLocator().defaultMinecraftDirectory().orElse(null))
                    .resolveAgainst(workingDirectory);
        }

        if ("minecraft".equals(launchArguments.gameProviderId()) && !resolvedMinecraftProviderConfig.dryRun()) {
            throw new LoaderException("Minecraft provider requires --minecraft-dry-run until managed Minecraft runtime ownership is explicitly requested.");
        }

        String resolvedVersion = resolvedMinecraftProviderConfig.requestedVersion();
        if ("minecraft".equals(launchArguments.gameProviderId())
            && (resolvedVersion == null || resolvedVersion.isBlank())
            && resolvedMinecraftProviderConfig.explicitVersionJson() != null) {
            String json;
            try {
                json = java.nio.file.Files.readString(resolvedMinecraftProviderConfig.explicitVersionJson());
            } catch (IOException exception) {
                throw new LoaderException(
                    "Failed to read Minecraft version JSON " + resolvedMinecraftProviderConfig.explicitVersionJson(),
                    exception
                );
            }
            resolvedVersion =
                new MinecraftVersionMetadataParser()
                    .parse(json, resolvedMinecraftProviderConfig.explicitVersionJson().toString(), resolvedMinecraftProviderConfig.side())
                    .id();
            resolvedMinecraftProviderConfig = resolvedMinecraftProviderConfig.withRequestedVersion(resolvedVersion);
        }

        if ("minecraft".equals(launchArguments.gameProviderId())
            && resolvedMinecraftProviderConfig.baselineServerEnabled()
            && (resolvedMinecraftProviderConfig.baselineVersion() == null || resolvedMinecraftProviderConfig.baselineVersion().isBlank())) {
            resolvedMinecraftProviderConfig = resolvedMinecraftProviderConfig.withBaselineVersion("latest-release");
        }

        if ("minecraft".equals(launchArguments.gameProviderId())
            && !resolvedMinecraftProviderConfig.baselineServerEnabled()
            && (resolvedMinecraftProviderConfig.requestedVersion() == null || resolvedMinecraftProviderConfig.requestedVersion().isBlank())) {
            throw new LoaderException("Minecraft provider requires --minecraft-version unless --minecraft-version-json contains an id");
        }

        if ("minecraft".equals(launchArguments.gameProviderId()) && resolvedMinecraftProviderConfig.realSmoke()) {
            if (resolvedMinecraftProviderConfig.side() != MinecraftSide.SERVER) {
                throw new LoaderException("--minecraft-real-smoke requires --minecraft-side server");
            }
            if (!resolvedMinecraftProviderConfig.dryRun()) {
                throw new LoaderException("--minecraft-real-smoke requires --minecraft-dry-run");
            }
        }

        if (resolvedMinecraftProviderConfig.launch()) {
            if (!"minecraft".equals(launchArguments.gameProviderId())) {
                throw new LoaderException("--minecraft-launch requires --game-provider minecraft");
            }
            if (resolvedMinecraftProviderConfig.cacheInspect()) {
                throw new LoaderException("--minecraft-cache-inspect cannot be combined with --minecraft-launch");
            }
            if (resolvedMinecraftProviderConfig.side() != MinecraftSide.SERVER) {
                throw new LoaderException("--minecraft-launch requires --minecraft-side server");
            }
            if (!resolvedMinecraftProviderConfig.dryRun()) {
                throw new LoaderException("--minecraft-launch requires --minecraft-dry-run");
            }
            if (!resolvedMinecraftProviderConfig.verifyFiles()) {
                throw new LoaderException("--minecraft-launch requires --minecraft-verify-files");
            }

            Path serverDirectory = resolvedMinecraftProviderConfig.serverDirectory();
            if (serverDirectory == null) {
                String serverVersionDirectory =
                    resolvedMinecraftProviderConfig.baselineServerEnabled()
                        ? resolvedMinecraftProviderConfig.requestedVersionOrBaseline()
                        : resolvedMinecraftProviderConfig.requestedVersion();
                Path defaultRoot =
                    resolvedMinecraftProviderConfig.baselineServerEnabled()
                        ? workingDirectory.resolve("minecraft-server-baseline")
                        : workingDirectory.resolve("minecraft-server");
                serverDirectory = defaultRoot.resolve(serverVersionDirectory);
            }
            serverDirectory = serverDirectory.toAbsolutePath().normalize();
            if (serverDirectory.equals(workingDirectory.toAbsolutePath().normalize())) {
                throw new LoaderException("Minecraft server directory must not be the loader working directory root");
            }
            resolvedMinecraftProviderConfig = resolvedMinecraftProviderConfig.withServerDirectory(serverDirectory);
        }

        if ("minecraft".equals(launchArguments.gameProviderId())) {
            if (resolvedMinecraftProviderConfig.offlineReplay() && !resolvedMinecraftProviderConfig.baselineServerEnabled()) {
                throw new LoaderException("--minecraft-offline-replay requires --minecraft-baseline-server");
            }
            if (resolvedMinecraftProviderConfig.offlineReplay() && !resolvedMinecraftProviderConfig.offline()) {
                throw new LoaderException("--minecraft-offline-replay requires --minecraft-offline");
            }
            if (resolvedMinecraftProviderConfig.requireReady() && !resolvedMinecraftProviderConfig.launch()) {
                throw new LoaderException("--minecraft-require-ready requires --minecraft-launch");
            }
            if (resolvedMinecraftProviderConfig.baselineServerEnabled() && resolvedMinecraftProviderConfig.side() != MinecraftSide.SERVER) {
                throw new LoaderException("--minecraft-baseline-server requires --minecraft-side server");
            }
            if (resolvedMinecraftProviderConfig.baselineServerEnabled() && resolvedMinecraftProviderConfig.cacheInspect()) {
                throw new LoaderException("--minecraft-baseline-server cannot be combined with --minecraft-cache-inspect");
            }
            if (resolvedMinecraftProviderConfig.offline() &&
                (resolvedMinecraftProviderConfig.fetchMetadata() || resolvedMinecraftProviderConfig.downloadServer() || resolvedMinecraftProviderConfig.cacheRepair())) {
                throw new LoaderException(
                    "--minecraft-offline cannot be combined with --minecraft-fetch-metadata, --minecraft-download-server, or --minecraft-cache-repair"
                );
            }
            if (resolvedMinecraftProviderConfig.forceRedownload() &&
                !resolvedMinecraftProviderConfig.fetchMetadata() &&
                !resolvedMinecraftProviderConfig.downloadServer() &&
                !resolvedMinecraftProviderConfig.cacheRepair()) {
                throw new LoaderException(
                    "--minecraft-force-redownload requires --minecraft-fetch-metadata, --minecraft-download-server, or --minecraft-cache-repair"
                );
            }
        }

        return launchArguments.withMinecraftProviderConfig(resolvedMinecraftProviderConfig).withMacheDirectory(
            resolveOptionalPath(workingDirectory, launchArguments.macheDirectory())
        );
    }

    private static MinecraftProcessResult launchMinecraftServer(
        LaunchContext context,
        MinecraftProviderConfig config,
        MinecraftDryRunResult dryRunResult,
        DiagnosticSink diagnosticSink
    ) throws LoaderException {
        Path serverJarPath = dryRunResult.serverJarPath();
        if (serverJarPath == null) {
            throw new LoaderException("Minecraft server launch requires a resolved server jar");
        }

        Path javaExecutable = new JavaExecutableResolver().resolve();
        MinecraftServerLaunchCommand launchCommand =
            dryRunResult.plannedRuntime() == null
                ? MinecraftServerLaunchCommand.simpleJar(javaExecutable, serverJarPath, config.serverJvmArgs(), config.serverArgs(), path -> displayPath(context, path))
                : dryRunResult.plannedRuntime().command();
        Path resultOutputPath = context.workingDirectory().resolve("minecraft-server-launch-result.json").toAbsolutePath().normalize();
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.server_launch.preflight",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Minecraft server launch preflight complete",
                minecraftServerLaunchDetails(context, config, serverJarPath, javaExecutable, null, resultOutputPath)
            )
        );
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.server_launch.start",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Minecraft server launch starting",
                minecraftServerLaunchDetails(context, config, serverJarPath, javaExecutable, null, resultOutputPath)
            )
        );

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
                        config.acceptEulaForTest()
                    ),
                    launchCommand,
                    path -> displayPath(context, path)
                );

        if (result.readyDetected()) {
            diagnosticSink.record(
                new DiagnosticEvent(
                "minecraft.server_launch.ready",
                    LaunchPhase.COMPLETE.name(),
                    0L,
                    "ok",
                    "Minecraft server readiness detected",
                    minecraftServerLaunchDetails(context, config, serverJarPath, javaExecutable, result, resultOutputPath)
                )
            );
        }
        if (result.stopRequested()) {
            diagnosticSink.record(
                new DiagnosticEvent(
                    "minecraft.server_launch.stop_request",
                    LaunchPhase.COMPLETE.name(),
                    0L,
                    "ok",
                    "Minecraft server stop requested",
                    minecraftServerLaunchDetails(context, config, serverJarPath, javaExecutable, result, resultOutputPath)
                )
            );
        }
        if (result.timedOut()) {
            diagnosticSink.record(
                new DiagnosticEvent(
                    "minecraft.server_launch.timeout",
                    LaunchPhase.COMPLETE.name(),
                    0L,
                    "ok",
                    "Minecraft server launch timed out",
                    minecraftServerLaunchDetails(context, config, serverJarPath, javaExecutable, result, resultOutputPath)
                )
            );
        }

        new MinecraftProcessResultWriter().write(resultOutputPath, result);
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.server_launch_result.write",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Minecraft server launch result written",
                minecraftServerLaunchDetails(context, config, serverJarPath, javaExecutable, result, resultOutputPath)
            )
        );
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.server_launch.complete",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Minecraft server launch complete",
                minecraftServerLaunchDetails(context, config, serverJarPath, javaExecutable, result, resultOutputPath)
            )
        );
        return result;
    }

    private static List<ModCandidate> parseMetadata(List<ModCandidate> discoveredMods, ModMetadataParser metadataParser)
        throws LoaderException {
        List<ModCandidate> parsedMods = new ArrayList<>(discoveredMods.size());
        for (ModCandidate candidate : discoveredMods) {
            parsedMods.add(candidate.withMetadata(metadataParser.parse(candidate)));
        }
        return List.copyOf(parsedMods);
    }

    private static MinecraftServerBaselineResult completeMinecraftBaseline(
        LaunchContext context,
        MinecraftProviderConfig config,
        MinecraftDryRunResult dryRunResult,
        DiagnosticSink diagnosticSink
    ) throws LoaderException {
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
                baselineDetails(context, config, artifactResolution, null)
            )
        );
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.version_select",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Minecraft version selection resolved",
                baselineDetails(context, config, artifactResolution, null)
            )
        );
        diagnosticSink.record(
            new DiagnosticEvent(
                config.offlineReplay() ? "minecraft.baseline.offline_replay" : "minecraft.baseline.artifacts.resolve",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                config.offlineReplay() ? "Minecraft baseline offline replay using cached artifacts" : "Minecraft baseline artifacts resolved",
                baselineDetails(context, config, artifactResolution, null)
            )
        );

        MinecraftProviderConfig launchConfig = config;
        if (config.serverDirectory() == null || serverDirectoryUsesRequestedSelector(context, config)) {
            launchConfig =
                config.withServerDirectory(
                    context.workingDirectory()
                        .resolve("minecraft-server-baseline")
                        .resolve(artifactResolution.metadata().id())
                        .toAbsolutePath()
                        .normalize()
                );
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
                    baselineDetails(context, launchConfig, artifactResolution, null)
                )
            );
            processResult = launchMinecraftServer(context, launchConfig, dryRunResult, diagnosticSink);
            diagnosticSink.record(
                new DiagnosticEvent(
                    "minecraft.baseline.launch.complete",
                    LaunchPhase.COMPLETE.name(),
                    0L,
                    "ok",
                    "Minecraft baseline launch complete",
                    baselineDetails(context, launchConfig, artifactResolution, processResult)
                )
            );
        }

        String manifestSha256 = artifactResolution.manifestRecord() == null ? null : artifactResolution.manifestRecord().sha256();
        String manifestPath =
            artifactResolution.manifestRecord() == null || !artifactResolution.manifestRecord().present()
                ? null
                : displayPath(context, artifactResolution.manifestRecord().path());
        String versionJsonSha256 = artifactResolution.versionRecord() == null ? null : artifactResolution.versionRecord().sha256();
        String versionJsonPath =
            artifactResolution.versionRecord() == null || !artifactResolution.versionRecord().present()
                ? null
                : displayPath(context, artifactResolution.versionRecord().path());
        String launchResultPath =
            processResult == null ? null : displayPath(context, context.workingDirectory().resolve("minecraft-server-launch-result.json"));
        boolean offlineReplaySucceeded =
            launchConfig.offlineReplay() && (processResult == null || !launchConfig.requireReady() || processResult.readyDetected());

        MinecraftServerBaseline baseline =
            new MinecraftServerBaseline(
                1,
                TARGET_MINECRAFT_VERSION,
                artifactResolution.metadata().id(),
                versionSelection,
                new MinecraftServerBaseline.Metadata(manifestPath, versionJsonPath, manifestSha256, versionJsonSha256),
                new MinecraftServerBaseline.ServerArtifact(
                    artifactResolution.serverRecord() == null ? null : displayPath(context, artifactResolution.serverRecord().path()),
                    artifactResolution.serverRecord() == null ? null : artifactResolution.serverRecord().sourceUrl(),
                    artifactResolution.serverRecord() == null ? null : artifactResolution.serverRecord().sha1(),
                    artifactResolution.serverRecord() == null ? null : artifactResolution.serverRecord().sha256(),
                    artifactResolution.serverRecord() == null ? null : artifactResolution.serverRecord().size(),
                    artifactResolution.serverRecord() != null && artifactResolution.serverRecord().verified()
                ),
                new MinecraftServerBaseline.Launch(
                    launchConfig.launch(),
                    launchResultPath,
                    processResult != null && processResult.started(),
                    processResult == null ? null : processResult.readyDetected(),
                    processResult == null ? null : processResult.exitCode(),
                    processResult != null && processResult.timedOut()
                ),
                new MinecraftServerBaseline.OfflineReplay(
                    launchConfig.offlineReplay(),
                    launchResultPath,
                    offlineReplaySucceeded,
                    artifactResolution.networkRequestCount()
                ),
                new MinecraftServerBaseline.ModIntegration(false, false, false)
            );

        new MinecraftServerBaselineWriter().write(launchConfig.baselineReportPath(), baseline);
        diagnosticSink.record(
            new DiagnosticEvent(
                "minecraft.baseline.write",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Minecraft baseline report written",
                baselineDetails(context, launchConfig, artifactResolution, processResult)
            )
        );

        if (launchConfig.requireReady() && (processResult == null || !processResult.readyDetected())) {
            throw new LoaderException("Minecraft baseline launch did not detect a ready line while --minecraft-require-ready was set.");
        }

        return new MinecraftServerBaselineResult(
            launchConfig.offlineReplay() ? MinecraftServerBaselineMode.OFFLINE_REPLAY : MinecraftServerBaselineMode.ACQUIRE,
            baseline,
            artifactResolution,
            dryRunResult,
            processResult,
            launchConfig.baselineReportPath()
        );
    }

    private static String verifyOrWriteLockfile(
        Path lockfilePath,
        LaunchContext context,
        ResolvedModSet resolvedMods,
        LockfileWriter lockfileWriter,
        LockfileVerifier lockfileVerifier
    ) throws LoaderException {
        if (lockfileVerifier.exists(lockfilePath)) {
            lockfileVerifier.verify(lockfilePath, context, resolvedMods);
            return "verified";
        }

        lockfileWriter.write(lockfilePath, context, resolvedMods);
        return "wrote";
    }

    private static void recordResourceDiagnostics(DiagnosticSink diagnosticSink, ResourceConflictIndex resourceConflictIndex) {
        for (ResourceConflict conflict : resourceConflictIndex.conflicts()) {
            diagnosticSink.record(
                new DiagnosticEvent(
                    "resource.duplicate",
                    LaunchPhase.CLASSPATH_PLAN.name(),
                    0L,
                    "ok",
                    "Duplicate resource detected",
                    details("resource", conflict.resourcePath(), "mods", String.join(",", conflict.modIds()))
                )
            );
        }
    }

    private static void recordSplitPackageDiagnostics(DiagnosticSink diagnosticSink, PackageOwnershipIndex packageOwnershipIndex) {
        for (PackageOwnershipIndex.SplitPackage splitPackage : packageOwnershipIndex.splitPackages()) {
            diagnosticSink.record(
                new DiagnosticEvent(
                    "package.split",
                    LaunchPhase.CLASSPATH_PLAN.name(),
                    0L,
                    "ok",
                    "Split package detected",
                    details("package", splitPackage.packageName(), "mods", String.join(",", splitPackage.modIds()))
                )
            );
        }
    }

    private static void enforceStrictResources(LaunchContext context, ResourceConflictIndex resourceConflictIndex) throws LoaderException {
        if (!context.strictResources() || resourceConflictIndex.conflicts().isEmpty()) {
            return;
        }
        ResourceConflict conflict = resourceConflictIndex.conflicts().getFirst();
        throw new LoaderException(
            "Duplicate resource " + conflict.resourcePath() + " found in mods " + String.join(",", conflict.modIds())
        );
    }

    private static void enforceStrictPackages(LaunchContext context, PackageOwnershipIndex packageOwnershipIndex) throws LoaderException {
        if (!context.strictPackages() || packageOwnershipIndex.splitPackages().isEmpty()) {
            return;
        }
        PackageOwnershipIndex.SplitPackage splitPackage = packageOwnershipIndex.splitPackages().getFirst();
        throw new LoaderException(
            "Split package " + splitPackage.packageName() + " found in mods " + String.join(",", splitPackage.modIds())
        );
    }

    private static void printExplain(LaunchContext context, FrozenModGraph frozenModGraph, int discoveredModCount, String lockfileAction) {
        System.out.println("[loader] explain: provider " + frozenModGraph.gameProviderId() + " " + frozenModGraph.gameProviderVersion());
        System.out.println("[loader] explain: discovered " + discoveredModCount + " " + pluralize(discoveredModCount));
        System.out.println("[loader] explain: resolved " + frozenModGraph.mods().size() + " " + pluralize(frozenModGraph.mods().size()));
        System.out.println("[loader] explain: lockfile " + lockfileAction);
        System.out.println("[loader] explain: duplicate resources " + frozenModGraph.resourceConflicts().size());
        System.out.println("[loader] explain: split packages " + frozenModGraph.packageOwnershipIndex().splitPackages().size());
        System.out.println("[loader] explain: dependency graph " + displayPath(context, context.workingDirectory().resolve("dependency-graph.json")));
        System.out.println("[loader] explain: modpack state " + displayPath(context, context.workingDirectory().resolve("modpack-state.json")));
    }

    private static void printMinecraftRuntimeExplain(MinecraftServerRuntimePlan plan) {
        System.out.println("[loader] explain-runtime: Mega-Milestone 7 runtime plan " + plan.resolvedMinecraftVersion());
        System.out.println("[loader] explain-runtime: selector " + plan.selectorUsed() + " via " + plan.selectorResolutionReason());
        System.out.println("[loader] explain-runtime: server artifact " + plan.serverJarSource() + " " + plan.serverJarPath());
        System.out.println("[loader] explain-runtime: launch mode " + plan.launchMode() + " because " + plan.launchModeReason());
        System.out.println("[loader] explain-runtime: classpath entries " + plan.classpathEntries().size());
        System.out.println("[loader] explain-runtime: wrote minecraft-server-runtime-plan.json");
    }

    private static void printMinecraftBoundaryExplain(MinecraftRuntimeBoundary boundary) {
        System.out.println("[loader] explain-boundary: Mega-Milestone 7 boundary is analysis-only");
        System.out.println("[loader] explain-boundary: packages " + boundary.packageOwnership().size());
        System.out.println("[loader] explain-boundary: resources " + boundary.resourceOwnership().size());
        System.out.println("[loader] explain-boundary: services " + boundary.serviceProviderOwnership().size());
        System.out.println("[loader] explain-boundary: violations " + boundary.violations().size());
        System.out.println("[loader] explain-boundary: wrote minecraft-runtime-boundary.json");
    }

    private static void printMinecraftModsExplain(MinecraftModIntegrationPlan plan) {
        System.out.println("[loader] explain-mods: Mega-Milestone 7 integration plan is analysis-only");
        System.out.println("[loader] explain-mods: discovered " + plan.discoveredModCandidates().size() + " candidates");
        System.out.println("[loader] explain-mods: accepted " + plan.acceptedMods().size() + " mods");
        System.out.println("[loader] explain-mods: rejected " + plan.rejectedMods().size() + " mods");
        System.out.println("[loader] explain-mods: future classpath entries " + plan.modClasspathPlan().plannedFutureModClasspathEntries().size());
        System.out.println("[loader] explain-mods: wrote minecraft-mod-integration-plan.json");
    }

    private static void writeStartupProfile(
        LaunchContext context,
        DiagnosticSink diagnosticSink,
        StartupProfileWriter startupProfileWriter
    ) throws LoaderException {
        if (!(diagnosticSink instanceof JsonDiagnosticSink jsonDiagnosticSink)) {
            return;
        }

        Path startupProfilePath = context.workingDirectory().resolve("diagnostics/startup-profile.json");
        StartupProfile profile = StartupProfile.from(jsonDiagnosticSink.events());
        startupProfileWriter.write(startupProfilePath, profile);
        diagnosticSink.record(
            new DiagnosticEvent(
                "startup_profile.write",
                LaunchPhase.COMPLETE.name(),
                0L,
                "ok",
                "Startup profile written",
                details("startupProfileOutputPath", displayPath(context, startupProfilePath))
            )
        );
    }

    private static Map<String, String> baselineDetails(
        LaunchContext context,
        MinecraftProviderConfig config,
        MinecraftArtifactResolver.Resolution artifactResolution,
        MinecraftProcessResult processResult
    ) {
        return details(
            "projectTargetMinecraft",
            TARGET_MINECRAFT_VERSION,
            "requestedBaselineVersion",
            config.requestedVersionOrBaseline(),
            "resolvedBaselineVersion",
            artifactResolution == null || artifactResolution.versionSelection() == null ? null : artifactResolution.versionSelection().resolved(),
            "versionSelectionSource",
            artifactResolution == null || artifactResolution.versionSelection() == null ? null : artifactResolution.versionSelection().source(),
            "cacheDirectory",
            config.cacheDirectory() == null ? null : displayPath(context, config.cacheDirectory()),
            "serverJar",
            artifactResolution == null || artifactResolution.serverJarPath() == null ? null : displayPath(context, artifactResolution.serverJarPath()),
            "serverJarSource",
            artifactResolution == null ? null : artifactResolution.serverJarSource(),
            "networkRequests",
            artifactResolution == null ? null : Integer.toString(artifactResolution.networkRequestCount()),
            "offline",
            Boolean.toString(config.offline()),
            "launchAttempted",
            Boolean.toString(config.launch()),
            "readyDetected",
            processResult == null ? null : Boolean.toString(processResult.readyDetected()),
            "requireReady",
            Boolean.toString(config.requireReady()),
            "baselineReportPath",
            displayPath(context, config.baselineReportPath())
        );
    }

    private static boolean serverDirectoryUsesRequestedSelector(LaunchContext context, MinecraftProviderConfig config) {
        if (config.serverDirectory() == null) {
            return false;
        }
        String requested = config.requestedVersionOrBaseline();
        if (requested == null || requested.isBlank()) {
            return false;
        }
        Path expected =
            context.workingDirectory()
                .resolve("minecraft-server-baseline")
                .resolve(requested)
                .toAbsolutePath()
                .normalize();
        return expected.equals(config.serverDirectory());
    }

    private static String displayPath(LaunchContext context, Path path) {
        Path normalizedWorkingDirectory = context.workingDirectory().toAbsolutePath().normalize();
        Path normalizedPath = path.toAbsolutePath().normalize();
        try {
            return normalizedWorkingDirectory.relativize(normalizedPath).toString().replace('\\', '/');
        } catch (IllegalArgumentException exception) {
            return normalizedPath.toString().replace('\\', '/');
        }
    }

    private static List<Path> runtimeJarsForPlan(
        LaunchContext context,
        Path serverJarPath,
        MinecraftServerRuntimePlan runtimePlan
    ) {
        List<Path> runtimeJars = new ArrayList<>();
        runtimeJars.add(serverJarPath);
        for (MinecraftServerRuntimeClasspath.Entry entry : runtimePlan.classpathEntries()) {
            Path path = Path.of(entry.path());
            runtimeJars.add(path.isAbsolute() ? path : context.workingDirectory().resolve(path));
        }
        return runtimeJars;
    }

    private static MinecraftReproducibilityCheck createReproducibilityCheck(
        LaunchContext context,
        MinecraftProviderConfig config,
        MinecraftArtifactCache artifactCache,
        MinecraftArtifactResolver.Resolution artifactResolution,
        List<ModCandidate> parsedMods,
        ResolvedModSet resolvedMods,
        String resolvedMinecraftVersion,
        MinecraftServerRuntimePlan firstRuntimePlan,
        MinecraftRuntimeBoundary firstRuntimeBoundary,
        MinecraftModIntegrationPlan firstIntegrationPlan,
        List<String> reportsWritten
    ) throws LoaderException {
        Path snapshotDirectory = context.workingDirectory().resolve(".minecraft-reproducibility").resolve("second-run");
        try {
            Files.createDirectories(snapshotDirectory);
        } catch (IOException exception) {
            throw new LoaderException("Failed to create reproducibility snapshot directory", exception);
        }

        MinecraftServerRuntimePlanner.PlannedRuntime secondRuntime =
            new MinecraftServerRuntimePlanner().plan(
                context.workingDirectory(),
                config,
                artifactCache,
                artifactResolution,
                path -> displayPath(context, path)
            );
        Path secondRuntimePlanPath = snapshotDirectory.resolve("minecraft-server-runtime-plan.json");
        new MinecraftServerRuntimePlanWriter().write(secondRuntimePlanPath, secondRuntime.plan());

        MinecraftRuntimeBoundary secondBoundary = null;
        Path secondBoundaryPath = snapshotDirectory.resolve("minecraft-runtime-boundary.json");
        if (firstRuntimeBoundary != null) {
            secondBoundary =
                new MinecraftRuntimeBoundaryBuilder()
                    .build(
                        secondRuntime.plan(),
                        runtimeJarsForPlan(context, artifactResolution.serverJarPath(), secondRuntime.plan()),
                        path -> displayPath(context, path),
                        config.strictBoundary(),
                        config.strictRuntimeConflicts()
                    );
            new MinecraftRuntimeBoundaryWriter().write(secondBoundaryPath, secondBoundary);
        }

        MinecraftModIntegrationPlan secondIntegrationPlan = null;
        Path secondIntegrationPlanPath = snapshotDirectory.resolve("minecraft-mod-integration-plan.json");
        if (firstIntegrationPlan != null && secondBoundary != null) {
            secondIntegrationPlan =
                new MinecraftModIntegrationPlanner()
                    .plan(
                        context,
                        parsedMods,
                        resolvedMods,
                        secondBoundary,
                        resolvedMinecraftVersion,
                        config.strictSide(),
                        config.strictClassVersions(),
                        config.strictRuntimeConflicts(),
                        path -> displayPath(context, path)
                    );
            new MinecraftModIntegrationPlanWriter().write(secondIntegrationPlanPath, secondIntegrationPlan);
        }

        Path secondPreflightPath = snapshotDirectory.resolve("minecraft-preflight-result.json");
        if (config.preflight() && secondBoundary != null) {
            MinecraftPreflightResult secondPreflight = buildPreflightResult(resolvedMinecraftVersion, reportsWritten, secondBoundary, secondIntegrationPlan);
            new MinecraftPreflightResultWriter().write(secondPreflightPath, secondPreflight);
        }

        List<MinecraftReproducibilityChecker.ReportPair> pairs = new ArrayList<>();
        pairs.add(
            new MinecraftReproducibilityChecker.ReportPair(
                "minecraft-server-runtime-plan.json",
                context.workingDirectory().resolve("minecraft-server-runtime-plan.json"),
                secondRuntimePlanPath
            )
        );
        if (firstRuntimeBoundary != null) {
            pairs.add(
                new MinecraftReproducibilityChecker.ReportPair(
                    "minecraft-runtime-boundary.json",
                    context.workingDirectory().resolve("minecraft-runtime-boundary.json"),
                    secondBoundaryPath
                )
            );
        }
        if (firstIntegrationPlan != null) {
            pairs.add(
                new MinecraftReproducibilityChecker.ReportPair(
                    "minecraft-mod-integration-plan.json",
                    context.workingDirectory().resolve("minecraft-mod-integration-plan.json"),
                    secondIntegrationPlanPath
                )
            );
        }
        if (config.preflight() && Files.isRegularFile(context.workingDirectory().resolve("minecraft-preflight-result.json"))) {
            pairs.add(
                new MinecraftReproducibilityChecker.ReportPair(
                    "minecraft-preflight-result.json",
                    context.workingDirectory().resolve("minecraft-preflight-result.json"),
                    secondPreflightPath
                )
            );
        }

        return new MinecraftReproducibilityChecker().check(
            "Mega-Milestone 7",
            pairs,
            config.offline() && artifactResolution.networkRequestCount() > 0
        );
    }

    private static MinecraftPreflightResult buildPreflightResult(
        String minecraftVersion,
        List<String> reportsWritten,
        MinecraftRuntimeBoundary runtimeBoundary,
        MinecraftModIntegrationPlan integrationPlan
    ) {
        List<com.mcmodloader.core.minecraft.MinecraftBoundaryViolation> issues = new ArrayList<>(runtimeBoundary.violations());
        if (integrationPlan != null) {
            issues.addAll(integrationPlan.issues());
        }
        List<MinecraftModRejection> rejectedMods = integrationPlan == null ? List.of() : integrationPlan.rejectedMods();
        int warningCount =
            (int)
                issues
                    .stream()
                    .filter(issue -> issue.severity() == com.mcmodloader.core.minecraft.MinecraftBoundarySeverity.WARNING)
                    .count();
        int fatalCount =
            (int)
                issues
                    .stream()
                    .filter(issue -> issue.fatalNow() || issue.severity() == com.mcmodloader.core.minecraft.MinecraftBoundarySeverity.FATAL)
                    .count();
        List<String> failureReasons = new ArrayList<>();
        for (com.mcmodloader.core.minecraft.MinecraftBoundaryViolation issue : issues) {
            if (issue.fatalNow() || issue.severity() == com.mcmodloader.core.minecraft.MinecraftBoundarySeverity.FATAL) {
                failureReasons.add(issue.type() + ": " + issue.reason());
            }
        }
        for (MinecraftModRejection rejection : rejectedMods) {
            failureReasons.add("rejected-mod " + rejection.candidate() + ": " + rejection.reason());
        }
        boolean preflightSucceeded = failureReasons.isEmpty();
        return new MinecraftPreflightResult(
            1,
            "Mega-Milestone 7",
            minecraftVersion,
            true,
            runtimeBoundary != null,
            integrationPlan != null,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            List.copyOf(reportsWritten),
            rejectedMods,
            issues,
            integrationPlan == null ? 0 : integrationPlan.acceptedMods().size(),
            rejectedMods.size(),
            warningCount,
            fatalCount,
            failureReasons,
            preflightSucceeded
        );
    }

    private static String pluralize(int count) {
        return count == 1 ? "mod" : "mods";
    }

    private static String joinOwners(List<EntrypointInvoker.EntrypointInvocation> invocations) {
        return invocations.stream().map(EntrypointInvoker.EntrypointInvocation::ownerModId).distinct().sorted().reduce((left, right) -> left + "," + right).orElse("");
    }

    private static String joinEntrypoints(List<EntrypointInvoker.EntrypointInvocation> invocations) {
        return invocations
            .stream()
            .map(EntrypointInvoker.EntrypointInvocation::entrypointClassName)
            .sorted()
            .reduce((left, right) -> left + "," + right)
            .orElse("");
    }

    private static Map<String, String> details(String... values) {
        Map<String, String> details = new LinkedHashMap<>();
        for (int index = 0; index + 1 < values.length; index += 2) {
            String value = values[index + 1];
            if (value != null && !value.isBlank()) {
                details.put(values[index], value);
            }
        }
        return details;
    }

    private static <T> T measure(
        DiagnosticSink sink,
        String name,
        LaunchPhase phase,
        ThrowingSupplier<T> supplier,
        Function<T, Map<String, String>> detailsFactory
    ) throws LoaderException {
        long start = System.nanoTime();
        try {
            T result = supplier.get();
            sink.record(new DiagnosticEvent(name, phase.name(), elapsedMillis(start), "ok", null, detailsFactory.apply(result)));
            return result;
        } catch (LoaderException exception) {
            sink.record(new DiagnosticEvent(name, phase.name(), elapsedMillis(start), "error", exception.getMessage(), null));
            throw new LoaderException("Failure during " + phase.name() + ": " + exception.getMessage(), exception);
        } catch (Exception exception) {
            sink.record(
                new DiagnosticEvent(name, phase.name(), elapsedMillis(start), "error", "Unexpected failure during " + name, null)
            );
            throw new LoaderException("Unexpected failure during " + name, exception);
        }
    }

    private static long elapsedMillis(long start) {
        return Math.max(0L, (System.nanoTime() - start) / 1_000_000L);
    }

    record LaunchArguments(
        String gameMainClass,
        String gameProviderId,
        List<String> launchArguments,
        boolean validateOnly,
        boolean explain,
        boolean strictResources,
        boolean strictPackages,
        MinecraftProviderConfig minecraftProviderConfig,
        Path macheDirectory,
        String macheVersion,
        boolean macheReferenceScan
    ) {
        LaunchArguments {
            launchArguments = List.copyOf(launchArguments);
        }

        LaunchArguments withMinecraftProviderConfig(MinecraftProviderConfig updatedMinecraftProviderConfig) {
            return new LaunchArguments(
                gameMainClass,
                gameProviderId,
                launchArguments,
                validateOnly,
                explain,
                strictResources,
                strictPackages,
                updatedMinecraftProviderConfig,
                macheDirectory,
                macheVersion,
                macheReferenceScan
            );
        }

        LaunchArguments withMacheDirectory(Path updatedMacheDirectory) {
            return new LaunchArguments(
                gameMainClass,
                gameProviderId,
                launchArguments,
                validateOnly,
                explain,
                strictResources,
                strictPackages,
                minecraftProviderConfig,
                updatedMacheDirectory,
                macheVersion,
                macheReferenceScan
            );
        }
    }

    private static Path resolveOptionalPath(Path workingDirectory, Path path) {
        if (path == null) {
            return null;
        }
        if (path.isAbsolute()) {
            return path.toAbsolutePath().normalize();
        }
        return workingDirectory.resolve(path).toAbsolutePath().normalize();
    }

    private static int parsePositiveInt(String value, String argumentName) throws LoaderException {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new LoaderException(argumentName + " requires a positive integer");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new LoaderException(argumentName + " requires a positive integer", exception);
        }
    }

    private static Map<String, String> minecraftServerLaunchDetails(
        LaunchContext context,
        MinecraftProviderConfig config,
        Path serverJarPath,
        Path javaExecutable,
        MinecraftProcessResult result,
        Path resultOutputPath
    ) {
        return details(
            "minecraftVersion",
            config.requestedVersion(),
            "serverDirectory",
            result == null ? displayPath(context, config.serverDirectory()) : result.serverDirectory(),
            "serverJar",
            result == null ? displayPath(context, serverJarPath) : result.serverJar(),
            "javaExecutable",
            result == null ? displayPath(context, javaExecutable) : result.javaExecutable(),
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
            displayPath(context, resultOutputPath)
        );
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
