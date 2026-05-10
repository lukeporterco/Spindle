package com.spindle.api.minecraft;

public interface MinecraftServerModInitializer {
  void onInitializeMinecraftServer(MinecraftServerModContext context) throws Exception;
}
