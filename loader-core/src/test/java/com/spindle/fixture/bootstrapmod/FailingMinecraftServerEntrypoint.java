package com.spindle.fixture.bootstrapmod;

import com.spindle.api.minecraft.MinecraftServerModContext;
import com.spindle.api.minecraft.MinecraftServerModInitializer;

public final class FailingMinecraftServerEntrypoint implements MinecraftServerModInitializer {
  @Override
  public void onInitializeMinecraftServer(MinecraftServerModContext context) {
    throw new IllegalStateException("entrypoint failed intentionally");
  }
}
