package com.mcmodloader.core.minecraft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftLaunchPlanBuilder {
    public MinecraftLaunchPlan build(
        Path workingDirectory,
        MinecraftProviderConfig config,
        MinecraftMetadataResolver.ResolvedVersionJson resolvedVersionJson,
        MinecraftVersionMetadata metadata,
        MinecraftLibrarySelector.Selection librarySelection,
        MinecraftArgumentResolver.ResolvedArguments resolvedArguments,
        MinecraftInstallLocator installLocator
    ) {
        Path minecraftDirectory = config.minecraftDirectory();
        Path clientJar = installLocator.clientJarPath(minecraftDirectory, metadata.id());
        Path primaryServerJar = installLocator.primaryServerJarPath(minecraftDirectory, metadata.id());
        Path alternateServerJar = installLocator.alternateServerJarPath(minecraftDirectory, metadata.id());
        Path serverJar = Files.isRegularFile(primaryServerJar) ? primaryServerJar : Files.isRegularFile(alternateServerJar) ? alternateServerJar : primaryServerJar;
        Path assetsRoot = installLocator.assetsRoot(minecraftDirectory);

        List<MinecraftLaunchPlan.Library> libraries = toPlanLibraries(workingDirectory, librarySelection.libraries());
        List<MinecraftLaunchPlan.Library> nativeLibraries = toPlanLibraries(workingDirectory, librarySelection.nativeLibraries());

        List<String> classpath = new ArrayList<>();
        for (MinecraftLibrarySelector.SelectedLibrary library : librarySelection.libraries()) {
            classpath.add(displayPath(workingDirectory, library.path()));
        }
        if (config.side() == MinecraftSide.CLIENT) {
            classpath.add(displayPath(workingDirectory, clientJar));
        } else if (metadata.serverDownload() != null) {
            classpath.add(displayPath(workingDirectory, serverJar));
        }

        MinecraftLaunchPlan.AssetIndex assetIndex = null;
        if (metadata.assetIndex() != null && metadata.assetIndex().id() != null && !metadata.assetIndex().id().isBlank()) {
            assetIndex =
                new MinecraftLaunchPlan.AssetIndex(
                    metadata.assetIndex().id(),
                    displayPath(workingDirectory, installLocator.assetIndexPath(minecraftDirectory, metadata.assetIndex().id())),
                    metadata.assetIndex().sha1(),
                    metadata.assetIndex().size()
                );
        }

        return new MinecraftLaunchPlan(
            1,
            "minecraft",
            metadata.id(),
            config.side().id(),
            config.side() == MinecraftSide.CLIENT ? metadata.mainClass() : null,
            displayPath(workingDirectory, minecraftDirectory),
            displayPath(workingDirectory, resolvedVersionJson.versionJsonPath()),
            config.side() == MinecraftSide.CLIENT ? displayPath(workingDirectory, clientJar) : null,
            metadata.serverDownload() != null ? displayPath(workingDirectory, serverJar) : null,
            assetIndex,
            libraries,
            nativeLibraries,
            classpath,
            resolvedArguments.jvmArguments(),
            resolvedArguments.gameArguments(),
            displayCommandPreview(workingDirectory, resolvedArguments.commandPreview()),
            List.of(),
            new MinecraftLaunchPlan.Metadata(metadata.type(), metadata.assets())
        );
    }

    private List<MinecraftLaunchPlan.Library> toPlanLibraries(Path workingDirectory, List<MinecraftLibrarySelector.SelectedLibrary> libraries) {
        List<MinecraftLaunchPlan.Library> planLibraries = new ArrayList<>(libraries.size());
        for (MinecraftLibrarySelector.SelectedLibrary library : libraries) {
            planLibraries.add(
                new MinecraftLaunchPlan.Library(
                    library.name(),
                    displayPath(workingDirectory, library.path()),
                    library.sha1(),
                    library.size(),
                    library.nativeLibrary()
                )
            );
        }
        return planLibraries;
    }

    private List<String> displayCommandPreview(Path workingDirectory, List<String> commandPreview) {
        List<String> values = new ArrayList<>(commandPreview.size());
        for (String value : commandPreview) {
            if (value == null || value.isBlank()) {
                continue;
            }
            try {
                Path path = Path.of(value);
                if (path.isAbsolute()) {
                    values.add(displayPath(workingDirectory, path));
                    continue;
                }
            } catch (Exception ignored) {
            }
            values.add(value);
        }
        return values;
    }

    private String displayPath(Path workingDirectory, Path path) {
        Path normalizedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
        Path normalizedPath = path.toAbsolutePath().normalize();
        try {
            return normalizedWorkingDirectory.relativize(normalizedPath).toString().replace('\\', '/');
        } catch (IllegalArgumentException exception) {
            return normalizedPath.toString().replace('\\', '/');
        }
    }
}
