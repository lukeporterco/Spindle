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
        MinecraftLibrarySelector.Selection selection,
        MinecraftInstallLocator installLocator
    ) throws LoaderException {
        List<Path> missingFiles = new ArrayList<>();
        recordMissing(resolvedVersionJson.versionJsonPath(), missingFiles);

        if (config.side() == MinecraftSide.CLIENT) {
            recordMissing(installLocator.clientJarPath(config.minecraftDirectory(), metadata.id()), missingFiles);
        } else if (metadata.serverDownload() != null) {
            Path primary = installLocator.primaryServerJarPath(config.minecraftDirectory(), metadata.id());
            Path alternate = installLocator.alternateServerJarPath(config.minecraftDirectory(), metadata.id());
            if (!Files.isRegularFile(primary) && !Files.isRegularFile(alternate)) {
                missingFiles.add(primary);
            }
        }

        for (MinecraftLibrarySelector.SelectedLibrary library : selection.libraries()) {
            recordMissing(library.path(), missingFiles);
        }
        for (MinecraftLibrarySelector.SelectedLibrary library : selection.nativeLibraries()) {
            recordMissing(library.path(), missingFiles);
        }

        if (
            config.side() == MinecraftSide.CLIENT &&
            metadata.assetIndex() != null &&
            metadata.assetIndex().id() != null &&
            !metadata.assetIndex().id().isBlank()
        ) {
            recordMissing(installLocator.assetIndexPath(config.minecraftDirectory(), metadata.assetIndex().id()), missingFiles);
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
