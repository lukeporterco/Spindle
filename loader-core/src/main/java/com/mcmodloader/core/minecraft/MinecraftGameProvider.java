package com.mcmodloader.core.minecraft;

import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.game.GameProvider;
import com.mcmodloader.core.launch.LaunchContext;
import java.nio.file.Files;

public final class MinecraftGameProvider implements GameProvider {
    private final MinecraftProviderConfig config;
    private final MinecraftMetadataResolver metadataResolver = new MinecraftMetadataResolver();
    private final MinecraftVersionMetadataParser metadataParser = new MinecraftVersionMetadataParser();
    private String resolvedVersion;

    public MinecraftGameProvider(MinecraftProviderConfig config) {
        this.config = config;
        this.resolvedVersion = config.requestedVersion();
    }

    @Override
    public String id() {
        return "minecraft";
    }

    @Override
    public String displayName() {
        return "Minecraft Java Edition";
    }

    @Override
    public String version() {
        return resolvedVersion == null || resolvedVersion.isBlank() ? "unknown" : resolvedVersion;
    }

    @Override
    public void validate(LaunchContext context) throws LoaderException {
        metadataResolver.validateAvailability(context.workingDirectory(), config);
        if ((resolvedVersion == null || resolvedVersion.isBlank()) && config.explicitVersionJson() != null && Files.isRegularFile(config.explicitVersionJson())) {
            String json;
            try {
                json = java.nio.file.Files.readString(config.explicitVersionJson());
            } catch (java.io.IOException exception) {
                throw new LoaderException("Failed to read Minecraft version JSON: " + config.explicitVersionJson(), exception);
            }
            resolvedVersion = metadataParser.parse(json, config.explicitVersionJson().toString(), config.side()).id();
        }
    }

    @Override
    public void launch(LaunchContext context, ClassLoader classLoader) throws LoaderException {
        throw new LoaderException("Minecraft launch is intentionally disabled in Milestone 3. Use --minecraft-dry-run only.");
    }

    public MinecraftProviderConfig config() {
        return config;
    }
}
