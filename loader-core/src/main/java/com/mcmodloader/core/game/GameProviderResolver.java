package com.mcmodloader.core.game;

import com.mcmodloader.core.diagnostics.LoaderException;
import com.mcmodloader.core.launch.LaunchContext;

public final class GameProviderResolver {
    public GameProvider resolve(LaunchContext context) throws LoaderException {
        if ("sample".equals(context.gameProviderId())) {
            GameProvider provider = new SampleGameProvider(context.targetMinecraftVersion());
            try {
                provider.validate(context);
            } catch (LoaderException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new LoaderException("Failed to validate game provider " + provider.id(), exception);
            }
            return provider;
        }

        throw new LoaderException("Unknown game provider: " + context.gameProviderId());
    }
}
