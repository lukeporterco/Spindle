package com.mcmodloader.core.io;

import com.mcmodloader.core.diagnostics.LoaderException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ProcessOutputReader {
  private ProcessOutputReader() {}

  public static String readFile(Path path) throws LoaderException {
    try {
      return Files.readString(path);
    } catch (IOException exception) {
      throw new LoaderException("Failed to read " + path.toString().replace('\\', '/'), exception);
    }
  }

  public static String readProcessStream(InputStream inputStream) throws LoaderException {
    try (inputStream) {
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new LoaderException("Failed to read child process stream", exception);
    }
  }
}
