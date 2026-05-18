package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook03MethodExitDispatchReportWriterTest {
  @TempDir Path tempDir;

  @Test
  void reportContainsPrimitiveKindAndNoRawClassBytes() throws Exception {
    SteelHook03MethodExitDispatchReport report =
        new SteelHook03MethodExitDispatchRunner()
            .run(
                SteelHook03TestFixtures.passedTarget28Report(),
                SteelHook03TestFixtures.methodExitFixtureClassBytes());
    Path outputPath = tempDir.resolve("minecraft-steelhook-0-3-method-exit-static-dispatch.json");

    new SteelHook03MethodExitDispatchReportWriter().write(outputPath, report);

    String json = Files.readString(outputPath, StandardCharsets.UTF_8);
    assertTrue(json.contains("\"primitiveKind\": \"METHOD_EXIT_STATIC_DISPATCH\""));
    assertTrue(json.contains("\"insertionCount\": 1"));
    assertTrue(json.contains("\"normalReturnOpcodeCount\": 1"));
    assertTrue(json.contains("\"insertedInstructionLength\": 3"));
    assertTrue(json.contains("\"runtimeClassLoadingPathEnabled\": false"));
    assertTrue(json.contains("\"classLoadingOccurred\": false"));
    assertTrue(json.contains("\"serverLaunchOccurred\": false"));
    assertTrue(json.contains("\"hookInstallationOccurred\": false"));
    assertTrue(json.contains("\"runtimeDispatchOccurred\": false"));
    assertTrue(json.contains("\"publicApiExposed\": false"));
    assertTrue(json.contains("\"javaAgentUsed\": false"));
    assertTrue(json.contains("\"mixinUsed\": false"));
    assertTrue(json.contains("\"javaModExecutionSandboxed\": false"));
    assertFalse(json.contains("classBytes"));
  }
}
