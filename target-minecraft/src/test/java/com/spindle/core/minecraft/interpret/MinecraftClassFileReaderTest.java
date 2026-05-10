package com.spindle.core.minecraft.interpret;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class MinecraftClassFileReaderTest {
  private final MinecraftClassFileReader reader = new MinecraftClassFileReader();

  @Test
  void readsClassStructureFromClassBytesWithoutInspectingLoadedClass() throws Exception {
    MinecraftInterpretedClass interpretedClass =
        reader.read(
            readResourceBytes(
                "com/spindle/core/minecraft/interpret/MinecraftClassFileReaderTest$Fixture.class"),
            "fixture.jar",
            "com/spindle/core/minecraft/interpret/MinecraftClassFileReaderTest$Fixture.class");

    assertEquals(
        "com.spindle.core.minecraft.interpret.MinecraftClassFileReaderTest$Fixture",
        interpretedClass.binaryName());
    assertEquals(
        "com/spindle/core/minecraft/interpret/MinecraftClassFileReaderTest$Fixture",
        interpretedClass.internalName());
    assertEquals(
        "com/spindle/core/minecraft/interpret/MinecraftClassFileReaderTest$FixtureParent",
        interpretedClass.superName());
    assertTrue(interpretedClass.accessFlags().contains("public"));
    assertTrue(interpretedClass.accessFlags().contains("final"));
    assertTrue(
        interpretedClass.fields().stream()
            .anyMatch(
                field ->
                    field.name().equals("FLAG")
                        && field.descriptor().equals("Ljava/lang/String;")
                        && field.accessFlags().contains("public")
                        && field.accessFlags().contains("static")
                        && field.accessFlags().contains("final")));
    assertTrue(
        interpretedClass.fields().stream()
            .anyMatch(
                field ->
                    field.name().equals("count")
                        && field.descriptor().equals("I")
                        && field.accessFlags().contains("private")
                        && field.accessFlags().contains("final")));
    assertTrue(
        interpretedClass.methods().stream()
            .anyMatch(
                method ->
                    method.name().equals("<init>")
                        && method.descriptor().equals("(I)V")
                        && method.constructor()));
    assertTrue(
        interpretedClass.methods().stream()
            .anyMatch(
                method ->
                    method.name().equals("compute")
                        && method.descriptor().equals("(I)I")
                        && !method.constructor()));
    assertTrue(
        interpretedClass.methods().stream()
            .anyMatch(
                method ->
                    method.name().equals("helper")
                        && method.accessFlags().contains("public")
                        && method.accessFlags().contains("static")
                        && method.staticMethod()));
  }

  private byte[] readResourceBytes(String resourceName) throws IOException {
    try (var inputStream =
        MinecraftClassFileReaderTest.class.getClassLoader().getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + resourceName);
      }
      return inputStream.readAllBytes();
    }
  }

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
      this(7);
    }

    public Fixture(int count) {
      super(11L);
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
