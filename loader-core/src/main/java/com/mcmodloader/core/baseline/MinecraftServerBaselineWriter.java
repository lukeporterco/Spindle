package com.mcmodloader.core.baseline;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mcmodloader.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftServerBaselineWriter {
    private final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

    public void write(Path outputPath, MinecraftServerBaseline baseline) throws LoaderException {
        JsonObject root = new JsonObject();
        root.addProperty("schema", baseline.schema());
        root.addProperty("projectTargetMinecraft", baseline.projectTargetMinecraft());
        root.addProperty("baselineMinecraft", baseline.baselineMinecraft());

        JsonObject versionSelection = new JsonObject();
        addString(versionSelection, "requested", baseline.versionSelection().requested());
        addString(versionSelection, "resolved", baseline.versionSelection().resolved());
        addString(versionSelection, "source", baseline.versionSelection().source());
        root.add("versionSelection", versionSelection);

        JsonObject metadata = new JsonObject();
        addString(metadata, "manifestPath", baseline.metadata().manifestPath());
        addString(metadata, "versionJsonPath", baseline.metadata().versionJsonPath());
        addString(metadata, "manifestSha256", baseline.metadata().manifestSha256());
        addString(metadata, "versionJsonSha256", baseline.metadata().versionJsonSha256());
        root.add("metadata", metadata);

        JsonObject serverArtifact = new JsonObject();
        addString(serverArtifact, "path", baseline.serverArtifact().path());
        addString(serverArtifact, "sourceUrl", baseline.serverArtifact().sourceUrl());
        addString(serverArtifact, "sha1", baseline.serverArtifact().sha1());
        addString(serverArtifact, "sha256", baseline.serverArtifact().sha256());
        if (baseline.serverArtifact().size() == null) {
            serverArtifact.add("size", JsonNull.INSTANCE);
        } else {
            serverArtifact.addProperty("size", baseline.serverArtifact().size());
        }
        serverArtifact.addProperty("verified", baseline.serverArtifact().verified());
        root.add("serverArtifact", serverArtifact);

        JsonObject launch = new JsonObject();
        launch.addProperty("attempted", baseline.launch().attempted());
        addString(launch, "resultPath", baseline.launch().resultPath());
        launch.addProperty("started", baseline.launch().started());
        addBooleanOrNull(launch, "readyDetected", baseline.launch().readyDetected());
        addNumberOrNull(launch, "exitCode", baseline.launch().exitCode());
        launch.addProperty("timedOut", baseline.launch().timedOut());
        root.add("launch", launch);

        JsonObject offlineReplay = new JsonObject();
        offlineReplay.addProperty("attempted", baseline.offlineReplay().attempted());
        addString(offlineReplay, "resultPath", baseline.offlineReplay().resultPath());
        offlineReplay.addProperty("succeeded", baseline.offlineReplay().succeeded());
        offlineReplay.addProperty("networkCalls", baseline.offlineReplay().networkCalls());
        root.add("offlineReplay", offlineReplay);

        JsonObject modIntegration = new JsonObject();
        modIntegration.addProperty("modClassLoaderCreated", baseline.modIntegration().modClassLoaderCreated());
        modIntegration.addProperty("entrypointsInvoked", baseline.modIntegration().entrypointsInvoked());
        modIntegration.addProperty("modJarsOnMinecraftClasspath", baseline.modIntegration().modJarsOnMinecraftClasspath());
        root.add("modIntegration", modIntegration);

        try {
            Path parent = outputPath.toAbsolutePath().normalize().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                gson.toJson(root, writer);
            }
        } catch (IOException exception) {
            throw new LoaderException("Failed to write Minecraft server baseline report " + outputPath.getFileName(), exception);
        }
    }

    private void addString(JsonObject object, String memberName, String value) {
        if (value == null) {
            object.add(memberName, JsonNull.INSTANCE);
        } else {
            object.addProperty(memberName, value);
        }
    }

    private void addBooleanOrNull(JsonObject object, String memberName, Boolean value) {
        if (value == null) {
            object.add(memberName, JsonNull.INSTANCE);
        } else {
            object.addProperty(memberName, value);
        }
    }

    private void addNumberOrNull(JsonObject object, String memberName, Number value) {
        if (value == null) {
            object.add(memberName, JsonNull.INSTANCE);
        } else {
            object.addProperty(memberName, value);
        }
    }
}
