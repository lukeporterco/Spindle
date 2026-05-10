package com.spindle.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class RepoStructureBoundaryTest {
  @Test
  void spindleLoaderCoreExcludesMinecraftTargetSourceDirectories() {
    Path sourceRoot = Path.of("src/main/java/com/spindle/core");
    assertFalse(Files.exists(sourceRoot.resolve("artifact")));
    assertFalse(Files.exists(sourceRoot.resolve("baseline")));
    assertFalse(Files.exists(sourceRoot.resolve("mache")));
    assertFalse(Files.exists(sourceRoot.resolve("minecraft")));
    Path processRoot = sourceRoot.resolve("process");
    assertTrue(Files.exists(processRoot));
    assertFalse(Files.exists(processRoot.resolve("MinecraftProcessConfig.java")));
    assertFalse(Files.exists(processRoot.resolve("MinecraftProcessResult.java")));
    assertFalse(Files.exists(processRoot.resolve("MinecraftProcessResultWriter.java")));
    assertFalse(Files.exists(processRoot.resolve("MinecraftServerProcessLauncher.java")));
    assertFalse(Files.exists(sourceRoot.resolve("app")));
    assertFalse(Files.exists(sourceRoot.resolve("cli")));
  }

  @Test
  void spindleLoaderCoreHasNoDirectMinecraftTargetImports() throws IOException {
    Path sourceRoot = Path.of("src/main/java");
    try (Stream<Path> paths = Files.walk(sourceRoot)) {
      List<String> forbiddenImports =
          paths
              .filter(path -> path.toString().endsWith(".java"))
              .flatMap(
                  path -> {
                    try {
                      return Files.readAllLines(path).stream();
                    } catch (IOException exception) {
                      throw new RuntimeException(exception);
                    }
                  })
              .filter(line -> line.startsWith("import com.spindle.core."))
              .filter(
                  line ->
                      line.contains(".artifact.")
                          || line.contains(".baseline.")
                          || line.contains(".mache.")
                          || line.contains(".minecraft.")
                          || (line.contains(".process.")
                              && !line.contains(".process.JavaExecutableResolver;")
                              && !line.contains(".process.ProcessOutputCapture;")))
              .toList();
      assertTrue(forbiddenImports.isEmpty(), () -> "Forbidden imports: " + forbiddenImports);
    }
  }

  @Test
  void loaderDocsUseSpindleLoaderIdentityWhileEcosystemDocsKeepSpindleBrand() throws IOException {
    String readme = Files.readString(Path.of("../README.md"));
    String agents = Files.readString(Path.of("../AGENTS.md"));

    assertTrue(readme.contains("Spindle Loader"));
    assertTrue(readme.contains("spindle-loader-api"));
    assertTrue(readme.contains("target-minecraft"));
    assertTrue(agents.contains("spindle-loader-api"));
    assertTrue(agents.contains("spindle-loader-core"));
    assertTrue(readme.contains("Spindle Ecosystem"));
  }
}
