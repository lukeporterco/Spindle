package com.spindle.core.minecraft.hook.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftBootstrapHookTransformationResultWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftBootstrapHookTransformationResultWriter writer =
      new MinecraftBootstrapHookTransformationResultWriter();

  @Test
  void writerOutputIsDeterministic() throws Exception {
    MinecraftBootstrapHookTransformer transformer =
        new MinecraftBootstrapHookTransformer(
            MinecraftBootstrapHookTransformerTest.validPatchPlan());
    MinecraftBootstrapHookTransformationResult result = transformer.withRuntimeObservation(1, true);

    if (result == null) {
      result =
          transformer.transform(
              "net.minecraft.server.Main",
              MinecraftBootstrapHookTransformerTest.fixtureClassBytes(
                  "net/minecraft/server/Main", "([Ljava/lang/String;)V", true, false));
      result = transformer.withRuntimeObservation(1, true);
    }

    Path first = tempDirectory.resolve("one/minecraft-hook-bootstrap-transformation-result.json");
    Path second = tempDirectory.resolve("two/minecraft-hook-bootstrap-transformation-result.json");

    writer.write(first, result);
    writer.write(second, result);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-9\""));
    assertTrue(
        firstJson.contains(
            "\"transformationMode\": \"bootstrap-fake-server-method-entry-transform\""));
    assertTrue(firstJson.contains("\"dispatcherInvocationCount\": 1"));
    assertTrue(firstJson.contains("\"dispatcherInvocationObserved\": true"));
  }
}
