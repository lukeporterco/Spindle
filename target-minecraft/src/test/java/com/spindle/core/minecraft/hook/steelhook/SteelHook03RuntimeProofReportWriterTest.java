package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook03RuntimeProofReportWriterTest {
  @TempDir Path tempDir;

  @Test
  void reportContainsNoRawClassBytesAndNextDirectionPointsToTarget31() throws Exception {
    SteelHook03RuntimeProofReport report =
        new SteelHook03GatedRuntimeProofRunner()
            .run(
                SteelHook03TestFixtures.runtimePlan(tempDir.resolve("unused.jar")),
                SteelHook03TestFixtures.passedTarget28Report(),
                SteelHook03TestFixtures.passedTarget29Report(),
                tempDir);
    Path outputPath =
        tempDir.resolve("minecraft-steelhook-0-3-generalized-transformer-gated-runtime-proof.json");

    new SteelHook03RuntimeProofReportWriter().write(outputPath, report);

    String json = Files.readString(outputPath, StandardCharsets.UTF_8);
    assertTrue(json.contains("\"milestoneName\": \"Target-30\""));
    assertTrue(json.contains("\"status\": \"gated-runtime-proof-ready\""));
    assertTrue(json.contains("\"nextDirection\": \"move-to-target-31-steelhook-0-3-completion\""));
    assertTrue(json.contains("\"runtimeClassLoaderSuccessCount\": 2"));
    assertTrue(json.contains("\"classInitialized\": false"));
    assertTrue(json.contains("\"minecraftMainInvoked\": false"));
    assertTrue(json.contains("\"serverLaunchOccurred\": false"));
    assertTrue(json.contains("\"hookInstallationOccurred\": false"));
    assertTrue(json.contains("\"runtimeDispatchOccurred\": false"));
    assertFalse(json.contains("\"transformedClassBytes\":"));
    assertFalse(json.contains("\"classBytes\":"));
  }
}
