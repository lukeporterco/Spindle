package com.spindle.core.minecraft;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TargetLayerBoundaryPrepTest {
  @Test
  void targetLayerBoundaryDocExistsAndNamesThePlannedLayers() throws IOException {
    Path docPath = resolveDocPath();

    assertTrue(Files.exists(docPath), () -> "Missing architecture doc: " + docPath);
    String content = Files.readString(docPath, StandardCharsets.UTF_8);

    assertTrue(content.contains("Injection Hook Subsystem"));
    assertTrue(content.contains("Target Layer API"));
    assertTrue(content.contains("Modding API"));
    assertTrue(content.contains("com.spindle.core.minecraft.hook"));
    assertTrue(content.contains("com.spindle.api.minecraft.target"));
  }

  private Path resolveDocPath() {
    Path local = Path.of("docs/architecture/target-layer-api-boundary.md");
    if (Files.exists(local)) {
      return local;
    }
    return Path.of("../docs/architecture/target-layer-api-boundary.md");
  }
}
