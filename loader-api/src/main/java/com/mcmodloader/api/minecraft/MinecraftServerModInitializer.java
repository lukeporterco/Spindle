package com.mcmodloader.api.minecraft;

public interface MinecraftServerModInitializer {
    void onInitializeMinecraftServer(MinecraftServerModContext context) throws Exception;
}
