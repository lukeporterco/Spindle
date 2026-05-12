package com.spindle.core.minecraft.hook.place;

import com.spindle.core.diagnostics.LoaderException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MinecraftMethodCodeReader {
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
  private static final int ACCESS_ABSTRACT = 0x0400;
  private static final int ACCESS_NATIVE = 0x0100;

  public MinecraftMethodCodeSummary read(
      byte[] classBytes, String ownerInternalName, String memberName, String descriptor)
      throws LoaderException {
    if (classBytes == null) {
      throw new LoaderException("Class bytes are required for method code analysis.");
    }
    try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(classBytes))) {
      if (input.readInt() != CLASS_MAGIC) {
        throw new LoaderException(
            "Invalid class file magic while reading method "
                + ownerInternalName
                + "."
                + memberName
                + descriptor);
      }

      input.readUnsignedShort();
      input.readUnsignedShort();
      ConstantPool constantPool = readConstantPool(input);
      input.readUnsignedShort();
      input.readUnsignedShort();
      input.readUnsignedShort();

      int interfaceCount = input.readUnsignedShort();
      for (int index = 0; index < interfaceCount; index++) {
        input.readUnsignedShort();
      }

      int fieldCount = input.readUnsignedShort();
      for (int index = 0; index < fieldCount; index++) {
        input.readUnsignedShort();
        input.readUnsignedShort();
        input.readUnsignedShort();
        skipAttributes(input);
      }

      int methodCount = input.readUnsignedShort();
      for (int index = 0; index < methodCount; index++) {
        int access = input.readUnsignedShort();
        String currentName = constantPool.utf8(input.readUnsignedShort());
        String currentDescriptor = constantPool.utf8(input.readUnsignedShort());
        int attributeCount = input.readUnsignedShort();
        boolean matches = memberName.equals(currentName) && descriptor.equals(currentDescriptor);
        if (!matches) {
          skipAttributeBodies(input, attributeCount);
          continue;
        }

        boolean abstractOrNative = (access & (ACCESS_ABSTRACT | ACCESS_NATIVE)) != 0;
        for (int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
          String attributeName = constantPool.utf8(input.readUnsignedShort());
          int attributeLength = input.readInt();
          if ("Code".equals(attributeName)) {
            return readCodeAttribute(input, attributeLength, abstractOrNative);
          }
          skipFully(input, Integer.toUnsignedLong(attributeLength));
        }
        return new MinecraftMethodCodeSummary(
            null, null, null, null, null, null, false, abstractOrNative, null);
      }

      return new MinecraftMethodCodeSummary(null, null, null, null, null, null, false, false, null);
    } catch (IOException exception) {
      throw new LoaderException(
          "Failed to read method code for " + ownerInternalName + "." + memberName + descriptor,
          exception);
    }
  }

  private MinecraftMethodCodeSummary readCodeAttribute(
      DataInputStream input, int attributeLength, boolean abstractOrNative)
      throws IOException, LoaderException {
    long remaining = Integer.toUnsignedLong(attributeLength);
    if (remaining < 12L) {
      throw new LoaderException("Malformed Code attribute: truncated header.");
    }

    int maxStack = input.readUnsignedShort();
    int maxLocals = input.readUnsignedShort();
    long codeLength = Integer.toUnsignedLong(input.readInt());
    remaining -= 8L;
    if (codeLength > remaining) {
      throw new LoaderException("Malformed Code attribute: code length exceeds attribute size.");
    }

    byte[] code = input.readNBytes((int) codeLength);
    if (code.length != (int) codeLength) {
      throw new LoaderException("Malformed Code attribute: truncated bytecode.");
    }
    remaining -= codeLength;
    if (remaining < 2L) {
      throw new LoaderException("Malformed Code attribute: missing exception table length.");
    }

    int exceptionTableCount = input.readUnsignedShort();
    remaining -= 2L;
    long exceptionTableBytes = (long) exceptionTableCount * 8L;
    if (exceptionTableBytes > remaining) {
      throw new LoaderException("Malformed Code attribute: truncated exception table.");
    }
    skipFully(input, exceptionTableBytes);
    remaining -= exceptionTableBytes;
    if (remaining < 2L) {
      throw new LoaderException("Malformed Code attribute: missing nested attribute count.");
    }

    int nestedAttributeCount = input.readUnsignedShort();
    remaining -= 2L;
    for (int index = 0; index < nestedAttributeCount; index++) {
      if (remaining < 6L) {
        throw new LoaderException("Malformed Code attribute: truncated nested attribute.");
      }
      input.readUnsignedShort();
      int nestedLength = input.readInt();
      remaining -= 6L;
      long nestedBodyLength = Integer.toUnsignedLong(nestedLength);
      if (nestedBodyLength > remaining) {
        throw new LoaderException("Malformed Code attribute: nested attribute exceeds bounds.");
      }
      skipFully(input, nestedBodyLength);
      remaining -= nestedBodyLength;
    }
    if (remaining > 0L) {
      skipFully(input, remaining);
    }

    return new MinecraftMethodCodeSummary(
        maxStack,
        maxLocals,
        code.length,
        sha256Hex(code),
        exceptionTableCount,
        nestedAttributeCount,
        true,
        abstractOrNative,
        0);
  }

  private ConstantPool readConstantPool(DataInputStream input) throws IOException, LoaderException {
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
            throw new LoaderException(
                "Unsupported constant pool tag " + tag + " while reading method code.");
      }
    }
    return new ConstantPool(tags, values);
  }

  private void skipAttributes(DataInputStream input) throws IOException, LoaderException {
    int attributeCount = input.readUnsignedShort();
    skipAttributeBodies(input, attributeCount);
  }

  private void skipAttributeBodies(DataInputStream input, int attributeCount)
      throws IOException, LoaderException {
    for (int index = 0; index < attributeCount; index++) {
      input.readUnsignedShort();
      int attributeLength = input.readInt();
      skipFully(input, Integer.toUnsignedLong(attributeLength));
    }
  }

  private void skipFully(DataInputStream input, long byteCount)
      throws IOException, LoaderException {
    try {
      input.skipNBytes(byteCount);
    } catch (IOException exception) {
      throw new LoaderException("Malformed class file: truncated attribute body.", exception);
    }
  }

  private String sha256Hex(byte[] bytes) throws LoaderException {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(bytes);
      StringBuilder builder = new StringBuilder(hash.length * 2);
      for (byte value : hash) {
        builder.append(Character.forDigit((value >>> 4) & 0xF, 16));
        builder.append(Character.forDigit(value & 0xF, 16));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new LoaderException("SHA-256 is unavailable for method code analysis.", exception);
    }
  }

  private static final class ConstantPool {
    private final int[] tags;
    private final Object[] values;

    private ConstantPool(int[] tags, Object[] values) {
      this.tags = tags;
      this.values = values;
    }

    private String utf8(int index) throws LoaderException {
      if (index <= 0 || index >= tags.length || tags[index] != CONSTANT_UTF8) {
        throw new LoaderException(
            "Invalid Utf8 constant pool index " + index + " while reading method code.");
      }
      return (String) values[index];
    }
  }
}
