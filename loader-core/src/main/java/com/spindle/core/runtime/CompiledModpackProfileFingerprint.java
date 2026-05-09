package com.spindle.core.runtime;

import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class CompiledModpackProfileFingerprint {
  public String compute(CompiledModpackProfile profile) throws LoaderException {
    MessageDigest digest = createDigest();
    update(digest, "schemaVersion", Integer.toString(profile.schemaVersion()));
    update(digest, "profileKind", profile.profileKind());
    update(digest, "loader.id", profile.loader().id());
    update(digest, "loader.version", profile.loader().version());
    update(digest, "game.id", profile.game().id());
    update(digest, "game.version", profile.game().version());
    update(digest, "game.side", profile.game().side());

    for (CompiledModpackProfile.Mod mod : profile.mods()) {
      update(digest, "mod.id", mod.id());
      update(digest, "mod.version", mod.version());
      update(digest, "mod.path", mod.path());
      update(digest, "mod.hash", mod.hash());
    }

    for (String modId : profile.resolvedOrder()) {
      update(digest, "resolvedOrder", modId);
    }

    for (CompiledModpackProfile.ClasspathEntry entry : profile.classpath()) {
      update(digest, "classpath.path", entry.path());
      update(digest, "classpath.owner", entry.owner());
    }

    update(digest, "ownership.classes.count", Integer.toString(profile.ownership().classes().count()));
    update(
        digest,
        "ownership.packages.count",
        Integer.toString(profile.ownership().packages().count()));
    update(
        digest,
        "ownership.resources.duplicates",
        Integer.toString(profile.ownership().resources().duplicates()));
    update(digest, "lockfile.mode", profile.lockfile().mode());
    update(digest, "lockfile.path", profile.lockfile().path());
    update(digest, "lockfile.fingerprint", profile.lockfile().fingerprint());
    return HexFormat.of().formatHex(digest.digest());
  }

  public static String fromFile(Path path) throws LoaderException {
    try {
      return sha256(Files.readAllBytes(path));
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to read compiled profile fingerprint input "
              + path.toString().replace('\\', '/'),
          exception);
    }
  }

  public static String sha256(byte[] bytes) throws LoaderException {
    return HexFormat.of().formatHex(createDigest().digest(bytes));
  }

  private static MessageDigest createDigest() throws LoaderException {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException exception) {
      throw new LoaderException("SHA-256 algorithm unavailable", exception);
    }
  }

  private static void update(MessageDigest digest, String key, String value) {
    digest.update(key.getBytes(StandardCharsets.UTF_8));
    digest.update((byte) '=');
    digest.update((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    digest.update((byte) '\n');
  }
}
