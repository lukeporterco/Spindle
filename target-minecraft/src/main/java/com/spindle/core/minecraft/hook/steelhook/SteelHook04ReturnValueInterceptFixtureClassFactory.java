package com.spindle.core.minecraft.hook.steelhook;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class SteelHook04ReturnValueInterceptFixtureClassFactory {
  static final String TARGET_OWNER_INTERNAL_NAME =
      "net/minecraft/server/Target33ReturnValueFixture";
  static final String TARGET_BINARY_NAME = "net.minecraft.server.Target33ReturnValueFixture";
  static final String TARGET_CLASS_ENTRY_NAME = TARGET_OWNER_INTERNAL_NAME + ".class";
  static final String PRIMITIVE_METHOD_NAME = "primitiveValue";
  static final String PRIMITIVE_DESCRIPTOR = "()I";
  static final String REFERENCE_METHOD_NAME = "referenceValue";
  static final String REFERENCE_DESCRIPTOR = "()Ljava/lang/String;";
  static final String VOID_METHOD_NAME = "voidValue";
  static final String VOID_DESCRIPTOR = "()V";
  static final String MULTIPLE_RETURNS_METHOD_NAME = "multipleReturns";
  static final String BRANCHING_METHOD_NAME = "branchingValue";
  static final String SWITCH_METHOD_NAME = "switchValue";
  static final String EXCEPTION_TABLE_METHOD_NAME = "exceptionTableValue";
  static final String STACKMAP_METHOD_NAME = "stackMapValue";
  static final String SYNCHRONIZED_METHOD_NAME = "synchronizedValue";
  static final String MISSING_PRODUCER_METHOD_NAME = "missingProducerValue";
  static final String ORIGINAL_REFERENCE = "original";
  static final String REPLACEMENT_REFERENCE = "replacement";

  byte[] createFixtureClassBytes() {
    try {
      ConstantPoolBuilder constantPool = new ConstantPoolBuilder();
      int thisClassUtf8 = constantPool.addUtf8(TARGET_OWNER_INTERNAL_NAME);
      int thisClass = constantPool.addClass(thisClassUtf8);
      int objectUtf8 = constantPool.addUtf8("java/lang/Object");
      int objectClass = constantPool.addClass(objectUtf8);
      int initUtf8 = constantPool.addUtf8("<init>");
      int clinitUtf8 = constantPool.addUtf8("<clinit>");
      int voidDescriptorUtf8 = constantPool.addUtf8(VOID_DESCRIPTOR);
      int primitiveNameUtf8 = constantPool.addUtf8(PRIMITIVE_METHOD_NAME);
      int primitiveDescriptorUtf8 = constantPool.addUtf8(PRIMITIVE_DESCRIPTOR);
      int referenceNameUtf8 = constantPool.addUtf8(REFERENCE_METHOD_NAME);
      int referenceDescriptorUtf8 = constantPool.addUtf8(REFERENCE_DESCRIPTOR);
      int voidNameUtf8 = constantPool.addUtf8(VOID_METHOD_NAME);
      int multipleReturnsUtf8 = constantPool.addUtf8(MULTIPLE_RETURNS_METHOD_NAME);
      int branchingUtf8 = constantPool.addUtf8(BRANCHING_METHOD_NAME);
      int switchUtf8 = constantPool.addUtf8(SWITCH_METHOD_NAME);
      int exceptionTableUtf8 = constantPool.addUtf8(EXCEPTION_TABLE_METHOD_NAME);
      int stackMapUtf8 = constantPool.addUtf8(STACKMAP_METHOD_NAME);
      int synchronizedUtf8 = constantPool.addUtf8(SYNCHRONIZED_METHOD_NAME);
      int missingProducerUtf8 = constantPool.addUtf8(MISSING_PRODUCER_METHOD_NAME);
      int codeUtf8 = constantPool.addUtf8("Code");
      int stackMapTableUtf8 = constantPool.addUtf8("StackMapTable");
      int initNameAndType = constantPool.addNameAndType(initUtf8, voidDescriptorUtf8);
      int objectInitMethodref = constantPool.addMethodref(objectClass, initNameAndType);
      int originalStringIndex = constantPool.addStringConstant(ORIGINAL_REFERENCE);
      constantPool.addStringConstant(REPLACEMENT_REFERENCE);

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
        output.writeShort(12);
        writeMethod(
            output,
            0x0001,
            initUtf8,
            voidDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(
                1,
                1,
                new byte[] {
                  0x2a, (byte) 0xb7, hi(objectInitMethodref), lo(objectInitMethodref), (byte) 0xb1
                }));
        writeMethod(
            output,
            0x0008,
            clinitUtf8,
            voidDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(0, 0, new byte[] {(byte) 0xb1}));
        writeMethod(
            output,
            0x0009,
            primitiveNameUtf8,
            primitiveDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(1, 0, new byte[] {0x10, 0x07, (byte) 0xac}));
        writeMethod(
            output,
            0x0009,
            referenceNameUtf8,
            referenceDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(1, 0, new byte[] {0x12, lo(originalStringIndex), (byte) 0xb0}));
        writeMethod(
            output,
            0x0009,
            voidNameUtf8,
            voidDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(0, 0, new byte[] {(byte) 0xb1}));
        writeMethod(
            output,
            0x0009,
            multipleReturnsUtf8,
            primitiveDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(1, 0, new byte[] {0x10, 0x07, (byte) 0xac, 0x10, 0x08, (byte) 0xac}));
        writeMethod(
            output,
            0x0009,
            branchingUtf8,
            primitiveDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(1, 0, new byte[] {0x03, (byte) 0xa7, 0x00, 0x00}));
        writeMethod(
            output,
            0x0009,
            switchUtf8,
            primitiveDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(1, 0, new byte[] {0x03, (byte) 0xaa, 0x00, 0x00, 0x00}));
        writeMethod(
            output,
            0x0009,
            exceptionTableUtf8,
            primitiveDescriptorUtf8,
            codeUtf8,
            codeAttributeBodyWithExceptionTable(1, 0, new byte[] {0x10, 0x07, (byte) 0xac}));
        writeMethod(
            output,
            0x0009,
            stackMapUtf8,
            primitiveDescriptorUtf8,
            codeUtf8,
            codeAttributeBodyWithNestedAttribute(
                1,
                0,
                new byte[] {0x10, 0x07, (byte) 0xac},
                stackMapTableUtf8,
                stackMapTableAttributeBody()));
        writeMethod(
            output,
            0x0029,
            synchronizedUtf8,
            primitiveDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(1, 0, new byte[] {0x10, 0x07, (byte) 0xac}));
        writeMethod(
            output,
            0x0009,
            missingProducerUtf8,
            primitiveDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(1, 0, new byte[] {0x04, (byte) 0xac}));
        output.writeShort(0);
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to build Target-33 fixture class.", exception);
    }
  }

  private void writeMethod(
      DataOutputStream output,
      int accessFlags,
      int nameIndex,
      int descriptorIndex,
      int codeUtf8,
      byte[] body)
      throws IOException {
    output.writeShort(accessFlags);
    output.writeShort(nameIndex);
    output.writeShort(descriptorIndex);
    output.writeShort(1);
    output.writeShort(codeUtf8);
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

  private byte[] codeAttributeBodyWithExceptionTable(int maxStack, int maxLocals, byte[] code)
      throws IOException {
    return codeAttributeBodyWithExceptionTable(
        maxStack, maxLocals, new byte[] {0x03, (byte) 0xac, 0x57, 0x04, (byte) 0xac}, 0, 2, 2);
  }

  private byte[] codeAttributeBodyWithExceptionTable(
      int maxStack, int maxLocals, byte[] code, int startPc, int endPc, int handlerPc)
      throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream output = new DataOutputStream(bytes)) {
      output.writeShort(maxStack);
      output.writeShort(maxLocals);
      output.writeInt(code.length);
      output.write(code);
      output.writeShort(1);
      output.writeShort(startPc);
      output.writeShort(endPc);
      output.writeShort(handlerPc);
      output.writeShort(0);
      output.writeShort(0);
    }
    return bytes.toByteArray();
  }

  private byte[] codeAttributeBodyWithNestedAttribute(
      int maxStack, int maxLocals, byte[] code, int nestedNameIndex, byte[] nestedBody)
      throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream output = new DataOutputStream(bytes)) {
      output.writeShort(maxStack);
      output.writeShort(maxLocals);
      output.writeInt(code.length);
      output.write(code);
      output.writeShort(0);
      output.writeShort(1);
      output.writeShort(nestedNameIndex);
      output.writeInt(nestedBody.length);
      output.write(nestedBody);
    }
    return bytes.toByteArray();
  }

  private byte[] stackMapTableAttributeBody() throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream output = new DataOutputStream(bytes)) {
      output.writeShort(0);
    }
    return bytes.toByteArray();
  }

  private byte hi(int value) {
    return (byte) (value >>> 8);
  }

  private byte lo(int value) {
    return (byte) value;
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

    private int addString(int stringIndex) {
      return addEntry((byte) 8, stringIndex);
    }

    private int addNameAndType(int nameIndex, int descriptorIndex) {
      return addEntry((byte) 12, nameIndex, descriptorIndex);
    }

    private int addMethodref(int classIndex, int nameAndTypeIndex) {
      return addEntry((byte) 10, classIndex, nameAndTypeIndex);
    }

    private int addStringConstant(String value) {
      int utf8 = addUtf8(value);
      return addString(utf8);
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
