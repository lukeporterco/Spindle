package com.spindle.sampleminecraftmod;

import com.spindle.api.minecraft.MinecraftServerModContext;
import com.spindle.api.minecraft.MinecraftServerModInitializer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SampleMinecraftServerMod implements MinecraftServerModInitializer {
  @Override
  public void onInitializeMinecraftServer(MinecraftServerModContext context) throws Exception {
    Path marker = context.modDataDirectory().resolve("bootstrap.marker");
    Files.createDirectories(context.modDataDirectory());
    Files.writeString(
        marker,
        context.modId()
            + "|"
            + context.modVersion()
            + "|"
            + context.minecraftVersion()
            + "|"
            + context.loaderVersion()
            + "|"
            + context.side()
            + System.lineSeparator(),
        StandardCharsets.UTF_8);
  }
}
