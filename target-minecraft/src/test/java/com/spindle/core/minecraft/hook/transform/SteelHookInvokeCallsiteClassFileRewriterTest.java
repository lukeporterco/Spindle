package com.spindle.core.minecraft.hook.transform;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.hook.steelhook.SteelHook04PrimitiveKind;
import com.spindle.core.minecraft.hook.steelhook.SteelHook04TestFixtures;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SteelHookInvokeCallsiteClassFileRewriterTest {
  private final SteelHookInvokeCallsiteClassFileRewriter rewriter =
      new SteelHookInvokeCallsiteClassFileRewriter();

  @Test
  void redirectRewritesTheSingleInvokestaticOriginalValueCallsiteToRedirectedValue() {
    SteelHookInvokeCallsiteRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                SteelHook04TestFixtures.INVOKE_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
                SteelHookInvokeOpcode.INVOKESTATIC,
                SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR),
            SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes());

    assertEquals(SteelHookInvokeCallsiteRewriteStatus.TRANSFORMED, result.status());
    assertTrue(result.bytecodeModified());
    assertTrue(result.transformedClassBytesProduced());
    assertNotNull(result.transformedClassBytes());
    assertEquals("invokestatic", result.matchedInvokeOpcode());
    assertEquals(1, result.matchedCallsiteCount());
  }

  @Test
  void wrapRewritesTheSingleInvokestaticOriginalValueCallsiteToWrappedValue() {
    SteelHookInvokeCallsiteRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHook04PrimitiveKind.INVOKE_WRAP,
                SteelHookInvokeCallsiteRewriteMode.WRAP,
                SteelHook04TestFixtures.INVOKE_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_WRAPPED_METHOD_NAME,
                SteelHookInvokeOpcode.INVOKESTATIC,
                SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR),
            SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes());

    assertEquals(SteelHookInvokeCallsiteRewriteStatus.TRANSFORMED, result.status());
    assertTrue(result.bytecodeModified());
    assertTrue(result.transformedClassBytesProduced());
    assertNotNull(result.transformedClassBytes());
    assertEquals("invokestatic", result.matchedInvokeOpcode());
    assertEquals(1, result.matchedCallsiteCount());
  }

  @Test
  void redirectKeepsCodeLengthUnchanged() {
    SteelHookInvokeCallsiteRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                SteelHook04TestFixtures.INVOKE_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
                SteelHookInvokeOpcode.INVOKESTATIC,
                SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR),
            SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes());

    assertEquals(result.originalCodeLength(), result.transformedCodeLength());
  }

  @Test
  void wrapKeepsCodeLengthUnchanged() {
    SteelHookInvokeCallsiteRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHook04PrimitiveKind.INVOKE_WRAP,
                SteelHookInvokeCallsiteRewriteMode.WRAP,
                SteelHook04TestFixtures.INVOKE_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_WRAPPED_METHOD_NAME,
                SteelHookInvokeOpcode.INVOKESTATIC,
                SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR),
            SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes());

    assertEquals(result.originalCodeLength(), result.transformedCodeLength());
  }

  @Test
  void redirectChangesClassAndCodeHashes() {
    SteelHookInvokeCallsiteRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                SteelHook04TestFixtures.INVOKE_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
                SteelHookInvokeOpcode.INVOKESTATIC,
                SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR),
            SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes());

    assertNotEquals(result.originalClassSha256(), result.transformedClassSha256());
    assertNotEquals(result.originalCodeSha256(), result.transformedCodeSha256());
  }

  @Test
  void wrapChangesClassAndCodeHashes() {
    SteelHookInvokeCallsiteRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHook04PrimitiveKind.INVOKE_WRAP,
                SteelHookInvokeCallsiteRewriteMode.WRAP,
                SteelHook04TestFixtures.INVOKE_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_WRAPPED_METHOD_NAME,
                SteelHookInvokeOpcode.INVOKESTATIC,
                SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR),
            SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes());

    assertNotEquals(result.originalClassSha256(), result.transformedClassSha256());
    assertNotEquals(result.originalCodeSha256(), result.transformedCodeSha256());
  }

  @Test
  void redirectPatchesOnlyTheMethodrefOperand() {
    byte[] originalBytes = SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes();
    SteelHookInvokeCallsiteRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHook04PrimitiveKind.INVOKE_REDIRECT,
                SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                SteelHook04TestFixtures.INVOKE_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
                SteelHookInvokeOpcode.INVOKESTATIC,
                SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR),
            originalBytes);

    byte[] originalCode = methodCode(originalBytes, SteelHook04TestFixtures.INVOKE_METHOD_NAME);
    byte[] transformedCode =
        methodCode(result.transformedClassBytes(), SteelHook04TestFixtures.INVOKE_METHOD_NAME);

    assertEquals((byte) 0xb8, transformedCode[0]);
    assertEquals((byte) 0xac, transformedCode[3]);
    assertArrayEquals(
        new byte[] {originalCode[0], originalCode[3]},
        new byte[] {transformedCode[0], transformedCode[3]});
    assertNotEquals(methodrefOperand(originalCode), methodrefOperand(transformedCode));
  }

  @Test
  void wrapPatchesOnlyTheMethodrefOperand() {
    byte[] originalBytes = SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes();
    SteelHookInvokeCallsiteRewriteResult result =
        rewriter.rewrite(
            request(
                SteelHook04PrimitiveKind.INVOKE_WRAP,
                SteelHookInvokeCallsiteRewriteMode.WRAP,
                SteelHook04TestFixtures.INVOKE_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_WRAPPED_METHOD_NAME,
                SteelHookInvokeOpcode.INVOKESTATIC,
                SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR),
            originalBytes);

    byte[] originalCode = methodCode(originalBytes, SteelHook04TestFixtures.INVOKE_METHOD_NAME);
    byte[] transformedCode =
        methodCode(result.transformedClassBytes(), SteelHook04TestFixtures.INVOKE_METHOD_NAME);

    assertEquals((byte) 0xb8, transformedCode[0]);
    assertEquals((byte) 0xac, transformedCode[3]);
    assertArrayEquals(
        new byte[] {originalCode[0], originalCode[3]},
        new byte[] {transformedCode[0], transformedCode[3]});
    assertNotEquals(methodrefOperand(originalCode), methodrefOperand(transformedCode));
  }

  @Test
  void rejectsNullOrMalformedRequest() {
    SteelHookInvokeCallsiteRewriteResult result =
        rewriter.rewrite(null, SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes());

    assertEquals(SteelHookInvokeCallsiteRewriteStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("Malformed invoke rewrite request"));
  }

  @Test
  void rejectsPrimitiveKindOtherThanInvokeRedirectOrInvokeWrap() {
    SteelHookInvokeCallsiteRewriteResult result =
        rewriter.rewrite(
            new SteelHookInvokeCallsiteRewriteRequest(
                "reject-wrong-primitive",
                "test",
                "Target-33",
                "source.json",
                SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
                SteelHookInvokeCallsiteRewriteMode.REDIRECT,
                SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
                SteelHook04TestFixtures.INVOKE_TARGET_BINARY_NAME,
                SteelHook04TestFixtures.INVOKE_TARGET_CLASS_ENTRY_NAME,
                SteelHook04TestFixtures.INVOKE_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
                SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
                SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
                SteelHookInvokeOpcode.INVOKESTATIC,
                SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
                SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
                SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
                SteelHookInvokeOpcode.INVOKESTATIC,
                false,
                false,
                false),
            SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes());

    assertEquals(SteelHookInvokeCallsiteRewriteStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("Unsupported primitive kind"));
  }

  @Test
  void rejectsWrongTargetOwner() {
    assertRejected("Wrong owner", wrongTargetOwnerRequest());
  }

  @Test
  void rejectsWrongTargetMethodName() {
    assertRejected("Wrong method name", requestForMethod("missingInvokeValue"));
  }

  @Test
  void rejectsWrongTargetDescriptor() {
    assertRejected(
        "Wrong descriptor",
        new SteelHookInvokeCallsiteRewriteRequest(
            "reject-wrong-target-descriptor",
            "test",
            "Target-33",
            "source.json",
            SteelHook04PrimitiveKind.INVOKE_REDIRECT,
            SteelHookInvokeCallsiteRewriteMode.REDIRECT,
            SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
            SteelHook04TestFixtures.INVOKE_TARGET_BINARY_NAME,
            SteelHook04TestFixtures.INVOKE_TARGET_CLASS_ENTRY_NAME,
            SteelHook04TestFixtures.INVOKE_METHOD_NAME,
            "()Ljava/lang/String;",
            SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
            SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
            SteelHookInvokeOpcode.INVOKESTATIC,
            SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
            SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
            SteelHookInvokeOpcode.INVOKESTATIC,
            false,
            false,
            false));
  }

  @Test
  void rejectsWrongInvokeOwner() {
    assertRejected(
        "Wrong invoke owner",
        new SteelHookInvokeCallsiteRewriteRequest(
            "reject-wrong-invoke-owner",
            "test",
            "Target-33",
            "source.json",
            SteelHook04PrimitiveKind.INVOKE_REDIRECT,
            SteelHookInvokeCallsiteRewriteMode.REDIRECT,
            SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
            SteelHook04TestFixtures.INVOKE_TARGET_BINARY_NAME,
            SteelHook04TestFixtures.INVOKE_TARGET_CLASS_ENTRY_NAME,
            SteelHook04TestFixtures.INVOKE_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
            "wrong/Owner",
            SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
            SteelHookInvokeOpcode.INVOKESTATIC,
            SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
            SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
            SteelHookInvokeOpcode.INVOKESTATIC,
            false,
            false,
            false));
  }

  @Test
  void rejectsWrongInvokeName() {
    assertRejected(
        "Wrong invoke name",
        request(
            SteelHook04PrimitiveKind.INVOKE_REDIRECT,
            SteelHookInvokeCallsiteRewriteMode.REDIRECT,
            SteelHook04TestFixtures.INVOKE_METHOD_NAME,
            "missingOriginalValue",
            SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
            SteelHookInvokeOpcode.INVOKESTATIC,
            SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR));
  }

  @Test
  void rejectsWrongInvokeDescriptor() {
    assertRejected(
        "Wrong invoke descriptor",
        request(
            SteelHook04PrimitiveKind.INVOKE_REDIRECT,
            SteelHookInvokeCallsiteRewriteMode.REDIRECT,
            SteelHook04TestFixtures.INVOKE_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
            SteelHookInvokeOpcode.INVOKESTATIC,
            "()Ljava/lang/String;"));
  }

  @Test
  void rejectsWrongInvokeOpcode() {
    assertRejected(
        "Wrong invoke opcode",
        request(
            SteelHook04PrimitiveKind.INVOKE_REDIRECT,
            SteelHookInvokeCallsiteRewriteMode.REDIRECT,
            SteelHook04TestFixtures.INVOKE_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
            SteelHookInvokeOpcode.INVOKEVIRTUAL,
            SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR));
  }

  @Test
  void rejectsNoMatchingCallsite() {
    assertRejected(
        "No matching callsite",
        requestForMethod(SteelHook04TestFixtures.INVOKE_NO_INVOKE_METHOD_NAME));
  }

  @Test
  void rejectsAmbiguousMultipleMatchingCallsites() {
    assertRejected(
        "Ambiguous multiple matching callsites",
        requestForMethod(SteelHook04TestFixtures.INVOKE_AMBIGUOUS_METHOD_NAME));
  }

  @Test
  void rejectsConstructorInvocationTarget() {
    assertRejected(
        "Constructor invocation targets are unsupported",
        requestForMethod(SteelHook04TestFixtures.INVOKE_CONSTRUCTOR_METHOD_NAME));
  }

  @Test
  void rejectsSpecialInvocationTarget() {
    assertRejected(
        "Special invocation targets are unsupported",
        requestForMethod(SteelHook04TestFixtures.INVOKE_SPECIAL_METHOD_NAME));
  }

  @Test
  void rejectsReplacementDescriptorMismatch() {
    assertRejected(
        "Replacement invoke descriptor must match",
        new SteelHookInvokeCallsiteRewriteRequest(
            "reject-replacement-descriptor",
            "test",
            "Target-33",
            "source.json",
            SteelHook04PrimitiveKind.INVOKE_REDIRECT,
            SteelHookInvokeCallsiteRewriteMode.REDIRECT,
            SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
            SteelHook04TestFixtures.INVOKE_TARGET_BINARY_NAME,
            SteelHook04TestFixtures.INVOKE_TARGET_CLASS_ENTRY_NAME,
            SteelHook04TestFixtures.INVOKE_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
            SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
            SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
            SteelHookInvokeOpcode.INVOKESTATIC,
            SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
            SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
            "()Ljava/lang/String;",
            SteelHookInvokeOpcode.INVOKESTATIC,
            false,
            false,
            false));
  }

  @Test
  void rejectsReplacementOpcodeMismatch() {
    assertRejected(
        "Replacement invoke opcode must match",
        new SteelHookInvokeCallsiteRewriteRequest(
            "reject-replacement-opcode",
            "test",
            "Target-33",
            "source.json",
            SteelHook04PrimitiveKind.INVOKE_REDIRECT,
            SteelHookInvokeCallsiteRewriteMode.REDIRECT,
            SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
            SteelHook04TestFixtures.INVOKE_TARGET_BINARY_NAME,
            SteelHook04TestFixtures.INVOKE_TARGET_CLASS_ENTRY_NAME,
            SteelHook04TestFixtures.INVOKE_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
            SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
            SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
            SteelHookInvokeOpcode.INVOKESTATIC,
            SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
            SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
            SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
            SteelHookInvokeOpcode.INVOKEVIRTUAL,
            false,
            false,
            false));
  }

  @Test
  void rejectsBranchingMethod() {
    assertRejected(
        "Branching methods are unsupported",
        requestForMethod(SteelHook04TestFixtures.INVOKE_BRANCHING_METHOD_NAME));
  }

  @Test
  void rejectsSwitchMethod() {
    assertRejected(
        "Switch methods are unsupported",
        requestForMethod(SteelHook04TestFixtures.INVOKE_SWITCH_METHOD_NAME));
  }

  @Test
  void rejectsExceptionTablePresent() {
    assertRejected(
        "Exception table entries are unsupported",
        requestForMethod(SteelHook04TestFixtures.INVOKE_EXCEPTION_TABLE_METHOD_NAME));
  }

  @Test
  void rejectsStackMapTablePresent() {
    assertRejected(
        "StackMapTable attributes are unsupported",
        requestForMethod(SteelHook04TestFixtures.INVOKE_STACKMAP_METHOD_NAME));
  }

  @Test
  void rejectsSynchronizedMethod() {
    assertRejected(
        "Synchronized methods are unsupported",
        requestForMethod(SteelHook04TestFixtures.INVOKE_SYNCHRONIZED_METHOD_NAME));
  }

  private SteelHookInvokeCallsiteRewriteRequest wrongTargetOwnerRequest() {
    return new SteelHookInvokeCallsiteRewriteRequest(
        "reject-wrong-owner",
        "test",
        "Target-33",
        "source.json",
        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
        "wrong/Owner",
        "wrong.Owner",
        "wrong/Owner.class",
        SteelHook04TestFixtures.INVOKE_METHOD_NAME,
        SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
        SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
        SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
        SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
        SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
        SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        false,
        false,
        false);
  }

  private SteelHookInvokeCallsiteRewriteRequest requestForMethod(String methodName) {
    return request(
        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
        methodName,
        SteelHook04TestFixtures.INVOKE_ORIGINAL_METHOD_NAME,
        SteelHook04TestFixtures.INVOKE_REDIRECTED_METHOD_NAME,
        SteelHookInvokeOpcode.INVOKESTATIC,
        SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR);
  }

  private SteelHookInvokeCallsiteRewriteRequest request(
      SteelHook04PrimitiveKind primitiveKind,
      SteelHookInvokeCallsiteRewriteMode rewriteMode,
      String methodName,
      String expectedInvokeName,
      String replacementInvokeName,
      SteelHookInvokeOpcode expectedInvokeOpcode,
      String invokeDescriptor) {
    return new SteelHookInvokeCallsiteRewriteRequest(
        "test-request",
        "test",
        "Target-33",
        "source.json",
        primitiveKind,
        rewriteMode,
        SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
        SteelHook04TestFixtures.INVOKE_TARGET_BINARY_NAME,
        SteelHook04TestFixtures.INVOKE_TARGET_CLASS_ENTRY_NAME,
        methodName,
        SteelHook04TestFixtures.INVOKE_INT_DESCRIPTOR,
        SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
        expectedInvokeName,
        invokeDescriptor,
        expectedInvokeOpcode,
        SteelHook04TestFixtures.INVOKE_TARGET_OWNER_INTERNAL_NAME,
        replacementInvokeName,
        invokeDescriptor,
        expectedInvokeOpcode,
        false,
        false,
        false);
  }

  private void assertRejected(
      String expectedMessagePart, SteelHookInvokeCallsiteRewriteRequest request) {
    SteelHookInvokeCallsiteRewriteResult result =
        rewriter.rewrite(request, SteelHook04TestFixtures.invokeCallsiteFixtureClassBytes());

    assertEquals(SteelHookInvokeCallsiteRewriteStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains(expectedMessagePart), result.failureReason());
  }

  private byte[] methodCode(byte[] classBytes, String methodName) {
    int constantPoolCount = unsignedShort(classBytes, 8);
    int offset = 10;
    String[] utf8 = new String[constantPoolCount];
    int[] classNameIndex = new int[constantPoolCount];
    for (int index = 1; index < constantPoolCount; index++) {
      int tag = classBytes[offset++] & 0xff;
      switch (tag) {
        case 1 -> {
          int length = unsignedShort(classBytes, offset);
          offset += 2;
          utf8[index] = new String(classBytes, offset, length, StandardCharsets.UTF_8);
          offset += length;
        }
        case 7, 8, 16, 19, 20 -> {
          classNameIndex[index] = unsignedShort(classBytes, offset);
          offset += 2;
        }
        case 3, 4 -> offset += 4;
        case 5, 6 -> {
          offset += 8;
          index++;
        }
        case 9, 10, 11, 12, 18 -> offset += 4;
        case 15 -> offset += 3;
        default -> throw new IllegalStateException("Unsupported constant pool tag " + tag);
      }
    }
    offset += 6;
    int interfacesCount = unsignedShort(classBytes, offset);
    offset += 2 + (interfacesCount * 2);
    int fieldsCount = unsignedShort(classBytes, offset);
    offset += 2;
    for (int field = 0; field < fieldsCount; field++) {
      offset += 6;
      int attributesCount = unsignedShort(classBytes, offset);
      offset += 2;
      for (int attribute = 0; attribute < attributesCount; attribute++) {
        offset += 2;
        int length = unsignedInt(classBytes, offset);
        offset += 4 + length;
      }
    }
    int methodsCount = unsignedShort(classBytes, offset);
    offset += 2;
    for (int method = 0; method < methodsCount; method++) {
      offset += 2;
      String name = utf8[unsignedShort(classBytes, offset)];
      offset += 2;
      offset += 2;
      int attributesCount = unsignedShort(classBytes, offset);
      offset += 2;
      for (int attribute = 0; attribute < attributesCount; attribute++) {
        String attributeName = utf8[unsignedShort(classBytes, offset)];
        offset += 2;
        int attributeLength = unsignedInt(classBytes, offset);
        offset += 4;
        if (methodName.equals(name) && "Code".equals(attributeName)) {
          int codeLength = unsignedInt(classBytes, offset + 4);
          int codeStart = offset + 8;
          byte[] code = new byte[codeLength];
          System.arraycopy(classBytes, codeStart, code, 0, codeLength);
          return code;
        }
        offset += attributeLength;
      }
    }
    throw new IllegalStateException("Method not found: " + methodName);
  }

  private int unsignedShort(byte[] bytes, int offset) {
    return ((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff);
  }

  private int unsignedInt(byte[] bytes, int offset) {
    return ((bytes[offset] & 0xff) << 24)
        | ((bytes[offset + 1] & 0xff) << 16)
        | ((bytes[offset + 2] & 0xff) << 8)
        | (bytes[offset + 3] & 0xff);
  }

  private int methodrefOperand(byte[] code) {
    return ((code[1] & 0xff) << 8) | (code[2] & 0xff);
  }
}
