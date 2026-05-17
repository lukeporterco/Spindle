package com.spindle.core.minecraft.hook.transform;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SteelHookMethodEntryClassFileRewriter {
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

  private final SteelHookStackMapTableRewriter stackMapTableRewriter =
      new SteelHookStackMapTableRewriter();

  public SteelHookMethodEntryRewriteResult rewrite(
      SteelHookMethodEntryRewriteRequest request, byte[] originalClassBytes) {
    byte[] immutableOriginal = originalClassBytes == null ? null : originalClassBytes.clone();
    if (request == null) {
      return failedResult(
          null, null, "Method-entry rewrite request is required.", false, false, false, null);
    }
    String requestFailureReason = validateRequest(request);
    if (requestFailureReason != null) {
      return failedResult(
          request, immutableOriginal, requestFailureReason, false, false, false, null);
    }
    if (immutableOriginal == null || immutableOriginal.length == 0) {
      return failedResult(
          request, null, request.scope() + " requires class bytes.", false, false, false, null);
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
            false,
            false,
            null);
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
            false,
            false,
            null);
      }
      if ((targetMethod.accessFlags() & (ACCESS_ABSTRACT | ACCESS_NATIVE)) != 0) {
        return failedResult(
            request,
            immutableOriginal,
            "Target method is abstract or native.",
            false,
            false,
            false,
            null);
      }
      if (targetMethod.codeAttribute() == null) {
        return failedResult(
            request,
            immutableOriginal,
            "Target method is missing a Code attribute.",
            false,
            false,
            false,
            null);
      }

      codeAttribute = targetMethod.codeAttribute();
      if (codeAttribute.stackMapTablePresent() && !request.stackMapTableRewriteSupported()) {
        return failedResult(
            request,
            immutableOriginal,
            "StackMapTable rewriting is not supported.",
            true,
            false,
            true,
            null);
      }
      if (codeAttribute.code().length + request.instructionLength() > 65535) {
        return failedResult(
            request,
            immutableOriginal,
            "Code length exceeds the JVM limit after insertion.",
            codeAttribute.stackMapTablePresent(),
            request.stackMapTableRewriteSupported(),
            false,
            null);
      }
      if (classFile.constantPoolCount() + 6 > 0xFFFF) {
        return failedResult(
            request,
            immutableOriginal,
            "Constant pool count exceeds the JVM limit after insertion.",
            codeAttribute.stackMapTablePresent(),
            request.stackMapTableRewriteSupported(),
            false,
            null);
      }

      SteelHookMethodEntryConstantPoolPatch constantPoolPatch =
          createConstantPoolPatch(classFile.constantPoolCount());
      byte[] insertedInstruction =
          invokestaticInstruction(constantPoolPatch.dispatcherMethodrefIndex());
      byte[] transformedCode = prepend(insertedInstruction, codeAttribute.code());
      RewrittenCodeAttribute rewrittenCodeAttribute =
          rewriteCodeAttribute(request, codeAttribute, transformedCode);
      byte[] transformedClassBytes =
          rewriteClassBytes(
              request,
              immutableOriginal,
              classFile,
              appendConstantPoolEntries(request, constantPoolPatch),
              constantPoolPatch.constantPoolCountAfter(),
              codeAttribute.attributeLengthOffset(),
              rewrittenCodeAttribute.body().length,
              codeAttribute.bodyEnd(),
              rewrittenCodeAttribute.body());

      String insertedInstructionHex = hex(insertedInstruction);
      String originalClassSha256 = sha256Hex(immutableOriginal);
      String transformedClassSha256 = sha256Hex(transformedClassBytes);
      String originalCodeSha256 = sha256Hex(codeAttribute.code());
      String transformedCodeSha256 = sha256Hex(transformedCode);
      SteelHookMethodEntryCodePatchResult codePatch =
          new SteelHookMethodEntryCodePatchResult(
              codeAttribute.code().length,
              transformedCode.length,
              originalCodeSha256,
              transformedCodeSha256,
              codeAttribute.maxStack(),
              codeAttribute.maxStack(),
              codeAttribute.maxLocals(),
              codeAttribute.maxLocals(),
              codeAttribute.exceptionTableCount(),
              codeAttribute.exceptionTableCount() > 0,
              insertedInstructionHex);
      SteelHookStackMapTablePatch stackMapTablePatch =
          rewrittenCodeAttribute.stackMapTablePatch() == null
              ? new SteelHookStackMapTablePatch(
                  codeAttribute.stackMapTablePresent(),
                  request.stackMapTableRewriteSupported(),
                  false,
                  false,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null)
              : rewrittenCodeAttribute.stackMapTablePatch();
      SteelHookMethodEntryTransformedClass transformedClass =
          new SteelHookMethodEntryTransformedClass(
              classFile.thisClassName(), transformedClassBytes, transformedClassSha256);
      return new SteelHookMethodEntryRewriteResult(
          SteelHookMethodEntryRewriteStatus.TRANSFORMED,
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
          stackMapTablePatch.present(),
          stackMapTablePatch.rewriteSupported(),
          stackMapTablePatch.rewriteApplied(),
          stackMapTablePatch.rejected(),
          stackMapTablePatch.entryCountBefore(),
          stackMapTablePatch.entryCountAfter(),
          stackMapTablePatch.firstFrameOffsetDeltaBefore(),
          stackMapTablePatch.firstFrameOffsetDeltaAfter(),
          stackMapTablePatch.originalBodyLength(),
          stackMapTablePatch.transformedBodyLength(),
          constantPoolPatch,
          codePatch,
          transformedClass);
    } catch (TransformationException exception) {
      return failedResult(
          request,
          immutableOriginal,
          exception.getMessage(),
          codeAttribute != null && codeAttribute.stackMapTablePresent(),
          request != null && request.stackMapTableRewriteSupported(),
          codeAttribute != null && codeAttribute.stackMapTablePresent(),
          null);
    }
  }

  private MethodLayout findTargetMethod(
      SteelHookMethodEntryRewriteRequest request, List<MethodLayout> methods) {
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

  private String validateRequest(SteelHookMethodEntryRewriteRequest request) {
    if (request.insertionOffset() != 0) {
      return "Method-entry rewriting supports insertion offset 0 only.";
    }
    if (!"invokestatic".equals(request.opcodeMnemonic())) {
      return "Method-entry rewriting supports invokestatic only.";
    }
    if (!"b8".equals(request.opcodeHex())) {
      return "Method-entry rewriting supports opcode hex b8 only.";
    }
    if (request.instructionLength() != 3) {
      return "Method-entry rewriting supports instruction length 3 only.";
    }
    if (request.publicApiExposed()) {
      return "Method-entry rewriting must not expose public API.";
    }
    if (request.javaModExecutionSandboxed()) {
      return "Method-entry rewriting must not claim Java mod execution sandboxing.";
    }
    if (isBlank(request.targetOwnerInternalName())
        || isBlank(request.targetMethodName())
        || isBlank(request.targetDescriptor())
        || isBlank(request.dispatcherOwnerInternalName())
        || isBlank(request.dispatcherMethodName())
        || isBlank(request.dispatcherDescriptor())) {
      return "Method-entry rewrite request target and dispatcher fields must be nonblank.";
    }
    return null;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private ClassFileLayout parseClassFile(
      SteelHookMethodEntryRewriteRequest request, byte[] classBytes)
      throws TransformationException {
    ClassReader reader = new ClassReader(classBytes, request);
    if (reader.readInt() != CLASS_MAGIC) {
      throw new TransformationException("Invalid class file magic for " + request.scope() + ".");
    }
    reader.skip(2);
    reader.skip(2);
    int constantPoolCount = reader.readUnsignedShort();
    int constantPoolStart = reader.position();
    ConstantPool constantPool = readConstantPool(reader, constantPoolCount, request);
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
      methods.add(readMethod(reader, constantPool, classBytes, request));
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

  private ConstantPool readConstantPool(
      ClassReader reader, int count, SteelHookMethodEntryRewriteRequest request)
      throws TransformationException {
    int[] tags = new int[count];
    Object[] values = new Object[count];
    for (int index = 1; index < count; index++) {
      int tag = reader.readUnsignedByte();
      tags[index] = tag;
      switch (tag) {
        case CONSTANT_UTF8 -> values[index] = reader.readUtf();
        case CONSTANT_INTEGER -> reader.skip(4);
        case CONSTANT_FLOAT -> reader.skip(4);
        case CONSTANT_LONG -> {
          reader.skip(8);
          index++;
        }
        case CONSTANT_DOUBLE -> {
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
                "Unsupported constant pool tag " + tag + " for " + request.scope() + ".");
      }
    }
    return new ConstantPool(tags, values, request);
  }

  private MethodLayout readMethod(
      ClassReader reader,
      ConstantPool constantPool,
      byte[] classBytes,
      SteelHookMethodEntryRewriteRequest request)
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
        throw new TransformationException("Malformed class file for " + request.scope() + ".");
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
                request);
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
      SteelHookMethodEntryRewriteRequest request)
      throws TransformationException {
    ClassReader reader =
        new ClassReader(classBytes, bodyOffset, bodyOffset + attributeLength, request);
    if (attributeLength < 12) {
      throw new TransformationException("Malformed Code attribute for " + request.scope() + ".");
    }
    int maxStack = reader.readUnsignedShort();
    int maxLocals = reader.readUnsignedShort();
    int codeLength = reader.readInt();
    if (codeLength < 0 || reader.position() + codeLength > bodyOffset + attributeLength) {
      throw new TransformationException(
          "Malformed Code attribute bytecode length for " + request.scope() + ".");
    }
    byte[] code = Arrays.copyOfRange(classBytes, reader.position(), reader.position() + codeLength);
    reader.skip(codeLength);
    int exceptionTableCount = reader.readUnsignedShort();
    List<ExceptionTableEntry> exceptionTable = new ArrayList<>(exceptionTableCount);
    for (int index = 0; index < exceptionTableCount; index++) {
      exceptionTable.add(
          new ExceptionTableEntry(
              reader.readUnsignedShort(),
              reader.readUnsignedShort(),
              reader.readUnsignedShort(),
              reader.readUnsignedShort()));
    }
    int nestedAttributeCount = reader.readUnsignedShort();
    boolean stackMapTablePresent = false;
    int stackMapTableCount = 0;
    List<NestedAttributeLayout> nestedAttributes = new ArrayList<>(nestedAttributeCount);
    for (int index = 0; index < nestedAttributeCount; index++) {
      int nestedNameIndex = reader.readUnsignedShort();
      String nestedName = constantPool.utf8(nestedNameIndex);
      int nestedLength = reader.readInt();
      int bodyStart = reader.position();
      if (bodyStart + nestedLength > bodyOffset + attributeLength) {
        throw new TransformationException(
            "Malformed Code attribute bounds for " + request.scope() + ".");
      }
      if ("StackMapTable".equals(nestedName)) {
        stackMapTablePresent = true;
        stackMapTableCount++;
      }
      nestedAttributes.add(
          new NestedAttributeLayout(
              nestedNameIndex,
              nestedName,
              Arrays.copyOfRange(classBytes, bodyStart, bodyStart + nestedLength)));
      reader.skip(nestedLength);
    }
    if (reader.position() != bodyOffset + attributeLength) {
      throw new TransformationException(
          "Malformed Code attribute bounds for " + request.scope() + ".");
    }
    return new CodeAttributeLayout(
        attributeLengthOffset,
        bodyOffset + attributeLength,
        maxStack,
        maxLocals,
        code,
        exceptionTableCount,
        exceptionTable,
        nestedAttributes,
        stackMapTablePresent,
        stackMapTableCount);
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

  private SteelHookMethodEntryConstantPoolPatch createConstantPoolPatch(
      int constantPoolCountBefore) {
    int dispatcherOwnerUtf8Index = constantPoolCountBefore;
    int dispatcherClassIndex = constantPoolCountBefore + 1;
    int dispatcherMethodNameUtf8Index = constantPoolCountBefore + 2;
    int dispatcherDescriptorUtf8Index = constantPoolCountBefore + 3;
    int dispatcherNameAndTypeIndex = constantPoolCountBefore + 4;
    int dispatcherMethodrefIndex = constantPoolCountBefore + 5;
    return new SteelHookMethodEntryConstantPoolPatch(
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
      SteelHookMethodEntryRewriteRequest request, SteelHookMethodEntryConstantPoolPatch patch)
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

  private RewrittenCodeAttribute rewriteCodeAttribute(
      SteelHookMethodEntryRewriteRequest request,
      CodeAttributeLayout codeAttribute,
      byte[] transformedCode)
      throws TransformationException {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      SteelHookStackMapTablePatch stackMapTablePatch = null;
      try (DataOutputStream output = new DataOutputStream(bytes)) {
        output.writeShort(codeAttribute.maxStack());
        output.writeShort(codeAttribute.maxLocals());
        output.writeInt(transformedCode.length);
        output.write(transformedCode);
        output.writeShort(codeAttribute.exceptionTableCount());
        for (ExceptionTableEntry entry : codeAttribute.exceptionTable()) {
          output.writeShort(entry.startPc() + request.instructionLength());
          output.writeShort(entry.endPc() + request.instructionLength());
          output.writeShort(entry.handlerPc() + request.instructionLength());
          output.writeShort(entry.catchType());
        }
        output.writeShort(codeAttribute.nestedAttributes().size());
        for (NestedAttributeLayout nestedAttribute : codeAttribute.nestedAttributes()) {
          output.writeShort(nestedAttribute.nameIndex());
          byte[] nestedBody = nestedAttribute.body();
          if ("StackMapTable".equals(nestedAttribute.name())) {
            if (codeAttribute.stackMapTableCount() > 1) {
              throw new TransformationException(
                  "Multiple StackMapTable attributes are not supported for "
                      + request.scope()
                      + ".");
            }
            SteelHookStackMapTableRewriteResult rewriteResult =
                stackMapTableRewriter.rewrite(
                    nestedAttribute.body(), request.instructionLength(), request.scope());
            if (rewriteResult.status() == SteelHookStackMapTableRewriteStatus.REJECTED) {
              throw new TransformationException(rewriteResult.failureReason());
            }
            nestedBody = rewriteResult.transformedBody();
            stackMapTablePatch = rewriteResult.patch(request.stackMapTableRewriteSupported());
          }
          output.writeInt(nestedBody.length);
          output.write(nestedBody);
        }
      }
      return new RewrittenCodeAttribute(bytes.toByteArray(), stackMapTablePatch);
    } catch (IOException exception) {
      throw new TransformationException(
          "Failed to rewrite Code attribute for " + request.scope() + ".");
    }
  }

  private byte[] invokestaticInstruction(int methodrefIndex) {
    return new byte[] {(byte) 0xb8, (byte) (methodrefIndex >>> 8), (byte) methodrefIndex};
  }

  private byte[] prepend(byte[] prefix, byte[] suffix) {
    byte[] combined = new byte[prefix.length + suffix.length];
    System.arraycopy(prefix, 0, combined, 0, prefix.length);
    System.arraycopy(suffix, 0, combined, prefix.length, suffix.length);
    return combined;
  }

  private byte[] rewriteClassBytes(
      SteelHookMethodEntryRewriteRequest request,
      byte[] originalClassBytes,
      ClassFileLayout classFile,
      byte[] appendedConstantPoolEntries,
      Integer constantPoolCountAfter,
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

  private SteelHookMethodEntryRewriteResult failedResult(
      SteelHookMethodEntryRewriteRequest request,
      byte[] originalClassBytes,
      String failureReason,
      boolean stackMapTablePresent,
      boolean stackMapTableRewriteSupported,
      boolean stackMapTableRejected,
      SteelHookStackMapTablePatch stackMapTablePatch) {
    SteelHookStackMapTablePatch resolvedStackMapTablePatch =
        stackMapTablePatch == null
            ? new SteelHookStackMapTablePatch(
                stackMapTablePresent,
                stackMapTableRewriteSupported,
                false,
                stackMapTableRejected,
                null,
                null,
                null,
                null,
                null,
                null)
            : stackMapTablePatch;
    return new SteelHookMethodEntryRewriteResult(
        SteelHookMethodEntryRewriteStatus.REJECTED,
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
        resolvedStackMapTablePatch.present(),
        resolvedStackMapTablePatch.rewriteSupported(),
        resolvedStackMapTablePatch.rewriteApplied(),
        resolvedStackMapTablePatch.rejected(),
        resolvedStackMapTablePatch.entryCountBefore(),
        resolvedStackMapTablePatch.entryCountAfter(),
        resolvedStackMapTablePatch.firstFrameOffsetDeltaBefore(),
        resolvedStackMapTablePatch.firstFrameOffsetDeltaAfter(),
        resolvedStackMapTablePatch.originalBodyLength(),
        resolvedStackMapTablePatch.transformedBodyLength(),
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
          "SHA-256 is unavailable for method-entry rewriting.", exception);
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

  private record ClassFileLayout(
      int constantPoolCount,
      int constantPoolStart,
      int constantPoolEnd,
      String thisClassName,
      List<MethodLayout> methods) {}

  private record MethodLayout(
      String name, String descriptor, int accessFlags, CodeAttributeLayout codeAttribute) {}

  private record CodeAttributeLayout(
      int attributeLengthOffset,
      int bodyEnd,
      int maxStack,
      int maxLocals,
      byte[] code,
      int exceptionTableCount,
      List<ExceptionTableEntry> exceptionTable,
      List<NestedAttributeLayout> nestedAttributes,
      boolean stackMapTablePresent,
      int stackMapTableCount) {
    private CodeAttributeLayout {
      code = code.clone();
      exceptionTable = List.copyOf(exceptionTable);
      nestedAttributes = List.copyOf(nestedAttributes);
    }
  }

  private record ExceptionTableEntry(int startPc, int endPc, int handlerPc, int catchType) {}

  private record NestedAttributeLayout(int nameIndex, String name, byte[] body) {
    private NestedAttributeLayout {
      body = body.clone();
    }
  }

  private record RewrittenCodeAttribute(
      byte[] body, SteelHookStackMapTablePatch stackMapTablePatch) {
    private RewrittenCodeAttribute {
      body = body.clone();
    }
  }

  private static final class ConstantPool {
    private final int[] tags;
    private final Object[] values;
    private final SteelHookMethodEntryRewriteRequest request;

    private ConstantPool(int[] tags, Object[] values, SteelHookMethodEntryRewriteRequest request) {
      this.tags = tags;
      this.values = values;
      this.request = request;
    }

    private String utf8(int index) throws TransformationException {
      if (index <= 0 || index >= tags.length || tags[index] != CONSTANT_UTF8) {
        throw new TransformationException(
            "Invalid Utf8 constant pool index for " + request.scope() + ": " + index);
      }
      return (String) values[index];
    }

    private String className(int index) throws TransformationException {
      if (index <= 0 || index >= tags.length || tags[index] != CONSTANT_CLASS) {
        throw new TransformationException(
            "Invalid Class constant pool index for " + request.scope() + ": " + index);
      }
      return utf8((Integer) values[index]);
    }
  }

  private static final class ClassReader {
    private final byte[] bytes;
    private final int limit;
    private final SteelHookMethodEntryRewriteRequest request;
    private int position;

    private ClassReader(byte[] bytes, SteelHookMethodEntryRewriteRequest request) {
      this(bytes, 0, bytes.length, request);
    }

    private ClassReader(
        byte[] bytes, int position, int limit, SteelHookMethodEntryRewriteRequest request) {
      this.bytes = bytes;
      this.position = position;
      this.limit = limit;
      this.request = request;
    }

    private int position() {
      return position;
    }

    private void position(int newPosition) throws TransformationException {
      if (newPosition < 0 || newPosition > limit) {
        throw new TransformationException("Malformed class file for " + request.scope() + ".");
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
        throw new TransformationException(
            "Malformed Utf8 constant pool entry for " + request.scope() + ".");
      }
    }

    private void skip(int byteCount) throws TransformationException {
      require(byteCount);
      position += byteCount;
    }

    private void require(int byteCount) throws TransformationException {
      if (byteCount < 0 || position + byteCount > limit) {
        throw new TransformationException("Malformed class file for " + request.scope() + ".");
      }
    }
  }

  private static final class TransformationException extends Exception {
    private TransformationException(String message) {
      super(message);
    }
  }
}
