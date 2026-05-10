package com.spindle.core.minecraft.interpret;

import com.spindle.core.diagnostics.LoaderException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class MinecraftClassFileReader {
  private static final int CLASS_MAGIC = 0xCAFEBABE;
  private static final int CONSTANT_UTF8 = 1;
  private static final int CONSTANT_INTEGER = 3;
  private static final int CONSTANT_FLOAT = 4;
  private static final int CONSTANT_LONG = 5;
  private static final int CONSTANT_DOUBLE = 6;
  private static final int CONSTANT_CLASS = 7;
  private static final int CONSTANT_STRING = 8;
  private static final int CONSTANT_FIELDREF = 9;
  private static final int CONSTANT_METHODREF = 10;
  private static final int CONSTANT_INTERFACE_METHODREF = 11;
  private static final int CONSTANT_NAME_AND_TYPE = 12;
  private static final int CONSTANT_METHOD_HANDLE = 15;
  private static final int CONSTANT_METHOD_TYPE = 16;
  private static final int CONSTANT_DYNAMIC = 17;
  private static final int CONSTANT_INVOKE_DYNAMIC = 18;
  private static final int CONSTANT_MODULE = 19;
  private static final int CONSTANT_PACKAGE = 20;

  public MinecraftInterpretedClass read(byte[] classBytes, String jarPath, String entryName)
      throws LoaderException {
    try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(classBytes))) {
      if (input.readInt() != CLASS_MAGIC) {
        throw failure(jarPath, entryName, "Invalid class file magic");
      }

      input.readUnsignedShort(); // minor version
      input.readUnsignedShort(); // major version
      ConstantPool constantPool = readConstantPool(input, jarPath, entryName);

      int access = input.readUnsignedShort();
      String internalName = constantPool.className(input.readUnsignedShort(), jarPath, entryName);
      int superIndex = input.readUnsignedShort();
      String superName =
          superIndex == 0 ? null : constantPool.className(superIndex, jarPath, entryName);

      int interfaceCount = input.readUnsignedShort();
      List<String> interfaces = new ArrayList<>();
      for (int index = 0; index < interfaceCount; index++) {
        interfaces.add(constantPool.className(input.readUnsignedShort(), jarPath, entryName));
      }

      int fieldCount = input.readUnsignedShort();
      List<MinecraftInterpretedField> fields = new ArrayList<>();
      for (int index = 0; index < fieldCount; index++) {
        int fieldAccess = input.readUnsignedShort();
        String name = constantPool.utf8(input.readUnsignedShort(), jarPath, entryName);
        String descriptor = constantPool.utf8(input.readUnsignedShort(), jarPath, entryName);
        skipAttributes(input, jarPath, entryName);
        fields.add(
            new MinecraftInterpretedField(
                name, descriptor, fieldAccess, MinecraftAccessFlags.fieldFlags(fieldAccess)));
      }

      int methodCount = input.readUnsignedShort();
      List<MinecraftInterpretedMethod> methods = new ArrayList<>();
      for (int index = 0; index < methodCount; index++) {
        int methodAccess = input.readUnsignedShort();
        String name = constantPool.utf8(input.readUnsignedShort(), jarPath, entryName);
        String descriptor = constantPool.utf8(input.readUnsignedShort(), jarPath, entryName);
        skipAttributes(input, jarPath, entryName);
        methods.add(
            new MinecraftInterpretedMethod(
                name,
                descriptor,
                methodAccess,
                MinecraftAccessFlags.methodFlags(methodAccess),
                "<init>".equals(name),
                MinecraftAccessFlags.isStatic(methodAccess)));
      }

      skipAttributes(input, jarPath, entryName);

      String binaryName = internalName.replace('/', '.');
      String packageName = packageName(binaryName);
      return new MinecraftInterpretedClass(
          binaryName,
          internalName,
          packageName,
          superName,
          List.copyOf(interfaces),
          access,
          MinecraftAccessFlags.classFlags(access),
          List.copyOf(fields),
          List.copyOf(methods));
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to read class file " + entryName + " from jar " + jarPath, exception);
    }
  }

  private ConstantPool readConstantPool(DataInputStream input, String jarPath, String entryName)
      throws IOException, LoaderException {
    int count = input.readUnsignedShort();
    int[] tags = new int[count];
    Object[] values = new Object[count];
    for (int index = 1; index < count; index++) {
      int tag = input.readUnsignedByte();
      tags[index] = tag;
      switch (tag) {
        case CONSTANT_UTF8 -> values[index] = input.readUTF();
        case CONSTANT_INTEGER -> values[index] = input.readInt();
        case CONSTANT_FLOAT -> values[index] = input.readFloat();
        case CONSTANT_LONG -> {
          values[index] = input.readLong();
          index++;
        }
        case CONSTANT_DOUBLE -> {
          values[index] = input.readDouble();
          index++;
        }
        case CONSTANT_CLASS,
            CONSTANT_STRING,
            CONSTANT_METHOD_TYPE,
            CONSTANT_MODULE,
            CONSTANT_PACKAGE ->
            values[index] = input.readUnsignedShort();
        case CONSTANT_FIELDREF,
            CONSTANT_METHODREF,
            CONSTANT_INTERFACE_METHODREF,
            CONSTANT_NAME_AND_TYPE,
            CONSTANT_DYNAMIC,
            CONSTANT_INVOKE_DYNAMIC ->
            values[index] = new int[] {input.readUnsignedShort(), input.readUnsignedShort()};
        case CONSTANT_METHOD_HANDLE ->
            values[index] = new int[] {input.readUnsignedByte(), input.readUnsignedShort()};
        default ->
            throw failure(
                jarPath, entryName, "Unsupported constant pool tag " + tag + " at index " + index);
      }
    }
    return new ConstantPool(tags, values);
  }

  private void skipAttributes(DataInputStream input, String jarPath, String entryName)
      throws IOException, LoaderException {
    int attributeCount = input.readUnsignedShort();
    for (int index = 0; index < attributeCount; index++) {
      input.readUnsignedShort();
      long attributeLength = Integer.toUnsignedLong(input.readInt());
      try {
        input.skipNBytes(attributeLength);
      } catch (IOException exception) {
        throw new LoaderException(
            "Failed to skip class attribute in " + entryName + " from jar " + jarPath, exception);
      }
    }
  }

  private String packageName(String binaryName) {
    int lastDot = binaryName.lastIndexOf('.');
    return lastDot < 0 ? "" : binaryName.substring(0, lastDot);
  }

  private LoaderException failure(String jarPath, String entryName, String message) {
    return new LoaderException(message + " in " + entryName + " from jar " + jarPath);
  }

  private static final class ConstantPool {
    private final int[] tags;
    private final Object[] values;

    private ConstantPool(int[] tags, Object[] values) {
      this.tags = tags;
      this.values = values;
    }

    private String utf8(int index, String jarPath, String entryName) throws LoaderException {
      if (index <= 0 || index >= tags.length || tags[index] != CONSTANT_UTF8) {
        throw new LoaderException(
            "Invalid Utf8 constant pool index "
                + index
                + " in "
                + entryName
                + " from jar "
                + jarPath);
      }
      return (String) values[index];
    }

    private String className(int index, String jarPath, String entryName) throws LoaderException {
      if (index <= 0 || index >= tags.length || tags[index] != CONSTANT_CLASS) {
        throw new LoaderException(
            "Invalid Class constant pool index "
                + index
                + " in "
                + entryName
                + " from jar "
                + jarPath);
      }
      int nameIndex = (Integer) values[index];
      return utf8(nameIndex, jarPath, entryName);
    }
  }
}
