package com.spindle.core.quality;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RuntimeQualityReportWriter {
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, RuntimeQualityReport report) throws LoaderException {
    try {
      Files.createDirectories(outputPath.getParent());
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(report, writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write runtime quality report " + outputPath.getFileName(), exception);
    }
  }
}
