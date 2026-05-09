package com.spindle.core.process;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftProcessResultWriter {
  private final Gson gson =
      new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path outputPath, MinecraftProcessResult result) throws LoaderException {
    JsonObject root = new JsonObject();
    root.addProperty("schema", result.schema());
    root.addProperty("minecraftVersion", result.minecraftVersion());
    root.addProperty("serverDirectory", result.serverDirectory());
    root.addProperty("serverJar", result.serverJar());
    root.addProperty("javaExecutable", result.javaExecutable());
    root.add("jvmArgs", toArray(result.jvmArgs()));
    root.add("serverArgs", toArray(result.serverArgs()));
    root.add("commandPreview", toArray(result.commandPreview()));
    root.addProperty("started", result.started());
    root.addProperty("readyDetected", result.readyDetected());
    root.addProperty("stopRequested", result.stopRequested());
    if (result.exitCode() == null) {
      root.add("exitCode", JsonNull.INSTANCE);
    } else {
      root.addProperty("exitCode", result.exitCode());
    }
    root.addProperty("timedOut", result.timedOut());
    root.addProperty("durationMs", result.durationMs());
    root.addProperty("stdoutTail", result.stdoutTail());
    root.addProperty("stderrTail", result.stderrTail());

    try {
      Files.createDirectories(outputPath.getParent());
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(root, writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write Minecraft server launch result " + outputPath.getFileName(), exception);
    }
  }

  private JsonArray toArray(java.util.List<String> values) {
    JsonArray array = new JsonArray();
    for (String value : values) {
      array.add(value);
    }
    return array;
  }
}
