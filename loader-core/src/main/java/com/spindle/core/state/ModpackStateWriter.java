package com.spindle.core.state;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.graph.FrozenModGraph;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModpackStateWriter {
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, ModpackState modpackState) throws LoaderException {
    try {
      Files.createDirectories(outputPath.getParent());
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(modpackState, writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write modpack state " + outputPath.getFileName(), exception);
    }
  }

  public ModpackState create(FrozenModGraph graph, Path workingDirectory) {
    return ModpackState.from(graph, workingDirectory);
  }
}
