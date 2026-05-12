package com.spindle.core.minecraft.hook.transform;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.hook.bytecode.MinecraftDecodedCodeAttribute;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchEligibility;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchKind;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchMode;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlan;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlanner;
import com.spindle.core.minecraft.hook.patch.MinecraftPatchCodeInsertion;
import com.spindle.core.minecraft.hook.patch.MinecraftPatchConstantPoolRequirement;
import com.spindle.core.minecraft.hook.patch.MinecraftPlannedHookPatch;
import com.spindle.core.minecraft.hook.place.MinecraftMethodCodeReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftFixtureBytecodeTransformerTest {
  private static final String TARGET_CLASS = "net/minecraft/server/Main";
  private static final String TARGET_METHOD = "main";
  private static final String TARGET_DESCRIPTOR = "([Ljava/lang/String;)V";

  private final MinecraftFixtureBytecodeTransformer transformer =
      new MinecraftFixtureBytecodeTransformer();
  private final MinecraftMethodCodeReader methodCodeReader = new MinecraftMethodCodeReader();

  @Test
  void validTargetSevenPatchTransformsFixtureMain() throws Exception {
    byte[] originalClassBytes =
        fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, false, false);
    byte[] originalSnapshot = originalClassBytes.clone();

    MinecraftDecodedCodeAttribute originalCode =
        methodCodeReader.readDecodedCode(
            originalClassBytes, TARGET_CLASS, TARGET_METHOD, TARGET_DESCRIPTOR);
    MinecraftFixtureTransformationResult firstResult =
        transformer.transformFixtureClass(originalClassBytes, validPatchPlan());
    MinecraftFixtureTransformationResult secondResult =
        transformer.transformFixtureClass(originalClassBytes, validPatchPlan());

    assertEquals(MinecraftFixtureTransformationStatus.TRANSFORMED, firstResult.status());
    assertTrue(firstResult.gate().passed());
    assertTrue(firstResult.fixtureTransformationOccurred());
    assertTrue(firstResult.fixtureBytecodeModified());
    assertTrue(firstResult.transformedClassBytesProduced());
    assertFalse(firstResult.minecraftRuntimeTransformed());
    assertFalse(firstResult.minecraftRuntimeClassLoadingChanged());
    assertFalse(firstResult.bootstrapTransformationEnabled());
    assertFalse(firstResult.publicApiExposed());
    assertFalse(firstResult.javaAgentUsed());
    assertFalse(firstResult.mixinUsed());
    assertFalse(firstResult.remappingOccurred());
    assertFalse(firstResult.javaModExecutionSandboxed());
    assertFalse(firstResult.gate().transformReadyForMinecraftRuntime());
    assertEquals(originalClassBytes.length, originalSnapshot.length);
    assertArrayEquals(originalSnapshot, originalClassBytes);
    assertNotNull(firstResult.transformedClass());
    assertNotEquals(firstResult.originalClassSha256(), firstResult.transformedClassSha256());
    assertNotEquals(firstResult.transformedClass().classBytes().length, originalClassBytes.length);
    assertNotEquals(
        firstResult.originalClassSha256(), firstResult.transformedClass().classSha256());
    assertEquals(
        firstResult.transformedClassSha256(), firstResult.transformedClass().classSha256());
    assertEquals(firstResult.constantPoolCountBefore() + 6, firstResult.constantPoolCountAfter());
    assertEquals(firstResult.constantPoolCountBefore() + 5, firstResult.methodrefIndex());
    assertTrue(firstResult.insertedInstructionHex().startsWith("b8 "));

    MinecraftDecodedCodeAttribute transformedCode =
        methodCodeReader.readDecodedCode(
            firstResult.transformedClass().classBytes(),
            TARGET_CLASS,
            TARGET_METHOD,
            TARGET_DESCRIPTOR);
    assertEquals(originalCode.codeLength() + 3, transformedCode.codeLength());
    assertEquals(originalCode.maxStack(), transformedCode.maxStack());
    assertEquals(originalCode.maxLocals(), transformedCode.maxLocals());
    assertEquals(originalCode.codeLength(), firstResult.originalCodeLength());
    assertEquals(transformedCode.codeLength(), firstResult.transformedCodeLength());
    assertEquals(firstResult.originalCodeSha256(), secondResult.originalCodeSha256());
    assertEquals(firstResult.transformedCodeSha256(), secondResult.transformedCodeSha256());
    assertEquals(firstResult.originalClassSha256(), secondResult.originalClassSha256());
    assertEquals(firstResult.transformedClassSha256(), secondResult.transformedClassSha256());
    assertArrayEquals(
        firstResult.transformedClass().classBytes(), secondResult.transformedClass().classBytes());

    byte[] transformedMethodCode = transformedCode.code();
    assertEquals(0xb8, transformedMethodCode[0] & 0xFF);
    int actualMethodrefIndex =
        ((transformedMethodCode[1] & 0xFF) << 8) | (transformedMethodCode[2] & 0xFF);
    assertEquals(firstResult.methodrefIndex(), actualMethodrefIndex);
    assertEquals(
        firstResult.methodrefIndex(), firstResult.constantPoolPatch().dispatcherMethodrefIndex());
  }

  @Test
  void exceptionTableEntriesShiftByThreeWhenPresent() throws Exception {
    byte[] originalClassBytes =
        fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, false, true);

    ExceptionTableEntry originalEntry = readMainExceptionTable(originalClassBytes).getFirst();
    MinecraftFixtureTransformationResult result =
        transformer.transformFixtureClass(originalClassBytes, validPatchPlan());
    ExceptionTableEntry transformedEntry =
        readMainExceptionTable(result.transformedClass().classBytes()).getFirst();

    assertEquals(MinecraftFixtureTransformationStatus.TRANSFORMED, result.status());
    assertEquals(originalEntry.startPc() + 3, transformedEntry.startPc());
    assertEquals(originalEntry.endPc() + 3, transformedEntry.endPc());
    assertEquals(originalEntry.handlerPc() + 3, transformedEntry.handlerPc());
  }

  @Test
  void fixtureMethodWithStackMapTableIsRejectedDeterministically() {
    byte[] originalClassBytes =
        fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, true, false);

    MinecraftFixtureTransformationResult result =
        transformer.transformFixtureClass(originalClassBytes, validPatchPlan());

    assertEquals(MinecraftFixtureTransformationStatus.REJECTED, result.status());
    assertEquals(
        "Target-8 fixture transformation rejects methods with StackMapTable.",
        result.failureReason());
  }

  @Test
  void missingCodeIsRejected() {
    byte[] originalClassBytes =
        fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, false, false, false);

    MinecraftFixtureTransformationResult result =
        transformer.transformFixtureClass(originalClassBytes, validPatchPlan());

    assertEquals(MinecraftFixtureTransformationStatus.REJECTED, result.status());
    assertEquals("Target-8 fixture method is missing a Code attribute.", result.failureReason());
  }

  @Test
  void wrongClassInternalNameIsRejected() {
    byte[] originalClassBytes =
        fixtureClassBytes("com/example/Main", TARGET_DESCRIPTOR, true, false, false);

    MinecraftFixtureTransformationResult result =
        transformer.transformFixtureClass(originalClassBytes, validPatchPlan());

    assertEquals(MinecraftFixtureTransformationStatus.REJECTED, result.status());
    assertEquals(
        "Target-8 requires fixture class net/minecraft/server/Main.", result.failureReason());
  }

  @Test
  void wrongMethodDescriptorIsRejected() {
    byte[] originalClassBytes = fixtureClassBytes(TARGET_CLASS, "()V", true, false, false);

    MinecraftFixtureTransformationResult result =
        transformer.transformFixtureClass(originalClassBytes, validPatchPlan());

    assertEquals(MinecraftFixtureTransformationStatus.REJECTED, result.status());
    assertEquals(
        "Target-8 requires exactly one net/minecraft/server/Main.main([Ljava/lang/String;)V method.",
        result.failureReason());
  }

  @Test
  void unsupportedPatchIdIsRejected() {
    MinecraftFixtureTransformationResult result =
        transformer.transformFixtureClass(
            fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, false, false),
            patchPlan("unsupported-target-7-patch", true, false));

    assertEquals(MinecraftFixtureTransformationStatus.PATCH_PLAN_GATE_FAILED, result.status());
    assertTrue(result.failureReason().contains("Unsupported Target-7 patch id"));
  }

  @Test
  void patchPlanGateFailureProducesFailedTargetEightResultWithoutThrowing() {
    MinecraftFixtureTransformationResult result =
        transformer.transformFixtureClass(
            fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, false, false),
            patchPlan(MinecraftHookPatchPlanner.SUPPORTED_PATCH_ID, false, false));

    assertEquals(MinecraftFixtureTransformationStatus.PATCH_PLAN_GATE_FAILED, result.status());
    assertFalse(result.gate().passed());
    assertEquals("Target-8 requires a passing Target-7 patch plan gate.", result.failureReason());
    assertFalse(result.fixtureTransformationOccurred());
    assertNull(result.transformedClass());
  }

  @Test
  void transformReadyForMinecraftRuntimeIsRejectedAndRemainsFalseInSuccessfulResults() {
    MinecraftFixtureTransformationResult rejected =
        transformer.transformFixtureClass(
            fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, false, false),
            patchPlan(MinecraftHookPatchPlanner.SUPPORTED_PATCH_ID, true, true));

    assertEquals(MinecraftFixtureTransformationStatus.PATCH_PLAN_GATE_FAILED, rejected.status());
    assertTrue(rejected.failureReason().contains("transformReadyForMinecraftRuntime"));

    MinecraftFixtureTransformationResult transformed =
        transformer.transformFixtureClass(
            fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, false, false),
            validPatchPlan());
    assertFalse(transformed.gate().transformReadyForMinecraftRuntime());
  }

  @Test
  void targetEightTransformDoesNotWriteEarlierHookArtifacts(
      @TempDir java.nio.file.Path tempDirectory) {
    transformer.transformFixtureClass(
        fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, false, false), validPatchPlan());

    assertFalse(
        java.nio.file.Files.exists(
            tempDirectory.resolve("minecraft-hook-installation-result.json")));
    assertFalse(
        java.nio.file.Files.exists(tempDirectory.resolve("minecraft-hook-placement-plan.json")));
    assertFalse(
        java.nio.file.Files.exists(tempDirectory.resolve("minecraft-hook-bytecode-analysis.json")));
    assertFalse(
        java.nio.file.Files.exists(tempDirectory.resolve("minecraft-hook-patch-plan.json")));
  }

  static MinecraftHookPatchPlan validPatchPlan() {
    return patchPlan(MinecraftHookPatchPlanner.SUPPORTED_PATCH_ID, true, false);
  }

  static MinecraftHookPatchPlan patchPlan(
      String patchId, boolean gatePassed, boolean transformReadyForMinecraftRuntime) {
    MinecraftPlannedHookPatch plannedPatch =
        new MinecraftPlannedHookPatch(
            patchId,
            "target-5.minecraft.server.main.method-entry-placement",
            "minecraft.26_1_2.server.main.entrypoint",
            "Target-6",
            "minecraft-26.1.2-server-known-symbols",
            MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH,
            MinecraftHookPatchMode.DRY_RUN_STATIC_DISPATCH_INVOKESTATIC,
            MinecraftHookPatchEligibility.FIXTURE_ONLY_FUTURE_TRANSFORM,
            TARGET_CLASS,
            TARGET_METHOD,
            TARGET_DESCRIPTOR,
            0,
            true,
            new MinecraftPatchCodeInsertion(
                MinecraftHookPatchPlanner.DISPATCHER_OWNER_INTERNAL_NAME,
                MinecraftHookPatchPlanner.DISPATCHER_METHOD_NAME,
                MinecraftHookPatchPlanner.DISPATCHER_DESCRIPTOR,
                "invokestatic",
                "b8",
                3,
                0,
                0,
                "b8 ?? ??"),
            List.of(
                new MinecraftPatchConstantPoolRequirement(
                    "Utf8", MinecraftHookPatchPlanner.DISPATCHER_OWNER_INTERNAL_NAME),
                new MinecraftPatchConstantPoolRequirement(
                    "Class", MinecraftHookPatchPlanner.DISPATCHER_OWNER_INTERNAL_NAME),
                new MinecraftPatchConstantPoolRequirement(
                    "Utf8", MinecraftHookPatchPlanner.DISPATCHER_METHOD_NAME),
                new MinecraftPatchConstantPoolRequirement(
                    "Utf8", MinecraftHookPatchPlanner.DISPATCHER_DESCRIPTOR),
                new MinecraftPatchConstantPoolRequirement(
                    "NameAndType",
                    MinecraftHookPatchPlanner.DISPATCHER_METHOD_NAME
                        + ":"
                        + MinecraftHookPatchPlanner.DISPATCHER_DESCRIPTOR),
                new MinecraftPatchConstantPoolRequirement(
                    "Methodref",
                    MinecraftHookPatchPlanner.DISPATCHER_OWNER_INTERNAL_NAME
                        + "."
                        + MinecraftHookPatchPlanner.DISPATCHER_METHOD_NAME
                        + ":"
                        + MinecraftHookPatchPlanner.DISPATCHER_DESCRIPTOR)),
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
            false,
            null,
            null,
            null,
            null,
            null,
            true,
            transformReadyForMinecraftRuntime);
    return new MinecraftHookPatchPlan(
        1,
        "Target-7",
        "minecraft",
        "26.1.2",
        "server",
        "minecraft-26.1.2-server-known-symbols",
        true,
        0,
        "net.minecraft.server.Main",
        gatePassed,
        gatePassed ? null : "Target-7 hook patch plan gate failed.",
        gatePassed,
        gatePassed,
        gatePassed ? 1 : 0,
        gatePassed
            ? MinecraftHookPatchEligibility.FIXTURE_ONLY_FUTURE_TRANSFORM
            : MinecraftHookPatchEligibility.NOT_ELIGIBLE,
        "target-5.minecraft.server.main.method-entry-placement",
        1,
        "Target-6",
        TARGET_CLASS,
        TARGET_METHOD,
        TARGET_DESCRIPTOR,
        1,
        4,
        3,
        "original-code-sha",
        0,
        true,
        0,
        "b8 ?? ??",
        plannedPatch.requiredConstantPoolEntries(),
        gatePassed,
        gatePassed,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        true,
        transformReadyForMinecraftRuntime,
        true,
        true,
        gatePassed,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        null,
        null,
        null,
        null,
        null,
        gatePassed ? List.of(plannedPatch) : List.of());
  }

  static byte[] fixtureClassBytes(
      String internalName,
      String mainDescriptor,
      boolean includeCode,
      boolean includeStackMapTable,
      boolean includeExceptionTable) {
    try {
      ConstantPoolBuilder constantPool = new ConstantPoolBuilder();
      int thisClassUtf8 = constantPool.addUtf8(internalName);
      int thisClass = constantPool.addClass(thisClassUtf8);
      int objectUtf8 = constantPool.addUtf8("java/lang/Object");
      int objectClass = constantPool.addClass(objectUtf8);
      int initUtf8 = constantPool.addUtf8("<init>");
      int voidDescriptorUtf8 = constantPool.addUtf8("()V");
      int initNameAndType = constantPool.addNameAndType(initUtf8, voidDescriptorUtf8);
      int objectInitMethodref = constantPool.addMethodref(objectClass, initNameAndType);
      int codeUtf8 = constantPool.addUtf8("Code");
      int mainUtf8 = constantPool.addUtf8("main");
      int mainDescriptorUtf8 = constantPool.addUtf8(mainDescriptor);
      int stackMapTableUtf8 = includeStackMapTable ? constantPool.addUtf8("StackMapTable") : -1;

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
        writeMainMethod(
            output,
            mainUtf8,
            mainDescriptorUtf8,
            codeUtf8,
            stackMapTableUtf8,
            includeCode,
            includeStackMapTable,
            includeExceptionTable);

        output.writeShort(0);
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to build fixture class bytes.", exception);
    }
  }

  private static void writeConstructor(
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
    byte[] codeBody = codeAttributeBody(1, 1, code, List.of(), List.of());
    output.writeInt(codeBody.length);
    output.write(codeBody);
  }

  private static void writeMainMethod(
      DataOutputStream output,
      int mainUtf8,
      int mainDescriptorUtf8,
      int codeUtf8,
      int stackMapTableUtf8,
      boolean includeCode,
      boolean includeStackMapTable,
      boolean includeExceptionTable)
      throws IOException {
    output.writeShort(0x0009);
    output.writeShort(mainUtf8);
    output.writeShort(mainDescriptorUtf8);
    if (!includeCode) {
      output.writeShort(0);
      return;
    }
    output.writeShort(1);
    output.writeShort(codeUtf8);
    byte[] code =
        includeExceptionTable ? new byte[] {(byte) 0xb1, (byte) 0xb1} : new byte[] {(byte) 0xb1};
    List<ExceptionTableEntry> exceptionTable =
        includeExceptionTable ? List.of(new ExceptionTableEntry(0, 1, 1, 0)) : List.of();
    List<AttributeBytes> nestedAttributes =
        includeStackMapTable
            ? List.of(new AttributeBytes(stackMapTableUtf8, new byte[] {0x00, 0x00}))
            : List.of();
    byte[] codeBody = codeAttributeBody(0, 1, code, exceptionTable, nestedAttributes);
    output.writeInt(codeBody.length);
    output.write(codeBody);
  }

  private static byte[] codeAttributeBody(
      int maxStack,
      int maxLocals,
      byte[] code,
      List<ExceptionTableEntry> exceptionTable,
      List<AttributeBytes> nestedAttributes)
      throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream output = new DataOutputStream(bytes)) {
      output.writeShort(maxStack);
      output.writeShort(maxLocals);
      output.writeInt(code.length);
      output.write(code);
      output.writeShort(exceptionTable.size());
      for (ExceptionTableEntry entry : exceptionTable) {
        output.writeShort(entry.startPc());
        output.writeShort(entry.endPc());
        output.writeShort(entry.handlerPc());
        output.writeShort(entry.catchType());
      }
      output.writeShort(nestedAttributes.size());
      for (AttributeBytes attribute : nestedAttributes) {
        output.writeShort(attribute.nameIndex());
        output.writeInt(attribute.body().length);
        output.write(attribute.body());
      }
    }
    return bytes.toByteArray();
  }

  static int constantPoolCount(byte[] classBytes) throws IOException {
    try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(classBytes))) {
      input.readInt();
      input.readUnsignedShort();
      input.readUnsignedShort();
      return input.readUnsignedShort();
    }
  }

  static List<ExceptionTableEntry> readMainExceptionTable(byte[] classBytes) throws Exception {
    try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(classBytes))) {
      input.readInt();
      input.readUnsignedShort();
      input.readUnsignedShort();
      int constantPoolCount = input.readUnsignedShort();
      String[] utf8Values = new String[constantPoolCount];
      int[] classNameIndexes = new int[constantPoolCount];
      for (int index = 1; index < constantPoolCount; index++) {
        int tag = input.readUnsignedByte();
        switch (tag) {
          case 1 -> utf8Values[index] = input.readUTF();
          case 3, 4 -> input.readInt();
          case 5, 6 -> {
            input.readLong();
            index++;
          }
          case 7 -> classNameIndexes[index] = input.readUnsignedShort();
          case 8, 16, 19, 20 -> input.readUnsignedShort();
          case 9, 10, 11, 12, 17, 18 -> {
            input.readUnsignedShort();
            input.readUnsignedShort();
          }
          case 15 -> {
            input.readUnsignedByte();
            input.readUnsignedShort();
          }
          default -> throw new IOException("Unsupported constant pool tag " + tag);
        }
      }
      input.readUnsignedShort();
      input.readUnsignedShort();
      input.readUnsignedShort();
      int interfaceCount = input.readUnsignedShort();
      for (int index = 0; index < interfaceCount; index++) {
        input.readUnsignedShort();
      }
      int fieldCount = input.readUnsignedShort();
      for (int index = 0; index < fieldCount; index++) {
        skipMember(input);
      }
      int methodCount = input.readUnsignedShort();
      for (int index = 0; index < methodCount; index++) {
        input.readUnsignedShort();
        String name = utf8Values[input.readUnsignedShort()];
        String descriptor = utf8Values[input.readUnsignedShort()];
        int attributeCount = input.readUnsignedShort();
        for (int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
          String attributeName = utf8Values[input.readUnsignedShort()];
          int attributeLength = input.readInt();
          if ("main".equals(name)
              && TARGET_DESCRIPTOR.equals(descriptor)
              && "Code".equals(attributeName)) {
            input.readUnsignedShort();
            input.readUnsignedShort();
            int codeLength = input.readInt();
            input.skipNBytes(codeLength);
            int exceptionTableCount = input.readUnsignedShort();
            List<ExceptionTableEntry> entries = new ArrayList<>(exceptionTableCount);
            for (int exceptionIndex = 0; exceptionIndex < exceptionTableCount; exceptionIndex++) {
              entries.add(
                  new ExceptionTableEntry(
                      input.readUnsignedShort(),
                      input.readUnsignedShort(),
                      input.readUnsignedShort(),
                      input.readUnsignedShort()));
            }
            return entries;
          }
          input.skipNBytes(attributeLength);
        }
      }
      return List.of();
    }
  }

  private static void skipMember(DataInputStream input) throws IOException {
    input.readUnsignedShort();
    input.readUnsignedShort();
    input.readUnsignedShort();
    int attributeCount = input.readUnsignedShort();
    for (int index = 0; index < attributeCount; index++) {
      input.readUnsignedShort();
      input.skipNBytes(input.readInt());
    }
  }

  private record AttributeBytes(int nameIndex, byte[] body) {}

  static record ExceptionTableEntry(int startPc, int endPc, int handlerPc, int catchType) {}

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

    private int addEntry(byte tag, int value) {
      try {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
          output.writeByte(tag);
          output.writeShort(value);
        }
        entries.add(bytes.toByteArray());
        return entries.size();
      } catch (IOException exception) {
        throw new IllegalStateException("Failed to add constant pool entry.", exception);
      }
    }

    private int addEntry(byte tag, int left, int right) {
      try {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
          output.writeByte(tag);
          output.writeShort(left);
          output.writeShort(right);
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
