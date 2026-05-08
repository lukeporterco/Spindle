package com.mcmodloader.core;

import com.mcmodloader.core.classpath.ModClassLoader;
import com.mcmodloader.core.diagnostics.DiagnosticEvent;
import com.mcmodloader.core.diagnostics.DiagnosticSink;
import com.mcmodloader.core.diagnostics.JsonDiagnosticSink;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.discovery.ModCandidate;
import com.mcmodloader.core.discovery.ModDiscoverer;
import com.mcmodloader.core.entrypoint.EntrypointInvoker;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.lockfile.LockfileVerifier;
import com.mcmodloader.core.lockfile.LockfileWriter;
import com.mcmodloader.core.metadata.ModMetadataParser;
import com.mcmodloader.core.resolve.DependencyResolver;
import com.mcmodloader.core.resolve.ResolvedModSet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class LoaderMain {
    public static final String LOADER_VERSION = "0.1.0";
    public static final String TARGET_MINECRAFT_VERSION = "26.1.2";

    private LoaderMain() {
    }

    public static void main(String[] args) {
        JsonDiagnosticSink diagnosticSink = null;
        int exitCode = 0;

        try {
            String gameMainClass = parseGameMainClass(args);
            LaunchContext context = createLaunchContext(gameMainClass);
            diagnosticSink = new JsonDiagnosticSink(context.workingDirectory().resolve("diagnostics/startup-trace.json"));
            run(context, diagnosticSink);
        } catch (LoaderException exception) {
            System.err.println("[loader] error: " + exception.getMessage());
            exitCode = 1;
        } catch (Exception exception) {
            System.err.println("[loader] error: unexpected failure");
            exception.printStackTrace(System.err);
            exitCode = 1;
        } finally {
            if (diagnosticSink != null) {
                try {
                    diagnosticSink.write();
                } catch (IOException exception) {
                    System.err.println("[loader] error: failed to write diagnostics");
                    exitCode = 1;
                }
            }
        }

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static void run(LaunchContext context, DiagnosticSink diagnosticSink) throws LoaderException {
        ModDiscoverer modDiscoverer = new ModDiscoverer();
        ModMetadataParser metadataParser = new ModMetadataParser();
        DependencyResolver dependencyResolver = new DependencyResolver();
        LockfileWriter lockfileWriter = new LockfileWriter();
        LockfileVerifier lockfileVerifier = new LockfileVerifier();
        EntrypointInvoker entrypointInvoker = new EntrypointInvoker();

        List<ModCandidate> discoveredMods =
            measure(diagnosticSink, "mod.discovery", "startup", () -> modDiscoverer.discover(context));
        System.out.println("[loader] discovered " + discoveredMods.size() + " " + pluralize(discoveredMods.size()));

        List<ModCandidate> parsedMods =
            measure(diagnosticSink, "metadata.parse", "startup", () -> parseMetadata(discoveredMods, metadataParser));

        ResolvedModSet resolvedMods =
            measure(diagnosticSink, "dependency.resolution", "startup", () -> dependencyResolver.resolve(context, parsedMods));
        System.out.println("[loader] resolved " + resolvedMods.mods().size() + " " + pluralize(resolvedMods.mods().size()));

        Path lockfilePath = context.workingDirectory().resolve("loader.lock.json");
        boolean wroteLockfile =
            measure(
                diagnosticSink,
                "lockfile.verify_or_write",
                "startup",
                () -> verifyOrWriteLockfile(lockfilePath, context, resolvedMods, lockfileWriter, lockfileVerifier)
            );
        System.out.println(wroteLockfile ? "[loader] wrote loader.lock.json" : "[loader] verified loader.lock.json");

        try (
            ModClassLoader modClassLoader =
                measure(
                    diagnosticSink,
                    "classpath.create",
                    "startup",
                    () -> ModClassLoader.create(resolvedMods, LoaderMain.class.getClassLoader())
                )
        ) {
            measure(
                diagnosticSink,
                "entrypoint.invoke",
                "startup",
                () -> {
                    entrypointInvoker.invoke(resolvedMods, modClassLoader);
                    return null;
                }
            );

            measure(
                diagnosticSink,
                "game.launch",
                "startup",
                () -> {
                    launchGame(context.gameMainClass());
                    return null;
                }
            );
        } catch (IOException exception) {
            throw new LoaderException("Failed to close mod class loader", exception);
        }

        System.out.println("[loader] startup complete");
    }

    private static String parseGameMainClass(String[] args) throws LoaderException {
        for (int index = 0; index < args.length; index++) {
            if ("--game-main".equals(args[index])) {
                if (index + 1 >= args.length) {
                    throw new LoaderException("Missing value for --game-main");
                }
                return args[index + 1];
            }
        }

        throw new LoaderException("Missing required argument --game-main");
    }

    private static LaunchContext createLaunchContext(String gameMainClass) {
        Path workingDirectory = Paths.get("").toAbsolutePath().normalize();
        return new LaunchContext(
            workingDirectory,
            workingDirectory.resolve("mods"),
            gameMainClass,
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

    private static void launchGame(String gameMainClass) throws LoaderException {
        try {
            Class<?> gameClass = Class.forName(gameMainClass, true, LoaderMain.class.getClassLoader());
            Method mainMethod = gameClass.getMethod("main", String[].class);
            if (!Modifier.isStatic(mainMethod.getModifiers())) {
                throw new LoaderException("Game main method must be static: " + gameMainClass);
            }
            mainMethod.invoke(null, (Object) new String[0]);
        } catch (ClassNotFoundException exception) {
            throw new LoaderException("Game main class not found: " + gameMainClass, exception);
        } catch (NoSuchMethodException exception) {
            throw new LoaderException("Game main method not found: " + gameMainClass, exception);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new LoaderException("Failed to launch game main class: " + gameMainClass, exception);
        }
    }

    private static String pluralize(int count) {
        return count == 1 ? "mod" : "mods";
    }

    private static <T> T measure(DiagnosticSink sink, String name, String phase, ThrowingSupplier<T> supplier)
        throws LoaderException {
        long start = System.nanoTime();
        try {
            T result = supplier.get();
            sink.record(new DiagnosticEvent(name, phase, elapsedMillis(start), "ok"));
            return result;
        } catch (LoaderException exception) {
            sink.record(new DiagnosticEvent(name, phase, elapsedMillis(start), "error"));
            throw exception;
        } catch (Exception exception) {
            sink.record(new DiagnosticEvent(name, phase, elapsedMillis(start), "error"));
            throw new LoaderException("Unexpected failure during " + name, exception);
        }
    }

    private static long elapsedMillis(long start) {
        return Math.max(0L, (System.nanoTime() - start) / 1_000_000L);
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
