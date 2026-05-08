package com.mcmodloader.core.game;

import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.launch.LaunchContext;
import com.mcmodloader.core.minecraft.MinecraftGameProvider;
import com.mcmodloader.core.minecraft.MinecraftProviderConfig;

public final class GameProviderResolver {
    public GameProvider resolve(LaunchContext context) throws LoaderException {
        return resolve(context, null);
    }

    public GameProvider resolve(LaunchContext context, MinecraftProviderConfig minecraftProviderConfig) throws LoaderException {
        GameProvider provider;
        if ("sample".equals(context.gameProviderId())) {
            provider = new SampleGameProvider(context.targetMinecraftVersion());
        } else if ("minecraft".equals(context.gameProviderId())) {
            if (minecraftProviderConfig == null) {
                throw new LoaderException("Minecraft provider configuration was not provided");
            }
            provider = new MinecraftGameProvider(minecraftProviderConfig);
        } else {
            throw new LoaderException("Unknown game provider: " + context.gameProviderId());
        }

        try {
            provider.validate(context);
        } catch (LoaderException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LoaderException("Failed to validate game provider " + provider.id(), exception);
        }
        return provider;
    }
}
