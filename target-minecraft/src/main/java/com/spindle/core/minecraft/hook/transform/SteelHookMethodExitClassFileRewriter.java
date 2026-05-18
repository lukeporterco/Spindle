package com.spindle.core.minecraft.hook.transform;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SteelHookMethodExitClassFileRewriter {
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
  private static final int ACCESS_NATIVE = 0x0100;
  private static final int ACCESS_SYNCHRONIZED = 0x0020;
  private static final int ACCESS_ABSTRACT = 0x0400;

  public SteelHookMethodExitRewriteResult rewrite(
      SteelHookMethodExitRewriteRequest request, byte[] originalClassBytes) {
    byte[] immutableOriginal = originalClassBytes == null ? null : originalClassBytes.clone();
    if (request == null) {
      return failedResult(null, null, "Method-exit rewrite request is required.", false, false);
    }
    String requestFailureReason = validateRequest(request);
    if (requestFailureReason != null) {
      return failedResult(request, immutableOriginal, requestFailureReason, false, false);
    }
    if (immutableOriginal == null || immutableOriginal.length == 0) {
      return failedResult(request, null, request.scope() + " requires class bytes.", false, false);
    }

    CodeAttributeLayout codeAttribute = null;
    try {
      ClassFileLayout classFile = parseClassFile(request, immutableOriginal);
      if (!request.targetOwnerInternalName().equals(classFile.thisClassName())) {
        return failedResult(
            request,
            immutableOriginal,
            "Target class internal name mismatch: expected "
                + request.targetOwnerInternalName()
                + " but found "
                + classFile.thisClassName()
                + ".",
            false,
            false);
      }
      MethodLayout targetMethod = findTargetMethod(request, classFile.methods());
      if (targetMethod == null) {
        return failedResult(
            request,
            immutableOriginal,
            "Target method not found exactly once: "
                + request.targetOwnerInternalName()
                + "."
                + request.targetMethodName()
                + request.targetDescriptor()
                + ".",
            false,
            false);
      }
      if ("<init>".equals(targetMethod.name())) {
        return failedResult(
            request,
            immutableOriginal,
            "Method-exit rewriting does not support constructors.",
            false,
            false);
      }
      if ("<clinit>".equals(targetMethod.name())) {
        return failedResult(
            request,
            immutableOriginal,
            "Method-exit rewriting does not support class initializers.",
            false,
            false);
      }
      if ((targetMethod.accessFlags() & (ACCESS_ABSTRACT | ACCESS_NATIVE)) != 0) {
        return failedResult(
            request, immutableOriginal, "Target method is abstract or native.", false, false);
      }
      if ((targetMethod.accessFlags() & ACCESS_SYNCHRONIZED) != 0) {
        return failedResult(
            request,
            immutableOriginal,
            "Method-exit rewriting does not support synchronized methods.",
            false,
            false);
      }
      if (targetMethod.codeAttribute() == null) {
        return failedResult(
            request, immutableOriginal, "Target method is missing a Code attribute.", false, false);
      }

      codeAttribute = targetMethod.codeAttribute();
      if (codeAttribute.stackMapTablePresent()) {
        return failedResult(
            request,
            immutableOriginal,
            "Method-exit rewriting rejects StackMapTable attributes.",
            true,
            codeAttribute.exceptionTableCount() > 0);
      }
      if (codeAttribute.exceptionTableCount() > 0) {
        return failedResult(
            request,
            immutableOriginal,
            "Method-exit rewriting rejects exception table entries.",
            false,
            true);
      }

      DescriptorCategory descriptorCategory = descriptorCategory(request.targetDescriptor());
      ReturnScanResult scanResult = scanCode(request, codeAttribute.code(), descriptorCategory);
      if (scanResult.returnOffsets().isEmpty()) {
        return failedResult(
            request,
            immutableOriginal,
            "Method-exit rewriting requires at least one supported normal return opcode.",
            false,
            false);
      }
      int transformedCodeLength =
          codeAttribute.code().length
              + (scanResult.returnOffsets().size() * request.instructionLength());
      if (transformedCodeLength > 65535) {
        return failedResult(
            request,
            immutableOriginal,
            "Code length exceeds the JVM limit after insertion.",
            false,
            false);
      }
      if (classFile.constantPoolCount() + 6 > 0xFFFF) {
        return failedResult(
            request,
            immutableOriginal,
            "Constant pool count exceeds the JVM limit after insertion.",
            false,
            false);
      }

      SteelHookMethodExitConstantPoolPatch constantPoolPatch =
          createConstantPoolPatch(classFile.constantPoolCount());
      byte[] insertedInstruction =
          invokestaticInstruction(constantPoolPatch.dispatcherMethodrefIndex());
      byte[] transformedCode =
          insertBeforeReturns(
              codeAttribute.code(), scanResult.returnOffsets(), insertedInstruction);
      byte[] rewrittenCodeAttributeBody = rewriteCodeAttribute(codeAttribute, transformedCode);
      byte[] transformedClassBytes =
          rewriteClassBytes(
              request,
              immutableOriginal,
              classFile,
              appendConstantPoolEntries(request, constantPoolPatch),
              constantPoolPatch.constantPoolCountAfter(),
              codeAttribute.attributeLengthOffset(),
              rewrittenCodeAttributeBody.length,
              codeAttribute.bodyEnd(),
              rewrittenCodeAttributeBody);

      String originalClassSha256 = sha256Hex(immutableOriginal);
      String transformedClassSha256 = sha256Hex(transformedClassBytes);
      String originalCodeSha256 = sha256Hex(codeAttribute.code());
      String transformedCodeSha256 = sha256Hex(transformedCode);
      String insertedInstructionHex = hex(insertedInstruction);
      List<Integer> transformedOffsets = new ArrayList<>(scanResult.returnOffsets().size());
      for (int index = 0; index < scanResult.returnOffsets().size(); index++) {
        transformedOffsets.add(
            scanResult.returnOffsets().get(index) + (index * request.instructionLength()));
      }
      SteelHookMethodExitCodePatchResult codePatch =
          new SteelHookMethodExitCodePatchResult(
              codeAttribute.code().length,
              transformedCode.length,
              originalCodeSha256,
              transformedCodeSha256,
              codeAttribute.maxStack(),
              codeAttribute.maxStack(),
              codeAttribute.maxLocals(),
              codeAttribute.maxLocals(),
              codeAttribute.exceptionTableCount(),
              false,
              scanResult.returnOffsets().size(),
              scanResult.returnOffsets().size(),
              scanResult.returnOffsets(),
              transformedOffsets,
              supportedReturnOpcodes(),
              insertedInstructionHex);
      SteelHookMethodExitTransformedClass transformedClass =
          new SteelHookMethodExitTransformedClass(
              classFile.thisClassName(), transformedClassBytes, transformedClassSha256);
      return new SteelHookMethodExitRewriteResult(
          SteelHookMethodExitRewriteStatus.TRANSFORMED,
          null,
          request,
          originalClassSha256,
          transformedClassSha256,
          originalCodeSha256,
          transformedCodeSha256,
          codeAttribute.code().length,
          transformedCode.length,
          constantPoolPatch.constantPoolCountBefore(),
          constantPoolPatch.constantPoolCountAfter(),
          constantPoolPatch.dispatcherMethodrefIndex(),
          insertedInstructionHex,
          true,
          true,
          true,
          false,
          false,
          false,
          false,
          false,
          false,
          scanResult.returnOffsets().size(),
          scanResult.returnOffsets().size(),
          constantPoolPatch,
          codePatch,
          transformedClass);
    } catch (TransformationException exception) {
      return failedResult(
          request,
          immutableOriginal,
          exception.getMessage(),
          codeAttribute != null && codeAttribute.stackMapTablePresent(),
          codeAttribute != null && codeAttribute.exceptionTableCount() > 0);
    }
  }

  private MethodLayout findTargetMethod(
      SteelHookMethodExitRewriteRequest request, List<MethodLayout> methods) {
    MethodLayout match = null;
    for (MethodLayout method : methods) {
      if (!request.targetMethodName().equals(method.name())
          || !request.targetDescriptor().equals(method.descriptor())) {
        continue;
      }
      if (match != null) {
        return null;
      }
      match = method;
    }
    return match;
  }

  private String validateRequest(SteelHookMethodExitRewriteRequest request) {
    if (!"invokestatic".equals(request.opcodeMnemonic())) {
      return "Method-exit rewriting supports invokestatic only.";
    }
    if (!"b8".equals(request.opcodeHex())) {
      return "Method-exit rewriting supports opcode hex b8 only.";
    }
    if (request.instructionLength() != 3) {
      return "Method-exit rewriting supports instruction length 3 only.";
    }
    if (request.stackMapTableRewriteSupported()) {
      return "Method-exit rewriting does not support StackMapTable rewriting.";
    }
    if (request.runtimeClassLoadingPathEnabled()) {
      return "Method-exit rewriting must keep runtime classloading disabled.";
    }
    if (request.publicApiExposed()) {
      return "Method-exit rewriting must not expose public API.";
    }
    if (request.javaModExecutionSandboxed()) {
      return "Method-exit rewriting must not claim Java mod execution sandboxing.";
    }
    if (isBlank(request.targetOwnerInternalName())
        || isBlank(request.targetMethodName())
        || isBlank(request.targetDescriptor())
        || isBlank(request.dispatcherOwnerInternalName())
        || isBlank(request.dispatcherMethodName())
        || isBlank(request.dispatcherDescriptor())) {
      return "Method-exit rewrite request target and dispatcher fields must be nonblank.";
    }
    return null;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private DescriptorCategory descriptorCategory(String descriptor) throws TransformationException {
    if (descriptor == null || descriptor.isBlank()) {
      throw new TransformationException("Method descriptor is required.");
    }
    int close = descriptor.lastIndexOf(')');
    if (!descriptor.startsWith("(") || close < 0 || close == descriptor.length() - 1) {
      throw new TransformationException("Malformed method descriptor " + descriptor + ".");
    }
    char type = descriptor.charAt(close + 1);
    return switch (type) {
      case 'V' -> DescriptorCategory.VOID;
      case 'Z', 'B', 'C', 'S', 'I' -> DescriptorCategory.INT_LIKE;
      case 'J' -> DescriptorCategory.LONG;
      case 'F' -> DescriptorCategory.FLOAT;
      case 'D' -> DescriptorCategory.DOUBLE;
      case 'L', '[' -> DescriptorCategory.REFERENCE;
      default ->
          throw new TransformationException(
              "Unsupported method return descriptor " + descriptor + ".");
    };
  }

  private ReturnScanResult scanCode(
      SteelHookMethodExitRewriteRequest request, byte[] code, DescriptorCategory descriptorCategory)
      throws TransformationException {
    List<Integer> returnOffsets = new ArrayList<>();
    boolean branchRewriteRequired = false;
    boolean switchRewriteRequired = false;
    int offset = 0;
    while (offset < code.length) {
      int opcode = code[offset] & 0xFF;
      SteelHookMethodExitReturnOpcode returnOpcode =
          SteelHookMethodExitReturnOpcode.fromOpcode(opcode);
      if (returnOpcode != null) {
        if (!matchesDescriptor(returnOpcode, descriptorCategory)) {
          throw new TransformationException(
              "Return opcode "
                  + returnOpcode.mnemonic()
                  + " does not match method descriptor "
                  + request.targetDescriptor()
                  + ".");
        }
        returnOffsets.add(offset);
        offset += returnOpcode.byteLength();
        continue;
      }
      if (isBranchOpcode(opcode)) {
        branchRewriteRequired = true;
        throw new TransformationException("Method-exit rewriting rejects branch opcodes.");
      }
      if (opcode == 0xaa) {
        switchRewriteRequired = true;
        throw new TransformationException("Method-exit rewriting rejects tableswitch.");
      }
      if (opcode == 0xab) {
        switchRewriteRequired = true;
        throw new TransformationException("Method-exit rewriting rejects lookupswitch.");
      }
      if (opcode == 0xa8 || opcode == 0xc9) {
        throw new TransformationException("Method-exit rewriting rejects jsr/jsr_w.");
      }
      if (opcode == 0xa9) {
        throw new TransformationException("Method-exit rewriting rejects ret.");
      }
      if (opcode == 0xc4) {
        throw new TransformationException("Method-exit rewriting rejects wide.");
      }
      if (opcode == 0xbf) {
        throw new TransformationException("Method-exit rewriting rejects athrow.");
      }
      int length = instructionLength(opcode);
      if (length <= 0) {
        throw new TransformationException(
            "Unsupported or malformed instruction encoding at bytecode offset " + offset + ".");
      }
      if (offset + length > code.length) {
        throw new TransformationException(
            "Unsupported or malformed instruction encoding at bytecode offset " + offset + ".");
      }
      offset += length;
    }
    return new ReturnScanResult(returnOffsets, branchRewriteRequired, switchRewriteRequired);
  }

  private boolean matchesDescriptor(
      SteelHookMethodExitReturnOpcode opcode, DescriptorCategory descriptorCategory) {
    return switch (opcode.descriptorCategory()) {
      case VOID -> descriptorCategory == DescriptorCategory.VOID;
      case INT_LIKE -> descriptorCategory == DescriptorCategory.INT_LIKE;
      case LONG -> descriptorCategory == DescriptorCategory.LONG;
      case FLOAT -> descriptorCategory == DescriptorCategory.FLOAT;
      case DOUBLE -> descriptorCategory == DescriptorCategory.DOUBLE;
      case REFERENCE -> descriptorCategory == DescriptorCategory.REFERENCE;
    };
  }

  private boolean isBranchOpcode(int opcode) {
    return (opcode >= 0x99 && opcode <= 0xa7) || opcode == 0xc6 || opcode == 0xc7 || opcode == 0xc8;
  }

  private int instructionLength(int opcode) {
    return switch (opcode) {
      case 0x00,
          0x01,
          0x02,
          0x03,
          0x04,
          0x05,
          0x06,
          0x07,
          0x08,
          0x09,
          0x0a,
          0x0b,
          0x0c,
          0x0d,
          0x0e,
          0x0f,
          0x1a,
          0x1b,
          0x1c,
          0x1d,
          0x1e,
          0x1f,
          0x20,
          0x21,
          0x22,
          0x23,
          0x24,
          0x25,
          0x26,
          0x27,
          0x28,
          0x29,
          0x2a,
          0x2b,
          0x2c,
          0x2d,
          0x2e,
          0x2f,
          0x30,
          0x31,
          0x32,
          0x33,
          0x34,
          0x35,
          0x3b,
          0x3c,
          0x3d,
          0x3e,
          0x3f,
          0x40,
          0x41,
          0x42,
          0x43,
          0x44,
          0x45,
          0x46,
          0x47,
          0x48,
          0x49,
          0x4a,
          0x4b,
          0x4c,
          0x4d,
          0x4e,
          0x4f,
          0x50,
          0x51,
          0x52,
          0x53,
          0x54,
          0x55,
          0x56,
          0x57,
          0x58,
          0x59,
          0x5a,
          0x5b,
          0x5c,
          0x5d,
          0x5e,
          0x5f,
          0x60,
          0x61,
          0x62,
          0x63,
          0x64,
          0x65,
          0x66,
          0x67,
          0x68,
          0x69,
          0x6a,
          0x6b,
          0x6c,
          0x6d,
          0x6e,
          0x6f,
          0x70,
          0x71,
          0x72,
          0x73,
          0x74,
          0x75,
          0x76,
          0x77,
          0x78,
          0x79,
          0x7a,
          0x7b,
          0x7c,
          0x7d,
          0x7e,
          0x7f,
          0x80,
          0x81,
          0x82,
          0x83,
          0x85,
          0x86,
          0x87,
          0x88,
          0x89,
          0x8a,
          0x8b,
          0x8c,
          0x8d,
          0x8e,
          0x8f,
          0x90,
          0x91,
          0x92,
          0x93,
          0x94,
          0x95,
          0x96,
          0x97,
          0x98,
          0xac,
          0xad,
          0xae,
          0xaf,
          0xb0,
          0xb1,
          0xbe,
          0xc2,
          0xc3,
          0xca ->
          1;
      case 0x10, 0x12, 0x15, 0x16, 0x17, 0x18, 0x19, 0x36, 0x37, 0x38, 0x39, 0x3a, 0xbc -> 2;
      case 0x11,
          0x13,
          0x14,
          0x84,
          0x99,
          0x9a,
          0x9b,
          0x9c,
          0x9d,
          0x9e,
          0x9f,
          0xa0,
          0xa1,
          0xa2,
          0xa3,
          0xa4,
          0xa5,
          0xa6,
          0xa7,
          0xa8,
          0xb2,
          0xb3,
          0xb4,
          0xb5,
          0xb6,
          0xb7,
          0xb8,
          0xbb,
          0xbd,
          0xc0,
          0xc1,
          0xc6,
          0xc7 ->
          3;
      case 0xc5 -> 4;
      case 0xb9, 0xba, 0xc8, 0xc9 -> 5;
      default -> -1;
    };
  }

  private List<String> supportedReturnOpcodes() {
    List<String> supported = new ArrayList<>();
    for (SteelHookMethodExitReturnOpcode opcode : SteelHookMethodExitReturnOpcode.values()) {
      supported.add(opcode.mnemonic());
    }
    return supported;
  }

  private byte[] insertBeforeReturns(
      byte[] originalCode, List<Integer> returnOffsets, byte[] insertedInstruction) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    int cursor = 0;
    for (int returnOffset : returnOffsets) {
      bytes.write(originalCode, cursor, returnOffset - cursor);
      bytes.write(insertedInstruction, 0, insertedInstruction.length);
      cursor = returnOffset;
    }
    bytes.write(originalCode, cursor, originalCode.length - cursor);
    return bytes.toByteArray();
  }

  private byte[] rewriteCodeAttribute(CodeAttributeLayout codeAttribute, byte[] transformedCode)
      throws TransformationException {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (DataOutputStream output = new DataOutputStream(bytes)) {
        output.writeShort(codeAttribute.maxStack());
        output.writeShort(codeAttribute.maxLocals());
        output.writeInt(transformedCode.length);
        output.write(transformedCode);
        output.writeShort(codeAttribute.exceptionTableCount());
        output.writeShort(codeAttribute.nestedAttributes().size());
        for (NestedAttributeLayout nestedAttribute : codeAttribute.nestedAttributes()) {
          output.writeShort(nestedAttribute.nameIndex());
          output.writeInt(nestedAttribute.body().length);
          output.write(nestedAttribute.body());
        }
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new TransformationException(
          "Failed to rewrite Code attribute for " + codeAttribute.scope() + ".");
    }
  }

  private byte[] invokestaticInstruction(int methodrefIndex) {
    return new byte[] {(byte) 0xb8, (byte) (methodrefIndex >>> 8), (byte) methodrefIndex};
  }

  private ClassFileLayout parseClassFile(
      SteelHookMethodExitRewriteRequest request, byte[] classBytes) throws TransformationException {
    ClassReader reader = new ClassReader(classBytes, request.scope());
    if (reader.readInt() != CLASS_MAGIC) {
      throw new TransformationException("Invalid class file magic for " + request.scope() + ".");
    }
    reader.skip(2);
    reader.skip(2);
    int constantPoolCount = reader.readUnsignedShort();
    int constantPoolStart = reader.position();
    ConstantPool constantPool = readConstantPool(reader, constantPoolCount, request.scope());
    int constantPoolEnd = reader.position();
    reader.skip(2);
    int thisClassIndex = reader.readUnsignedShort();
    reader.skip(2);
    String thisClassName = constantPool.className(thisClassIndex);
    int interfaceCount = reader.readUnsignedShort();
    reader.skip(interfaceCount * 2);
    int fieldCount = reader.readUnsignedShort();
    for (int index = 0; index < fieldCount; index++) {
      skipMember(reader);
    }
    int methodCount = reader.readUnsignedShort();
    List<MethodLayout> methods = new ArrayList<>(methodCount);
    for (int index = 0; index < methodCount; index++) {
      methods.add(readMethod(reader, constantPool, classBytes, request.scope()));
    }
    int classAttributeCount = reader.readUnsignedShort();
    for (int index = 0; index < classAttributeCount; index++) {
      skipAttribute(reader);
    }
    if (reader.position() != classBytes.length) {
      throw new TransformationException("Malformed class file for " + request.scope() + ".");
    }
    return new ClassFileLayout(
        constantPoolCount, constantPoolStart, constantPoolEnd, thisClassName, methods);
  }

  private ConstantPool readConstantPool(ClassReader reader, int count, String scope)
      throws TransformationException {
    int[] tags = new int[count];
    Object[] values = new Object[count];
    for (int index = 1; index < count; index++) {
      int tag = reader.readUnsignedByte();
      tags[index] = tag;
      switch (tag) {
        case CONSTANT_UTF8 -> values[index] = reader.readUtf();
        case CONSTANT_INTEGER, CONSTANT_FLOAT -> reader.skip(4);
        case CONSTANT_LONG, CONSTANT_DOUBLE -> {
          reader.skip(8);
          index++;
        }
        case CONSTANT_CLASS,
            CONSTANT_STRING,
            CONSTANT_METHOD_TYPE,
            CONSTANT_MODULE,
            CONSTANT_PACKAGE ->
            values[index] = reader.readUnsignedShort();
        case CONSTANT_FIELDREF,
            CONSTANT_METHODREF,
            CONSTANT_INTERFACE_METHODREF,
            CONSTANT_NAME_AND_TYPE,
            CONSTANT_DYNAMIC,
            CONSTANT_INVOKE_DYNAMIC ->
            values[index] = new int[] {reader.readUnsignedShort(), reader.readUnsignedShort()};
        case CONSTANT_METHOD_HANDLE ->
            values[index] = new int[] {reader.readUnsignedByte(), reader.readUnsignedShort()};
        default ->
            throw new TransformationException(
                "Unsupported constant pool tag " + tag + " for " + scope + ".");
      }
    }
    return new ConstantPool(tags, values, scope);
  }

  private MethodLayout readMethod(
      ClassReader reader, ConstantPool constantPool, byte[] classBytes, String scope)
      throws TransformationException {
    int accessFlags = reader.readUnsignedShort();
    String name = constantPool.utf8(reader.readUnsignedShort());
    String descriptor = constantPool.utf8(reader.readUnsignedShort());
    int attributeCount = reader.readUnsignedShort();
    CodeAttributeLayout codeAttribute = null;
    for (int index = 0; index < attributeCount; index++) {
      int attributeNameIndex = reader.readUnsignedShort();
      String attributeName = constantPool.utf8(attributeNameIndex);
      int attributeLengthOffset = reader.position();
      int attributeLength = reader.readInt();
      int bodyOffset = reader.position();
      int bodyEnd = bodyOffset + attributeLength;
      if (bodyEnd > classBytes.length) {
        throw new TransformationException("Malformed class file for " + scope + ".");
      }
      if ("Code".equals(attributeName)) {
        if (codeAttribute != null) {
          throw new TransformationException("Target method has multiple Code attributes.");
        }
        codeAttribute =
            parseCodeAttribute(
                classBytes,
                constantPool,
                attributeLengthOffset,
                bodyOffset,
                attributeLength,
                scope);
      }
      reader.position(bodyEnd);
    }
    return new MethodLayout(name, descriptor, accessFlags, codeAttribute);
  }

  private CodeAttributeLayout parseCodeAttribute(
      byte[] classBytes,
      ConstantPool constantPool,
      int attributeLengthOffset,
      int bodyOffset,
      int attributeLength,
      String scope)
      throws TransformationException {
    ClassReader reader =
        new ClassReader(classBytes, bodyOffset, bodyOffset + attributeLength, scope);
    if (attributeLength < 12) {
      throw new TransformationException("Malformed Code attribute for " + scope + ".");
    }
    int maxStack = reader.readUnsignedShort();
    int maxLocals = reader.readUnsignedShort();
    int codeLength = reader.readInt();
    if (codeLength < 0 || reader.position() + codeLength > bodyOffset + attributeLength) {
      throw new TransformationException(
          "Malformed Code attribute bytecode length for " + scope + ".");
    }
    byte[] code = Arrays.copyOfRange(classBytes, reader.position(), reader.position() + codeLength);
    reader.skip(codeLength);
    int exceptionTableCount = reader.readUnsignedShort();
    for (int index = 0; index < exceptionTableCount; index++) {
      reader.skip(8);
    }
    int nestedAttributeCount = reader.readUnsignedShort();
    boolean stackMapTablePresent = false;
    List<NestedAttributeLayout> nestedAttributes = new ArrayList<>(nestedAttributeCount);
    for (int index = 0; index < nestedAttributeCount; index++) {
      int nestedNameIndex = reader.readUnsignedShort();
      String nestedName = constantPool.utf8(nestedNameIndex);
      int nestedLength = reader.readInt();
      int bodyStart = reader.position();
      if (bodyStart + nestedLength > bodyOffset + attributeLength) {
        throw new TransformationException("Malformed Code attribute bounds for " + scope + ".");
      }
      if ("StackMapTable".equals(nestedName)) {
        stackMapTablePresent = true;
      }
      nestedAttributes.add(
          new NestedAttributeLayout(
              nestedNameIndex,
              nestedName,
              Arrays.copyOfRange(classBytes, bodyStart, bodyStart + nestedLength)));
      reader.skip(nestedLength);
    }
    if (reader.position() != bodyOffset + attributeLength) {
      throw new TransformationException("Malformed Code attribute bounds for " + scope + ".");
    }
    return new CodeAttributeLayout(
        scope,
        attributeLengthOffset,
        bodyOffset + attributeLength,
        maxStack,
        maxLocals,
        code,
        exceptionTableCount,
        nestedAttributes,
        stackMapTablePresent);
  }

  private void skipMember(ClassReader reader) throws TransformationException {
    reader.skip(2);
    reader.skip(2);
    reader.skip(2);
    int attributeCount = reader.readUnsignedShort();
    for (int index = 0; index < attributeCount; index++) {
      skipAttribute(reader);
    }
  }

  private void skipAttribute(ClassReader reader) throws TransformationException {
    reader.skip(2);
    reader.skip(reader.readInt());
  }

  private SteelHookMethodExitConstantPoolPatch createConstantPoolPatch(
      int constantPoolCountBefore) {
    int dispatcherOwnerUtf8Index = constantPoolCountBefore;
    int dispatcherClassIndex = constantPoolCountBefore + 1;
    int dispatcherMethodNameUtf8Index = constantPoolCountBefore + 2;
    int dispatcherDescriptorUtf8Index = constantPoolCountBefore + 3;
    int dispatcherNameAndTypeIndex = constantPoolCountBefore + 4;
    int dispatcherMethodrefIndex = constantPoolCountBefore + 5;
    return new SteelHookMethodExitConstantPoolPatch(
        constantPoolCountBefore,
        constantPoolCountBefore + 6,
        6,
        dispatcherOwnerUtf8Index,
        dispatcherClassIndex,
        dispatcherMethodNameUtf8Index,
        dispatcherDescriptorUtf8Index,
        dispatcherNameAndTypeIndex,
        dispatcherMethodrefIndex);
  }

  private byte[] appendConstantPoolEntries(
      SteelHookMethodExitRewriteRequest request, SteelHookMethodExitConstantPoolPatch patch)
      throws TransformationException {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (DataOutputStream output = new DataOutputStream(bytes)) {
        writeUtf8(output, request.dispatcherOwnerInternalName());
        output.writeByte(CONSTANT_CLASS);
        output.writeShort(patch.dispatcherOwnerUtf8Index());
        writeUtf8(output, request.dispatcherMethodName());
        writeUtf8(output, request.dispatcherDescriptor());
        output.writeByte(CONSTANT_NAME_AND_TYPE);
        output.writeShort(patch.dispatcherMethodNameUtf8Index());
        output.writeShort(patch.dispatcherDescriptorUtf8Index());
        output.writeByte(CONSTANT_METHODREF);
        output.writeShort(patch.dispatcherClassIndex());
        output.writeShort(patch.dispatcherNameAndTypeIndex());
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new TransformationException("Failed to append dispatcher constant pool entries.");
    }
  }

  private void writeUtf8(DataOutputStream output, String value) throws IOException {
    output.writeByte(CONSTANT_UTF8);
    output.writeUTF(value);
  }

  private byte[] rewriteClassBytes(
      SteelHookMethodExitRewriteRequest request,
      byte[] originalClassBytes,
      ClassFileLayout classFile,
      byte[] appendedConstantPoolEntries,
      int constantPoolCountAfter,
      int attributeLengthOffset,
      int transformedAttributeLength,
      int codeAttributeBodyEnd,
      byte[] rewrittenCodeAttributeBody)
      throws TransformationException {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (DataOutputStream output = new DataOutputStream(bytes)) {
        output.write(originalClassBytes, 0, 8);
        output.writeShort(constantPoolCountAfter);
        output.write(
            originalClassBytes,
            classFile.constantPoolStart(),
            classFile.constantPoolEnd() - classFile.constantPoolStart());
        output.write(appendedConstantPoolEntries);
        output.write(
            originalClassBytes,
            classFile.constantPoolEnd(),
            attributeLengthOffset - classFile.constantPoolEnd());
        output.writeInt(transformedAttributeLength);
        output.write(rewrittenCodeAttributeBody);
        output.write(
            originalClassBytes,
            codeAttributeBodyEnd,
            originalClassBytes.length - codeAttributeBodyEnd);
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new TransformationException(
          "Failed to rewrite class bytes for " + request.scope() + ".");
    }
  }

  private SteelHookMethodExitRewriteResult failedResult(
      SteelHookMethodExitRewriteRequest request,
      byte[] originalClassBytes,
      String failureReason,
      boolean stackMapTablePresent,
      boolean exceptionTablePresent) {
    return new SteelHookMethodExitRewriteResult(
        SteelHookMethodExitRewriteStatus.REJECTED,
        failureReason,
        request,
        originalClassBytes == null ? null : sha256Hex(originalClassBytes),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        false,
        false,
        stackMapTablePresent,
        false,
        false,
        exceptionTablePresent,
        false,
        false,
        null,
        null,
        null,
        null,
        null);
  }

  private String sha256Hex(byte[] bytes) {
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
      throw new IllegalStateException(
          "SHA-256 is unavailable for method-exit rewriting.", exception);
    }
  }

  private String hex(byte[] bytes) {
    StringBuilder builder = new StringBuilder(bytes.length * 3 - 1);
    for (int index = 0; index < bytes.length; index++) {
      if (index > 0) {
        builder.append(' ');
      }
      int value = bytes[index] & 0xFF;
      builder.append(Character.forDigit((value >>> 4) & 0xF, 16));
      builder.append(Character.forDigit(value & 0xF, 16));
    }
    return builder.toString();
  }

  private enum DescriptorCategory {
    VOID,
    INT_LIKE,
    LONG,
    FLOAT,
    DOUBLE,
    REFERENCE
  }

  private record ReturnScanResult(
      List<Integer> returnOffsets, boolean branchRewriteRequired, boolean switchRewriteRequired) {
    private ReturnScanResult {
      returnOffsets = List.copyOf(returnOffsets);
    }
  }

  private record ClassFileLayout(
      int constantPoolCount,
      int constantPoolStart,
      int constantPoolEnd,
      String thisClassName,
      List<MethodLayout> methods) {}

  private record MethodLayout(
      String name, String descriptor, int accessFlags, CodeAttributeLayout codeAttribute) {}

  private record CodeAttributeLayout(
      String scope,
      int attributeLengthOffset,
      int bodyEnd,
      int maxStack,
      int maxLocals,
      byte[] code,
      int exceptionTableCount,
      List<NestedAttributeLayout> nestedAttributes,
      boolean stackMapTablePresent) {
    private CodeAttributeLayout {
      code = code.clone();
      nestedAttributes = List.copyOf(nestedAttributes);
    }
  }

  private record NestedAttributeLayout(int nameIndex, String name, byte[] body) {
    private NestedAttributeLayout {
      body = body.clone();
    }
  }

  private static final class ConstantPool {
    private final int[] tags;
    private final Object[] values;
    private final String scope;

    private ConstantPool(int[] tags, Object[] values, String scope) {
      this.tags = tags;
      this.values = values;
      this.scope = scope;
    }

    private String utf8(int index) throws TransformationException {
      if (index <= 0 || index >= tags.length || tags[index] != CONSTANT_UTF8) {
        throw new TransformationException(
            "Invalid Utf8 constant pool index for " + scope + ": " + index);
      }
      return (String) values[index];
    }

    private String className(int index) throws TransformationException {
      if (index <= 0 || index >= tags.length || tags[index] != CONSTANT_CLASS) {
        throw new TransformationException(
            "Invalid Class constant pool index for " + scope + ": " + index);
      }
      return utf8((Integer) values[index]);
    }
  }

  private static final class ClassReader {
    private final byte[] bytes;
    private final int limit;
    private final String scope;
    private int position;

    private ClassReader(byte[] bytes, String scope) {
      this(bytes, 0, bytes.length, scope);
    }

    private ClassReader(byte[] bytes, int position, int limit, String scope) {
      this.bytes = bytes;
      this.position = position;
      this.limit = limit;
      this.scope = scope;
    }

    private int position() {
      return position;
    }

    private void position(int newPosition) throws TransformationException {
      if (newPosition < 0 || newPosition > limit) {
        throw new TransformationException("Malformed class file for " + scope + ".");
      }
      position = newPosition;
    }

    private int readUnsignedByte() throws TransformationException {
      require(1);
      return bytes[position++] & 0xFF;
    }

    private int readUnsignedShort() throws TransformationException {
      require(2);
      int value = ((bytes[position] & 0xFF) << 8) | (bytes[position + 1] & 0xFF);
      position += 2;
      return value;
    }

    private int readInt() throws TransformationException {
      require(4);
      int value =
          ((bytes[position] & 0xFF) << 24)
              | ((bytes[position + 1] & 0xFF) << 16)
              | ((bytes[position + 2] & 0xFF) << 8)
              | (bytes[position + 3] & 0xFF);
      position += 4;
      return value;
    }

    private String readUtf() throws TransformationException {
      int length = readUnsignedShort();
      require(length);
      byte[] utf = Arrays.copyOfRange(bytes, position, position + length);
      position += length;
      try (var input =
          new java.io.DataInputStream(
              new java.io.SequenceInputStream(
                  new java.io.ByteArrayInputStream(
                      new byte[] {(byte) (length >>> 8), (byte) length}),
                  new java.io.ByteArrayInputStream(utf)))) {
        return input.readUTF();
      } catch (IOException exception) {
        throw new TransformationException("Malformed Utf8 constant pool entry for " + scope + ".");
      }
    }

    private void skip(int byteCount) throws TransformationException {
      require(byteCount);
      position += byteCount;
    }

    private void require(int byteCount) throws TransformationException {
      if (byteCount < 0 || position + byteCount > limit) {
        throw new TransformationException("Malformed class file for " + scope + ".");
      }
    }
  }

  private static final class TransformationException extends Exception {
    private TransformationException(String message) {
      super(message);
    }
  }
}
