package com.spindle.core.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftModExecutionResultWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftModExecutionResult result) throws LoaderException {
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(result, writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write Minecraft mod execution result " + outputPath, exception);
    }
  }
}
