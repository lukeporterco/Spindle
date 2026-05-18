package com.spindle.core.minecraft.hook.steelhook;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class SteelHook04InvokeCallsiteFixtureClassFactory {
  static final String TARGET_OWNER_INTERNAL_NAME =
      "net/minecraft/server/Target34InvokeCallsiteFixture";
  static final String TARGET_BINARY_NAME = "net.minecraft.server.Target34InvokeCallsiteFixture";
  static final String TARGET_CLASS_ENTRY_NAME = TARGET_OWNER_INTERNAL_NAME + ".class";
  static final String ORIGINAL_METHOD_NAME = "originalValue";
  static final String REDIRECTED_METHOD_NAME = "redirectedValue";
  static final String WRAPPED_METHOD_NAME = "wrappedValue";
  static final String INVOKE_METHOD_NAME = "invokeValue";
  static final String NO_INVOKE_METHOD_NAME = "noInvokeValue";
  static final String AMBIGUOUS_METHOD_NAME = "ambiguousInvokeValue";
  static final String CONSTRUCTOR_METHOD_NAME = "constructorInvokeValue";
  static final String SPECIAL_METHOD_NAME = "specialInvokeValue";
  static final String BRANCHING_METHOD_NAME = "branchingInvokeValue";
  static final String SWITCH_METHOD_NAME = "switchInvokeValue";
  static final String EXCEPTION_TABLE_METHOD_NAME = "exceptionTableInvokeValue";
  static final String STACKMAP_METHOD_NAME = "stackMapInvokeValue";
  static final String SYNCHRONIZED_METHOD_NAME = "synchronizedInvokeValue";
  static final String SPECIAL_HELPER_METHOD_NAME = "specialHelper";
  static final String INT_DESCRIPTOR = "()I";
  static final String VOID_DESCRIPTOR = "()V";

  byte[] createFixtureClassBytes() {
    try {
      ConstantPoolBuilder constantPool = new ConstantPoolBuilder();
      int thisClassUtf8 = constantPool.addUtf8(TARGET_OWNER_INTERNAL_NAME);
      int thisClass = constantPool.addClass(thisClassUtf8);
      int objectUtf8 = constantPool.addUtf8("java/lang/Object");
      int objectClass = constantPool.addClass(objectUtf8);
      int initUtf8 = constantPool.addUtf8("<init>");
      int voidDescriptorUtf8 = constantPool.addUtf8(VOID_DESCRIPTOR);
      int intDescriptorUtf8 = constantPool.addUtf8(INT_DESCRIPTOR);
      int codeUtf8 = constantPool.addUtf8("Code");
      int stackMapTableUtf8 = constantPool.addUtf8("StackMapTable");

      int originalNameUtf8 = constantPool.addUtf8(ORIGINAL_METHOD_NAME);
      int redirectedNameUtf8 = constantPool.addUtf8(REDIRECTED_METHOD_NAME);
      int wrappedNameUtf8 = constantPool.addUtf8(WRAPPED_METHOD_NAME);
      int invokeNameUtf8 = constantPool.addUtf8(INVOKE_METHOD_NAME);
      int noInvokeNameUtf8 = constantPool.addUtf8(NO_INVOKE_METHOD_NAME);
      int ambiguousNameUtf8 = constantPool.addUtf8(AMBIGUOUS_METHOD_NAME);
      int constructorNameUtf8 = constantPool.addUtf8(CONSTRUCTOR_METHOD_NAME);
      int specialNameUtf8 = constantPool.addUtf8(SPECIAL_METHOD_NAME);
      int branchingNameUtf8 = constantPool.addUtf8(BRANCHING_METHOD_NAME);
      int switchNameUtf8 = constantPool.addUtf8(SWITCH_METHOD_NAME);
      int exceptionTableNameUtf8 = constantPool.addUtf8(EXCEPTION_TABLE_METHOD_NAME);
      int stackMapNameUtf8 = constantPool.addUtf8(STACKMAP_METHOD_NAME);
      int synchronizedNameUtf8 = constantPool.addUtf8(SYNCHRONIZED_METHOD_NAME);
      int specialHelperNameUtf8 = constantPool.addUtf8(SPECIAL_HELPER_METHOD_NAME);

      int targetInitNameAndType = constantPool.addNameAndType(initUtf8, voidDescriptorUtf8);
      int objectInitNameAndType = constantPool.addNameAndType(initUtf8, voidDescriptorUtf8);
      int originalNameAndType = constantPool.addNameAndType(originalNameUtf8, intDescriptorUtf8);
      int redirectedNameAndType =
          constantPool.addNameAndType(redirectedNameUtf8, intDescriptorUtf8);
      int wrappedNameAndType = constantPool.addNameAndType(wrappedNameUtf8, intDescriptorUtf8);
      int specialHelperNameAndType =
          constantPool.addNameAndType(specialHelperNameUtf8, intDescriptorUtf8);

      int objectInitMethodref = constantPool.addMethodref(objectClass, objectInitNameAndType);
      int targetInitMethodref = constantPool.addMethodref(thisClass, targetInitNameAndType);
      int originalMethodref = constantPool.addMethodref(thisClass, originalNameAndType);
      int redirectedMethodref = constantPool.addMethodref(thisClass, redirectedNameAndType);
      int wrappedMethodref = constantPool.addMethodref(thisClass, wrappedNameAndType);
      int specialHelperMethodref = constantPool.addMethodref(thisClass, specialHelperNameAndType);

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
        output.writeShort(15);
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
            0x0009,
            originalNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(1, 0, new byte[] {0x10, 0x07, (byte) 0xac}));
        writeMethod(
            output,
            0x0009,
            redirectedNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(1, 0, new byte[] {0x10, 0x2a, (byte) 0xac}));
        writeMethod(
            output,
            0x0009,
            wrappedNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(
                1,
                0,
                new byte[] {
                  (byte) 0xb8, hi(originalMethodref), lo(originalMethodref), (byte) 0xac
                }));
        writeMethod(
            output,
            0x0009,
            invokeNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(
                1,
                0,
                new byte[] {
                  (byte) 0xb8, hi(originalMethodref), lo(originalMethodref), (byte) 0xac
                }));
        writeMethod(
            output,
            0x0009,
            noInvokeNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(1, 0, new byte[] {0x10, 0x07, (byte) 0xac}));
        writeMethod(
            output,
            0x0009,
            ambiguousNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(
                2,
                0,
                new byte[] {
                  (byte) 0xb8,
                  hi(originalMethodref),
                  lo(originalMethodref),
                  0x57,
                  (byte) 0xb8,
                  hi(originalMethodref),
                  lo(originalMethodref),
                  (byte) 0xac
                }));
        writeMethod(
            output,
            0x0009,
            constructorNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(
                2,
                0,
                new byte[] {
                  (byte) 0xbb,
                  hi(objectClass),
                  lo(objectClass),
                  0x59,
                  (byte) 0xb7,
                  hi(objectInitMethodref),
                  lo(objectInitMethodref),
                  0x57,
                  0x10,
                  0x07,
                  (byte) 0xac
                }));
        writeMethod(
            output,
            0x0001,
            specialNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(
                1,
                1,
                new byte[] {
                  0x2a,
                  (byte) 0xb7,
                  hi(specialHelperMethodref),
                  lo(specialHelperMethodref),
                  (byte) 0xac
                }));
        writeMethod(
            output,
            0x0002,
            specialHelperNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(1, 1, new byte[] {0x10, 0x07, (byte) 0xac}));
        writeMethod(
            output,
            0x0009,
            branchingNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(1, 0, new byte[] {0x03, (byte) 0xa7, 0x00, 0x00}));
        writeMethod(
            output,
            0x0009,
            switchNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(1, 0, new byte[] {0x03, (byte) 0xaa, 0x00, 0x00, 0x00}));
        writeMethod(
            output,
            0x0009,
            exceptionTableNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBodyWithExceptionTable(
                1,
                0,
                new byte[] {
                  (byte) 0xb8,
                  hi(originalMethodref),
                  lo(originalMethodref),
                  (byte) 0xac,
                  0x57,
                  0x04,
                  (byte) 0xac
                },
                0,
                3,
                4));
        writeMethod(
            output,
            0x0009,
            stackMapNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBodyWithNestedAttribute(
                1,
                0,
                new byte[] {(byte) 0xb8, hi(originalMethodref), lo(originalMethodref), (byte) 0xac},
                stackMapTableUtf8,
                stackMapTableAttributeBody()));
        writeMethod(
            output,
            0x0029,
            synchronizedNameUtf8,
            intDescriptorUtf8,
            codeUtf8,
            codeAttributeBody(
                1,
                0,
                new byte[] {
                  (byte) 0xb8, hi(originalMethodref), lo(originalMethodref), (byte) 0xac
                }));
        output.writeShort(0);
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to build Target-34 fixture class.", exception);
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
