package com.spindle.core.minecraft.hook.place;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.diagnostics.LoaderException;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class MinecraftMethodCodeReaderTest {
  private final MinecraftMethodCodeReader reader = new MinecraftMethodCodeReader();

  @Test
  void validFixtureMethodReportsStableCodeMetadata() throws Exception {
    byte[] classBytes = readResourceBytes("net/minecraft/server/Main.class");

    MinecraftMethodCodeSummary first =
        reader.read(classBytes, "net/minecraft/server/Main", "main", "([Ljava/lang/String;)V");
    MinecraftMethodCodeSummary second =
        reader.read(classBytes, "net/minecraft/server/Main", "main", "([Ljava/lang/String;)V");

    assertTrue(first.hasCodeAttribute());
    assertFalse(first.abstractOrNative());
    assertNotNull(first.codeLength());
    assertTrue(first.codeLength() > 0);
    assertNotNull(first.codeSha256());
    assertEquals(first.codeSha256(), second.codeSha256());
    assertEquals(64, first.codeSha256().length());
    assertEquals(0, first.methodEntryOffset());
  }

  @Test
  void missingMethodReturnsNoCodeSummary() throws Exception {
    MinecraftMethodCodeSummary summary =
        reader.read(
            readResourceBytes("net/minecraft/server/Main.class"),
            "net/minecraft/server/Main",
            "missing",
            "()V");

    assertFalse(summary.hasCodeAttribute());
    assertFalse(summary.abstractOrNative());
    assertNull(summary.codeSha256());
    assertNull(summary.methodEntryOffset());
  }

  @Test
  void abstractOrNativeMethodReportsNoCodeAttribute() throws Exception {
    MinecraftMethodCodeSummary summary =
        reader.read(
            readResourceBytes(
                "com/spindle/core/minecraft/hook/place/MinecraftMethodCodeReaderTest$NoCodeMain.class"),
            "net/minecraft/server/Main",
            "main",
            "([Ljava/lang/String;)V");

    assertFalse(summary.hasCodeAttribute());
    assertTrue(summary.abstractOrNative());
    assertNull(summary.codeLength());
    assertNull(summary.codeSha256());
  }

  @Test
  void malformedClassInputFails() {
    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () -> reader.read(new byte[] {0x00, 0x01}, "net/minecraft/server/Main", "main", "()V"));

    assertNotNull(exception.getMessage());
    assertFalse(exception.getMessage().isBlank());
  }

  private byte[] readResourceBytes(String resourceName) throws IOException {
    try (var inputStream =
        MinecraftMethodCodeReaderTest.class.getClassLoader().getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + resourceName);
      }
      return inputStream.readAllBytes();
    }
  }

  public abstract static class NoCodeMain {
    public static native void main(String[] args);
  }
}
