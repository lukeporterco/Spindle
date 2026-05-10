package com.spindle.core.security.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RestrictedToolRequestWriter {
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path requestPath, RestrictedToolRequest request) throws LoaderException {
    JsonObject root = new JsonObject();
    root.addProperty("schemaVersion", RestrictedToolRequest.SCHEMA_VERSION);
    root.addProperty("worker", request.worker());
    root.addProperty("workingDirectory", request.workingDirectory().toString().replace('\\', '/'));
    root.addProperty("outputPath", request.outputPath().toString().replace('\\', '/'));

    JsonArray mods = new JsonArray();
    for (RestrictedToolRequest.ModInput mod : request.mods()) {
      JsonObject modObject = new JsonObject();
      modObject.addProperty("modId", mod.modId());
      modObject.addProperty("relativePath", mod.relativePath());
      modObject.addProperty("sha256", mod.sha256());
      modObject.addProperty("jarPath", mod.jarPath().toString().replace('\\', '/'));
      mods.add(modObject);
    }
    root.add("mods", mods);

    try {
      Files.createDirectories(requestPath.getParent());
      try (Writer writer = Files.newBufferedWriter(requestPath, StandardCharsets.UTF_8)) {
        gson.toJson(root, writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write restricted tool request " + requestPath.getFileName(), exception);
    }
  }
}
