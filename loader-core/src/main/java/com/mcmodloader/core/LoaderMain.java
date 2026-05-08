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
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.launch.LaunchPhase;
import com.mcmodloader.core.lockfile.LockfileVerifier;
import com.mcmodloader.core.lockfile.LockfileWriter;
import com.mcmodloader.core.metadata.ModMetadataParser;
import com.mcmodloader.core.ownership.ClassOwnershipIndex;
import com.mcmodloader.core.resolve.DependencyResolver;
import com.mcmodloader.core.resolve.ResolvedModSet;
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
                () -> parseArguments(args),
                parsedArguments -> details(
                    "gameMainClass",
                    parsedArguments.gameMainClass(),
                    "gameProviderId",
                    parsedArguments.gameProviderId(),
                    "launchArgumentCount",
                    Integer.toString(parsedArguments.launchArguments().size())
                )
            );

        LaunchContext context = createLaunchContext(workingDirectory, launchArguments);
        GameProvider gameProvider =
            measure(
                diagnosticSink,
                "game_provider.resolve",
                LaunchPhase.GAME_PROVIDER_RESOLVE,
                () -> new GameProviderResolver().resolve(context),
                provider -> details(
                    "gameProviderId",
                    provider.id(),
                    "gameProviderName",
                    provider.displayName(),
                    "gameProviderVersion",
                    provider.version()
                )
            );

        run(context, gameProvider, diagnosticSink);
    }

    static LaunchArguments parseArguments(String[] args) throws LoaderException {
        String gameMainClass = null;
        String gameProviderId = DEFAULT_GAME_PROVIDER_ID;
        List<String> launchArguments = new ArrayList<>();

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

            launchArguments.add(argument);
        }

        if (gameMainClass == null) {
            throw new LoaderException("Missing required argument --game-main");
        }

        return new LaunchArguments(gameMainClass, gameProviderId, launchArguments);
    }

    private static void run(LaunchContext context, GameProvider gameProvider, DiagnosticSink diagnosticSink) throws LoaderException {
        ModDiscoverer modDiscoverer = new ModDiscoverer();
        ModMetadataParser metadataParser = new ModMetadataParser();
        DependencyResolver dependencyResolver = new DependencyResolver();
        LockfileWriter lockfileWriter = new LockfileWriter();
        LockfileVerifier lockfileVerifier = new LockfileVerifier();
        RuntimeClasspathPlanner classpathPlanner = new RuntimeClasspathPlanner();
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
        boolean wroteLockfile =
            measure(
                diagnosticSink,
                "lockfile.verify_or_write",
                LaunchPhase.LOCKFILE,
                () -> verifyOrWriteLockfile(lockfilePath, context, resolvedMods, lockfileWriter, lockfileVerifier),
                wrote -> details("lockfileAction", Boolean.TRUE.equals(wrote) ? "write" : "verify")
            );
        System.out.println(wroteLockfile ? "[loader] wrote loader.lock.json" : "[loader] verified loader.lock.json");

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
            ClassOwnershipIndex ownershipIndex =
                measure(
                    diagnosticSink,
                    "ownership.index",
                    LaunchPhase.ENTRYPOINT_INVOKE,
                    () -> ClassOwnershipIndex.build(resolvedMods),
                    index -> details("classOwnershipCount", Integer.toString(index.classOwners().size()))
                );

            List<EntrypointInvoker.EntrypointInvocation> invocations =
                measure(
                    diagnosticSink,
                    "entrypoint.invoke",
                    LaunchPhase.ENTRYPOINT_INVOKE,
                    () -> entrypointInvoker.invoke(resolvedMods, modClassLoader, ownershipIndex),
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
                details("resolvedModCount", Integer.toString(resolvedMods.mods().size()))
            )
        );
    }

    private static LaunchContext createLaunchContext(Path workingDirectory, LaunchArguments launchArguments) {
        return new LaunchContext(
            workingDirectory,
            workingDirectory.resolve("mods"),
            launchArguments.gameMainClass(),
            launchArguments.gameProviderId(),
            launchArguments.launchArguments(),
            LOADER_VERSION,
            Runtime.version().feature(),
            TARGET_MINECRAFT_VERSION
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

    private static boolean verifyOrWriteLockfile(
        Path lockfilePath,
        LaunchContext context,
        ResolvedModSet resolvedMods,
        LockfileWriter lockfileWriter,
        LockfileVerifier lockfileVerifier
    ) throws LoaderException {
        if (lockfileVerifier.exists(lockfilePath)) {
            lockfileVerifier.verify(lockfilePath, context, resolvedMods);
            return false;
        }

        lockfileWriter.write(lockfilePath, context, resolvedMods);
        return true;
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

    record LaunchArguments(String gameMainClass, String gameProviderId, List<String> launchArguments) {
        LaunchArguments {
            launchArguments = List.copyOf(launchArguments);
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
