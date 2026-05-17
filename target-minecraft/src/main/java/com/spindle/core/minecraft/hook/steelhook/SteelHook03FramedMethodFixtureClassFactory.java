package com.spindle.core.minecraft.hook.steelhook;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class SteelHook03FramedMethodFixtureClassFactory {
  static final String TARGET_OWNER_INTERNAL_NAME = "com/spindle/steelhook/Target28FramedMain";
  static final String TARGET_BINARY_NAME = "com.spindle.steelhook.Target28FramedMain";
  static final String TARGET_CLASS_ENTRY_NAME = TARGET_OWNER_INTERNAL_NAME + ".class";
  static final String TARGET_METHOD_NAME = "main";
  static final String TARGET_DESCRIPTOR = "([Ljava/lang/String;)V";

  byte[] createFramedMethodFixtureClassBytes() {
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
      int stackMapTableUtf8 = constantPool.addUtf8("StackMapTable");

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
        writeFramedMainMethod(output, mainUtf8, mainDescriptorUtf8, codeUtf8, stackMapTableUtf8);

        output.writeShort(0);
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Failed to build Target-28 framed method fixture class.", exception);
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
    byte[] codeBody = codeAttributeBody(1, 1, code, List.of());
    output.writeInt(codeBody.length);
    output.write(codeBody);
  }

  private void writeFramedMainMethod(
      DataOutputStream output,
      int mainUtf8,
      int mainDescriptorUtf8,
      int codeUtf8,
      int stackMapTableUtf8)
      throws IOException {
    output.writeShort(0x0009);
    output.writeShort(mainUtf8);
    output.writeShort(mainDescriptorUtf8);
    output.writeShort(1);
    output.writeShort(codeUtf8);
    byte[] code = new byte[] {0x2a, (byte) 0xc6, 0x00, 0x04, (byte) 0xb1, (byte) 0xb1};
    byte[] stackMapTableBody = new byte[] {0x00, 0x01, 0x05};
    byte[] codeBody =
        codeAttributeBody(
            1, 1, code, List.of(new AttributeBytes(stackMapTableUtf8, stackMapTableBody)));
    output.writeInt(codeBody.length);
    output.write(codeBody);
  }

  private byte[] codeAttributeBody(
      int maxStack, int maxLocals, byte[] code, List<AttributeBytes> nestedAttributes)
      throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream output = new DataOutputStream(bytes)) {
      output.writeShort(maxStack);
      output.writeShort(maxLocals);
      output.writeInt(code.length);
      output.write(code);
      output.writeShort(0);
      output.writeShort(nestedAttributes.size());
      for (AttributeBytes attribute : nestedAttributes) {
        output.writeShort(attribute.nameIndex());
        output.writeInt(attribute.body().length);
        output.write(attribute.body());
      }
    }
    return bytes.toByteArray();
  }

  private record AttributeBytes(int nameIndex, byte[] body) {}

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
      return addConstantPoolEntry((byte) 7, nameIndex);
    }

    private int addNameAndType(int nameIndex, int descriptorIndex) {
      return addConstantPoolEntry((byte) 12, nameIndex, descriptorIndex);
    }

    private int addMethodref(int classIndex, int nameAndTypeIndex) {
      return addConstantPoolEntry((byte) 10, classIndex, nameAndTypeIndex);
    }

    private int addConstantPoolEntry(byte tag, int... values) {
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
