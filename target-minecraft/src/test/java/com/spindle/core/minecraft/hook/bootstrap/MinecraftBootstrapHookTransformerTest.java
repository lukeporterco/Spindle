package com.spindle.core.minecraft.hook.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchKind;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchMode;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlan;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchPlanner;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftBootstrapHookTransformerTest {
  private static final String TARGET_CLASS = "net/minecraft/server/Main";
  private static final String TARGET_METHOD = "main";
  private static final String TARGET_DESCRIPTOR = "([Ljava/lang/String;)V";
  private static final Gson GSON = new Gson();

  @Test
  void validTargetSevenPatchTransformsFakeServerMain() {
    MinecraftBootstrapHookTransformer transformer =
        new MinecraftBootstrapHookTransformer(validPatchPlan());

    MinecraftBootstrapHookTransformationResult result =
        transformer.transform(
            "net.minecraft.server.Main",
            fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, false));

    assertEquals(MinecraftBootstrapHookTransformationStatus.TRANSFORMED, result.status());
    assertTrue(result.gate().passed());
    assertTrue(result.bootstrapTransformationEnabled());
    assertTrue(result.runtimeClassLoaderTransformationEnabled());
    assertTrue(result.fakeServerRuntimeTransformed());
    assertFalse(result.realMinecraftRuntimeTransformed());
    assertTrue(result.transformationOccurred());
    assertTrue(result.patchingOccurred());
    assertTrue(result.bytecodeModified());
    assertEquals(
        "target-7.minecraft.server.main.method-entry-dispatch-patch", result.sourcePatchId());
    assertEquals("net.minecraft.server.Main", result.targetBinaryName());
    assertEquals("net/minecraft/server/Main", result.targetInternalName());
    assertEquals("bootstrap-fake-server-only", result.scope());
    assertEquals("bootstrap-fake-server-method-entry-transform", result.transformationMode().id());
  }

  @Test
  void invalidPatchPlanGateProducesFailedTargetNineResultWithoutThrowing() {
    MinecraftBootstrapHookTransformationResult result =
        new MinecraftBootstrapHookTransformer(
                patchPlan(MinecraftHookPatchPlanner.SUPPORTED_PATCH_ID, false, true, false))
            .transform(
                "net.minecraft.server.Main",
                fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, false));

    assertEquals(
        MinecraftBootstrapHookTransformationStatus.PATCH_PLAN_GATE_FAILED, result.status());
    assertFalse(result.gate().passed());
    assertFalse(result.transformationOccurred());
    assertNull(result.transformedClassBytes());
  }

  @Test
  void unsupportedPatchIdFailsTargetNineGate() {
    MinecraftBootstrapHookTransformationResult result =
        new MinecraftBootstrapHookTransformer(patchPlan("unsupported", true, true, false))
            .transform(
                "net.minecraft.server.Main",
                fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, false));

    assertEquals(
        MinecraftBootstrapHookTransformationStatus.PATCH_PLAN_GATE_FAILED, result.status());
    assertTrue(result.failureReason().contains("Unsupported Target-7 patch id"));
  }

  @Test
  void transformReadyForMinecraftRuntimeTrueIsRejected() {
    MinecraftBootstrapHookTransformationResult result =
        new MinecraftBootstrapHookTransformer(
                patchPlan(MinecraftHookPatchPlanner.SUPPORTED_PATCH_ID, true, true, true))
            .transform(
                "net.minecraft.server.Main",
                fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, false));

    assertEquals(
        MinecraftBootstrapHookTransformationStatus.PATCH_PLAN_GATE_FAILED, result.status());
    assertTrue(result.failureReason().contains("transformReadyForMinecraftRuntime"));
  }

  @Test
  void transformReadyForFixtureOnlyFalseIsRejected() {
    MinecraftBootstrapHookTransformationResult result =
        new MinecraftBootstrapHookTransformer(
                patchPlan(MinecraftHookPatchPlanner.SUPPORTED_PATCH_ID, true, false, false))
            .transform(
                "net.minecraft.server.Main",
                fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, false));

    assertEquals(
        MinecraftBootstrapHookTransformationStatus.PATCH_PLAN_GATE_FAILED, result.status());
    assertTrue(result.failureReason().contains("transformReadyForFixtureOnly"));
  }

  @Test
  void wrongInternalClassNameIsRejected() {
    MinecraftBootstrapHookTransformationResult result =
        new MinecraftBootstrapHookTransformer(validPatchPlan())
            .transform(
                "net.minecraft.server.Main",
                fixtureClassBytes("com/example/Main", TARGET_DESCRIPTOR, true, false));

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("net/minecraft/server/Main"));
  }

  @Test
  void stackMapTableIsRejectedThroughTargetEightTransformer() {
    MinecraftBootstrapHookTransformationResult result =
        new MinecraftBootstrapHookTransformer(validPatchPlan())
            .transform(
                "net.minecraft.server.Main",
                fixtureClassBytes(TARGET_CLASS, TARGET_DESCRIPTOR, true, true));

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
    assertEquals(
        "Target-8 fixture transformation rejects methods with StackMapTable.",
        result.failureReason());
  }

  static MinecraftHookPatchPlan validPatchPlan() {
    return patchPlan(MinecraftHookPatchPlanner.SUPPORTED_PATCH_ID, true, true, false);
  }

  static MinecraftHookPatchPlan patchPlan(
      String patchId,
      boolean gatePassed,
      boolean transformReadyForFixtureOnly,
      boolean transformReadyForMinecraftRuntime) {
    JsonObject root = new JsonObject();
    root.addProperty("schema", 1);
    root.addProperty("milestoneName", "Target-7");
    root.addProperty("target", "minecraft");
    root.addProperty("minecraftVersion", "26.1.2");
    root.addProperty("side", "server");
    root.addProperty("catalogId", "minecraft-26.1.2-server-known-symbols");
    root.addProperty("sourceContractValidationPassed", true);
    root.addProperty("sourceContractErrorCount", 0);
    root.addProperty("minecraftMainClass", "net.minecraft.server.Main");
    root.addProperty("gatePassed", gatePassed);
    if (gatePassed) {
      root.add("gateFailureReason", null);
    } else {
      root.addProperty("gateFailureReason", "Target-7 hook patch plan gate failed.");
    }
    root.addProperty("patchPlanningSucceeded", gatePassed);
    root.addProperty("patchPlanned", gatePassed);
    root.addProperty("plannedPatchCount", gatePassed ? 1 : 0);
    root.addProperty(
        "patchEligibility", gatePassed ? "FIXTURE_ONLY_FUTURE_TRANSFORM" : "NOT_ELIGIBLE");
    root.addProperty(
        "selectedPlacementId", "target-5.minecraft.server.main.method-entry-placement");
    root.addProperty("selectedBytecodeAnalysisSchema", 1);
    root.addProperty("selectedBytecodeAnalysisMilestone", "Target-6");
    root.addProperty("targetClass", TARGET_CLASS);
    root.addProperty("targetMethod", TARGET_METHOD);
    root.addProperty("targetDescriptor", TARGET_DESCRIPTOR);
    root.addProperty("originalCodeLength", 1);
    root.addProperty("plannedCodeLength", 4);
    root.addProperty("codeLengthDelta", 3);
    root.addProperty("originalCodeSha256", "original-code-sha");
    root.addProperty("insertionOffset", 0);
    root.addProperty("insertionInstructionBoundary", true);
    root.addProperty("insertBeforeOriginalInstructionOffset", 0);
    root.addProperty("insertedInstructionHex", "b8 ?? ??");
    root.add("requiredConstantPoolEntries", new JsonArray());
    root.addProperty("constantPoolRewriteRequired", gatePassed);
    root.addProperty("codeRewriteRequired", gatePassed);
    root.addProperty("maxStackRewriteRequired", false);
    root.addProperty("maxLocalsRewriteRequired", false);
    root.addProperty("exceptionTableRewriteRequired", false);
    root.addProperty("stackMapTableRewriteRequired", false);
    root.addProperty("nestedCodeAttributeRewriteRequired", false);
    root.addProperty("lineNumberTableRewriteRequired", false);
    root.addProperty("localVariableTableRewriteRequired", false);
    root.addProperty("branchOffsetRewriteRequired", false);
    root.addProperty("switchOffsetRewriteRequired", false);
    root.addProperty("transformReadyForFixtureOnly", transformReadyForFixtureOnly);
    root.addProperty("transformReadyForMinecraftRuntime", transformReadyForMinecraftRuntime);
    root.addProperty("codeAttributeParsed", true);
    root.addProperty("instructionInspectionOccurred", true);
    root.addProperty("patchPlanningOccurred", gatePassed);
    root.addProperty("injectionOccurred", false);
    root.addProperty("transformationOccurred", false);
    root.addProperty("patchingOccurred", false);
    root.addProperty("bytecodeModified", false);
    root.addProperty("javaAgentUsed", false);
    root.addProperty("mixinUsed", false);
    root.addProperty("remappingOccurred", false);
    root.addProperty("publicApiExposed", false);
    root.addProperty("javaModExecutionSandboxed", false);
    root.add("branchTargetAdjustmentSummary", null);
    root.add("switchTargetAdjustmentSummary", null);
    root.add("exceptionTableImpact", null);
    root.add("stackMapImpact", null);
    root.add("nestedAttributeImpact", null);
    JsonArray plannedPatches = new JsonArray();
    if (gatePassed) {
      JsonObject patch = new JsonObject();
      patch.addProperty("id", patchId);
      patch.addProperty(
          "sourcePlacementId", "target-5.minecraft.server.main.method-entry-placement");
      patch.addProperty("sourceContractId", "minecraft.26_1_2.server.main.entrypoint");
      patch.addProperty("sourceBytecodeAnalysisMilestone", "Target-6");
      patch.addProperty("catalogId", "minecraft-26.1.2-server-known-symbols");
      patch.addProperty("kind", MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH.name());
      patch.addProperty("mode", MinecraftHookPatchMode.DRY_RUN_STATIC_DISPATCH_INVOKESTATIC.name());
      patch.addProperty("patchEligibility", "FIXTURE_ONLY_FUTURE_TRANSFORM");
      patch.addProperty("ownerInternalName", TARGET_CLASS);
      patch.addProperty("memberName", TARGET_METHOD);
      patch.addProperty("descriptor", TARGET_DESCRIPTOR);
      patch.addProperty("insertionOffset", 0);
      patch.addProperty("required", true);
      JsonObject codeInsertion = new JsonObject();
      codeInsertion.addProperty(
          "dispatcherOwnerInternalName", MinecraftHookPatchPlanner.DISPATCHER_OWNER_INTERNAL_NAME);
      codeInsertion.addProperty(
          "dispatcherMethodName", MinecraftHookPatchPlanner.DISPATCHER_METHOD_NAME);
      codeInsertion.addProperty(
          "dispatcherDescriptor", MinecraftHookPatchPlanner.DISPATCHER_DESCRIPTOR);
      codeInsertion.addProperty("plannedOpcode", "invokestatic");
      codeInsertion.addProperty("plannedOpcodeHex", "b8");
      codeInsertion.addProperty("plannedInstructionLength", 3);
      codeInsertion.addProperty("stackDelta", 0);
      codeInsertion.addProperty("requiredMaxStackIncrease", 0);
      codeInsertion.addProperty("insertedInstructionHex", "b8 ?? ??");
      patch.add("codeInsertion", codeInsertion);
      patch.add("requiredConstantPoolEntries", new JsonArray());
      patch.addProperty("constantPoolRewriteRequired", true);
      patch.addProperty("codeRewriteRequired", true);
      patch.addProperty("maxStackRewriteRequired", false);
      patch.addProperty("maxLocalsRewriteRequired", false);
      patch.addProperty("exceptionTableRewriteRequired", false);
      patch.addProperty("stackMapTableRewriteRequired", false);
      patch.addProperty("nestedCodeAttributeRewriteRequired", false);
      patch.addProperty("lineNumberTableRewriteRequired", false);
      patch.addProperty("localVariableTableRewriteRequired", false);
      patch.addProperty("branchOffsetRewriteRequired", false);
      patch.addProperty("switchOffsetRewriteRequired", false);
      patch.add("branchTargetAdjustmentSummary", null);
      patch.add("switchTargetAdjustmentSummary", null);
      patch.add("exceptionTableImpact", null);
      patch.add("stackMapImpact", null);
      patch.add("nestedAttributeImpact", null);
      patch.addProperty("transformReadyForFixtureOnly", transformReadyForFixtureOnly);
      patch.addProperty("transformReadyForMinecraftRuntime", transformReadyForMinecraftRuntime);
      plannedPatches.add(patch);
    }
    root.add("plannedPatches", plannedPatches);
    return GSON.fromJson(root, MinecraftHookPatchPlan.class);
  }

  static byte[] fixtureClassBytes(
      String internalName,
      String mainDescriptor,
      boolean includeCode,
      boolean includeStackMapTable) {
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
            includeStackMapTable);

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
    byte[] codeBody = codeAttributeBody(1, 1, code, List.of());
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
      boolean includeStackMapTable)
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
    List<AttributeBytes> nestedAttributes =
        includeStackMapTable
            ? List.of(new AttributeBytes(stackMapTableUtf8, new byte[] {0x00, 0x00}))
            : List.of();
    byte[] codeBody = codeAttributeBody(0, 1, new byte[] {(byte) 0xb1}, nestedAttributes);
    output.writeInt(codeBody.length);
    output.write(codeBody);
  }

  private static byte[] codeAttributeBody(
      int maxStack, int maxLocals, byte[] code, List<AttributeBytes> nestedAttributes)
      throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream output = new DataOutputStream(bytes)) {
      output.writeShort(maxStack);
      output.writeShort(maxLocals);
      output.writeInt(code.length);
      output.write(code);
      output.writeShort(0);
      output.writeShort(nestedAttributes.size());
      for (AttributeBytes attribute : nestedAttributes) {
        output.writeShort(attribute.nameIndex());
        output.writeInt(attribute.body().length);
        output.write(attribute.body());
      }
    }
    return bytes.toByteArray();
  }

  private record AttributeBytes(int nameIndex, byte[] body) {}

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
