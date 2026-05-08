package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.LoaderMain;
import com.mcmodloader.core.artifact.MinecraftArtifactCache;
import com.mcmodloader.core.artifact.MinecraftArtifactRecord;
import com.mcmodloader.core.artifact.MinecraftArtifactResolver;
import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.process.JavaExecutableResolver;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public final class MinecraftServerRuntimePlanner {
    private static final String MILESTONE_NAME = "Mega-Milestone 7";
    private final MinecraftBundledServerInspector bundledInspector = new MinecraftBundledServerInspector();
    private final MinecraftBundledServerResolver bundledResolver = new MinecraftBundledServerResolver();

    public PlannedRuntime plan(
        Path workingDirectory,
        MinecraftProviderConfig config,
        MinecraftArtifactCache cache,
        MinecraftArtifactResolver.Resolution artifactResolution,
        Function<Path, String> displayPath
    ) throws LoaderException {
        Path serverJar = artifactResolution.serverJarPath();
        Path javaExecutable = new JavaExecutableResolver().resolve();
        MinecraftRuntimeCacheLayout runtimeLayout = new MinecraftRuntimeCacheLayout(cache.cacheDirectory(), artifactResolution.metadata().id());
        MinecraftBundledServerInspector.Inspection inspection = bundledInspector.inspect(serverJar);
        MinecraftBundledServerResolver.ResolvedBundledServer bundled =
            bundledResolver.resolve(serverJar, inspection, runtimeLayout, config.cacheStrict());

        MinecraftServerRuntimeMode mode = inspection.bundled() ? MinecraftServerRuntimeMode.BUNDLED_SERVER : MinecraftServerRuntimeMode.SIMPLE_JAR;
        List<Path> rawClasspath = new ArrayList<>();
        List<MinecraftServerRuntimeClasspath.Entry> classpathEntries = new ArrayList<>();
        MinecraftArtifactRecord serverRecord = artifactResolution.serverRecord();

        MinecraftServerLaunchCommand command;
        if (mode == MinecraftServerRuntimeMode.BUNDLED_SERVER) {
            rawClasspath.addAll(bundled.classpath());
            for (Path classpathEntry : rawClasspath) {
                MinecraftRuntimeFile runtimeFile =
                    bundled.extractedFiles()
                        .stream()
                        .filter(file -> file.path().equals(classpathEntry.toAbsolutePath().normalize()))
                        .findFirst()
                        .orElse(null);
                classpathEntries.add(
                    new MinecraftServerRuntimeClasspath.Entry(
                        displayPath.apply(classpathEntry),
                        MinecraftClasspathOwnership.MINECRAFT_BUNDLED_LIBRARY.id(),
                        runtimeFile == null ? "bundled-server" : runtimeFile.origin(),
                        runtimeFile == null ? null : runtimeFile.sha256()
                    )
                );
            }
            command =
                MinecraftServerLaunchCommand.classpath(
                    javaExecutable,
                    inspection.mainClass() == null || inspection.mainClass().isBlank() ? "net.minecraft.server.Main" : inspection.mainClass(),
                    rawClasspath,
                    serverJar,
                    config.serverJvmArgs(),
                    config.serverArgs(),
                    displayPath
                );
        } else {
            classpathEntries.add(
                new MinecraftServerRuntimeClasspath.Entry(
                    displayPath.apply(serverJar),
                    MinecraftClasspathOwnership.MINECRAFT_SERVER_JAR.id(),
                    artifactResolution.serverJarSource(),
                    serverRecord == null ? null : serverRecord.sha256()
                )
            );
            command = MinecraftServerLaunchCommand.simpleJar(javaExecutable, serverJar, config.serverJvmArgs(), config.serverArgs(), displayPath);
        }

        boolean cacheOnly =
            artifactResolution.networkRequestCount() == 0 &&
            ("cache".equals(artifactResolution.serverJarSource()) || "local".equals(artifactResolution.serverJarSource()));
        boolean replayableOffline = serverJar != null && serverRecord != null && serverRecord.present() && artifactResolution.networkRequestCount() == 0;

        MinecraftRuntimeProvenance provenance =
            new MinecraftRuntimeProvenance(
                1,
                MILESTONE_NAME,
                List.of(
                    "minecraftVersion=" + artifactResolution.metadata().id(),
                    "serverJarSource=" + artifactResolution.serverJarSource(),
                    "versionJsonSource=" + artifactResolution.resolvedVersionJson().metadataSource()
                ),
                cache.displayPath(cache.cacheDirectory()),
                artifactResolution.networkRequestCount() == 0 ? "none" : "mojang-metadata-or-server-artifact",
                config.offlineReplay(),
                config.cacheStrict(),
                config.launch() ? "launch" : "plan",
                displayPath.apply(workingDirectory),
                displayPath.apply(workingDirectory),
                List.of("minecraft-artifacts.json", "minecraft-launch-plan.json")
            );

        MinecraftServerRuntimePlan plan =
            new MinecraftServerRuntimePlan(
                1,
                MILESTONE_NAME,
                "25",
                LoaderMain.TARGET_MINECRAFT_VERSION,
                artifactResolution.metadata().id(),
                artifactResolution.versionSelection() == null ? config.requestedVersionOrBaseline() : artifactResolution.versionSelection().requested(),
                artifactResolution.versionSelection() == null ? "exact version or explicit version JSON" : artifactResolution.versionSelection().source(),
                artifactResolution.manifestRecord() == null ? "none" : artifactResolution.manifestRecord().status().id(),
                artifactResolution.resolvedVersionJson().metadataSource(),
                serverJar == null ? null : displayPath.apply(serverJar),
                artifactResolution.serverJarSource(),
                serverRecord == null ? null : serverRecord.sha1(),
                serverRecord == null ? null : serverRecord.sha256(),
                serverRecord == null ? null : serverRecord.size(),
                mode.id(),
                inspection.bundled() ? "server jar contains bundled runtime metadata" : "server jar has no bundled runtime metadata",
                mode == MinecraftServerRuntimeMode.BUNDLED_SERVER ? command.mainClass() : null,
                List.copyOf(classpathEntries),
                bundled.extractedFiles(),
                command.jvmArgs(),
                command.serverArgs(),
                displayPath.apply(config.serverDirectory() == null ? workingDirectory : config.serverDirectory()),
                displayPath.apply(javaExecutable),
                command.commandPreview(),
                cache.displayPath(cache.cacheDirectory()),
                cache.displayPath(runtimeLayout.runtimeDirectory()),
                config.offline(),
                config.cacheStrict(),
                artifactResolution.networkRequestCount(),
                cacheOnly,
                replayableOffline,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                provenance
            );
        return new PlannedRuntime(plan, command);
    }

    public record PlannedRuntime(MinecraftServerRuntimePlan plan, MinecraftServerLaunchCommand command) {
    }
}
