package com.mcmodloader.core.diagnostics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class JsonDiagnosticSink implements DiagnosticSink {
    private final Path outputPath;
    private final List<DiagnosticEvent> events = new ArrayList<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public JsonDiagnosticSink(Path outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public void record(DiagnosticEvent event) {
        events.add(event);
    }

    @Override
    public void write() throws IOException {
        Files.createDirectories(outputPath.getParent());

        JsonObject root = new JsonObject();
        root.add("events", gson.toJsonTree(events));

        try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            gson.toJson(root, writer);
        }
    }
}
