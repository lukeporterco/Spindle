package com.mcmodloader.core.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mcmodloader.core.diagnostics.LoaderException;
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

    JsonObject loader = new JsonObject();
    loader.addProperty("id", profile.loader().id());
    loader.addProperty("version", profile.loader().version());
    root.add("loader", loader);

    JsonObject game = new JsonObject();
    game.addProperty("id", profile.game().id());
    game.addProperty("version", profile.game().version());
    game.addProperty("side", profile.game().side());
    root.add("game", game);

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
