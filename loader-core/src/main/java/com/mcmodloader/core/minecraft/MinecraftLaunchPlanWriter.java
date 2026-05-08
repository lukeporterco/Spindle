package com.mcmodloader.core.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mcmodloader.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftLaunchPlanWriter {
    private final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

    public void write(Path outputPath, MinecraftLaunchPlan plan) throws LoaderException {
        JsonObject root = new JsonObject();
        root.addProperty("schema", plan.schema());
        root.addProperty("provider", plan.provider());
        root.addProperty("minecraftVersion", plan.minecraftVersion());
        root.addProperty("side", plan.side());
        addStringOrNull(root, "mainClass", plan.mainClass());
        addStringOrNull(root, "minecraftDirectory", plan.minecraftDirectory());
        addStringOrNull(root, "serverJarSource", plan.serverJarSource());
        root.addProperty("versionJson", plan.versionJson());
        addStringOrNull(root, "gameJar", plan.gameJar());
        addStringOrNull(root, "serverJar", plan.serverJar());

        if (plan.assetIndex() == null) {
            root.add("assetIndex", JsonNull.INSTANCE);
        } else {
            JsonObject assetIndex = new JsonObject();
            assetIndex.addProperty("id", plan.assetIndex().id());
            assetIndex.addProperty("path", plan.assetIndex().path());
            addStringOrNull(assetIndex, "sha1", plan.assetIndex().sha1());
            assetIndex.addProperty("size", plan.assetIndex().size());
            root.add("assetIndex", assetIndex);
        }

        root.add("libraries", toLibraryArray(plan.libraries()));
        root.add("nativeLibraries", toLibraryArray(plan.nativeLibraries()));
        root.add("classpath", toStringArray(plan.classpath()));
        root.add("jvmArguments", toStringArray(plan.jvmArguments()));
        root.add("gameArguments", toStringArray(plan.gameArguments()));
        root.add("commandPreview", toStringArray(plan.commandPreview()));
        root.add("missingFiles", toStringArray(plan.missingFiles()));

        JsonObject metadata = new JsonObject();
        addStringOrNull(metadata, "type", plan.metadata().type());
        addStringOrNull(metadata, "assets", plan.metadata().assets());
        root.add("metadata", metadata);

        try {
            Files.createDirectories(outputPath.getParent());
            try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                gson.toJson(root, writer);
            }
        } catch (IOException exception) {
            throw new LoaderException("Failed to write Minecraft launch plan " + outputPath.getFileName(), exception);
        }
    }

    private JsonArray toLibraryArray(java.util.List<MinecraftLaunchPlan.Library> libraries) {
        JsonArray array = new JsonArray();
        for (MinecraftLaunchPlan.Library library : libraries) {
            JsonObject entry = new JsonObject();
            addStringOrNull(entry, "name", library.name());
            entry.addProperty("path", library.path());
            addStringOrNull(entry, "sha1", library.sha1());
            entry.addProperty("size", library.size());
            entry.addProperty("native", library.nativeLibrary());
            array.add(entry);
        }
        return array;
    }

    private JsonArray toStringArray(java.util.List<String> values) {
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }

    private void addStringOrNull(JsonObject object, String memberName, String value) {
        if (value == null) {
            object.add(memberName, JsonNull.INSTANCE);
        } else {
            object.addProperty(memberName, value);
        }
    }
}
