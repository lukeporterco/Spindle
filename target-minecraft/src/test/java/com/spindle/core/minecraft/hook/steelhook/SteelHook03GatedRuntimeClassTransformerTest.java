package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationStatus;
import org.junit.jupiter.api.Test;

class SteelHook03GatedRuntimeClassTransformerTest {
  @Test
  void methodEntrySpecTransformsFramedFixtureBytesWithStackMapRewriteSupport() {
    SteelHook03GatedRuntimeClassTransformer transformer =
        new SteelHook03GatedRuntimeClassTransformer(
            SteelHook03RuntimeTransformSpec.methodEntryStaticDispatch());

    var result =
        transformer.transform(
            "net.minecraft.server.Main",
            SteelHook03TestFixtures.runtimeFramedMainFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.TRANSFORMED, result.status());
    assertEquals(
        "steelhook-0-3-gated-runtime-method-entry-transform", result.transformationMode().id());
    assertNotNull(result.transformedClassBytes());
    assertTrue(transformer.currentMethodEntryRewriteResult().stackMapTableRewriteSupported());
    assertTrue(transformer.currentMethodEntryRewriteResult().stackMapTableRewriteApplied());
    assertTrue(transformer.currentMethodEntryRewriteResult().methodEntryTransformationOccurred());
    assertNull(transformer.currentMethodExitRewriteResult());
    assertSameCurrentResult(transformer, result);
  }

  @Test
  void methodEntrySpecRejectsNullClassBytes() {
    SteelHook03GatedRuntimeClassTransformer transformer =
        new SteelHook03GatedRuntimeClassTransformer(
            SteelHook03RuntimeTransformSpec.methodEntryStaticDispatch());

    var result = transformer.transform("net.minecraft.server.Main", null);

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("requires runtime class bytes"));
    assertSameCurrentResult(transformer, result);
  }

  @Test
  void methodEntrySpecRejectsBinaryNameMismatch() {
    SteelHook03GatedRuntimeClassTransformer transformer =
        new SteelHook03GatedRuntimeClassTransformer(
            SteelHook03RuntimeTransformSpec.methodEntryStaticDispatch());

    var result =
        transformer.transform(
            "com.example.OtherMain", SteelHook03TestFixtures.runtimeFramedMainFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("supports only net.minecraft.server.Main"));
    assertSameCurrentResult(transformer, result);
  }

  @Test
  void methodExitSpecTransformsUnframedMethodExitFixtureBytes() {
    SteelHook03GatedRuntimeClassTransformer transformer =
        new SteelHook03GatedRuntimeClassTransformer(
            SteelHook03RuntimeTransformSpec.methodExitStaticDispatch());

    var result =
        transformer.transform(
            "net.minecraft.server.Main", SteelHook03TestFixtures.methodExitFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.TRANSFORMED, result.status());
    assertEquals(
        "steelhook-0-3-gated-runtime-method-exit-transform", result.transformationMode().id());
    assertNotNull(result.transformedClassBytes());
    assertNull(transformer.currentMethodEntryRewriteResult());
    assertFalse(transformer.currentMethodExitRewriteResult().stackMapTableRewriteSupported());
    assertTrue(transformer.currentMethodExitRewriteResult().methodExitTransformationOccurred());
    assertSameCurrentResult(transformer, result);
  }

  @Test
  void methodExitSpecRejectsFramedMethodExitFixtureBytes() {
    SteelHook03GatedRuntimeClassTransformer transformer =
        new SteelHook03GatedRuntimeClassTransformer(
            SteelHook03RuntimeTransformSpec.methodExitStaticDispatch());

    var result =
        transformer.transform(
            "net.minecraft.server.Main",
            SteelHook03TestFixtures.runtimeFramedMainFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("StackMapTable"));
    assertNotNull(transformer.currentMethodExitRewriteResult());
    assertSameCurrentResult(transformer, result);
  }

  @Test
  void methodExitSpecRejectsBinaryNameMismatch() {
    SteelHook03GatedRuntimeClassTransformer transformer =
        new SteelHook03GatedRuntimeClassTransformer(
            SteelHook03RuntimeTransformSpec.methodExitStaticDispatch());

    var result =
        transformer.transform(
            "com.example.OtherMain", SteelHook03TestFixtures.methodExitFixtureClassBytes());

    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("supports only net.minecraft.server.Main"));
    assertSameCurrentResult(transformer, result);
  }

  @Test
  void unsupportedPrimitiveKindOrNullSpecIsRejectedDeterministically() {
    SteelHook03GatedRuntimeClassTransformer nullSpecTransformer =
        new SteelHook03GatedRuntimeClassTransformer(null);
    var nullSpecResult =
        nullSpecTransformer.transform(
            "net.minecraft.server.Main", SteelHook03TestFixtures.methodExitFixtureClassBytes());
    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, nullSpecResult.status());
    assertTrue(nullSpecResult.failureReason().contains("requires a runtime transform spec"));

    SteelHook03RuntimeTransformSpec unsupportedSpec =
        new SteelHook03RuntimeTransformSpec(
            null,
            "Target-29",
            "net.minecraft.server.Main",
            "net/minecraft/server/Main",
            "net/minecraft/server/Main.class",
            "main",
            "([Ljava/lang/String;)V",
            "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
            "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher",
            "afterMinecraftServerMain",
            "()V",
            "invokestatic",
            "b8",
            3,
            null,
            false,
            true,
            false,
            false);
    SteelHook03GatedRuntimeClassTransformer unsupportedTransformer =
        new SteelHook03GatedRuntimeClassTransformer(unsupportedSpec);
    var unsupportedResult =
        unsupportedTransformer.transform(
            "net.minecraft.server.Main", SteelHook03TestFixtures.methodExitFixtureClassBytes());
    assertEquals(MinecraftBootstrapHookTransformationStatus.REJECTED, unsupportedResult.status());
    assertTrue(unsupportedResult.failureReason().contains("approved SteelHook 0.3 primitive kind"));
  }

  @Test
  void transformedBytesExistOnlyInInternalTransformationResult() {
    SteelHook03GatedRuntimeClassTransformer transformer =
        new SteelHook03GatedRuntimeClassTransformer(
            SteelHook03RuntimeTransformSpec.methodExitStaticDispatch());

    var result =
        transformer.transform(
            "net.minecraft.server.Main", SteelHook03TestFixtures.methodExitFixtureClassBytes());

    assertNotNull(result.transformedClassBytes());
    assertNotNull(transformer.currentResult().transformedClassBytes());
  }

  private void assertSameCurrentResult(
      SteelHook03GatedRuntimeClassTransformer transformer,
      com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationResult result) {
    assertNotNull(transformer.currentResult());
    assertEquals(result.status(), transformer.currentResult().status());
    assertEquals(result.failureReason(), transformer.currentResult().failureReason());
  }
}
