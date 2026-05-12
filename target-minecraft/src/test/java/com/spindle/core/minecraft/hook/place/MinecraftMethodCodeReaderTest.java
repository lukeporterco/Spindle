package com.spindle.core.minecraft.hook.place;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.diagnostics.LoaderException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
            fixtureClassBytes("net/minecraft/server/Main", "([Ljava/lang/String;)V", false),
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

  @Test
  void wrongInternalClassNameFailsDeterministically() throws Exception {
    LoaderException exception =
        assertThrows(
            LoaderException.class,
            () ->
                reader.read(
                    readResourceBytes("net/minecraft/server/Main.class"),
                    "com/example/Main",
                    "main",
                    "([Ljava/lang/String;)V"));

    assertEquals(
        "Class internal name mismatch while reading method com/example/Main.main([Ljava/lang/String;)V: found net/minecraft/server/Main",
        exception.getMessage());
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

  private byte[] fixtureClassBytes(
      String internalName, String mainDescriptor, boolean includeCode) {
    try {
      ConstantPoolBuilder constantPool = new ConstantPoolBuilder();
      int thisClassUtf8 = constantPool.addUtf8(internalName);
      int thisClass = constantPool.addClass(thisClassUtf8);
      int objectUtf8 = constantPool.addUtf8("java/lang/Object");
      int objectClass = constantPool.addClass(objectUtf8);
      int initUtf8 = constantPool.addUtf8("<init>");
      int voidDescriptorUtf8 = constantPool.addUtf8("()V");
      int initNameAndType = constantPool.addNameAndType(initUtf8, voidDescriptorUtf8);
      int objectInitMethodref = constantPool.addMethodref(objectClass, initNameAndType);
      int codeUtf8 = constantPool.addUtf8("Code");
      int mainUtf8 = constantPool.addUtf8("main");
      int mainDescriptorUtf8 = constantPool.addUtf8(mainDescriptor);

      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (DataOutputStream output = new DataOutputStream(bytes)) {
        output.writeInt(0xCAFEBABE);
        output.writeShort(0);
        output.writeShort(61);
        constantPool.write(output);
        output.writeShort(0x0031);
        output.writeShort(thisClass);
        output.writeShort(objectClass);
        output.writeShort(0);
        output.writeShort(0);
        output.writeShort(2);

        writeConstructor(output, initUtf8, voidDescriptorUtf8, codeUtf8, objectInitMethodref);
        writeMainMethod(output, mainUtf8, mainDescriptorUtf8, codeUtf8, includeCode);
        output.writeShort(0);
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to build fixture class bytes.", exception);
    }
  }

  private void writeConstructor(
      DataOutputStream output,
      int initUtf8,
      int voidDescriptorUtf8,
      int codeUtf8,
      int objectInitMethodref)
      throws IOException {
    output.writeShort(0x0001);
    output.writeShort(initUtf8);
    output.writeShort(voidDescriptorUtf8);
    output.writeShort(1);
    output.writeShort(codeUtf8);
    byte[] code =
        new byte[] {
          0x2a,
          (byte) 0xb7,
          (byte) (objectInitMethodref >>> 8),
          (byte) objectInitMethodref,
          (byte) 0xb1
        };
    byte[] codeBody = codeAttributeBody(1, 1, code);
    output.writeInt(codeBody.length);
    output.write(codeBody);
  }

  private void writeMainMethod(
      DataOutputStream output,
      int mainUtf8,
      int mainDescriptorUtf8,
      int codeUtf8,
      boolean includeCode)
      throws IOException {
    output.writeShort(includeCode ? 0x0009 : 0x0109);
    output.writeShort(mainUtf8);
    output.writeShort(mainDescriptorUtf8);
    if (!includeCode) {
      output.writeShort(0);
      return;
    }
    output.writeShort(1);
    output.writeShort(codeUtf8);
    byte[] codeBody = codeAttributeBody(0, 1, new byte[] {(byte) 0xb1});
    output.writeInt(codeBody.length);
    output.write(codeBody);
  }

  private byte[] codeAttributeBody(int maxStack, int maxLocals, byte[] code) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream output = new DataOutputStream(bytes)) {
      output.writeShort(maxStack);
      output.writeShort(maxLocals);
      output.writeInt(code.length);
      output.write(code);
      output.writeShort(0);
      output.writeShort(0);
    }
    return bytes.toByteArray();
  }

  private static final class ConstantPoolBuilder {
    private final java.util.List<ConstantPoolEntry> entries = new java.util.ArrayList<>();

    private int addUtf8(String value) {
      entries.add(
          output -> {
            output.writeByte(1);
            output.writeUTF(value);
          });
      return entries.size();
    }

    private int addClass(int nameIndex) {
      entries.add(
          output -> {
            output.writeByte(7);
            output.writeShort(nameIndex);
          });
      return entries.size();
    }

    private int addNameAndType(int nameIndex, int descriptorIndex) {
      entries.add(
          output -> {
            output.writeByte(12);
            output.writeShort(nameIndex);
            output.writeShort(descriptorIndex);
          });
      return entries.size();
    }

    private int addMethodref(int classIndex, int nameAndTypeIndex) {
      entries.add(
          output -> {
            output.writeByte(10);
            output.writeShort(classIndex);
            output.writeShort(nameAndTypeIndex);
          });
      return entries.size();
    }

    private void write(DataOutputStream output) throws IOException {
      output.writeShort(entries.size() + 1);
      for (ConstantPoolEntry entry : entries) {
        entry.write(output);
      }
    }
  }

  @FunctionalInterface
  private interface ConstantPoolEntry {
    void write(DataOutputStream output) throws IOException;
  }

  public abstract static class NoCodeMain {
    public static native void main(String[] args);
  }
}
