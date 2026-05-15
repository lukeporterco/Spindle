package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationMode;
import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationStatus;
import org.junit.jupiter.api.Test;

class SteelHook02GatedRuntimeClassTransformerTest {
  @Test
  void shouldTransformReturnsTrueOnlyForMinecraftMain() throws Exception {
    SteelHook02GatedRuntimeClassTransformer transformer =
        new SteelHook02GatedRuntimeClassTransformer(
            SteelHook02TestFixtures.validContractGeneralizationAnalysis(),
            SteelHook02TestFixtures.validMethodEntryTransformerResult());

    assertTrue(transformer.shouldTransform("net.minecraft.server.Main"));
    assertFalse(transformer.shouldTransform("com.example.Other"));
  }

  @Test
  void validTarget24AnalysisPlusValidTarget25ResultTransformsTargetBytes() throws Exception {
    SteelHook02MethodEntryTransformerResult target25 =
        SteelHook02TestFixtures.validMethodEntryTransformerResult();
    SteelHook02GatedRuntimeClassTransformer transformer =
        new SteelHook02GatedRuntimeClassTransformer(
            SteelHook02TestFixtures.validContractGeneralizationAnalysis(), target25);

    var result =
        transformer.transform(
            "net.minecraft.server.Main",
            SteelHook02TestFixtures.readResourceBytes("net/minecraft/server/Main.class"));

    assertEquals(MinecraftBootstrapHookTransformationStatus.TRANSFORMED, result.status());
    assertEquals("Target-26", result.milestoneName());
    assertEquals(
        MinecraftBootstrapHookTransformationMode.STEELHOOK_0_2_GATED_RUNTIME_METHOD_ENTRY_TRANSFORM,
        result.transformationMode());
    assertTrue(result.runtimeClassLoaderTransformationEnabled());
    assertFalse(result.bootstrapTransformationEnabled());
    assertFalse(result.fakeServerRuntimeTransformed());
    assertTrue(result.realMinecraftRuntimeTransformed());
    assertEquals(0, result.dispatcherInvocationCount());
    assertFalse(result.dispatcherInvocationObserved());
    assertFalse(result.minecraftMainInvoked());
    assertNotNull(result.transformedClassBytes());
    assertEquals(target25.originalClassSha256(), result.originalClassSha256());
    assertEquals(target25.transformedClassSha256(), result.transformedClassSha256());
  }

  @Test
  void nullTarget25ResultIsRejected() {
    SteelHook02GatedRuntimeClassTransformer transformer =
        new SteelHook02GatedRuntimeClassTransformer(
            SteelHook02TestFixtures.validContractGeneralizationAnalysis(), null);

    var result = transformer.transform("net.minecraft.server.Main", new byte[] {1});

    assertEquals(
        MinecraftBootstrapHookTransformationStatus.PATCH_PLAN_GATE_FAILED, result.status());
  }

  @Test
  void wrongBinaryNameIsRejected() throws Exception {
    SteelHook02GatedRuntimeClassTransformer transformer =
        new SteelHook02GatedRuntimeClassTransformer(
            SteelHook02TestFixtures.validContractGeneralizationAnalysis(),
            SteelHook02TestFixtures.validMethodEntryTransformerResult());

    var result = transformer.transform("com.example.Main", new byte[] {1});

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
  }

  @Test
  void nullOrEmptyClassBytesAreRejected() throws Exception {
    SteelHook02GatedRuntimeClassTransformer transformer =
        new SteelHook02GatedRuntimeClassTransformer(
            SteelHook02TestFixtures.validContractGeneralizationAnalysis(),
            SteelHook02TestFixtures.validMethodEntryTransformerResult());

    assertEquals(
        MinecraftBootstrapHookTransformationStatus.REJECTED,
        transformer.transform("net.minecraft.server.Main", null).status());
    assertEquals(
        MinecraftBootstrapHookTransformationStatus.REJECTED,
        transformer.transform("net.minecraft.server.Main", new byte[0]).status());
  }

  @Test
  void target25RuntimeClassLoadingEnabledIsRejected() throws Exception {
    SteelHook02MethodEntryTransformerResult base =
        SteelHook02TestFixtures.validMethodEntryTransformerResult();
    SteelHook02MethodEntryTransformerResult drifted =
        new SteelHook02MethodEntryTransformerResult(
            base.schema(),
            base.milestoneName(),
            base.target(),
            base.steelHookVersion(),
            base.sourcePatchPlanMilestone(),
            base.sourcePrimitiveBoundaryMilestone(),
            base.sourceContractGeneralizationMilestone(),
            base.localTransformationOnly(),
            true,
            base.classLoadingOccurred(),
            base.hookInstallationOccurred(),
            base.runtimeDispatchOccurred(),
            base.realMinecraftRuntimeTransformed(),
            base.publicApiExposed(),
            base.javaAgentUsed(),
            base.mixinUsed(),
            base.javaModExecutionSandboxed(),
            base.minecraftRuntimeTransformReady(),
            base.target25TransformerExtractionOccurred(),
            base.methodEntryTransformationOccurred(),
            base.bytecodeModified(),
            base.transformedClassBytesProduced(),
            base.eligibleForTarget26GatedRuntimeTransformation(),
            base.gatePassed(),
            base.status(),
            base.nextDirection(),
            base.failureReason(),
            base.originalClassSha256(),
            base.transformedClassSha256(),
            base.originalCodeSha256(),
            base.transformedCodeSha256(),
            base.originalCodeLength(),
            base.transformedCodeLength(),
            base.constantPoolCountBefore(),
            base.constantPoolCountAfter(),
            base.methodrefIndex(),
            base.insertedInstructionHex(),
            base.gate(),
            base.targetDescriptor(),
            base.dispatcherDescriptor(),
            base.primitiveContract(),
            base.generalizedPatchPlan(),
            base.targetClassBytes(),
            base.findings());
    SteelHook02GatedRuntimeClassTransformer transformer =
        new SteelHook02GatedRuntimeClassTransformer(
            SteelHook02TestFixtures.validContractGeneralizationAnalysis(), drifted);

    var result = transformer.transform("net.minecraft.server.Main", new byte[] {1});

    assertEquals(
        MinecraftBootstrapHookTransformationStatus.PATCH_PLAN_GATE_FAILED, result.status());
    assertNull(result.transformedClassBytes());
  }
}
