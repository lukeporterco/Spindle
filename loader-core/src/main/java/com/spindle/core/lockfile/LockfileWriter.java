package com.spindle.core.lockfile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.resolve.ResolvedModSet;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LockfileWriter {
  private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  public void write(Path lockfilePath, LaunchContext context, ResolvedModSet resolvedModSet)
      throws LoaderException {
    Lockfile lockfile = Lockfile.from(context, resolvedModSet);
    try {
      Files.createDirectories(lockfilePath.getParent());
      try (Writer writer = Files.newBufferedWriter(lockfilePath, StandardCharsets.UTF_8)) {
        gson.toJson(lockfile, writer);
      }
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write lockfile " + lockfilePath.getFileName(), exception);
    }
  }
}
