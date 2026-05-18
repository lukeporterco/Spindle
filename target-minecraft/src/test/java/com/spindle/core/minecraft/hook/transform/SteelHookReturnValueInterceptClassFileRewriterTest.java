package com.spindle.core.minecraft.hook.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.hook.steelhook.SteelHook04PrimitiveKind;
import com.spindle.core.minecraft.hook.steelhook.SteelHook04TestFixtures;
import org.junit.jupiter.api.Test;

class SteelHookReturnValueInterceptClassFileRewriterTest {
  private final SteelHookReturnValueInterceptClassFileRewriter rewriter =
      new SteelHookReturnValueInterceptClassFileRewriter();

  @Test
  void observesPrimitiveIntReturnWithoutModifyingBytes() {
    SteelHookReturnValueInterceptRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                SteelHook04TestFixtures.PRIMITIVE_METHOD_NAME,
                SteelHook04TestFixtures.PRIMITIVE_DESCRIPTOR,
                SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                null,
                null),
            SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(SteelHookReturnValueInterceptRewriteStatus.OBSERVED, result.status());
    assertFalse(result.bytecodeModified());
    assertFalse(result.transformedClassBytesProduced());
    assertEquals("ireturn", result.matchedReturnOpcode());
    assertEquals("bipush", result.matchedProducerOpcode());
    assertEquals(1, result.matchCount());
  }

  @Test
  void replacesPrimitiveIntReturnWithBoundedCodeLengthPreservingPatch() {
    SteelHookReturnValueInterceptRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHookReturnValueInterceptMode.REPLACE_RETURN_VALUE,
                SteelHook04TestFixtures.PRIMITIVE_METHOD_NAME,
                SteelHook04TestFixtures.PRIMITIVE_DESCRIPTOR,
                SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                42,
                null),
            SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(SteelHookReturnValueInterceptRewriteStatus.TRANSFORMED, result.status());
    assertTrue(result.bytecodeModified());
    assertTrue(result.transformedClassBytesProduced());
    assertNotNull(result.transformedClassBytes());
    assertEquals(result.originalCodeLength(), result.transformedCodeLength());
    assertNotEquals(result.originalCodeSha256(), result.transformedCodeSha256());
    assertTrue(result.replacementSummary().contains("7 -> 42"));
  }

  @Test
  void observesReferenceStringReturnWithoutModifyingBytes() {
    SteelHookReturnValueInterceptRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                SteelHook04TestFixtures.REFERENCE_METHOD_NAME,
                SteelHook04TestFixtures.REFERENCE_DESCRIPTOR,
                SteelHookReturnValueInterceptKind.REFERENCE_STRING,
                null,
                null),
            SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(SteelHookReturnValueInterceptRewriteStatus.OBSERVED, result.status());
    assertFalse(result.bytecodeModified());
    assertFalse(result.transformedClassBytesProduced());
    assertEquals("areturn", result.matchedReturnOpcode());
    assertEquals("ldc", result.matchedProducerOpcode());
  }

  @Test
  void replacesReferenceStringReturnWithBoundedCodeLengthPreservingPatch() {
    SteelHookReturnValueInterceptRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHookReturnValueInterceptMode.REPLACE_RETURN_VALUE,
                SteelHook04TestFixtures.REFERENCE_METHOD_NAME,
                SteelHook04TestFixtures.REFERENCE_DESCRIPTOR,
                SteelHookReturnValueInterceptKind.REFERENCE_STRING,
                null,
                SteelHook04TestFixtures.REPLACEMENT_REFERENCE),
            SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(SteelHookReturnValueInterceptRewriteStatus.TRANSFORMED, result.status());
    assertTrue(result.bytecodeModified());
    assertTrue(result.transformedClassBytesProduced());
    assertEquals(result.originalCodeLength(), result.transformedCodeLength());
    assertNotEquals(result.originalClassSha256(), result.transformedClassSha256());
    assertTrue(result.replacementSummary().contains("\"original\" -> replacement"));
  }

  @Test
  void rejectsNullOrMalformedRequest() {
    SteelHookReturnValueInterceptRewriteResult result =
        rewriter.rewrite(null, SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(SteelHookReturnValueInterceptRewriteStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("Malformed intercept request"));
  }

  @Test
  void rejectsPrimitiveKindOtherThanReturnValueIntercept() {
    SteelHookReturnValueInterceptRewriteResult result =
        rewriter.rewrite(
            new SteelHookReturnValueInterceptRewriteRequest(
                "reject-wrong-primitive",
                "test",
                "Target-32",
                "source.json",
                SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                SteelHook04TestFixtures.TARGET_OWNER_INTERNAL_NAME,
                SteelHook04TestFixtures.TARGET_BINARY_NAME,
                SteelHook04TestFixtures.TARGET_CLASS_ENTRY_NAME,
                SteelHook04TestFixtures.PRIMITIVE_METHOD_NAME,
                SteelHook04TestFixtures.PRIMITIVE_DESCRIPTOR,
                SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                null,
                null,
                false,
                false,
                false),
            SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(SteelHookReturnValueInterceptRewriteStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("Unsupported primitive kind"));
  }

  @Test
  void rejectsWrongOwner() {
    SteelHookReturnValueInterceptRewriteResult result =
        rewriter.rewrite(
            new SteelHookReturnValueInterceptRewriteRequest(
                "reject-wrong-owner",
                "test",
                "Target-32",
                "source.json",
                SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
                SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                "wrong/Owner",
                "wrong.Owner",
                "wrong/Owner.class",
                SteelHook04TestFixtures.PRIMITIVE_METHOD_NAME,
                SteelHook04TestFixtures.PRIMITIVE_DESCRIPTOR,
                SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                null,
                null,
                false,
                false,
                false),
            SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(SteelHookReturnValueInterceptRewriteStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("Wrong owner"));
  }

  @Test
  void rejectsWrongMethodName() {
    assertRejected(
        "Wrong method name", "missingMethod", SteelHook04TestFixtures.PRIMITIVE_DESCRIPTOR);
  }

  @Test
  void rejectsWrongDescriptor() {
    assertRejected(
        "Wrong descriptor", SteelHook04TestFixtures.PRIMITIVE_METHOD_NAME, "()Ljava/lang/Object;");
  }

  @Test
  void rejectsVoidReturn() {
    assertRejected(
        "Void return descriptors are unsupported",
        SteelHook04TestFixtures.VOID_METHOD_NAME,
        SteelHook04TestFixtures.VOID_DESCRIPTOR);
  }

  @Test
  void rejectsConstructorTarget() {
    assertRejected("Constructor targets are unsupported", "<init>", "()V");
  }

  @Test
  void rejectsClassInitializerTarget() {
    assertRejected("Class initializer targets are unsupported", "<clinit>", "()V");
  }

  @Test
  void rejectsMultipleReturnOpcodes() {
    assertRejected(
        "Multiple return opcodes are unsupported",
        SteelHook04TestFixtures.MULTIPLE_RETURNS_METHOD_NAME,
        SteelHook04TestFixtures.PRIMITIVE_DESCRIPTOR);
  }

  @Test
  void rejectsBranchingMethod() {
    assertRejected(
        "Branching methods are unsupported",
        SteelHook04TestFixtures.BRANCHING_METHOD_NAME,
        SteelHook04TestFixtures.PRIMITIVE_DESCRIPTOR);
  }

  @Test
  void rejectsSwitchMethod() {
    assertRejected(
        "Switch methods are unsupported",
        SteelHook04TestFixtures.SWITCH_METHOD_NAME,
        SteelHook04TestFixtures.PRIMITIVE_DESCRIPTOR);
  }

  @Test
  void rejectsExceptionTablePresent() {
    assertRejected(
        "Exception table entries are unsupported",
        SteelHook04TestFixtures.EXCEPTION_TABLE_METHOD_NAME,
        SteelHook04TestFixtures.PRIMITIVE_DESCRIPTOR);
  }

  @Test
  void rejectsStackMapTablePresent() {
    assertRejected(
        "StackMapTable attributes are unsupported",
        SteelHook04TestFixtures.STACKMAP_METHOD_NAME,
        SteelHook04TestFixtures.PRIMITIVE_DESCRIPTOR);
  }

  @Test
  void rejectsSynchronizedMethod() {
    assertRejected(
        "Synchronized methods are unsupported",
        SteelHook04TestFixtures.SYNCHRONIZED_METHOD_NAME,
        SteelHook04TestFixtures.PRIMITIVE_DESCRIPTOR);
  }

  @Test
  void rejectsMissingSupportedProducerBeforeReturn() {
    assertRejected(
        "Missing supported producer immediately before return",
        SteelHook04TestFixtures.MISSING_PRODUCER_METHOD_NAME,
        SteelHook04TestFixtures.PRIMITIVE_DESCRIPTOR);
  }

  @Test
  void rejectsReplacementKindThatDoesNotMatchDescriptor() {
    SteelHookReturnValueInterceptRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHookReturnValueInterceptMode.REPLACE_RETURN_VALUE,
                SteelHook04TestFixtures.PRIMITIVE_METHOD_NAME,
                SteelHook04TestFixtures.PRIMITIVE_DESCRIPTOR,
                SteelHookReturnValueInterceptKind.REFERENCE_STRING,
                null,
                SteelHook04TestFixtures.REPLACEMENT_REFERENCE),
            SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(SteelHookReturnValueInterceptRewriteStatus.REJECTED, result.status());
    assertTrue(
        result.failureReason().contains("Replacement kind does not match target descriptor"));
  }

  private void assertRejected(String expectedMessagePart, String methodName, String descriptor) {
    SteelHookReturnValueInterceptRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHookReturnValueInterceptMode.OBSERVE_ONLY,
                methodName,
                descriptor,
                SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
                null,
                null),
            SteelHook04TestFixtures.returnValueInterceptFixtureClassBytes());

    assertEquals(SteelHookReturnValueInterceptRewriteStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains(expectedMessagePart), result.failureReason());
  }

  private SteelHookReturnValueInterceptRewriteRequest request(
      SteelHookReturnValueInterceptMode mode,
      String methodName,
      String descriptor,
      SteelHookReturnValueInterceptKind interceptKind,
      Integer replacementPrimitiveValue,
      String replacementReferenceValue) {
    return new SteelHookReturnValueInterceptRewriteRequest(
        "test-request",
        "test",
        "Target-32",
        "source.json",
        SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
        mode,
        SteelHook04TestFixtures.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04TestFixtures.TARGET_BINARY_NAME,
        SteelHook04TestFixtures.TARGET_CLASS_ENTRY_NAME,
        methodName,
        descriptor,
        interceptKind,
        replacementPrimitiveValue,
        replacementReferenceValue,
        false,
        false,
        false);
  }
}
