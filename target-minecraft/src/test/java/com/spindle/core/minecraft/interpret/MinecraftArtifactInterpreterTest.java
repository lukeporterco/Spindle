package com.spindle.core.minecraft.interpret;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftArtifactInterpreterTest {
  @TempDir Path tempDirectory;

  private final MinecraftArtifactInterpreter interpreter = new MinecraftArtifactInterpreter();
  private final MinecraftArtifactInterpretationWriter writer =
      new MinecraftArtifactInterpretationWriter();

  @Test
  void interpretsArtifactsDeterministicallyAndWritesStableJson() throws Exception {
    Path serverJar = tempDirectory.resolve("z-server.jar");
    createJar(
        serverJar,
        List.of(
            new JarClassEntry(
                "com/spindle/sampleserverfixture/FakeMinecraftServerMain.class",
                readResourceBytes(
                    "com/spindle/sampleserverfixture/FakeMinecraftServerMain.class"))));
    Path interpretationJar = tempDirectory.resolve("a-interpretation.jar");
    createJar(
        interpretationJar,
        List.of(
            new JarClassEntry(
                "com/spindle/core/minecraft/interpret/MinecraftArtifactInterpreterTest$Fixture.class",
                readResourceBytes(
                    "com/spindle/core/minecraft/interpret/MinecraftArtifactInterpreterTest$Fixture.class")),
            new JarClassEntry(
                "com/spindle/core/minecraft/interpret/MinecraftArtifactInterpreterTest$FixtureParent.class",
                readResourceBytes(
                    "com/spindle/core/minecraft/interpret/MinecraftArtifactInterpreterTest$FixtureParent.class"))));

    List<MinecraftArtifactInterpreter.JarInput> firstInputs =
        List.of(
            MinecraftArtifactInterpreter.JarInput.of(
                serverJar, "z-server.jar", "minecraft-server-jar", "fixture-server", "sha-server"),
            MinecraftArtifactInterpreter.JarInput.of(
                interpretationJar,
                "a-interpretation.jar",
                "minecraft-bundled-library",
                "fixture-interpretation",
                "sha-interpretation"));
    List<MinecraftArtifactInterpreter.JarInput> secondInputs =
        List.of(firstInputs.get(1), firstInputs.get(0));

    MinecraftArtifactInterpretation first =
        interpreter.interpret("26.1.2", MinecraftSide.SERVER, firstInputs);
    MinecraftArtifactInterpretation second =
        interpreter.interpret("26.1.2", MinecraftSide.SERVER, secondInputs);

    Path firstOutput = tempDirectory.resolve("first/minecraft-artifact-interpretation.json");
    Path secondOutput = tempDirectory.resolve("second/minecraft-artifact-interpretation.json");
    writer.write(firstOutput, first);
    writer.write(secondOutput, second);

    String firstJson = Files.readString(firstOutput, StandardCharsets.UTF_8);
    String secondJson = Files.readString(secondOutput, StandardCharsets.UTF_8);

    assertEquals(1, first.schema());
    assertEquals("Target-1", first.milestoneName());
    assertTrue(first.analysisOnly());
    assertTrue(!first.classLoadingOccurred());
    assertTrue(!first.injectionOccurred());
    assertTrue(!first.hookInstallationOccurred());
    assertEquals(2, first.jars().size());
    assertEquals("a-interpretation.jar", first.jars().getFirst().path());
    assertEquals(3, first.classCount());
    assertTrue(first.methodCount() > 0);
    assertEquals(first.packages().stream().sorted().toList(), first.packages());
    assertEquals(
        first.jars().getFirst().classes().stream()
            .map(MinecraftInterpretedClass::binaryName)
            .sorted()
            .toList(),
        first.jars().getFirst().classes().stream()
            .map(MinecraftInterpretedClass::binaryName)
            .toList());
    MinecraftInterpretedClass fixtureClass =
        first.jars().getFirst().classes().stream()
            .filter(
                value ->
                    value
                        .binaryName()
                        .equals(
                            "com.spindle.core.minecraft.interpret.MinecraftArtifactInterpreterTest$Fixture"))
            .findFirst()
            .orElseThrow();
    assertEquals(
        fixtureClass.methods().stream()
            .sorted(
                Comparator.comparing(MinecraftInterpretedMethod::name)
                    .thenComparing(MinecraftInterpretedMethod::descriptor))
            .toList(),
        fixtureClass.methods());
    assertEquals(firstJson, secondJson);
    assertTrue(firstJson.contains("\"analysisOnly\": true"));
    assertTrue(firstJson.contains("\"classLoadingOccurred\": false"));
    assertTrue(firstJson.contains("\"injectionOccurred\": false"));
    assertTrue(firstJson.contains("\"hookInstallationOccurred\": false"));
    assertTrue(firstJson.contains("\"binaryName\""));
    assertTrue(firstJson.contains("\"descriptor\""));
  }

  private void createJar(Path jarPath, List<JarClassEntry> entries) throws IOException {
    Files.createDirectories(jarPath.getParent());
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jar = new JarOutputStream(outputStream)) {
      for (JarClassEntry entry : entries) {
        jar.putNextEntry(new JarEntry(entry.name()));
        jar.write(entry.bytes());
        jar.closeEntry();
      }
    }
  }

  private byte[] readResourceBytes(String resourceName) throws IOException {
    try (var inputStream =
        MinecraftArtifactInterpreterTest.class.getClassLoader().getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + resourceName);
      }
      return inputStream.readAllBytes();
    }
  }

  private record JarClassEntry(String name, byte[] bytes) {}

  public static class FixtureParent {
    protected FixtureParent(long parentValue) {
      if (parentValue < 0) {
        throw new IllegalArgumentException("parentValue");
      }
    }
  }

  public static final class Fixture extends FixtureParent implements Runnable {
    public static final String FLAG = "fixture";
    private final int count;

    public Fixture() {
      this(3);
    }

    public Fixture(int count) {
      super(5L);
      this.count = count;
    }

    public static String helper() {
      return FLAG;
    }

    public int compute(int value) {
      return value + count;
    }

    @Override
    public void run() {}
  }
}
