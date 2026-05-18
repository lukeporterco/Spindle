package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationStatus;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SteelHook04GatedRuntimeClassTransformerTest {
  @Test
  void transformsReturnValueInterceptFixtureBytesForRuntimeClassDefinition() {
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(
            SteelHook04RuntimeTransformSpec.returnValueInterceptPrimitiveReplacement());

    var result =
        transformer.transform(
            SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_BINARY_NAME,
            SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.TRANSFORMED, result.status());
    assertNotNull(result.transformedClassBytes());
    assertNotNull(transformer.currentReturnValueInterceptRewriteResult());
    assertNull(transformer.currentInvokeCallsiteRewriteResult());
  }

  @Test
  void transformsInvokeRedirectFixtureBytesForRuntimeClassDefinition() {
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(
            SteelHook04RuntimeTransformSpec.invokeRedirect());

    var result =
        transformer.transform(
            SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
            SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.TRANSFORMED, result.status());
    assertNotNull(result.transformedClassBytes());
    assertNotNull(transformer.currentInvokeCallsiteRewriteResult());
    assertNull(transformer.currentReturnValueInterceptRewriteResult());
  }

  @Test
  void transformsInvokeWrapFixtureBytesForRuntimeClassDefinition() {
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(SteelHook04RuntimeTransformSpec.invokeWrap());

    var result =
        transformer.transform(
            SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
            SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.TRANSFORMED, result.status());
    assertNotNull(result.transformedClassBytes());
    assertNotNull(transformer.currentInvokeCallsiteRewriteResult());
  }

  @Test
  void rejectsNullSpecBeforeTransformedBytesAreProduced() {
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(null);

    var result =
        transformer.transform(
            SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_BINARY_NAME,
            SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
    assertNull(result.transformedClassBytes());
  }

  @Test
  void rejectsNullPrimitiveKindBeforeTransformedBytesAreProduced() {
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(
            SteelHook04RuntimeTransformSpec.unsupportedOrMalformedPlanForRejectionProof());

    var result =
        transformer.transform(
            SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_BINARY_NAME,
            SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
    assertNull(result.transformedClassBytes());
  }

  @Test
  void rejectsUnsupportedOrMalformedPrimitivePlanBeforeTransformedBytesAreProduced() {
    SteelHook04RuntimeTransformSpec spec =
        new SteelHook04RuntimeTransformSpec(
            "bad",
            SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
            "Target-35",
            "bad.json",
            SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
            SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_BINARY_NAME,
            SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
            SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_METHOD_NAME,
            SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_DESCRIPTOR,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            true,
            false,
            false);
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(spec);

    var result =
        transformer.transform(
            SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_BINARY_NAME,
            SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
    assertNull(result.transformedClassBytes());
  }

  @Test
  void rejectsWrongBinaryName() {
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(
            SteelHook04RuntimeTransformSpec.returnValueInterceptPrimitiveReplacement());

    var result =
        transformer.transform(
            "wrong.Name", SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
  }

  @Test
  void rejectsNullOriginalClassBytes() {
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(
            SteelHook04RuntimeTransformSpec.returnValueInterceptPrimitiveReplacement());

    var result =
        transformer.transform(
            SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_BINARY_NAME, null);

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
  }

  @Test
  void rejectsEmptyOriginalClassBytes() {
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(
            SteelHook04RuntimeTransformSpec.returnValueInterceptPrimitiveReplacement());

    var result =
        transformer.transform(
            SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_BINARY_NAME, new byte[0]);

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
  }

  @Test
  void rejectsRuntimeClassLoadingPathEnabledFalse() {
    SteelHook04RuntimeTransformSpec base =
        SteelHook04RuntimeTransformSpec.returnValueInterceptPrimitiveReplacement();
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(
            new SteelHook04RuntimeTransformSpec(
                base.id(),
                base.primitiveKind(),
                base.sourceMilestone(),
                base.sourceReportId(),
                base.targetOwnerInternalName(),
                base.targetBinaryName(),
                base.targetClassEntryName(),
                base.targetMethodName(),
                base.targetDescriptor(),
                base.returnValueInterceptMode(),
                base.returnValueInterceptKind(),
                base.replacementPrimitiveValue(),
                base.replacementReferenceValue(),
                base.invokeRewriteMode(),
                base.expectedInvokeOwnerInternalName(),
                base.expectedInvokeName(),
                base.expectedInvokeDescriptor(),
                base.expectedInvokeOpcode(),
                base.replacementInvokeOwnerInternalName(),
                base.replacementInvokeName(),
                base.replacementInvokeDescriptor(),
                base.replacementInvokeOpcode(),
                false,
                false,
                false));

    var result =
        transformer.transform(
            SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_BINARY_NAME,
            SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
  }

  @Test
  void rejectsPublicApiExposedTrue() {
    SteelHook04RuntimeTransformSpec base = SteelHook04RuntimeTransformSpec.invokeRedirect();
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(
            new SteelHook04RuntimeTransformSpec(
                base.id(),
                base.primitiveKind(),
                base.sourceMilestone(),
                base.sourceReportId(),
                base.targetOwnerInternalName(),
                base.targetBinaryName(),
                base.targetClassEntryName(),
                base.targetMethodName(),
                base.targetDescriptor(),
                base.returnValueInterceptMode(),
                base.returnValueInterceptKind(),
                base.replacementPrimitiveValue(),
                base.replacementReferenceValue(),
                base.invokeRewriteMode(),
                base.expectedInvokeOwnerInternalName(),
                base.expectedInvokeName(),
                base.expectedInvokeDescriptor(),
                base.expectedInvokeOpcode(),
                base.replacementInvokeOwnerInternalName(),
                base.replacementInvokeName(),
                base.replacementInvokeDescriptor(),
                base.replacementInvokeOpcode(),
                true,
                true,
                false));

    var result =
        transformer.transform(
            SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
            SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
  }

  @Test
  void rejectsJavaModExecutionSandboxedTrue() {
    SteelHook04RuntimeTransformSpec base = SteelHook04RuntimeTransformSpec.invokeWrap();
    SteelHook04GatedRuntimeClassTransformer transformer =
        new SteelHook04GatedRuntimeClassTransformer(
            new SteelHook04RuntimeTransformSpec(
                base.id(),
                base.primitiveKind(),
                base.sourceMilestone(),
                base.sourceReportId(),
                base.targetOwnerInternalName(),
                base.targetBinaryName(),
                base.targetClassEntryName(),
                base.targetMethodName(),
                base.targetDescriptor(),
                base.returnValueInterceptMode(),
                base.returnValueInterceptKind(),
                base.replacementPrimitiveValue(),
                base.replacementReferenceValue(),
                base.invokeRewriteMode(),
                base.expectedInvokeOwnerInternalName(),
                base.expectedInvokeName(),
                base.expectedInvokeDescriptor(),
                base.expectedInvokeOpcode(),
                base.replacementInvokeOwnerInternalName(),
                base.replacementInvokeName(),
                base.replacementInvokeDescriptor(),
                base.replacementInvokeOpcode(),
                true,
                false,
                true));

    var result =
        transformer.transform(
            SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
            SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
  }

  @Test
  void doesNotExposeRawTransformedBytesThroughReportModels() {
    assertFalse(hasByteArrayRecordComponent(SteelHook04RuntimePrimitiveProof.class));
    assertFalse(hasByteArrayRecordComponent(SteelHook04GatedRuntimeProofReport.class));
  }

  private boolean hasByteArrayRecordComponent(Class<?> type) {
    return Arrays.stream(type.getRecordComponents())
        .map(RecordComponent::getType)
        .anyMatch(componentType -> componentType == byte[].class);
  }
}
