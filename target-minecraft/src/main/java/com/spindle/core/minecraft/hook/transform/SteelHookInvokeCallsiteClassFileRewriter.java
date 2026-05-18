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

public final class SteelHookInvokeCallsiteClassFileRewriter {
  private static final int ACC_SYNCHRONIZED = 0x0020;
  private static final int TABLESWITCH = 0xaa;
  private static final int LOOKUPSWITCH = 0xab;
  private static final int WIDE = 0xc4;

  public SteelHookInvokeCallsiteRewriteResult rewrite(
      SteelHookInvokeCallsiteRewriteRequest request, byte[] classBytes) {
    if (request == null) {
      return rejected(
          null,
          null,
          "Malformed invoke rewrite request: request is required.",
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
          "Malformed invoke rewrite request: class bytes are required.",
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

      ConstantPool constantPool = classFile.constantPool();
      ScanResult scanResult = scanCode(codeAttribute.code(), constantPool);
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
      if (scanResult.constructorInvocation()) {
        throw new TransformationException("Constructor invocation targets are unsupported.");
      }
      if (scanResult.specialInvocation()) {
        throw new TransformationException("Special invocation targets are unsupported.");
      }

      CallsiteMatch match = matchCallsite(request, scanResult);
      if (!match.exactMatches().isEmpty() && match.exactMatches().size() > 1) {
        throw new TransformationException("Ambiguous multiple matching callsites are unsupported.");
      }
      if (match.specificMismatchReason() != null) {
        throw new TransformationException(match.specificMismatchReason());
      }
      if (match.exactMatches().isEmpty()) {
        throw new TransformationException("No matching callsite was found.");
      }

      InvokeInstruction instruction = match.exactMatches().getFirst();
      int replacementMethodrefIndex =
          constantPool.methodrefIndex(
              request.replacementInvokeOwnerInternalName(),
              request.replacementInvokeName(),
              request.replacementInvokeDescriptor());
      if (replacementMethodrefIndex < 0 || replacementMethodrefIndex > 0xffff) {
        throw new TransformationException(
            "Replacement invoke target is unavailable in the fixture constant pool.");
      }

      byte[] transformedCode = Arrays.copyOf(codeAttribute.code(), codeAttribute.code().length);
      transformedCode[instruction.operandOffset()] = hi(replacementMethodrefIndex);
      transformedCode[instruction.operandOffset() + 1] = lo(replacementMethodrefIndex);
      byte[] rewrittenCodeAttribute =
          rewriteCodeAttributeBody(codeAttribute, transformedCode, request.scope());
      byte[] transformedClassBytes =
          rewriteMethodCodeAttribute(
              immutableOriginal, method, rewrittenCodeAttribute, request.scope());

      return new SteelHookInvokeCallsiteRewriteResult(
          SteelHookInvokeCallsiteRewriteStatus.TRANSFORMED,
          null,
          request,
          sha256Hex(immutableOriginal),
          sha256Hex(transformedClassBytes),
          sha256Hex(codeAttribute.code()),
          sha256Hex(transformedCode),
          codeAttribute.code().length,
          transformedCode.length,
          instruction.opcode().id(),
          match.exactMatches().size(),
          true,
          true,
          false,
          false,
          false,
          false,
          false,
          "patched methodref operand: "
              + request.expectedInvokeName()
              + request.expectedInvokeDescriptor()
              + " -> "
              + request.replacementInvokeName()
              + request.replacementInvokeDescriptor(),
          transformedClassBytes);
    } catch (TransformationException exception) {
      return rejected(
          request, immutableOriginal, exception.getMessage(), false, false, false, false, false);
    }
  }

  private void validateRequest(SteelHookInvokeCallsiteRewriteRequest request)
      throws TransformationException {
    if (request.primitiveKind() != SteelHook04PrimitiveKind.INVOKE_REDIRECT
        && request.primitiveKind() != SteelHook04PrimitiveKind.INVOKE_WRAP) {
      throw new TransformationException(
          "Unsupported primitive kind: only INVOKE_REDIRECT and INVOKE_WRAP are allowed.");
    }
    if (request.rewriteMode() == null) {
      throw new TransformationException(
          "Malformed invoke rewrite request: rewrite mode is required.");
    }
    if (request.primitiveKind() == SteelHook04PrimitiveKind.INVOKE_REDIRECT
        && request.rewriteMode() != SteelHookInvokeCallsiteRewriteMode.REDIRECT) {
      throw new TransformationException(
          "Malformed invoke rewrite request: INVOKE_REDIRECT requires REDIRECT mode.");
    }
    if (request.primitiveKind() == SteelHook04PrimitiveKind.INVOKE_WRAP
        && request.rewriteMode() != SteelHookInvokeCallsiteRewriteMode.WRAP) {
      throw new TransformationException(
          "Malformed invoke rewrite request: INVOKE_WRAP requires WRAP mode.");
    }
    requireText(request.targetOwnerInternalName(), "Target owner");
    requireText(request.targetMethodName(), "Target method name");
    requireText(request.targetDescriptor(), "Target descriptor");
    requireText(request.expectedInvokeOwnerInternalName(), "Expected invoke owner");
    requireText(request.expectedInvokeName(), "Expected invoke name");
    requireText(request.expectedInvokeDescriptor(), "Expected invoke descriptor");
    requireText(request.replacementInvokeOwnerInternalName(), "Replacement invoke owner");
    requireText(request.replacementInvokeName(), "Replacement invoke name");
    requireText(request.replacementInvokeDescriptor(), "Replacement invoke descriptor");
    if (request.expectedInvokeOpcode() == null) {
      throw new TransformationException(
          "Malformed invoke rewrite request: expected invoke opcode is required.");
    }
    if (request.replacementInvokeOpcode() == null) {
      throw new TransformationException(
          "Malformed invoke rewrite request: replacement invoke opcode is required.");
    }
    if (request.expectedInvokeOpcode() != SteelHookInvokeOpcode.INVOKESTATIC) {
      throw new TransformationException(
          "Wrong invoke opcode: Target-34 supports INVOKESTATIC only.");
    }
    if (request.replacementInvokeOpcode() != request.expectedInvokeOpcode()) {
      throw new TransformationException(
          "Replacement invoke opcode must match the expected opcode.");
    }
    if (!request.expectedInvokeDescriptor().equals(request.replacementInvokeDescriptor())) {
      throw new TransformationException(
          "Replacement invoke descriptor must match the expected invoke descriptor.");
    }
  }

  private void requireText(String value, String label) throws TransformationException {
    if (value == null || value.isBlank()) {
      throw new TransformationException(
          "Malformed invoke rewrite request: " + label + " is required.");
    }
  }

  private MethodLayout locateMethod(
      SteelHookInvokeCallsiteRewriteRequest request, List<MethodLayout> methods)
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

  private CallsiteMatch matchCallsite(
      SteelHookInvokeCallsiteRewriteRequest request, ScanResult scanResult)
      throws TransformationException {
    List<InvokeInstruction> exactMatches = new ArrayList<>();
    List<InvokeInstruction> ownerDescriptorOpcodeMatches = new ArrayList<>();
    List<InvokeInstruction> ownerNameOpcodeMatches = new ArrayList<>();
    List<InvokeInstruction> ownerNameDescriptorMatches = new ArrayList<>();
    List<InvokeInstruction> nameDescriptorOpcodeMatches = new ArrayList<>();
    for (InvokeInstruction instruction : scanResult.invokeInstructions()) {
      if (instruction.opcode() == request.expectedInvokeOpcode()
          && instruction.ownerInternalName().equals(request.expectedInvokeOwnerInternalName())
          && instruction.name().equals(request.expectedInvokeName())
          && instruction.descriptor().equals(request.expectedInvokeDescriptor())) {
        exactMatches.add(instruction);
      }
      if (instruction.opcode() == request.expectedInvokeOpcode()
          && instruction.ownerInternalName().equals(request.expectedInvokeOwnerInternalName())
          && instruction.descriptor().equals(request.expectedInvokeDescriptor())) {
        ownerDescriptorOpcodeMatches.add(instruction);
      }
      if (instruction.opcode() == request.expectedInvokeOpcode()
          && instruction.ownerInternalName().equals(request.expectedInvokeOwnerInternalName())
          && instruction.name().equals(request.expectedInvokeName())) {
        ownerNameOpcodeMatches.add(instruction);
      }
      if (instruction.ownerInternalName().equals(request.expectedInvokeOwnerInternalName())
          && instruction.name().equals(request.expectedInvokeName())
          && instruction.descriptor().equals(request.expectedInvokeDescriptor())) {
        ownerNameDescriptorMatches.add(instruction);
      }
      if (instruction.opcode() == request.expectedInvokeOpcode()
          && instruction.name().equals(request.expectedInvokeName())
          && instruction.descriptor().equals(request.expectedInvokeDescriptor())) {
        nameDescriptorOpcodeMatches.add(instruction);
      }
    }
    String mismatchReason = null;
    if (scanResult.invokeInstructions().size() == 1) {
      InvokeInstruction observed = scanResult.invokeInstructions().getFirst();
      if (!observed.ownerInternalName().equals(request.expectedInvokeOwnerInternalName())) {
        mismatchReason = "Wrong invoke owner: " + request.expectedInvokeOwnerInternalName() + ".";
      } else if (!observed.name().equals(request.expectedInvokeName())) {
        mismatchReason = "Wrong invoke name: " + request.expectedInvokeName() + ".";
      } else if (!observed.descriptor().equals(request.expectedInvokeDescriptor())) {
        mismatchReason = "Wrong invoke descriptor: " + request.expectedInvokeDescriptor() + ".";
      } else if (observed.opcode() != request.expectedInvokeOpcode()) {
        mismatchReason = "Wrong invoke opcode: " + request.expectedInvokeOpcode().id() + ".";
      }
    } else if (exactMatches.isEmpty()) {
      if (nameDescriptorOpcodeMatches.size() == 1) {
        mismatchReason = "Wrong invoke owner: " + request.expectedInvokeOwnerInternalName() + ".";
      } else if (ownerDescriptorOpcodeMatches.size() == 1) {
        mismatchReason = "Wrong invoke name: " + request.expectedInvokeName() + ".";
      } else if (ownerNameOpcodeMatches.size() == 1) {
        mismatchReason = "Wrong invoke descriptor: " + request.expectedInvokeDescriptor() + ".";
      } else if (ownerNameDescriptorMatches.size() == 1) {
        mismatchReason = "Wrong invoke opcode: " + request.expectedInvokeOpcode().id() + ".";
      }
    }
    return new CallsiteMatch(exactMatches, mismatchReason);
  }

  private ScanResult scanCode(byte[] code, ConstantPool constantPool)
      throws TransformationException {
    int offset = 0;
    boolean branchingMethod = false;
    boolean switchMethod = false;
    boolean constructorInvocation = false;
    boolean specialInvocation = false;
    List<InvokeInstruction> invokeInstructions = new ArrayList<>();
    while (offset < code.length) {
      int opcode = code[offset] & 0xff;
      if (isBranchOpcode(opcode)) {
        branchingMethod = true;
        break;
      }
      if (opcode == TABLESWITCH || opcode == LOOKUPSWITCH) {
        switchMethod = true;
        break;
      }
      if (opcode == WIDE) {
        throw new TransformationException("Wide instructions are unsupported.");
      }
      if (opcode == SteelHookInvokeOpcode.INVOKESTATIC.opcode()
          || opcode == SteelHookInvokeOpcode.INVOKEVIRTUAL.opcode()
          || opcode == SteelHookInvokeOpcode.INVOKEINTERFACE.opcode()
          || opcode == SteelHookInvokeOpcode.INVOKESPECIAL.opcode()) {
        int methodrefIndex = unsignedShort(code, offset + 1);
        MethodrefInfo methodref = constantPool.methodref(methodrefIndex);
        SteelHookInvokeOpcode invokeOpcode = fromBytecodeOpcode(opcode);
        invokeInstructions.add(
            new InvokeInstruction(
                offset,
                offset + 1,
                invokeOpcode,
                methodref.ownerInternalName(),
                methodref.name(),
                methodref.descriptor(),
                methodrefIndex));
        if ("<init>".equals(methodref.name())) {
          constructorInvocation = true;
          break;
        }
        if (invokeOpcode == SteelHookInvokeOpcode.INVOKESPECIAL) {
          specialInvocation = true;
          break;
        }
      }
      offset += instructionLength(opcode, code, offset);
    }
    return new ScanResult(
        branchingMethod,
        switchMethod,
        constructorInvocation,
        specialInvocation,
        invokeInstructions);
  }

  private SteelHookInvokeOpcode fromBytecodeOpcode(int opcode) throws TransformationException {
    for (SteelHookInvokeOpcode candidate : SteelHookInvokeOpcode.values()) {
      if (candidate.opcode() == opcode) {
        return candidate;
      }
    }
    throw new TransformationException("Unsupported invoke opcode 0x" + Integer.toHexString(opcode));
  }

  private boolean isBranchOpcode(int opcode) {
    return (opcode >= 0x99 && opcode <= 0xa8) || opcode == 0xc6 || opcode == 0xc7;
  }

  private int instructionLength(int opcode, byte[] code, int offset)
      throws TransformationException {
    return switch (opcode) {
      case 0x00, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x2a, 0x57, 0x59, 0xac, 0xb0, 0xb1 -> 1;
      case 0x10, 0x12 -> 2;
      case 0xbb -> 3;
      case 0xa7, 0xb6, 0xb7, 0xb8 -> 3;
      case 0xb9 -> 5;
      default ->
          throw new TransformationException(
              "Unsupported opcode 0x" + Integer.toHexString(opcode) + " in bounded fixture.");
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
      SteelHookInvokeCallsiteRewriteRequest request, byte[] classBytes)
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

  private SteelHookInvokeCallsiteRewriteResult rejected(
      SteelHookInvokeCallsiteRewriteRequest request,
      byte[] originalClassBytes,
      String failureReason,
      boolean exceptionTablePresent,
      boolean stackMapTablePresent,
      boolean synchronizedMethod,
      boolean branchingMethod,
      boolean switchMethod) {
    return new SteelHookInvokeCallsiteRewriteResult(
        SteelHookInvokeCallsiteRewriteStatus.REJECTED,
        failureReason,
        request,
        originalClassBytes == null ? null : sha256Hex(originalClassBytes),
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

  private int unsignedShort(byte[] bytes, int offset) {
    return ((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff);
  }

  private byte hi(int value) {
    return (byte) (value >>> 8);
  }

  private byte lo(int value) {
    return (byte) value;
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
      throw new IllegalStateException("SHA-256 is unavailable for Target-34 rewriting.", exception);
    }
  }

  private record CallsiteMatch(
      List<InvokeInstruction> exactMatches, String specificMismatchReason) {
    private CallsiteMatch {
      exactMatches = List.copyOf(exactMatches);
    }
  }

  private record ScanResult(
      boolean branchingMethod,
      boolean switchMethod,
      boolean constructorInvocation,
      boolean specialInvocation,
      List<InvokeInstruction> invokeInstructions) {
    private ScanResult {
      invokeInstructions = List.copyOf(invokeInstructions);
    }
  }

  private record InvokeInstruction(
      int offset,
      int operandOffset,
      SteelHookInvokeOpcode opcode,
      String ownerInternalName,
      String name,
      String descriptor,
      int methodrefIndex) {}

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

  private record MethodrefInfo(String ownerInternalName, String name, String descriptor) {}

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
          case 9, 10, 11 -> {
            entries[index] =
                new RefEntry(tag, reader.readUnsignedShort(), reader.readUnsignedShort());
          }
          case 12 ->
              entries[index] =
                  new NameAndTypeEntry(reader.readUnsignedShort(), reader.readUnsignedShort());
          case 15 -> {
            reader.readUnsignedByte();
            reader.readUnsignedShort();
            entries[index] = new CpEntry(tag);
          }
          case 16, 18, 19, 20 -> {
            reader.readUnsignedShort();
            if (tag == 18) {
              reader.readUnsignedShort();
            }
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

    private MethodrefInfo methodref(int index) throws TransformationException {
      if (index <= 0 || index >= entries.length || !(entries[index] instanceof RefEntry entry)) {
        throw new TransformationException("Malformed methodref constant pool index " + index + ".");
      }
      if (entry.tag() != 10 && entry.tag() != 11) {
        throw new TransformationException(
            "Expected methodref at constant pool index " + index + ".");
      }
      NameAndTypeEntry nameAndType = nameAndType(entry.nameAndTypeIndex());
      return new MethodrefInfo(
          className(entry.classIndex()),
          utf8(nameAndType.nameIndex()),
          utf8(nameAndType.descriptorIndex()));
    }

    private NameAndTypeEntry nameAndType(int index) throws TransformationException {
      if (index <= 0
          || index >= entries.length
          || !(entries[index] instanceof NameAndTypeEntry entry)) {
        throw new TransformationException(
            "Malformed name-and-type constant pool index " + index + ".");
      }
      return entry;
    }

    private int methodrefIndex(String ownerInternalName, String name, String descriptor)
        throws TransformationException {
      for (int index = 1; index < entries.length; index++) {
        if (entries[index] instanceof RefEntry entry && entry.tag() == 10) {
          MethodrefInfo methodref = methodref(index);
          if (ownerInternalName.equals(methodref.ownerInternalName())
              && name.equals(methodref.name())
              && descriptor.equals(methodref.descriptor())) {
            return index;
          }
        }
      }
      return -1;
    }
  }

  private static class CpEntry {
    private final int tag;

    private CpEntry(int tag) {
      this.tag = tag;
    }

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

    @SuppressWarnings("unused")
    private int stringIndex() {
      return stringIndex;
    }
  }

  private static final class RefEntry extends CpEntry {
    private final int classIndex;
    private final int nameAndTypeIndex;

    private RefEntry(int tag, int classIndex, int nameAndTypeIndex) {
      super(tag);
      this.classIndex = classIndex;
      this.nameAndTypeIndex = nameAndTypeIndex;
    }

    private int classIndex() {
      return classIndex;
    }

    private int nameAndTypeIndex() {
      return nameAndTypeIndex;
    }
  }

  private static final class NameAndTypeEntry extends CpEntry {
    private final int nameIndex;
    private final int descriptorIndex;

    private NameAndTypeEntry(int nameIndex, int descriptorIndex) {
      super(12);
      this.nameIndex = nameIndex;
      this.descriptorIndex = descriptorIndex;
    }

    private int nameIndex() {
      return nameIndex;
    }

    private int descriptorIndex() {
      return descriptorIndex;
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
