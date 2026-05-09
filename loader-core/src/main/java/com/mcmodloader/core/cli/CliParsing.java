package com.mcmodloader.core.cli;

import com.mcmodloader.core.diagnostics.LoaderException;
import java.nio.file.Path;

final class CliParsing {
  private CliParsing() {}

  static int parsePositiveInt(String value, String argumentName) throws LoaderException {
    try {
      int parsed = Integer.parseInt(value);
      if (parsed <= 0) {
        throw new LoaderException(argumentName + " requires a positive integer");
      }
      return parsed;
    } catch (NumberFormatException exception) {
      throw new LoaderException(argumentName + " requires a positive integer", exception);
    }
  }

  static Path resolveOptionalPath(Path workingDirectory, Path path) {
    if (path == null) {
      return null;
    }
    if (path.isAbsolute()) {
      return path.toAbsolutePath().normalize();
    }
    return workingDirectory.resolve(path).toAbsolutePath().normalize();
  }

  static String requireValue(String[] args, int index, String argumentName) throws LoaderException {
    if (index + 1 >= args.length) {
      throw new LoaderException("Missing value for " + argumentName);
    }
    return args[index + 1];
  }
}
