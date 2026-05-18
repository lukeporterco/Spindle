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

public final class SteelHook04RuntimeFixtureJarWriter {
  public Path write(Path outputPath, String classEntryName, byte[] classBytes)
      throws LoaderException {
    if (outputPath == null) {
      throw new LoaderException("Target-35 requires an output path for the runtime fixture jar.");
    }
    if (classEntryName == null || classEntryName.isBlank()) {
      throw new LoaderException("Target-35 requires a fixture class entry name.");
    }
    if (classBytes == null || classBytes.length == 0) {
      throw new LoaderException("Target-35 requires controlled fixture class bytes.");
    }
    try {
      Path parent = outputPath.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
      try (OutputStream outputStream = Files.newOutputStream(outputPath);
          JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest)) {
        jarOutputStream.putNextEntry(new JarEntry(classEntryName));
        jarOutputStream.write(classBytes);
        jarOutputStream.closeEntry();
      }
      return outputPath;
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to write SteelHook 0.4 runtime fixture jar " + outputPath.getFileName(),
          exception);
    }
  }
}
