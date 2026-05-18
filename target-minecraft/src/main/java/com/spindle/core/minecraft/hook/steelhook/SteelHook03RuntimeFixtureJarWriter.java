package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class SteelHook03RuntimeFixtureJarWriter {
  public Path write(Path outputPath, byte[] mainClassBytes) throws LoaderException {
    if (outputPath == null) {
      throw new LoaderException("Target-30 requires an output path for the runtime fixture jar.");
    }
    if (mainClassBytes == null || mainClassBytes.length == 0) {
      throw new LoaderException("Target-30 requires controlled fixture class bytes.");
    }
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
      manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "net.minecraft.server.Main");
      try (OutputStream outputStream = Files.newOutputStream(outputPath);
          JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest)) {
        jarOutputStream.putNextEntry(new JarEntry("net/minecraft/server/Main.class"));
        jarOutputStream.write(mainClassBytes);
        jarOutputStream.closeEntry();
      }
      return outputPath;
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write SteelHook 0.3 runtime fixture jar " + outputPath.getFileName(),
          exception);
    }
  }
}
