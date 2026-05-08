package com.mcmodloader.core.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcmodloader.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StartupProfileWriter {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public void write(Path outputPath, StartupProfile profile) throws LoaderException {
        try {
            Files.createDirectories(outputPath.getParent());
            try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                gson.toJson(profile, writer);
            }
        } catch (IOException exception) {
            throw new LoaderException("Failed to write startup profile " + outputPath.getFileName(), exception);
        }
    }
}
