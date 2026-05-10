package com.spindle.core.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.spindle.core.artifact.MinecraftVersionId;
import com.spindle.core.diagnostics.LoaderException;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftVersionManifestParser {
  public MinecraftVersionManifest parse(String json, String sourceDescription)
      throws LoaderException {
    JsonObject root = parseObject(json, sourceDescription);
    JsonObject latest = getObject(root, "latest");
    String latestRelease = optionalSafeVersionId(getString(latest, "release"));
    String latestSnapshot = optionalSafeVersionId(getString(latest, "snapshot"));

    List<MinecraftVersionManifest.VersionEntry> versions = new ArrayList<>();
    JsonArray versionsArray = getArray(root, "versions");
    if (versionsArray != null) {
      for (JsonElement element : versionsArray) {
        if (!element.isJsonObject()) {
          continue;
        }
        JsonObject versionObject = element.getAsJsonObject();
        versions.add(
            new MinecraftVersionManifest.VersionEntry(
                optionalSafeVersionId(getString(versionObject, "id")),
                getString(versionObject, "type"),
                getString(versionObject, "url"),
                getString(versionObject, "sha1"),
                getString(versionObject, "releaseTime"),
                getString(versionObject, "time")));
      }
    }

    return new MinecraftVersionManifest(latestRelease, latestSnapshot, versions);
  }

  private JsonObject parseObject(String json, String sourceDescription) throws LoaderException {
    try {
      JsonElement root = JsonParser.parseString(json);
      if (!root.isJsonObject()) {
        throw new LoaderException(
            "Minecraft version manifest must be a JSON object: " + sourceDescription);
      }
      return root.getAsJsonObject();
    } catch (JsonParseException exception) {
      throw new LoaderException(
          "Invalid Minecraft version manifest JSON: " + sourceDescription, exception);
    }
  }

  private JsonObject getObject(JsonObject object, String memberName) {
    JsonElement element = object.get(memberName);
    return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
  }

  private JsonArray getArray(JsonObject object, String memberName) {
    JsonElement element = object.get(memberName);
    return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
  }

  private String getString(JsonObject object, String memberName) {
    if (object == null) {
      return null;
    }
    JsonElement element = object.get(memberName);
    return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
  }

  private String optionalSafeVersionId(String versionId) throws LoaderException {
    return versionId == null ? null : MinecraftVersionId.requireSafe(versionId);
  }
}
