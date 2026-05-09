package com.spindle.core.runtime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CompiledModpackProfileReader {
  public CompiledModpackProfile read(Path path) throws LoaderException {
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
      return new CompiledModpackProfile(
          requiredInt(root, "schemaVersion"),
          requiredString(root, "profileKind"),
          requiredString(root, "fingerprint"),
          requiredString(root, "inputFingerprint"),
          readCache(requiredObject(root, "cache")),
          readLoader(requiredObject(root, "loader")),
          readGame(requiredObject(root, "game")),
          readMetadata(requiredObject(root, "metadata")),
          readMods(requiredArray(root, "mods")),
          readStringArray(requiredArray(root, "resolvedOrder")),
          readClasspath(requiredArray(root, "classpath")),
          readOwnership(requiredObject(root, "ownership")),
          readLockfile(requiredObject(root, "lockfile")),
          readLifecycle(requiredObject(root, "lifecycle")),
          readContexts(requiredObject(root, "contexts")),
          readPackagePolicy(requiredObject(root, "packagePolicy")),
          readQuality(requiredObject(root, "quality")));
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to read compiled modpack profile " + path.toString().replace('\\', '/'),
          exception);
    }
  }

  private CompiledModpackProfile.Cache readCache(JsonObject object) {
    return new CompiledModpackProfile.Cache(requiredString(object, "status"), requiredString(object, "reason"));
  }

  private CompiledModpackProfile.Loader readLoader(JsonObject object) {
    return new CompiledModpackProfile.Loader(requiredString(object, "id"), requiredString(object, "version"));
  }

  private CompiledModpackProfile.Game readGame(JsonObject object) {
    return new CompiledModpackProfile.Game(
        requiredString(object, "id"),
        requiredString(object, "version"),
        requiredString(object, "side"));
  }

  private CompiledModpackProfile.Metadata readMetadata(JsonObject object) {
    List<Integer> schemaVersions = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "schemaVersions")) {
      schemaVersions.add(element.getAsInt());
    }
    return new CompiledModpackProfile.Metadata(schemaVersions);
  }

  private List<CompiledModpackProfile.Mod> readMods(JsonArray array) {
    List<CompiledModpackProfile.Mod> mods = new ArrayList<>();
    for (JsonElement element : array) {
      JsonObject object = element.getAsJsonObject();
      mods.add(
          new CompiledModpackProfile.Mod(
              requiredString(object, "id"),
              requiredString(object, "version"),
              requiredString(object, "path"),
              requiredString(object, "hash")));
    }
    return List.copyOf(mods);
  }

  private List<String> readStringArray(JsonArray array) {
    List<String> values = new ArrayList<>();
    for (JsonElement element : array) {
      values.add(element.getAsString());
    }
    return List.copyOf(values);
  }

  private List<CompiledModpackProfile.ClasspathEntry> readClasspath(JsonArray array) {
    List<CompiledModpackProfile.ClasspathEntry> entries = new ArrayList<>();
    for (JsonElement element : array) {
      JsonObject object = element.getAsJsonObject();
      entries.add(
          new CompiledModpackProfile.ClasspathEntry(
              requiredString(object, "path"), requiredString(object, "owner")));
    }
    return List.copyOf(entries);
  }

  private CompiledModpackProfile.Ownership readOwnership(JsonObject object) {
    JsonObject classes = requiredObject(object, "classes");
    JsonObject packages = requiredObject(object, "packages");
    JsonObject resources = requiredObject(object, "resources");
    return new CompiledModpackProfile.Ownership(
        new CompiledModpackProfile.Count(requiredInt(classes, "count")),
        new CompiledModpackProfile.Count(requiredInt(packages, "count")),
        new CompiledModpackProfile.Resources(requiredInt(resources, "duplicates")));
  }

  private CompiledModpackProfile.Lockfile readLockfile(JsonObject object) {
    return new CompiledModpackProfile.Lockfile(
        requiredString(object, "mode"),
        requiredString(object, "path"),
        requiredString(object, "fingerprint"));
  }

  private CompiledModpackProfile.Lifecycle readLifecycle(JsonObject object) {
    List<String> phaseOrder = readStringArray(requiredArray(object, "phaseOrder"));
    List<CompiledModpackProfile.LifecycleHandler> handlers = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "handlers")) {
      JsonObject handler = element.getAsJsonObject();
      handlers.add(
          new CompiledModpackProfile.LifecycleHandler(
              requiredString(handler, "phase"),
              requiredString(handler, "modId"),
              requiredString(handler, "ownerModId"),
              requiredString(handler, "kind"),
              requiredString(handler, "className"),
              requiredString(handler, "methodName"),
              optionalString(handler, "interfaceName"),
              requiredString(handler, "jarPath"),
              requiredString(handler, "jarHash")));
    }
    return new CompiledModpackProfile.Lifecycle(phaseOrder, handlers);
  }

  private CompiledModpackProfile.Contexts readContexts(JsonObject object) {
    List<CompiledModpackProfile.ModContextPlan> contexts = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "mods")) {
      JsonObject context = element.getAsJsonObject();
      JsonObject storage = requiredObject(context, "storage");
      contexts.add(
          new CompiledModpackProfile.ModContextPlan(
              requiredString(context, "modId"),
              new CompiledModpackProfile.Storage(
                  requiredBoolean(storage, "config"),
                  requiredBoolean(storage, "data"),
                  requiredBoolean(storage, "cache"),
                  requiredBoolean(storage, "generated")),
              requiredString(context, "configDirectory"),
              requiredString(context, "dataDirectory"),
              requiredString(context, "cacheDirectory"),
              requiredString(context, "generatedDirectory")));
    }
    return new CompiledModpackProfile.Contexts(contexts);
  }

  private CompiledModpackProfile.PackagePolicy readPackagePolicy(JsonObject object) {
    List<CompiledModpackProfile.SplitPackage> splitPackages = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "splitPackages")) {
      JsonObject splitPackage = element.getAsJsonObject();
      splitPackages.add(
          new CompiledModpackProfile.SplitPackage(
              requiredString(splitPackage, "packageName"),
              readStringArray(requiredArray(splitPackage, "modIds"))));
    }

    List<CompiledModpackProfile.PackageOwner> packageOwners = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "packageOwners")) {
      JsonObject packageOwner = element.getAsJsonObject();
      packageOwners.add(
          new CompiledModpackProfile.PackageOwner(
              requiredString(packageOwner, "packageName"),
              readStringArray(requiredArray(packageOwner, "modIds"))));
    }

    List<ProtectedPackageViolation> fatalViolations = new ArrayList<>();
    for (JsonElement element : requiredArray(object, "fatalViolations")) {
      JsonObject violation = element.getAsJsonObject();
      fatalViolations.add(
          new ProtectedPackageViolation(
              requiredString(violation, "modId"),
              requiredString(violation, "packageName"),
              requiredString(violation, "reason")));
    }

    return new CompiledModpackProfile.PackagePolicy(
        readStringArray(requiredArray(object, "protectedPackages")),
        splitPackages,
        readStringArray(requiredArray(object, "duplicateClasses")),
        packageOwners,
        fatalViolations);
  }

  private CompiledModpackProfile.Quality readQuality(JsonObject object) {
    return new CompiledModpackProfile.Quality(
        requiredInt(object, "score"),
        requiredInt(object, "fatalCount"),
        requiredInt(object, "warningCount"));
  }

  private JsonObject requiredObject(JsonObject object, String key) {
    return object.getAsJsonObject(key);
  }

  private JsonArray requiredArray(JsonObject object, String key) {
    return object.getAsJsonArray(key);
  }

  private String requiredString(JsonObject object, String key) {
    return object.get(key).getAsString();
  }

  private String optionalString(JsonObject object, String key) {
    return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : null;
  }

  private int requiredInt(JsonObject object, String key) {
    return object.get(key).getAsInt();
  }

  private boolean requiredBoolean(JsonObject object, String key) {
    return object.get(key).getAsBoolean();
  }
}
