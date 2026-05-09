package com.mcmodloader.fixture.bootstrapmod;

import com.mcmodloader.api.minecraft.MinecraftServerModContext;
import com.mcmodloader.api.minecraft.MinecraftServerModInitializer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RecordingMinecraftServerEntrypoint implements MinecraftServerModInitializer {
    @Override
    public void onInitializeMinecraftServer(MinecraftServerModContext context) throws Exception {
        boolean loaderInternalsDenied;
        try {
            Class.forName("com.mcmodloader.core.LoaderMain", true, getClass().getClassLoader());
            loaderInternalsDenied = false;
        } catch (ClassNotFoundException exception) {
            loaderInternalsDenied = true;
        }
        Path marker = context.modDataDirectory().resolve("entrypoint.marker");
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
                + "|loaderInternalsDenied="
                + loaderInternalsDenied
                + "|apiVisible="
                + MinecraftServerModContext.class.getName()
                + System.lineSeparator(),
            StandardCharsets.UTF_8
        );
    }
}
