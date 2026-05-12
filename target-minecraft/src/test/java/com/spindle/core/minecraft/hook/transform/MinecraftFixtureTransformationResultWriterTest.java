package com.spindle.core.minecraft.hook.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftFixtureTransformationResultWriterTest {
  @TempDir Path tempDirectory;

  private final MinecraftFixtureBytecodeTransformer transformer =
      new MinecraftFixtureBytecodeTransformer();
  private final MinecraftFixtureTransformationResultWriter writer =
      new MinecraftFixtureTransformationResultWriter();

  @Test
  void writerOutputIsDeterministic() throws Exception {
    MinecraftFixtureTransformationResult result =
        transformer.transformFixtureClass(
            MinecraftFixtureBytecodeTransformerTest.fixtureClassBytes(
                "net/minecraft/server/Main", "([Ljava/lang/String;)V", true, false, false),
            MinecraftFixtureBytecodeTransformerTest.validPatchPlan());

    Path first = tempDirectory.resolve("one/minecraft-fixture-transformation-result.json");
    Path second = tempDirectory.resolve("two/minecraft-fixture-transformation-result.json");

    writer.write(first, result);
    writer.write(second, result);

    String firstJson = Files.readString(first, StandardCharsets.UTF_8);
    String secondJson = Files.readString(second, StandardCharsets.UTF_8);

    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"milestoneName\": \"Target-8\""));
    assertTrue(firstJson.contains("\"transformationScope\": \"fixture-only\""));
    assertTrue(firstJson.contains("\"status\": \"transformed\""));
    assertTrue(firstJson.contains("\"failureReason\": null"));
    assertTrue(firstJson.contains("\"dispatcherMethodrefIndex\""));
    assertTrue(firstJson.contains("\"transformedClassBytesProduced\": true"));
    assertFalse(firstJson.contains("\"transformedClassBytes\""));
  }
}
