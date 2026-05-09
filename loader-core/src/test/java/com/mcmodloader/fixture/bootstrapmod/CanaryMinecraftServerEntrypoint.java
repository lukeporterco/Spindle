package com.mcmodloader.fixture.bootstrapmod;

import com.mcmodloader.api.minecraft.MinecraftServerModContext;
import com.mcmodloader.api.minecraft.MinecraftServerModInitializer;

public final class CanaryMinecraftServerEntrypoint implements MinecraftServerModInitializer {
    static {
        if (true) {
            throw new IllegalStateException("canary should never be loaded during planning");
        }
    }

    @Override
    public void onInitializeMinecraftServer(MinecraftServerModContext context) {
    }
}
