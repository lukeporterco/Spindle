package com.mcmodloader.fixture.bootstrapmod;

import com.mcmodloader.api.minecraft.MinecraftServerModContext;
import com.mcmodloader.api.minecraft.MinecraftServerModInitializer;

public final class FailingMinecraftServerEntrypoint implements MinecraftServerModInitializer {
    @Override
    public void onInitializeMinecraftServer(MinecraftServerModContext context) {
        throw new IllegalStateException("entrypoint failed intentionally");
    }
}
