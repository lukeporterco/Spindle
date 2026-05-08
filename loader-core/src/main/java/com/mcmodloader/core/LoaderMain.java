package com.mcmodloader.core;

import com.mcmodloader.core.classpath.ModClassLoader;
import com.mcmodloader.core.classpath.RuntimeClasspathPlan;
import com.mcmodloader.core.classpath.RuntimeClasspathPlanner;
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
import com.mcmodloader.core.minecraft.MinecraftProviderConfig;
import com.mcmodloader.core.minecraft.MinecraftSide;
import com.mcmodloader.core.minecraft.MinecraftVersionMetadata;
import com.mcmodloader.core.minecraft.MinecraftVersionMetadataParser;
import com.mcmodloader.core.ownership.ClassOwnershipIndex;
import com.mcmodloader.core.ownership.PackageOwnershipIndex;
import com.mcmodloader.core.profile.StartupProfile;
import com.mcmodloader.core.profile.StartupProfileWriter;
import com.mcmodloader.core.resource.ResourceConflict;
import com.mcmodloader.core.resource.ResourceConflictIndex;
import com.mcmodloader.core.resolve.DependencyResolver;
import com.mcmodloader.core.resolve.ResolvedModSet;
import com.mcmodloader.core.state.ModpackState;
import com.mcmodloader.core.state.ModpackStateWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Path minecraftOutputPlan = Path.of("minecraft-launch-plan.json");
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

            if ("--minecraft-output-plan".equals(argument)) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --minecraft-output-plan");
                }
                minecraftOutputPlan = Path.of(args[++index]);
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
                minecraftOutputPlan
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
            MinecraftDryRunResult dryRunResult = runMinecraftDryRun(context, launchArguments, minecraftGameProvider, diagnosticSink);
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
        DiagnosticSink diagnosticSink
    ) throws LoaderException {
        MinecraftProviderConfig config = minecraftGameProvider.config();
        MinecraftMetadataResolver metadataResolver = new MinecraftMetadataResolver();
        MinecraftVersionMetadataParser metadataParser = new MinecraftVersionMetadataParser();
        MinecraftInstallLocator installLocator = new MinecraftInstallLocator();
        MinecraftLibrarySelector librarySelector = new MinecraftLibrarySelector();
        MinecraftArgumentResolver argumentResolver = new MinecraftArgumentResolver();
        MinecraftLaunchPlanBuilder launchPlanBuilder = new MinecraftLaunchPlanBuilder();
        MinecraftLaunchPlanWriter launchPlanWriter = new MinecraftLaunchPlanWriter();
        MinecraftFileVerifier fileVerifier = new MinecraftFileVerifier();
        MacheReferenceScanner macheReferenceScanner = new MacheReferenceScanner();
        MacheReferenceWriter macheReferenceWriter = new MacheReferenceWriter();

        MinecraftMetadataResolver.ResolvedVersionJson resolvedVersionJson =
            measure(
                diagnosticSink,
                "minecraft.metadata.resolve",
                LaunchPhase.COMPLETE,
                () -> metadataResolver.resolve(context.workingDirectory(), config),
                resolved -> details(
                    "minecraftVersion",
                    resolved.requestedVersion(),
                    "minecraftSide",
                    config.side().id(),
                    "versionJsonPath",
                    displayPath(context, resolved.versionJsonPath()),
                    "metadataSource",
                    resolved.metadataSource()
                )
            );

        MinecraftVersionMetadata metadata =
            measure(
                diagnosticSink,
                "minecraft.metadata.parse",
                LaunchPhase.COMPLETE,
                () -> metadataParser.parse(resolvedVersionJson.json(), resolvedVersionJson.versionJsonPath().toString(), config.side()),
                parsedMetadata -> details(
                    "minecraftVersion",
                    parsedMetadata.id(),
                    "minecraftSide",
                    config.side().id(),
                    "mainClass",
                    parsedMetadata.mainClass()
                )
            );

        MinecraftLibrarySelector.Selection selection =
            measure(
                diagnosticSink,
                "minecraft.library.select",
                LaunchPhase.COMPLETE,
                () -> librarySelector.select(metadata, installLocator.librariesRoot(config.minecraftDirectory())),
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
        } else if (metadata.serverDownload() != null) {
            Path primaryServerJar = installLocator.primaryServerJarPath(config.minecraftDirectory(), metadata.id());
            Path alternateServerJar = installLocator.alternateServerJarPath(config.minecraftDirectory(), metadata.id());
            launchClasspath.add(
                java.nio.file.Files.isRegularFile(primaryServerJar)
                    ? primaryServerJar
                    : java.nio.file.Files.isRegularFile(alternateServerJar) ? alternateServerJar : primaryServerJar
            );
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
                    installLocator.assetsRoot(config.minecraftDirectory()),
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
            launchPlanBuilder.build(context.workingDirectory(), config, resolvedVersionJson, metadata, selection, resolvedArguments, installLocator);

        List<Path> missingFiles =
            config.verifyFiles()
                ? measure(
                    diagnosticSink,
                    "minecraft.file_verify",
                    LaunchPhase.COMPLETE,
                    () -> fileVerifier.verify(config, resolvedVersionJson, metadata, selection, installLocator),
                    missing -> details(
                        "minecraftVersion",
                        metadata.id(),
                        "minecraftSide",
                        config.side().id(),
                        "missingFileCount",
                        Integer.toString(missing.size())
                    )
                )
                : fileVerifier.verify(config, resolvedVersionJson, metadata, selection, installLocator);

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
                    "macheReferenceScan",
                    Boolean.toString(launchArguments.macheReferenceScan() && launchArguments.macheDirectory() != null),
                    "macheReportOutputPath",
                    launchArguments.macheReferenceScan() && launchArguments.macheDirectory() != null
                        ? displayPath(context, context.workingDirectory().resolve("mache-reference-report.json"))
                        : null
                )
            )
        );

        return new MinecraftDryRunResult(launchPlan, macheReferenceReport);
    }

    private static LaunchContext createLaunchContext(Path workingDirectory, LaunchArguments launchArguments) {
        String targetMinecraftVersion =
            "minecraft".equals(launchArguments.gameProviderId())
                    && launchArguments.minecraftProviderConfig() != null
                    && launchArguments.minecraftProviderConfig().requestedVersion() != null
                ? launchArguments.minecraftProviderConfig().requestedVersion()
                : TARGET_MINECRAFT_VERSION;
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
        if ("minecraft".equals(launchArguments.gameProviderId()) && resolvedMinecraftProviderConfig.minecraftDirectory() == null) {
            resolvedMinecraftProviderConfig =
                new MinecraftProviderConfig(
                    resolvedMinecraftProviderConfig.requestedVersion(),
                    new MinecraftInstallLocator().defaultMinecraftDirectory().orElse(null),
                    resolvedMinecraftProviderConfig.explicitVersionJson(),
                    resolvedMinecraftProviderConfig.manifestJson(),
                    resolvedMinecraftProviderConfig.side(),
                    resolvedMinecraftProviderConfig.dryRun(),
                    resolvedMinecraftProviderConfig.verifyFiles(),
                    resolvedMinecraftProviderConfig.fetchMetadata(),
                    resolvedMinecraftProviderConfig.outputPlanPath()
                ).resolveAgainst(workingDirectory);
        }

        if ("minecraft".equals(launchArguments.gameProviderId()) && !resolvedMinecraftProviderConfig.dryRun()) {
            throw new LoaderException("Minecraft provider requires --minecraft-dry-run in Milestone 3");
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
            resolvedMinecraftProviderConfig =
                new MinecraftProviderConfig(
                    resolvedVersion,
                    resolvedMinecraftProviderConfig.minecraftDirectory(),
                    resolvedMinecraftProviderConfig.explicitVersionJson(),
                    resolvedMinecraftProviderConfig.manifestJson(),
                    resolvedMinecraftProviderConfig.side(),
                    resolvedMinecraftProviderConfig.dryRun(),
                    resolvedMinecraftProviderConfig.verifyFiles(),
                    resolvedMinecraftProviderConfig.fetchMetadata(),
                    resolvedMinecraftProviderConfig.outputPlanPath()
                );
        }

        if ("minecraft".equals(launchArguments.gameProviderId())
            && (resolvedMinecraftProviderConfig.requestedVersion() == null || resolvedMinecraftProviderConfig.requestedVersion().isBlank())) {
            throw new LoaderException("Minecraft provider requires --minecraft-version unless --minecraft-version-json contains an id");
        }

        return launchArguments.withMinecraftProviderConfig(resolvedMinecraftProviderConfig).withMacheDirectory(
            resolveOptionalPath(workingDirectory, launchArguments.macheDirectory())
        );
    }

    private static List<ModCandidate> parseMetadata(List<ModCandidate> discoveredMods, ModMetadataParser metadataParser)
        throws LoaderException {
        List<ModCandidate> parsedMods = new ArrayList<>(discoveredMods.size());
        for (ModCandidate candidate : discoveredMods) {
            parsedMods.add(candidate.withMetadata(metadataParser.parse(candidate)));
        }
        return List.copyOf(parsedMods);
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

    private static String displayPath(LaunchContext context, Path path) {
        return context.workingDirectory().relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/');
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

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
