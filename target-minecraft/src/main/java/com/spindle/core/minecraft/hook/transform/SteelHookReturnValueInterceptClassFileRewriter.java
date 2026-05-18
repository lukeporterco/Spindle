package com.spindle.core.minecraft.hook.transform;

import com.spindle.core.minecraft.hook.steelhook.SteelHook04PrimitiveKind;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SteelHookReturnValueInterceptClassFileRewriter {
  private static final int ACC_SYNCHRONIZED = 0x0020;
  private static final int BIPUSH = 0x10;
  private static final int IRETURN = 0xac;
  private static final int LDC = 0x12;
  private static final int ARETURN = 0xb0;
  private static final int RETURN = 0xb1;
  private static final int TABLESWITCH = 0xaa;
  private static final int LOOKUPSWITCH = 0xab;

  public SteelHookReturnValueInterceptRewriteResult rewrite(
      SteelHookReturnValueInterceptRewriteRequest request, byte[] classBytes) {
    if (request == null) {
      return rejected(
          null,
          null,
          "Malformed intercept request: request is required.",
          false,
          false,
          false,
          false,
          false);
    }
    byte[] immutableOriginal = classBytes == null ? null : classBytes.clone();
    if (immutableOriginal == null || immutableOriginal.length == 0) {
      return rejected(
          request,
          immutableOriginal,
          "Malformed intercept request: class bytes are required.",
          false,
          false,
          false,
          false,
          false);
    }
    try {
      validateRequest(request);
      ClassFileLayout classFile = parseClassFile(request, immutableOriginal);
      if (!request.targetOwnerInternalName().equals(classFile.thisClassName())) {
        throw new TransformationException(
            "Wrong owner: expected "
                + request.targetOwnerInternalName()
                + " but found "
                + classFile.thisClassName()
                + ".");
      }
      MethodLayout method = locateMethod(request, classFile.methods());
      if ("<init>".equals(method.name())) {
        throw new TransformationException("Constructor targets are unsupported.");
      }
      if ("<clinit>".equals(method.name())) {
        throw new TransformationException("Class initializer targets are unsupported.");
      }
      if ((method.accessFlags() & ACC_SYNCHRONIZED) != 0) {
        return rejected(
            request,
            immutableOriginal,
            "Synchronized methods are unsupported.",
            false,
            false,
            true,
            false,
            false);
      }
      CodeAttributeLayout codeAttribute = method.codeAttribute();
      if (codeAttribute == null) {
        throw new TransformationException("Target method is missing a Code attribute.");
      }
      if (codeAttribute.exceptionTableCount() > 0) {
        return rejected(
            request,
            immutableOriginal,
            "Exception table entries are unsupported.",
            true,
            false,
            false,
            false,
            false);
      }
      if (codeAttribute.stackMapTablePresent()) {
        return rejected(
            request,
            immutableOriginal,
            "StackMapTable attributes are unsupported.",
            false,
            true,
            false,
            false,
            false);
      }

      DescriptorSupport descriptorSupport = descriptorSupport(request.targetDescriptor());
      if (descriptorSupport == DescriptorSupport.VOID) {
        throw new TransformationException("Void return descriptors are unsupported.");
      }
      if (!descriptorMatchesKind(descriptorSupport, request.interceptKind())) {
        throw new TransformationException(
            "Replacement kind does not match target descriptor "
                + request.targetDescriptor()
                + ".");
      }

      ScanResult scanResult =
          scanCode(codeAttribute.code(), descriptorSupport, classFile.constantPool());
      if (scanResult.branchingMethod()) {
        return rejected(
            request,
            immutableOriginal,
            "Branching methods are unsupported.",
            false,
            false,
            false,
            true,
            false);
      }
      if (scanResult.switchMethod()) {
        return rejected(
            request,
            immutableOriginal,
            "Switch methods are unsupported.",
            false,
            false,
            false,
            false,
            true);
      }
      if (scanResult.returnCount() != 1) {
        throw new TransformationException("Multiple return opcodes are unsupported.");
      }
      if (!scanResult.supportedMatch()) {
        throw new TransformationException("Missing supported producer immediately before return.");
      }

      String originalClassSha256 = sha256Hex(immutableOriginal);
      String originalCodeSha256 = sha256Hex(codeAttribute.code());
      if (request.mode() == SteelHookReturnValueInterceptMode.OBSERVE_ONLY) {
        return new SteelHookReturnValueInterceptRewriteResult(
            SteelHookReturnValueInterceptRewriteStatus.OBSERVED,
            null,
            request,
            originalClassSha256,
            null,
            originalCodeSha256,
            null,
            codeAttribute.code().length,
            codeAttribute.code().length,
            scanResult.returnOpcodeMnemonic(),
            scanResult.producerOpcodeMnemonic(),
            1,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            null,
            null);
      }

      validateReplacementRequest(request);
      byte[] transformedCode = Arrays.copyOf(codeAttribute.code(), codeAttribute.code().length);
      String replacementSummary;
      if (request.interceptKind() == SteelHookReturnValueInterceptKind.PRIMITIVE_INT) {
        transformedCode[scanResult.producerOperandOffset()] =
            request.replacementPrimitiveValue().byteValue();
        replacementSummary =
            "primitive replacement: "
                + scanResult.producerValueSummary()
                + " -> "
                + request.replacementPrimitiveValue();
      } else {
        int replacementIndex =
            classFile.constantPool().stringConstantPoolIndex(request.replacementReferenceValue());
        if (replacementIndex < 0 || replacementIndex > 0xff) {
          throw new TransformationException(
              "Replacement reference constant is unavailable in the fixture constant pool.");
        }
        transformedCode[scanResult.producerOperandOffset()] = (byte) replacementIndex;
        replacementSummary =
            "reference replacement: "
                + scanResult.producerValueSummary()
                + " -> "
                + request.replacementReferenceValue();
      }

      byte[] rewrittenCodeAttribute =
          rewriteCodeAttributeBody(codeAttribute, transformedCode, request.scope());
      byte[] transformedClassBytes =
          rewriteMethodCodeAttribute(
              immutableOriginal, method, rewrittenCodeAttribute, request.scope());

      return new SteelHookReturnValueInterceptRewriteResult(
          SteelHookReturnValueInterceptRewriteStatus.TRANSFORMED,
          null,
          request,
          originalClassSha256,
          sha256Hex(transformedClassBytes),
          originalCodeSha256,
          sha256Hex(transformedCode),
          codeAttribute.code().length,
          transformedCode.length,
          scanResult.returnOpcodeMnemonic(),
          scanResult.producerOpcodeMnemonic(),
          1,
          true,
          true,
          false,
          false,
          false,
          false,
          false,
          replacementSummary,
          transformedClassBytes);
    } catch (TransformationException exception) {
      return rejected(
          request, immutableOriginal, exception.getMessage(), false, false, false, false, false);
    }
  }

  private void validateRequest(SteelHookReturnValueInterceptRewriteRequest request)
      throws TransformationException {
    if (request.primitiveKind() != SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT) {
      throw new TransformationException(
          "Unsupported primitive kind: only RETURN_VALUE_INTERCEPT is allowed.");
    }
    if (request.mode() == null) {
      throw new TransformationException("Malformed intercept request: mode is required.");
    }
    if (request.interceptKind() == null) {
      throw new TransformationException("Malformed intercept request: intercept kind is required.");
    }
    requireText(request.targetOwnerInternalName(), "Target owner");
    requireText(request.targetMethodName(), "Target method name");
    requireText(request.targetDescriptor(), "Target descriptor");
  }

  private void validateReplacementRequest(SteelHookReturnValueInterceptRewriteRequest request)
      throws TransformationException {
    if (request.interceptKind() == SteelHookReturnValueInterceptKind.PRIMITIVE_INT) {
      if (request.replacementPrimitiveValue() == null) {
        throw new TransformationException(
            "Malformed intercept request: primitive replacement value is required.");
      }
    } else if (request.replacementReferenceValue() == null
        || request.replacementReferenceValue().isBlank()) {
      throw new TransformationException(
          "Malformed intercept request: reference replacement value is required.");
    }
  }

  private void requireText(String value, String label) throws TransformationException {
    if (value == null || value.isBlank()) {
      throw new TransformationException("Malformed intercept request: " + label + " is required.");
    }
  }

  private boolean descriptorMatchesKind(
      DescriptorSupport descriptorSupport, SteelHookReturnValueInterceptKind interceptKind) {
    return switch (descriptorSupport) {
      case PRIMITIVE_INT -> interceptKind == SteelHookReturnValueInterceptKind.PRIMITIVE_INT;
      case REFERENCE_STRING -> interceptKind == SteelHookReturnValueInterceptKind.REFERENCE_STRING;
      case VOID -> false;
    };
  }

  private DescriptorSupport descriptorSupport(String descriptor) throws TransformationException {
    if (descriptor == null || descriptor.isBlank()) {
      throw new TransformationException("Target descriptor is required.");
    }
    int close = descriptor.lastIndexOf(')');
    if (!descriptor.startsWith("(") || close < 0 || close == descriptor.length() - 1) {
      throw new TransformationException("Malformed method descriptor " + descriptor + ".");
    }
    String returnDescriptor = descriptor.substring(close + 1);
    return switch (returnDescriptor) {
      case "I" -> DescriptorSupport.PRIMITIVE_INT;
      case "V" -> DescriptorSupport.VOID;
      case "Ljava/lang/String;" -> DescriptorSupport.REFERENCE_STRING;
      default ->
          throw new TransformationException(
              "Unsupported method return descriptor " + descriptor + ".");
    };
  }

  private MethodLayout locateMethod(
      SteelHookReturnValueInterceptRewriteRequest request, List<MethodLayout> methods)
      throws TransformationException {
    MethodLayout exact = null;
    boolean namePresent = false;
    boolean descriptorPresent = false;
    for (MethodLayout method : methods) {
      if (request.targetMethodName().equals(method.name())) {
        namePresent = true;
      }
      if (request.targetDescriptor().equals(method.descriptor())) {
        descriptorPresent = true;
      }
      if (request.targetMethodName().equals(method.name())
          && request.targetDescriptor().equals(method.descriptor())) {
        exact = method;
      }
    }
    if (exact != null) {
      return exact;
    }
    if (!namePresent) {
      throw new TransformationException("Wrong method name: " + request.targetMethodName() + ".");
    }
    if (!descriptorPresent) {
      throw new TransformationException("Wrong descriptor: " + request.targetDescriptor() + ".");
    }
    throw new TransformationException("Target method could not be resolved.");
  }

  private ScanResult scanCode(
      byte[] code, DescriptorSupport descriptorSupport, ConstantPool constantPool)
      throws TransformationException {
    int offset = 0;
    int returnCount = 0;
    int previousOffset = -1;
    int previousOpcode = -1;
    int matchedReturnOffset = -1;
    int matchedProducerOperandOffset = -1;
    String matchedProducerMnemonic = null;
    String producerValueSummary = null;
    while (offset < code.length) {
      int opcode = code[offset] & 0xff;
      if (isBranchOpcode(opcode)) {
        return new ScanResult(true, false, returnCount, false, null, null, -1, null);
      }
      if (opcode == TABLESWITCH || opcode == LOOKUPSWITCH) {
        return new ScanResult(false, true, returnCount, false, null, null, -1, null);
      }
      if (opcode == IRETURN || opcode == ARETURN || opcode == RETURN) {
        returnCount++;
        matchedReturnOffset = offset;
        if (!supportedReturnOpcode(opcode, descriptorSupport)) {
          throw new TransformationException(
              "Return opcode does not match target descriptor " + descriptorSupport + ".");
        }
        if (previousOffset >= 0
            && previousOffset + instructionLength(previousOpcode, code, previousOffset) == offset) {
          if (descriptorSupport == DescriptorSupport.PRIMITIVE_INT && previousOpcode == BIPUSH) {
            matchedProducerOperandOffset = previousOffset + 1;
            matchedProducerMnemonic = "bipush";
            producerValueSummary = Integer.toString((byte) code[matchedProducerOperandOffset]);
          } else if (descriptorSupport == DescriptorSupport.REFERENCE_STRING
              && previousOpcode == LDC) {
            matchedProducerOperandOffset = previousOffset + 1;
            matchedProducerMnemonic = "ldc";
            producerValueSummary =
                "\"" + constantPool.stringValue(code[matchedProducerOperandOffset] & 0xff) + "\"";
          }
        }
      }
      int length = instructionLength(opcode, code, offset);
      previousOffset = offset;
      previousOpcode = opcode;
      offset += length;
    }
    String returnMnemonic =
        matchedReturnOffset < 0 ? null : opcodeMnemonic(code[matchedReturnOffset] & 0xff);
    boolean supportedMatch = matchedProducerOperandOffset >= 0 && returnCount == 1;
    return new ScanResult(
        false,
        false,
        returnCount,
        supportedMatch,
        returnMnemonic,
        matchedProducerMnemonic,
        matchedProducerOperandOffset,
        producerValueSummary);
  }

  private boolean supportedReturnOpcode(int opcode, DescriptorSupport descriptorSupport) {
    return switch (descriptorSupport) {
      case PRIMITIVE_INT -> opcode == IRETURN;
      case REFERENCE_STRING -> opcode == ARETURN;
      case VOID -> opcode == RETURN;
    };
  }

  private boolean isBranchOpcode(int opcode) {
    return (opcode >= 0x99 && opcode <= 0xa8) || opcode == 0xc6 || opcode == 0xc7;
  }

  private int instructionLength(int opcode, byte[] code, int offset)
      throws TransformationException {
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
          0x2a,
          0x57,
          IRETURN,
          ARETURN,
          RETURN ->
          1;
      case BIPUSH, LDC -> 2;
      case 0xb7 -> 3;
      default ->
          throw new TransformationException(
              "Unsupported opcode 0x" + Integer.toHexString(opcode) + " in bounded fixture.");
    };
  }

  private String opcodeMnemonic(int opcode) {
    return switch (opcode) {
      case BIPUSH -> "bipush";
      case LDC -> "ldc";
      case IRETURN -> "ireturn";
      case ARETURN -> "areturn";
      case RETURN -> "return";
      default -> "opcode-0x" + Integer.toHexString(opcode);
    };
  }

  private byte[] rewriteCodeAttributeBody(
      CodeAttributeLayout codeAttribute, byte[] transformedCode, String scope)
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
        for (NestedAttribute nestedAttribute : codeAttribute.nestedAttributes()) {
          output.writeShort(nestedAttribute.nameIndex());
          output.writeInt(nestedAttribute.body().length);
          output.write(nestedAttribute.body());
        }
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new TransformationException("Failed to rewrite Code attribute for " + scope + ".");
    }
  }

  private byte[] rewriteMethodCodeAttribute(
      byte[] classBytes, MethodLayout method, byte[] rewrittenCodeAttributeBody, String scope)
      throws TransformationException {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream(classBytes.length + 16);
      try (DataOutputStream output = new DataOutputStream(bytes)) {
        output.write(classBytes, 0, method.codeAttribute().attributeBodyOffset());
        output.write(rewrittenCodeAttributeBody);
        int originalBodyEnd =
            method.codeAttribute().attributeBodyOffset() + method.codeAttribute().attributeLength();
        output.write(classBytes, originalBodyEnd, classBytes.length - originalBodyEnd);
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new TransformationException("Failed to rewrite class bytes for " + scope + ".");
    }
  }

  private ClassFileLayout parseClassFile(
      SteelHookReturnValueInterceptRewriteRequest request, byte[] classBytes)
      throws TransformationException {
    ClassReader reader = new ClassReader(classBytes, request.scope());
    if (reader.readInt() != 0xCAFEBABE) {
      throw new TransformationException("Malformed class file header.");
    }
    reader.readUnsignedShort();
    reader.readUnsignedShort();
    ConstantPool constantPool = ConstantPool.read(reader);
    int accessFlags = reader.readUnsignedShort();
    int thisClassIndex = reader.readUnsignedShort();
    int superClassIndex = reader.readUnsignedShort();
    String thisClassName = constantPool.className(thisClassIndex);
    int interfacesCount = reader.readUnsignedShort();
    for (int index = 0; index < interfacesCount; index++) {
      reader.readUnsignedShort();
    }
    int fieldsCount = reader.readUnsignedShort();
    for (int index = 0; index < fieldsCount; index++) {
      skipMember(reader);
    }
    int methodsCount = reader.readUnsignedShort();
    List<MethodLayout> methods = new ArrayList<>(methodsCount);
    for (int index = 0; index < methodsCount; index++) {
      methods.add(readMethod(reader, constantPool, classBytes, request.scope()));
    }
    int classAttributesCount = reader.readUnsignedShort();
    for (int index = 0; index < classAttributesCount; index++) {
      skipAttribute(reader);
    }
    if (reader.position() != classBytes.length) {
      throw new TransformationException("Malformed class file length.");
    }
    return new ClassFileLayout(accessFlags, thisClassName, superClassIndex, constantPool, methods);
  }

  private void skipMember(ClassReader reader) throws TransformationException {
    reader.readUnsignedShort();
    reader.readUnsignedShort();
    reader.readUnsignedShort();
    int attributesCount = reader.readUnsignedShort();
    for (int index = 0; index < attributesCount; index++) {
      skipAttribute(reader);
    }
  }

  private void skipAttribute(ClassReader reader) throws TransformationException {
    reader.readUnsignedShort();
    int length = reader.readInt();
    reader.skip(length);
  }

  private MethodLayout readMethod(
      ClassReader reader, ConstantPool constantPool, byte[] classBytes, String scope)
      throws TransformationException {
    int accessFlags = reader.readUnsignedShort();
    String name = constantPool.utf8(reader.readUnsignedShort());
    String descriptor = constantPool.utf8(reader.readUnsignedShort());
    int attributesCount = reader.readUnsignedShort();
    CodeAttributeLayout codeAttribute = null;
    for (int index = 0; index < attributesCount; index++) {
      int nameIndex = reader.readUnsignedShort();
      String attributeName = constantPool.utf8(nameIndex);
      int attributeLength = reader.readInt();
      int bodyOffset = reader.position();
      if ("Code".equals(attributeName)) {
        if (codeAttribute != null) {
          throw new TransformationException("Target method has multiple Code attributes.");
        }
        codeAttribute =
            parseCodeAttribute(
                classBytes, constantPool, nameIndex, bodyOffset, attributeLength, scope);
      }
      reader.skip(attributeLength);
    }
    return new MethodLayout(name, descriptor, accessFlags, codeAttribute);
  }

  private CodeAttributeLayout parseCodeAttribute(
      byte[] classBytes,
      ConstantPool constantPool,
      int attributeNameIndex,
      int bodyOffset,
      int attributeLength,
      String scope)
      throws TransformationException {
    ClassReader reader =
        new ClassReader(classBytes, bodyOffset, bodyOffset + attributeLength, scope);
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
    reader.skip(exceptionTableCount * 8);
    int nestedCount = reader.readUnsignedShort();
    boolean stackMapTablePresent = false;
    List<NestedAttribute> nestedAttributes = new ArrayList<>(nestedCount);
    for (int index = 0; index < nestedCount; index++) {
      int nestedNameIndex = reader.readUnsignedShort();
      String nestedName = constantPool.utf8(nestedNameIndex);
      int nestedLength = reader.readInt();
      int nestedBodyStart = reader.position();
      byte[] body = Arrays.copyOfRange(classBytes, nestedBodyStart, nestedBodyStart + nestedLength);
      if ("StackMapTable".equals(nestedName)) {
        stackMapTablePresent = true;
      }
      nestedAttributes.add(new NestedAttribute(nestedNameIndex, body));
      reader.skip(nestedLength);
    }
    if (reader.position() != bodyOffset + attributeLength) {
      throw new TransformationException("Malformed Code attribute bounds for " + scope + ".");
    }
    return new CodeAttributeLayout(
        attributeNameIndex,
        bodyOffset,
        attributeLength,
        maxStack,
        maxLocals,
        code,
        exceptionTableCount,
        stackMapTablePresent,
        nestedAttributes);
  }

  private SteelHookReturnValueInterceptRewriteResult rejected(
      SteelHookReturnValueInterceptRewriteRequest request,
      byte[] originalClassBytes,
      String failureReason,
      boolean exceptionTablePresent,
      boolean stackMapTablePresent,
      boolean synchronizedMethod,
      boolean branchingMethod,
      boolean switchMethod) {
    return new SteelHookReturnValueInterceptRewriteResult(
        SteelHookReturnValueInterceptRewriteStatus.REJECTED,
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
        0,
        false,
        false,
        exceptionTablePresent,
        stackMapTablePresent,
        synchronizedMethod,
        branchingMethod,
        switchMethod,
        null,
        null);
  }

  private String sha256Hex(byte[] bytes) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(bytes);
      StringBuilder builder = new StringBuilder(hash.length * 2);
      for (byte value : hash) {
        builder.append(Character.forDigit((value >>> 4) & 0xf, 16));
        builder.append(Character.forDigit(value & 0xf, 16));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable for Target-33 rewriting.", exception);
    }
  }

  private enum DescriptorSupport {
    PRIMITIVE_INT,
    REFERENCE_STRING,
    VOID
  }

  private record ScanResult(
      boolean branchingMethod,
      boolean switchMethod,
      int returnCount,
      boolean supportedMatch,
      String returnOpcodeMnemonic,
      String producerOpcodeMnemonic,
      int producerOperandOffset,
      String producerValueSummary) {}

  private record ClassFileLayout(
      int accessFlags,
      String thisClassName,
      int superClassIndex,
      ConstantPool constantPool,
      List<MethodLayout> methods) {}

  private record MethodLayout(
      String name, String descriptor, int accessFlags, CodeAttributeLayout codeAttribute) {}

  private record CodeAttributeLayout(
      int attributeNameIndex,
      int attributeBodyOffset,
      int attributeLength,
      int maxStack,
      int maxLocals,
      byte[] code,
      int exceptionTableCount,
      boolean stackMapTablePresent,
      List<NestedAttribute> nestedAttributes) {
    private CodeAttributeLayout {
      code = code.clone();
      nestedAttributes = List.copyOf(nestedAttributes);
    }
  }

  private record NestedAttribute(int nameIndex, byte[] body) {
    private NestedAttribute {
      body = body.clone();
    }
  }

  private static final class ConstantPool {
    private final CpEntry[] entries;

    private ConstantPool(CpEntry[] entries) {
      this.entries = entries;
    }

    private static ConstantPool read(ClassReader reader) throws TransformationException {
      int constantPoolCount = reader.readUnsignedShort();
      CpEntry[] entries = new CpEntry[constantPoolCount];
      for (int index = 1; index < constantPoolCount; index++) {
        int tag = reader.readUnsignedByte();
        switch (tag) {
          case 1 -> entries[index] = new Utf8Entry(reader.readUtf8());
          case 7 -> entries[index] = new ClassEntry(reader.readUnsignedShort());
          case 8 -> entries[index] = new StringEntry(reader.readUnsignedShort());
          case 3, 4 -> {
            reader.readInt();
            entries[index] = new CpEntry(tag);
          }
          case 5, 6 -> {
            reader.readInt();
            reader.readInt();
            entries[index] = new CpEntry(tag);
            index++;
          }
          case 9, 10, 11, 12, 18 -> {
            reader.readUnsignedShort();
            reader.readUnsignedShort();
            entries[index] = new CpEntry(tag);
          }
          case 15 -> {
            reader.readUnsignedByte();
            reader.readUnsignedShort();
            entries[index] = new CpEntry(tag);
          }
          case 16, 19, 20 -> {
            reader.readUnsignedShort();
            entries[index] = new CpEntry(tag);
          }
          default ->
              throw new TransformationException("Unsupported constant pool tag " + tag + ".");
        }
      }
      return new ConstantPool(entries);
    }

    private String utf8(int index) throws TransformationException {
      if (index <= 0 || index >= entries.length || !(entries[index] instanceof Utf8Entry entry)) {
        throw new TransformationException("Malformed constant pool Utf8 index " + index + ".");
      }
      return entry.value();
    }

    private String className(int index) throws TransformationException {
      if (index <= 0 || index >= entries.length || !(entries[index] instanceof ClassEntry entry)) {
        throw new TransformationException("Malformed class constant pool index " + index + ".");
      }
      return utf8(entry.nameIndex());
    }

    private int stringConstantPoolIndex(String value) throws TransformationException {
      for (int index = 1; index < entries.length; index++) {
        if (entries[index] instanceof StringEntry entry
            && value.equals(utf8(entry.stringIndex()))) {
          return index;
        }
      }
      return -1;
    }

    private String stringValue(int index) throws TransformationException {
      if (index <= 0 || index >= entries.length || !(entries[index] instanceof StringEntry entry)) {
        throw new TransformationException("Malformed string constant pool index " + index + ".");
      }
      return utf8(entry.stringIndex());
    }
  }

  private static class CpEntry {
    private final int tag;

    private CpEntry(int tag) {
      this.tag = tag;
    }

    @SuppressWarnings("unused")
    int tag() {
      return tag;
    }
  }

  private static final class Utf8Entry extends CpEntry {
    private final String value;

    private Utf8Entry(String value) {
      super(1);
      this.value = value;
    }

    private String value() {
      return value;
    }
  }

  private static final class ClassEntry extends CpEntry {
    private final int nameIndex;

    private ClassEntry(int nameIndex) {
      super(7);
      this.nameIndex = nameIndex;
    }

    private int nameIndex() {
      return nameIndex;
    }
  }

  private static final class StringEntry extends CpEntry {
    private final int stringIndex;

    private StringEntry(int stringIndex) {
      super(8);
      this.stringIndex = stringIndex;
    }

    private int stringIndex() {
      return stringIndex;
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

    private ClassReader(byte[] bytes, int start, int limit, String scope) {
      this.bytes = bytes;
      this.position = start;
      this.limit = limit;
      this.scope = scope;
    }

    private int position() {
      return position;
    }

    private int readUnsignedByte() throws TransformationException {
      ensureRemaining(1);
      return bytes[position++] & 0xff;
    }

    private int readUnsignedShort() throws TransformationException {
      ensureRemaining(2);
      int value = ((bytes[position] & 0xff) << 8) | (bytes[position + 1] & 0xff);
      position += 2;
      return value;
    }

    private int readInt() throws TransformationException {
      ensureRemaining(4);
      int value =
          ((bytes[position] & 0xff) << 24)
              | ((bytes[position + 1] & 0xff) << 16)
              | ((bytes[position + 2] & 0xff) << 8)
              | (bytes[position + 3] & 0xff);
      position += 4;
      return value;
    }

    private String readUtf8() throws TransformationException {
      int length = readUnsignedShort();
      ensureRemaining(length);
      String value = new String(bytes, position, length, StandardCharsets.UTF_8);
      position += length;
      return value;
    }

    private void skip(int length) throws TransformationException {
      ensureRemaining(length);
      position += length;
    }

    private void ensureRemaining(int length) throws TransformationException {
      if (length < 0 || position + length > limit) {
        throw new TransformationException("Malformed class file bounds for " + scope + ".");
      }
    }
  }

  private static final class TransformationException extends Exception {
    private TransformationException(String message) {
      super(message);
    }
  }
}
