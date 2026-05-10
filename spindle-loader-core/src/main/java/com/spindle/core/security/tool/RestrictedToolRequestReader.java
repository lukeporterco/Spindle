package com.spindle.core.security.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RestrictedToolRequestReader {
  public RestrictedToolRequest read(Path requestPath) {
    try {
      if (!Files.isRegularFile(requestPath)) {
        throw new IllegalArgumentException(
            "Restricted tool request file was not found: "
                + requestPath.toString().replace('\\', '/'));
      }
      JsonObject root =
          JsonParser.parseString(Files.readString(requestPath, StandardCharsets.UTF_8))
              .getAsJsonObject();
      int schemaVersion = requiredInt(root, "schemaVersion");
      if (schemaVersion != RestrictedToolRequest.SCHEMA_VERSION) {
        throw new IllegalArgumentException(
            "Unsupported restricted tool request schemaVersion `" + schemaVersion + "`.");
      }
      String worker = requiredString(root, "worker");
      if (!RestrictedToolRequest.STATIC_RISK_SCAN_WORKER.equals(worker)) {
        throw new IllegalArgumentException(
            "Restricted tool request worker must be `"
                + RestrictedToolRequest.STATIC_RISK_SCAN_WORKER
                + "`.");
      }
      Path workingDirectory = Path.of(requiredString(root, "workingDirectory"));
      Path outputPath = Path.of(requiredString(root, "outputPath"));
      JsonArray modsArray = requiredArray(root, "mods");
      if (modsArray.isEmpty()) {
        throw new IllegalArgumentException("Restricted tool request mods array must be non-empty.");
      }
      List<RestrictedToolRequest.ModInput> mods = new ArrayList<>();
      for (var element : modsArray) {
        JsonObject modObject = element.getAsJsonObject();
        mods.add(
            new RestrictedToolRequest.ModInput(
                requiredString(modObject, "modId"),
                requiredString(modObject, "relativePath"),
                requiredString(modObject, "sha256"),
                Path.of(requiredString(modObject, "jarPath"))));
      }
      return new RestrictedToolRequest(worker, workingDirectory, outputPath, mods);
    } catch (IOException exception) {
      throw new IllegalArgumentException(
          "Failed to read restricted tool request: " + requestPath.toString().replace('\\', '/'),
          exception);
    } catch (IllegalStateException | JsonParseException exception) {
      throw new IllegalArgumentException("Restricted tool request was not valid JSON.", exception);
    }
  }

  private JsonArray requiredArray(JsonObject root, String fieldName) {
    if (!root.has(fieldName) || !root.get(fieldName).isJsonArray()) {
      throw new IllegalArgumentException(
          "Restricted tool request is missing array field `" + fieldName + "`.");
    }
    return root.getAsJsonArray(fieldName);
  }

  private String requiredString(JsonObject root, String fieldName) {
    if (!root.has(fieldName) || root.get(fieldName).isJsonNull()) {
      throw new IllegalArgumentException(
          "Restricted tool request is missing string field `" + fieldName + "`.");
    }
    String value = root.get(fieldName).getAsString().trim();
    if (value.isEmpty()) {
      throw new IllegalArgumentException(
          "Restricted tool request is missing string field `" + fieldName + "`.");
    }
    return value;
  }

  private int requiredInt(JsonObject root, String fieldName) {
    if (!root.has(fieldName) || root.get(fieldName).isJsonNull()) {
      throw new IllegalArgumentException(
          "Restricted tool request is missing integer field `" + fieldName + "`.");
    }
    return root.get(fieldName).getAsInt();
  }
}
