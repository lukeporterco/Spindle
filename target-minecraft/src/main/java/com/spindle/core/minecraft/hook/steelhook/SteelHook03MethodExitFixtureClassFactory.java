package com.spindle.core.minecraft.hook.steelhook;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class SteelHook03MethodExitFixtureClassFactory {
  static final String TARGET_OWNER_INTERNAL_NAME = "net/minecraft/server/Main";
  static final String TARGET_BINARY_NAME = "net.minecraft.server.Main";
  static final String TARGET_CLASS_ENTRY_NAME = TARGET_OWNER_INTERNAL_NAME + ".class";
  static final String TARGET_METHOD_NAME = "main";
  static final String TARGET_DESCRIPTOR = "([Ljava/lang/String;)V";

  byte[] createMethodExitFixtureClassBytes() {
    try {
      ConstantPoolBuilder constantPool = new ConstantPoolBuilder();
      int thisClassUtf8 = constantPool.addUtf8(TARGET_OWNER_INTERNAL_NAME);
      int thisClass = constantPool.addClass(thisClassUtf8);
      int objectUtf8 = constantPool.addUtf8("java/lang/Object");
      int objectClass = constantPool.addClass(objectUtf8);
      int initUtf8 = constantPool.addUtf8("<init>");
      int voidDescriptorUtf8 = constantPool.addUtf8("()V");
      int initNameAndType = constantPool.addNameAndType(initUtf8, voidDescriptorUtf8);
      int objectInitMethodref = constantPool.addMethodref(objectClass, initNameAndType);
      int codeUtf8 = constantPool.addUtf8("Code");
      int mainUtf8 = constantPool.addUtf8(TARGET_METHOD_NAME);
      int mainDescriptorUtf8 = constantPool.addUtf8(TARGET_DESCRIPTOR);

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
        writeMainMethod(output, mainUtf8, mainDescriptorUtf8, codeUtf8);
        output.writeShort(0);
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Failed to build Target-29 method-exit fixture class.", exception);
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
    byte[] body = codeAttributeBody(1, 1, code);
    output.writeInt(body.length);
    output.write(body);
  }

  private void writeMainMethod(
      DataOutputStream output, int mainUtf8, int mainDescriptorUtf8, int codeUtf8)
      throws IOException {
    output.writeShort(0x0009);
    output.writeShort(mainUtf8);
    output.writeShort(mainDescriptorUtf8);
    output.writeShort(1);
    output.writeShort(codeUtf8);
    byte[] body = codeAttributeBody(0, 1, new byte[] {(byte) 0xb1});
    output.writeInt(body.length);
    output.write(body);
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
    private final List<byte[]> entries = new ArrayList<>();

    private int addUtf8(String value) {
      try {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
          output.writeByte(1);
          output.writeUTF(value);
        }
        entries.add(bytes.toByteArray());
        return entries.size();
      } catch (IOException exception) {
        throw new IllegalStateException("Failed to add Utf8 constant pool entry.", exception);
      }
    }

    private int addClass(int nameIndex) {
      return addEntry((byte) 7, nameIndex);
    }

    private int addNameAndType(int nameIndex, int descriptorIndex) {
      return addEntry((byte) 12, nameIndex, descriptorIndex);
    }

    private int addMethodref(int classIndex, int nameAndTypeIndex) {
      return addEntry((byte) 10, classIndex, nameAndTypeIndex);
    }

    private int addEntry(byte tag, int... values) {
      try {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
          output.writeByte(tag);
          for (int value : values) {
            output.writeShort(value);
          }
        }
        entries.add(bytes.toByteArray());
        return entries.size();
      } catch (IOException exception) {
        throw new IllegalStateException("Failed to add constant pool entry.", exception);
      }
    }

    private void write(DataOutputStream output) throws IOException {
      output.writeShort(entries.size() + 1);
      for (byte[] entry : entries) {
        output.write(entry);
      }
    }
  }
}
