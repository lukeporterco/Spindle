package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook03FramedMethodFoundationReportWriterTest {
  @TempDir Path tempDir;

  @Test
  void reportContainsNoRawClassBytes() throws Exception {
    SteelHook03FramedMethodFoundationReport report =
        new SteelHook03FramedMethodFoundationRunner()
            .run(
                SteelHook03TestFixtures.passedCompletionReport(),
                SteelHook03TestFixtures.framedFixtureClassBytes());
    Path outputPath = tempDir.resolve("minecraft-steelhook-0-3-framed-method-foundation.json");

    new SteelHook03FramedMethodFoundationReportWriter().write(outputPath, report);

    String json = Files.readString(outputPath, StandardCharsets.UTF_8);
    assertTrue(json.contains("\"stackMapTableRewriteApplied\": true"));
    assertFalse(json.contains("\"transformedClassBytes\":"));
    assertFalse(json.contains("\"classBytes\":"));
    assertFalse(json.contains("\"rawStackMapTable\":"));
  }
}
