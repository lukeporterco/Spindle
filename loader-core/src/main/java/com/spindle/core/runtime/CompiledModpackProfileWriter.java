package com.spindle.core.runtime;

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

public final class CompiledModpackProfileWriter {
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  public CompiledModpackProfileResult write(Path outputPath, CompiledModpackProfile profile)
      throws LoaderException {
    JsonObject root = new JsonObject();
    root.addProperty("schemaVersion", profile.schemaVersion());
    root.addProperty("profileKind", profile.profileKind());
    root.addProperty("fingerprint", profile.fingerprint());
    root.addProperty("inputFingerprint", profile.inputFingerprint());

    JsonObject cache = new JsonObject();
    cache.addProperty("status", profile.cache().status());
    cache.addProperty("reason", profile.cache().reason());
    root.add("cache", cache);

    JsonObject loader = new JsonObject();
    loader.addProperty("id", profile.loader().id());
    loader.addProperty("version", profile.loader().version());
    root.add("loader", loader);

    JsonObject game = new JsonObject();
    game.addProperty("id", profile.game().id());
    game.addProperty("version", profile.game().version());
    game.addProperty("side", profile.game().side());
    root.add("game", game);

    JsonObject metadata = new JsonObject();
    JsonArray schemaVersions = new JsonArray();
    for (Integer schemaVersion : profile.metadata().schemaVersions()) {
      schemaVersions.add(schemaVersion);
    }
    metadata.add("schemaVersions", schemaVersions);
    root.add("metadata", metadata);

    JsonArray mods = new JsonArray();
    for (CompiledModpackProfile.Mod mod : profile.mods()) {
      JsonObject modObject = new JsonObject();
      modObject.addProperty("id", mod.id());
      modObject.addProperty("version", mod.version());
      modObject.addProperty("path", mod.path());
      modObject.addProperty("hash", mod.hash());
      mods.add(modObject);
    }
    root.add("mods", mods);

    JsonArray resolvedOrder = new JsonArray();
    for (String modId : profile.resolvedOrder()) {
      resolvedOrder.add(modId);
    }
    root.add("resolvedOrder", resolvedOrder);

    JsonArray classpath = new JsonArray();
    for (CompiledModpackProfile.ClasspathEntry entry : profile.classpath()) {
      JsonObject classpathObject = new JsonObject();
      classpathObject.addProperty("path", entry.path());
      classpathObject.addProperty("owner", entry.owner());
      classpath.add(classpathObject);
    }
    root.add("classpath", classpath);

    JsonObject ownership = new JsonObject();
    JsonObject classes = new JsonObject();
    classes.addProperty("count", profile.ownership().classes().count());
    ownership.add("classes", classes);
    JsonObject packages = new JsonObject();
    packages.addProperty("count", profile.ownership().packages().count());
    ownership.add("packages", packages);
    JsonObject resources = new JsonObject();
    resources.addProperty("duplicates", profile.ownership().resources().duplicates());
    ownership.add("resources", resources);
    root.add("ownership", ownership);

    JsonObject lockfile = new JsonObject();
    lockfile.addProperty("mode", profile.lockfile().mode());
    lockfile.addProperty("path", profile.lockfile().path());
    lockfile.addProperty("fingerprint", profile.lockfile().fingerprint());
    root.add("lockfile", lockfile);

    JsonObject lifecycle = new JsonObject();
    JsonArray phaseOrder = new JsonArray();
    for (String phase : profile.lifecycle().phaseOrder()) {
      phaseOrder.add(phase);
    }
    lifecycle.add("phaseOrder", phaseOrder);
    JsonArray handlers = new JsonArray();
    for (CompiledModpackProfile.LifecycleHandler handler : profile.lifecycle().handlers()) {
      JsonObject handlerObject = new JsonObject();
      handlerObject.addProperty("phase", handler.phase());
      handlerObject.addProperty("modId", handler.modId());
      handlerObject.addProperty("ownerModId", handler.ownerModId());
      handlerObject.addProperty("kind", handler.kind());
      handlerObject.addProperty("className", handler.className());
      handlerObject.addProperty("methodName", handler.methodName());
      if (handler.interfaceName() != null) {
        handlerObject.addProperty("interfaceName", handler.interfaceName());
      }
      handlerObject.addProperty("jarPath", handler.jarPath());
      handlerObject.addProperty("jarHash", handler.jarHash());
      handlers.add(handlerObject);
    }
    lifecycle.add("handlers", handlers);
    root.add("lifecycle", lifecycle);

    JsonObject contexts = new JsonObject();
    JsonArray contextMods = new JsonArray();
    for (CompiledModpackProfile.ModContextPlan context : profile.contexts().mods()) {
      JsonObject contextObject = new JsonObject();
      contextObject.addProperty("modId", context.modId());
      JsonObject storage = new JsonObject();
      storage.addProperty("config", context.storage().config());
      storage.addProperty("data", context.storage().data());
      storage.addProperty("cache", context.storage().cache());
      storage.addProperty("generated", context.storage().generated());
      contextObject.add("storage", storage);
      contextObject.addProperty("configDirectory", context.configDirectory());
      contextObject.addProperty("dataDirectory", context.dataDirectory());
      contextObject.addProperty("cacheDirectory", context.cacheDirectory());
      contextObject.addProperty("generatedDirectory", context.generatedDirectory());
      contextMods.add(contextObject);
    }
    contexts.add("mods", contextMods);
    root.add("contexts", contexts);

    JsonObject packagePolicy = new JsonObject();
    JsonArray protectedPackages = new JsonArray();
    for (String protectedPackage : profile.packagePolicy().protectedPackages()) {
      protectedPackages.add(protectedPackage);
    }
    packagePolicy.add("protectedPackages", protectedPackages);
    JsonArray splitPackages = new JsonArray();
    for (CompiledModpackProfile.SplitPackage splitPackage : profile.packagePolicy().splitPackages()) {
      JsonObject splitPackageObject = new JsonObject();
      splitPackageObject.addProperty("packageName", splitPackage.packageName());
      JsonArray modIds = new JsonArray();
      for (String modId : splitPackage.modIds()) {
        modIds.add(modId);
      }
      splitPackageObject.add("modIds", modIds);
      splitPackages.add(splitPackageObject);
    }
    packagePolicy.add("splitPackages", splitPackages);
    JsonArray duplicateClasses = new JsonArray();
    for (String duplicateClass : profile.packagePolicy().duplicateClasses()) {
      duplicateClasses.add(duplicateClass);
    }
    packagePolicy.add("duplicateClasses", duplicateClasses);
    JsonArray packageOwners = new JsonArray();
    for (CompiledModpackProfile.PackageOwner owner : profile.packagePolicy().packageOwners()) {
      JsonObject ownerObject = new JsonObject();
      ownerObject.addProperty("packageName", owner.packageName());
      JsonArray modIds = new JsonArray();
      for (String modId : owner.modIds()) {
        modIds.add(modId);
      }
      ownerObject.add("modIds", modIds);
      packageOwners.add(ownerObject);
    }
    packagePolicy.add("packageOwners", packageOwners);
    JsonArray fatalViolations = new JsonArray();
    for (ProtectedPackageViolation violation : profile.packagePolicy().fatalViolations()) {
      JsonObject violationObject = new JsonObject();
      violationObject.addProperty("modId", violation.modId());
      violationObject.addProperty("packageName", violation.packageName());
      violationObject.addProperty("reason", violation.reason());
      fatalViolations.add(violationObject);
    }
    packagePolicy.add("fatalViolations", fatalViolations);
    root.add("packagePolicy", packagePolicy);

    JsonObject quality = new JsonObject();
    quality.addProperty("score", profile.quality().score());
    quality.addProperty("fatalCount", profile.quality().fatalCount());
    quality.addProperty("warningCount", profile.quality().warningCount());
    root.add("quality", quality);

    try {
      Files.createDirectories(outputPath.getParent());
      try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
        gson.toJson(root, writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write compiled modpack profile " + outputPath.getFileName(), exception);
    }

    return new CompiledModpackProfileResult(
        outputPath, profile.schemaVersion(), profile.profileKind(), profile.fingerprint());
  }
}
