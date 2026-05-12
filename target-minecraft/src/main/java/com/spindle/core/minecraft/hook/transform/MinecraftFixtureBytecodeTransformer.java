package com.spindle.core.minecraft.hook.transform;

import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchMode;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlan;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlanner;
import com.spindle.core.minecraft.hook.patch.MinecraftPlannedHookPatch;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MinecraftFixtureBytecodeTransformer {
  public static final String MILESTONE_NAME = "Target-8";
  public static final String TRANSFORMATION_SCOPE = "fixture-only";

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
  private static final int INSERTION_OFFSET = 0;
  private static final int INSERTION_LENGTH = 3;

  public MinecraftFixtureTransformationResult transformFixtureClass(
      byte[] originalClassBytes, MinecraftHookPatchPlan patchPlan) {
    byte[] immutableOriginal = originalClassBytes == null ? null : originalClassBytes.clone();
    MinecraftFixtureTransformationGate gate = evaluateGate(patchPlan);
    if (!gate.passed()) {
      return failedResult(
          immutableOriginal,
          patchPlan,
          gate,
          MinecraftFixtureTransformationStatus.PATCH_PLAN_GATE_FAILED,
          gate.failureReason(),
          null,
          null,
          null,
          null,
          null);
    }
    if (immutableOriginal == null || immutableOriginal.length == 0) {
      return failedResult(
          null,
          patchPlan,
          gate,
          MinecraftFixtureTransformationStatus.REJECTED,
          "Fixture class bytes are required for Target-8 transformation.",
          null,
          null,
          null,
          null,
          null);
    }

    try {
      MinecraftPlannedHookPatch plannedPatch = patchPlan.plannedPatches().getFirst();
      ClassFileLayout classFile = parseClassFile(immutableOriginal);
      if (!"net/minecraft/server/Main".equals(classFile.thisClassName())) {
        return failedResult(
            immutableOriginal,
            patchPlan,
            gate,
            MinecraftFixtureTransformationStatus.REJECTED,
            "Target-8 requires fixture class net/minecraft/server/Main.",
            null,
            null,
            null,
            null,
            null);
      }
      MethodLayout targetMethod = findTargetMethod(classFile.methods());
      if (targetMethod == null) {
        return failedResult(
            immutableOriginal,
            patchPlan,
            gate,
            MinecraftFixtureTransformationStatus.REJECTED,
            "Target-8 requires exactly one net/minecraft/server/Main.main([Ljava/lang/String;)V method.",
            null,
            null,
            null,
            null,
            null);
      }
      if ((targetMethod.accessFlags() & (ACCESS_ABSTRACT | ACCESS_NATIVE)) != 0) {
        return failedResult(
            immutableOriginal,
            patchPlan,
            gate,
            MinecraftFixtureTransformationStatus.REJECTED,
            "Target-8 cannot transform an abstract or native fixture method.",
            null,
            null,
            null,
            null,
            null);
      }
      if (targetMethod.codeAttribute() == null) {
        return failedResult(
            immutableOriginal,
            patchPlan,
            gate,
            MinecraftFixtureTransformationStatus.REJECTED,
            "Target-8 fixture method is missing a Code attribute.",
            null,
            null,
            null,
            null,
            null);
      }

      CodeAttributeLayout codeAttribute = targetMethod.codeAttribute();
      if (codeAttribute.stackMapTablePresent()) {
        return failedResult(
            immutableOriginal,
            patchPlan,
            gate,
            MinecraftFixtureTransformationStatus.REJECTED,
            "Target-8 fixture transformation rejects methods with StackMapTable.",
            null,
            null,
            null,
            null,
            null);
      }
      if (codeAttribute.code().length + INSERTION_LENGTH > 65535) {
        return failedResult(
            immutableOriginal,
            patchPlan,
            gate,
            MinecraftFixtureTransformationStatus.REJECTED,
            "Target-8 fixture transformation exceeds the maximum Code length.",
            null,
            null,
            null,
            null,
            null);
      }
      if (classFile.constantPoolCount() + 6 > 0xFFFF) {
        return failedResult(
            immutableOriginal,
            patchPlan,
            gate,
            MinecraftFixtureTransformationStatus.REJECTED,
            "Target-8 fixture transformation exceeds the maximum constant_pool_count.",
            null,
            null,
            null,
            null,
            null);
      }

      MinecraftFixtureConstantPoolPatch constantPoolPatch =
          createConstantPoolPatch(classFile.constantPoolCount());
      byte[] insertedInstruction =
          invokestaticInstruction(constantPoolPatch.dispatcherMethodrefIndex());
      byte[] transformedCode = prepend(insertedInstruction, codeAttribute.code());
      byte[] rewrittenCodeAttribute = rewriteCodeAttribute(codeAttribute, transformedCode);
      int transformedCodeLength = transformedCode.length;
      int transformedAttributeLength = rewrittenCodeAttribute.length;

      byte[] transformedClassBytes =
          rewriteClassBytes(
              immutableOriginal,
              classFile,
              appendConstantPoolEntries(constantPoolPatch),
              constantPoolPatch.constantPoolCountAfter(),
              codeAttribute.attributeLengthOffset(),
              transformedAttributeLength,
              codeAttribute.bodyEnd(),
              rewrittenCodeAttribute);

      String insertedInstructionHex = hex(insertedInstruction);
      String originalClassSha256 = sha256Hex(immutableOriginal);
      String transformedClassSha256 = sha256Hex(transformedClassBytes);
      String originalCodeSha256 = sha256Hex(codeAttribute.code());
      String transformedCodeSha256 = sha256Hex(transformedCode);
      MinecraftFixtureCodePatchResult codePatch =
          new MinecraftFixtureCodePatchResult(
              codeAttribute.code().length,
              transformedCodeLength,
              originalCodeSha256,
              transformedCodeSha256,
              codeAttribute.maxStack(),
              codeAttribute.maxStack(),
              codeAttribute.maxLocals(),
              codeAttribute.maxLocals(),
              codeAttribute.exceptionTableCount(),
              codeAttribute.exceptionTableCount() > 0,
              insertedInstructionHex);
      MinecraftFixtureTransformedClass transformedClass =
          new MinecraftFixtureTransformedClass(
              classFile.thisClassName(), transformedClassBytes, transformedClassSha256);

      return new MinecraftFixtureTransformationResult(
          1,
          MILESTONE_NAME,
          TRANSFORMATION_SCOPE,
          MinecraftFixtureTransformationStatus.TRANSFORMED,
          gate,
          plannedPatch.id(),
          plannedPatch.sourcePlacementId(),
          plannedPatch.sourceContractId(),
          plannedPatch.ownerInternalName(),
          plannedPatch.memberName(),
          plannedPatch.descriptor(),
          INSERTION_OFFSET,
          insertedInstructionHex,
          originalClassSha256,
          transformedClassSha256,
          originalCodeSha256,
          transformedCodeSha256,
          codeAttribute.code().length,
          transformedCodeLength,
          constantPoolPatch.constantPoolCountBefore(),
          constantPoolPatch.constantPoolCountAfter(),
          constantPoolPatch.dispatcherMethodrefIndex(),
          true,
          true,
          true,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          null,
          constantPoolPatch,
          codePatch,
          transformedClass);
    } catch (TransformationException exception) {
      return failedResult(
          immutableOriginal,
          patchPlan,
          gate,
          MinecraftFixtureTransformationStatus.REJECTED,
          exception.getMessage(),
          null,
          null,
          null,
          null,
          null);
    }
  }

  private MinecraftFixtureTransformationGate evaluateGate(MinecraftHookPatchPlan patchPlan) {
    if (patchPlan == null) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires a Target-7 patch plan.",
          false,
          false,
          false,
          0,
          null,
          false,
          false);
    }
    if (!patchPlan.gatePassed()) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires a passing Target-7 patch plan gate.",
          false,
          patchPlan.patchPlanningSucceeded(),
          patchPlan.patchPlanned(),
          patchPlan.plannedPatchCount(),
          null,
          patchPlan.transformReadyForFixtureOnly(),
          patchPlan.transformReadyForMinecraftRuntime());
    }
    if (!patchPlan.patchPlanningSucceeded() || !patchPlan.patchPlanned()) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires a successful Target-7 patch plan.",
          true,
          patchPlan.patchPlanningSucceeded(),
          patchPlan.patchPlanned(),
          patchPlan.plannedPatchCount(),
          null,
          patchPlan.transformReadyForFixtureOnly(),
          patchPlan.transformReadyForMinecraftRuntime());
    }
    if (patchPlan.plannedPatchCount() != 1 || patchPlan.plannedPatches().size() != 1) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires exactly one planned Target-7 patch.",
          true,
          true,
          true,
          patchPlan.plannedPatchCount(),
          null,
          patchPlan.transformReadyForFixtureOnly(),
          patchPlan.transformReadyForMinecraftRuntime());
    }
    MinecraftPlannedHookPatch patch = patchPlan.plannedPatches().getFirst();
    if (!MinecraftHookPatchPlanner.SUPPORTED_PATCH_ID.equals(patch.id())) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Unsupported Target-7 patch id for Target-8: " + patch.id(),
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (patch.mode() != MinecraftHookPatchMode.DRY_RUN_STATIC_DISPATCH_INVOKESTATIC) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires dry-run-static-dispatch-invokestatic mode.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (!"net/minecraft/server/Main".equals(patch.ownerInternalName())
        || !"main".equals(patch.memberName())
        || !"([Ljava/lang/String;)V".equals(patch.descriptor())) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires patch target net/minecraft/server/Main.main([Ljava/lang/String;)V.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (patch.insertionOffset() != INSERTION_OFFSET
        || patchPlan.insertionOffset() == null
        || patchPlan.insertionOffset() != INSERTION_OFFSET) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires insertion offset 0.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (patch.codeInsertion() == null
        || !"invokestatic".equals(patch.codeInsertion().plannedOpcode())
        || patch.codeInsertion().plannedInstructionLength() != INSERTION_LENGTH) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires one 3-byte invokestatic patch instruction.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (!MinecraftHookPatchPlanner.DISPATCHER_OWNER_INTERNAL_NAME.equals(
            patch.codeInsertion().dispatcherOwnerInternalName())
        || !MinecraftHookPatchPlanner.DISPATCHER_METHOD_NAME.equals(
            patch.codeInsertion().dispatcherMethodName())
        || !MinecraftHookPatchPlanner.DISPATCHER_DESCRIPTOR.equals(
            patch.codeInsertion().dispatcherDescriptor())) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires dispatcher com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher.beforeMinecraftServerMain:()V.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (!patchPlan.transformReadyForFixtureOnly() || !patch.transformReadyForFixtureOnly()) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 requires transformReadyForFixtureOnly to be true.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          patch.transformReadyForMinecraftRuntime());
    }
    if (patchPlan.transformReadyForMinecraftRuntime()
        || patch.transformReadyForMinecraftRuntime()) {
      return new MinecraftFixtureTransformationGate(
          false,
          "Target-8 must not report transformReadyForMinecraftRuntime.",
          true,
          true,
          true,
          1,
          patch.id(),
          patch.transformReadyForFixtureOnly(),
          true);
    }
    return new MinecraftFixtureTransformationGate(
        true, null, true, true, true, 1, patch.id(), true, false);
  }

  private MethodLayout findTargetMethod(List<MethodLayout> methods) {
    MethodLayout match = null;
    for (MethodLayout method : methods) {
      if (!"main".equals(method.name()) || !"([Ljava/lang/String;)V".equals(method.descriptor())) {
        continue;
      }
      if (match != null) {
        return null;
      }
      match = method;
    }
    return match;
  }

  private ClassFileLayout parseClassFile(byte[] classBytes) throws TransformationException {
    ClassReader reader = new ClassReader(classBytes);
    if (reader.readInt() != CLASS_MAGIC) {
      throw new TransformationException(
          "Invalid class file magic for Target-8 fixture transformation.");
    }
    reader.skip(2);
    reader.skip(2);
    int constantPoolCount = reader.readUnsignedShort();
    int constantPoolStart = reader.position();
    ConstantPool constantPool = readConstantPool(reader, constantPoolCount);
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
      methods.add(readMethod(reader, constantPool, classBytes));
    }
    int classAttributeCount = reader.readUnsignedShort();
    for (int index = 0; index < classAttributeCount; index++) {
      skipAttribute(reader);
    }
    if (reader.position() != classBytes.length) {
      throw new TransformationException(
          "Malformed class file for Target-8 fixture transformation.");
    }
    return new ClassFileLayout(
        constantPoolCount, constantPoolStart, constantPoolEnd, thisClassName, methods);
  }

  private ConstantPool readConstantPool(ClassReader reader, int count)
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
                "Unsupported constant pool tag " + tag + " for Target-8 fixture transformation.");
      }
    }
    return new ConstantPool(tags, values);
  }

  private MethodLayout readMethod(ClassReader reader, ConstantPool constantPool, byte[] classBytes)
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
        throw new TransformationException(
            "Malformed class file for Target-8 fixture transformation.");
      }
      if ("Code".equals(attributeName)) {
        if (codeAttribute != null) {
          throw new TransformationException(
              "Target-8 fixture method has multiple Code attributes.");
        }
        codeAttribute =
            parseCodeAttribute(
                classBytes, constantPool, attributeLengthOffset, bodyOffset, attributeLength);
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
      int attributeLength)
      throws TransformationException {
    ClassReader reader = new ClassReader(classBytes, bodyOffset, bodyOffset + attributeLength);
    if (attributeLength < 12) {
      throw new TransformationException(
          "Malformed Code attribute for Target-8 fixture transformation.");
    }
    int maxStack = reader.readUnsignedShort();
    int maxLocals = reader.readUnsignedShort();
    int codeLength = reader.readInt();
    if (codeLength < 0 || reader.position() + codeLength > bodyOffset + attributeLength) {
      throw new TransformationException(
          "Malformed Code attribute bytecode length for Target-8 fixture transformation.");
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
    int nestedAttributesOffset = reader.position();
    int nestedAttributeCount = reader.readUnsignedShort();
    boolean stackMapTablePresent = false;
    for (int index = 0; index < nestedAttributeCount; index++) {
      String nestedName = constantPool.utf8(reader.readUnsignedShort());
      int nestedLength = reader.readInt();
      if ("StackMapTable".equals(nestedName)) {
        stackMapTablePresent = true;
      }
      reader.skip(nestedLength);
    }
    if (reader.position() != bodyOffset + attributeLength) {
      throw new TransformationException(
          "Malformed Code attribute bounds for Target-8 fixture transformation.");
    }
    byte[] nestedAttributesBlock =
        Arrays.copyOfRange(classBytes, nestedAttributesOffset, bodyOffset + attributeLength);
    return new CodeAttributeLayout(
        attributeLengthOffset,
        bodyOffset,
        bodyOffset + attributeLength,
        maxStack,
        maxLocals,
        code,
        exceptionTableCount,
        exceptionTable,
        nestedAttributesBlock,
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

  private MinecraftFixtureConstantPoolPatch createConstantPoolPatch(int constantPoolCountBefore) {
    int dispatcherOwnerUtf8Index = constantPoolCountBefore;
    int dispatcherClassIndex = constantPoolCountBefore + 1;
    int dispatcherMethodNameUtf8Index = constantPoolCountBefore + 2;
    int dispatcherDescriptorUtf8Index = constantPoolCountBefore + 3;
    int dispatcherNameAndTypeIndex = constantPoolCountBefore + 4;
    int dispatcherMethodrefIndex = constantPoolCountBefore + 5;
    return new MinecraftFixtureConstantPoolPatch(
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

  private byte[] appendConstantPoolEntries(MinecraftFixtureConstantPoolPatch patch)
      throws TransformationException {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (DataOutputStream output = new DataOutputStream(bytes)) {
        writeUtf8(output, MinecraftHookPatchPlanner.DISPATCHER_OWNER_INTERNAL_NAME);
        output.writeByte(CONSTANT_CLASS);
        output.writeShort(patch.dispatcherOwnerUtf8Index());
        writeUtf8(output, MinecraftHookPatchPlanner.DISPATCHER_METHOD_NAME);
        writeUtf8(output, MinecraftHookPatchPlanner.DISPATCHER_DESCRIPTOR);
        output.writeByte(CONSTANT_NAME_AND_TYPE);
        output.writeShort(patch.dispatcherMethodNameUtf8Index());
        output.writeShort(patch.dispatcherDescriptorUtf8Index());
        output.writeByte(CONSTANT_METHODREF);
        output.writeShort(patch.dispatcherClassIndex());
        output.writeShort(patch.dispatcherNameAndTypeIndex());
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new TransformationException(
          "Failed to append Target-8 dispatcher constant pool entries.");
    }
  }

  private void writeUtf8(DataOutputStream output, String value) throws IOException {
    output.writeByte(CONSTANT_UTF8);
    output.writeUTF(value);
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
        for (ExceptionTableEntry entry : codeAttribute.exceptionTable()) {
          output.writeShort(entry.startPc() + INSERTION_LENGTH);
          output.writeShort(entry.endPc() + INSERTION_LENGTH);
          output.writeShort(entry.handlerPc() + INSERTION_LENGTH);
          output.writeShort(entry.catchType());
        }
        output.write(codeAttribute.nestedAttributesBlock());
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new TransformationException(
          "Failed to rewrite Code attribute for Target-8 fixture transformation.");
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
      throw new TransformationException("Failed to rewrite Target-8 fixture class bytes.");
    }
  }

  private MinecraftFixtureTransformationResult failedResult(
      byte[] originalClassBytes,
      MinecraftHookPatchPlan patchPlan,
      MinecraftFixtureTransformationGate gate,
      MinecraftFixtureTransformationStatus status,
      String failureReason,
      MinecraftFixtureConstantPoolPatch constantPoolPatch,
      MinecraftFixtureCodePatchResult codePatch,
      String transformedClassSha256,
      MinecraftFixtureTransformedClass transformedClass,
      Integer methodrefIndex) {
    MinecraftPlannedHookPatch plannedPatch =
        patchPlan == null || patchPlan.plannedPatches().isEmpty()
            ? null
            : patchPlan.plannedPatches().getFirst();
    String originalClassSha256 = originalClassBytes == null ? null : sha256Hex(originalClassBytes);
    return new MinecraftFixtureTransformationResult(
        1,
        MILESTONE_NAME,
        TRANSFORMATION_SCOPE,
        status,
        gate,
        plannedPatch == null ? null : plannedPatch.id(),
        plannedPatch == null ? null : plannedPatch.sourcePlacementId(),
        plannedPatch == null ? null : plannedPatch.sourceContractId(),
        plannedPatch == null
            ? patchPlan == null ? null : patchPlan.targetClass()
            : plannedPatch.ownerInternalName(),
        plannedPatch == null
            ? patchPlan == null ? null : patchPlan.targetMethod()
            : plannedPatch.memberName(),
        plannedPatch == null
            ? patchPlan == null ? null : patchPlan.targetDescriptor()
            : plannedPatch.descriptor(),
        patchPlan == null ? null : patchPlan.insertionOffset(),
        null,
        originalClassSha256,
        transformedClassSha256,
        null,
        null,
        null,
        null,
        constantPoolPatch == null ? null : constantPoolPatch.constantPoolCountBefore(),
        constantPoolPatch == null ? null : constantPoolPatch.constantPoolCountAfter(),
        methodrefIndex,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        failureReason,
        constantPoolPatch,
        codePatch,
        transformedClass);
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
          "SHA-256 is unavailable for Target-8 transformation.", exception);
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
      int bodyOffset,
      int bodyEnd,
      int maxStack,
      int maxLocals,
      byte[] code,
      int exceptionTableCount,
      List<ExceptionTableEntry> exceptionTable,
      byte[] nestedAttributesBlock,
      boolean stackMapTablePresent) {
    private CodeAttributeLayout {
      code = code.clone();
      exceptionTable = List.copyOf(exceptionTable);
      nestedAttributesBlock = nestedAttributesBlock.clone();
    }
  }

  private record ExceptionTableEntry(int startPc, int endPc, int handlerPc, int catchType) {}

  private static final class ConstantPool {
    private final int[] tags;
    private final Object[] values;

    private ConstantPool(int[] tags, Object[] values) {
      this.tags = tags;
      this.values = values;
    }

    private String utf8(int index) throws TransformationException {
      if (index <= 0 || index >= tags.length || tags[index] != CONSTANT_UTF8) {
        throw new TransformationException(
            "Invalid Utf8 constant pool index for Target-8 fixture transformation: " + index);
      }
      return (String) values[index];
    }

    private String className(int index) throws TransformationException {
      if (index <= 0 || index >= tags.length || tags[index] != CONSTANT_CLASS) {
        throw new TransformationException(
            "Invalid Class constant pool index for Target-8 fixture transformation: " + index);
      }
      return utf8((Integer) values[index]);
    }
  }

  private static final class ClassReader {
    private final byte[] bytes;
    private final int limit;
    private int position;

    private ClassReader(byte[] bytes) {
      this(bytes, 0, bytes.length);
    }

    private ClassReader(byte[] bytes, int position, int limit) {
      this.bytes = bytes;
      this.position = position;
      this.limit = limit;
    }

    private int position() {
      return position;
    }

    private void position(int newPosition) throws TransformationException {
      if (newPosition < 0 || newPosition > limit) {
        throw new TransformationException(
            "Malformed class file for Target-8 fixture transformation.");
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
            "Malformed Utf8 constant pool entry for Target-8 fixture transformation.");
      }
    }

    private void skip(int byteCount) throws TransformationException {
      require(byteCount);
      position += byteCount;
    }

    private void require(int byteCount) throws TransformationException {
      if (byteCount < 0 || position + byteCount > limit) {
        throw new TransformationException(
            "Malformed class file for Target-8 fixture transformation.");
      }
    }
  }

  private static final class TransformationException extends Exception {
    private TransformationException(String message) {
      super(message);
    }
  }
}
