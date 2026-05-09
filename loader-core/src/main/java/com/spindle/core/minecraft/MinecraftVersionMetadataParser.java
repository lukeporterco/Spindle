package com.spindle.core.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.spindle.core.diagnostics.LoaderException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MinecraftVersionMetadataParser {
  public MinecraftVersionMetadata parse(String json, String sourceDescription, MinecraftSide side)
      throws LoaderException {
    JsonObject root = parseObject(json, sourceDescription);
    String id =
        requireString(root, "id", "Minecraft version metadata is missing id: " + sourceDescription);
    String mainClass = getString(root, "mainClass");
    if (side == MinecraftSide.CLIENT && (mainClass == null || mainClass.isBlank())) {
      throw new LoaderException(
          "Minecraft client version metadata is missing mainClass: " + sourceDescription);
    }

    return new MinecraftVersionMetadata(
        id,
        getString(root, "type"),
        mainClass,
        getString(root, "assets"),
        parseAssetIndex(root.getAsJsonObject("assetIndex")),
        parseDownload(getObject(getObject(root, "downloads"), "client")),
        parseDownload(getObject(getObject(root, "downloads"), "server")),
        parseLibraries(getArray(root, "libraries")),
        parseArguments(root),
        getString(root, "minecraftArguments"));
  }

  private JsonObject parseObject(String json, String sourceDescription) throws LoaderException {
    try {
      JsonElement root = JsonParser.parseString(json);
      if (!root.isJsonObject()) {
        throw new LoaderException(
            "Minecraft version metadata must be a JSON object: " + sourceDescription);
      }
      return root.getAsJsonObject();
    } catch (JsonParseException exception) {
      throw new LoaderException(
          "Invalid Minecraft version metadata JSON: " + sourceDescription, exception);
    }
  }

  private MinecraftVersionMetadata.AssetIndex parseAssetIndex(JsonObject assetIndexObject) {
    if (assetIndexObject == null) {
      return null;
    }
    return new MinecraftVersionMetadata.AssetIndex(
        getString(assetIndexObject, "id"),
        getString(assetIndexObject, "url"),
        getString(assetIndexObject, "sha1"),
        getLong(assetIndexObject, "size"));
  }

  private MinecraftVersionMetadata.Download parseDownload(JsonObject downloadObject) {
    if (downloadObject == null) {
      return null;
    }
    return new MinecraftVersionMetadata.Download(
        getString(downloadObject, "path"),
        getString(downloadObject, "url"),
        getString(downloadObject, "sha1"),
        getLong(downloadObject, "size"));
  }

  private List<MinecraftVersionMetadata.Library> parseLibraries(JsonArray librariesArray) {
    if (librariesArray == null) {
      return List.of();
    }

    List<MinecraftVersionMetadata.Library> libraries = new ArrayList<>();
    for (JsonElement element : librariesArray) {
      if (!element.isJsonObject()) {
        continue;
      }
      JsonObject libraryObject = element.getAsJsonObject();
      JsonObject downloadsObject = getObject(libraryObject, "downloads");
      libraries.add(
          new MinecraftVersionMetadata.Library(
              getString(libraryObject, "name"),
              parseDownload(getObject(downloadsObject, "artifact")),
              parseDownloadMap(getObject(downloadsObject, "classifiers")),
              parseRules(getArray(libraryObject, "rules")),
              parseStringMap(getObject(libraryObject, "natives"))));
    }
    return libraries;
  }

  private MinecraftVersionMetadata.Arguments parseArguments(JsonObject root) {
    JsonObject argumentsObject = getObject(root, "arguments");
    if (argumentsObject == null) {
      return new MinecraftVersionMetadata.Arguments(List.of(), List.of());
    }
    return new MinecraftVersionMetadata.Arguments(
        parseArgumentArray(getArray(argumentsObject, "game")),
        parseArgumentArray(getArray(argumentsObject, "jvm")));
  }

  private List<MinecraftVersionMetadata.Argument> parseArgumentArray(JsonArray array) {
    if (array == null) {
      return List.of();
    }

    List<MinecraftVersionMetadata.Argument> arguments = new ArrayList<>();
    for (JsonElement element : array) {
      if (element.isJsonPrimitive()) {
        arguments.add(
            new MinecraftVersionMetadata.Argument(List.of(), List.of(element.getAsString())));
        continue;
      }
      if (!element.isJsonObject()) {
        continue;
      }
      JsonObject object = element.getAsJsonObject();
      JsonElement valueElement = object.get("value");
      List<String> values = new ArrayList<>();
      if (valueElement != null) {
        if (valueElement.isJsonPrimitive()) {
          values.add(valueElement.getAsString());
        } else if (valueElement.isJsonArray()) {
          for (JsonElement arrayElement : valueElement.getAsJsonArray()) {
            if (arrayElement.isJsonPrimitive()) {
              values.add(arrayElement.getAsString());
            }
          }
        }
      }
      arguments.add(
          new MinecraftVersionMetadata.Argument(parseRules(getArray(object, "rules")), values));
    }
    return arguments;
  }

  private List<MinecraftVersionMetadata.Rule> parseRules(JsonArray rulesArray) {
    if (rulesArray == null) {
      return List.of();
    }

    List<MinecraftVersionMetadata.Rule> rules = new ArrayList<>();
    for (JsonElement element : rulesArray) {
      if (!element.isJsonObject()) {
        continue;
      }
      JsonObject ruleObject = element.getAsJsonObject();
      JsonObject osObject = getObject(ruleObject, "os");
      rules.add(
          new MinecraftVersionMetadata.Rule(
              getString(ruleObject, "action"),
              getString(osObject, "name"),
              getString(osObject, "arch"),
              getString(osObject, "version")));
    }
    return rules;
  }

  private Map<String, MinecraftVersionMetadata.Download> parseDownloadMap(JsonObject object) {
    if (object == null) {
      return Map.of();
    }

    Map<String, MinecraftVersionMetadata.Download> downloads = new LinkedHashMap<>();
    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      if (entry.getValue().isJsonObject()) {
        downloads.put(entry.getKey(), parseDownload(entry.getValue().getAsJsonObject()));
      }
    }
    return downloads;
  }

  private Map<String, String> parseStringMap(JsonObject object) {
    if (object == null) {
      return Map.of();
    }

    Map<String, String> values = new LinkedHashMap<>();
    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      if (entry.getValue().isJsonPrimitive()) {
        values.put(entry.getKey(), entry.getValue().getAsString());
      }
    }
    return values;
  }

  private JsonObject getObject(JsonObject object, String memberName) {
    JsonElement element = object == null ? null : object.get(memberName);
    return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
  }

  private JsonArray getArray(JsonObject object, String memberName) {
    JsonElement element = object == null ? null : object.get(memberName);
    return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
  }

  private String getString(JsonObject object, String memberName) {
    JsonElement element = object == null ? null : object.get(memberName);
    return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
  }

  private String requireString(JsonObject object, String memberName, String message)
      throws LoaderException {
    String value = getString(object, memberName);
    if (value == null || value.isBlank()) {
      throw new LoaderException(message);
    }
    return value;
  }

  private long getLong(JsonObject object, String memberName) {
    JsonElement element = object == null ? null : object.get(memberName);
    return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
        ? element.getAsLong()
        : 0L;
  }
}
