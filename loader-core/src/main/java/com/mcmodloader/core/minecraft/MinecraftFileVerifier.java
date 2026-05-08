package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.diagnostics.LoaderException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftFileVerifier {
    public List<Path> verify(
        MinecraftProviderConfig config,
        MinecraftMetadataResolver.ResolvedVersionJson resolvedVersionJson,
        MinecraftVersionMetadata metadata,
        Path resolvedServerJar,
        MinecraftLibrarySelector.Selection selection,
        MinecraftInstallLocator installLocator
    ) throws LoaderException {
        List<Path> missingFiles = new ArrayList<>();
        recordMissing(resolvedVersionJson.versionJsonPath(), missingFiles);

        if (config.side() == MinecraftSide.CLIENT) {
            if (config.minecraftDirectory() == null) {
                throw new LoaderException("Minecraft client dry run requires a Minecraft directory");
            }
            recordMissing(installLocator.clientJarPath(config.minecraftDirectory(), metadata.id()), missingFiles);
        } else if (metadata.serverDownload() != null) {
            if (resolvedServerJar == null || !Files.isRegularFile(resolvedServerJar)) {
                missingFiles.add(
                    resolvedServerJar == null
                        ? installLocator.primaryServerJarPath(
                            config.minecraftDirectory() == null ? Path.of("missing-minecraft-dir") : config.minecraftDirectory(),
                            metadata.id()
                        )
                        : resolvedServerJar.toAbsolutePath().normalize()
                );
            }
        }

        if (config.side() == MinecraftSide.CLIENT) {
            for (MinecraftLibrarySelector.SelectedLibrary library : selection.libraries()) {
                recordMissing(library.path(), missingFiles);
            }
            for (MinecraftLibrarySelector.SelectedLibrary library : selection.nativeLibraries()) {
                recordMissing(library.path(), missingFiles);
            }

            if (
                metadata.assetIndex() != null &&
                metadata.assetIndex().id() != null &&
                !metadata.assetIndex().id().isBlank()
            ) {
                recordMissing(installLocator.assetIndexPath(config.minecraftDirectory(), metadata.assetIndex().id()), missingFiles);
            }
        }

        if (config.verifyFiles() && !missingFiles.isEmpty()) {
            throw new LoaderException(
                "Minecraft file verification failed. Missing files: " +
                missingFiles.stream().map(path -> path.toString().replace('\\', '/')).reduce((left, right) -> left + ", " + right).orElse("")
            );
        }

        return List.copyOf(missingFiles);
    }

    private void recordMissing(Path path, List<Path> missingFiles) {
        if (!Files.isRegularFile(path)) {
            missingFiles.add(path.toAbsolutePath().normalize());
        }
    }
}
