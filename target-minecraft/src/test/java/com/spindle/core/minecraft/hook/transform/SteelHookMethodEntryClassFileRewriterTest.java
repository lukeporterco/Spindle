package com.spindle.core.minecraft.hook.transform;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.hook.place.MinecraftMethodCodeReader;
import com.spindle.core.minecraft.hook.steelhook.SteelHook02TestFixtures;
import com.spindle.core.minecraft.hook.steelhook.SteelHook03TestFixtures;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class SteelHookMethodEntryClassFileRewriterTest {
  private static final String TARGET_CLASS = "net/minecraft/server/Main";
  private static final String TARGET_METHOD = "main";
  private static final String TARGET_DESCRIPTOR = "([Ljava/lang/String;)V";

  private final SteelHookMethodEntryClassFileRewriter rewriter =
      new SteelHookMethodEntryClassFileRewriter();
  private final MinecraftMethodCodeReader methodCodeReader = new MinecraftMethodCodeReader();

  @Test
  void validMethodEntryRequestTransformsFixtureMainClass() throws Exception {
    byte[] originalClassBytes = readResourceBytes("net/minecraft/server/Main.class");
    byte[] originalSnapshot = originalClassBytes.clone();

    SteelHookMethodEntryRewriteResult first = rewriter.rewrite(validRequest(), originalClassBytes);
    SteelHookMethodEntryRewriteResult second = rewriter.rewrite(validRequest(), originalClassBytes);

    assertEquals(SteelHookMethodEntryRewriteStatus.TRANSFORMED, first.status());
    assertTrue(first.methodEntryTransformationOccurred());
    assertTrue(first.bytecodeModified());
    assertTrue(first.transformedClassBytesProduced());
    assertNotNull(first.transformedClass());
    assertEquals(first.constantPoolCountBefore() + 6, first.constantPoolCountAfter());
    assertEquals(first.constantPoolCountBefore() + 5, first.methodrefIndex());
    assertTrue(first.insertedInstructionHex().startsWith("b8 "));
    assertFalse(first.stackMapTablePresent());
    assertFalse(first.stackMapTableRewriteApplied());
    assertArrayEquals(originalSnapshot, originalClassBytes);
    assertNotEquals(first.originalClassSha256(), first.transformedClassSha256());
    assertEquals(first.originalClassSha256(), second.originalClassSha256());
    assertEquals(first.transformedClassSha256(), second.transformedClassSha256());
    assertArrayEquals(
        first.transformedClass().classBytes(), second.transformedClass().classBytes());

    byte[] transformedMethodCode =
        methodCodeReader
            .readDecodedCode(
                first.transformedClass().classBytes(),
                TARGET_CLASS,
                TARGET_METHOD,
                TARGET_DESCRIPTOR)
            .code();
    assertEquals(0xb8, transformedMethodCode[0] & 0xFF);
    int actualMethodrefIndex =
        ((transformedMethodCode[1] & 0xFF) << 8) | (transformedMethodCode[2] & 0xFF);
    assertEquals(first.methodrefIndex(), actualMethodrefIndex);
  }

  @Test
  void nullEmptyAndMalformedClassBytesAreRejected() {
    assertEquals(
        SteelHookMethodEntryRewriteStatus.REJECTED,
        rewriter.rewrite(validRequest(), null).status());
    assertEquals(
        SteelHookMethodEntryRewriteStatus.REJECTED,
        rewriter.rewrite(validRequest(), new byte[0]).status());
    assertEquals(
        SteelHookMethodEntryRewriteStatus.REJECTED,
        rewriter.rewrite(validRequest(), new byte[] {0, 1, 2}).status());
  }

  @Test
  void wrongClassInternalNameIsRejected() throws Exception {
    SteelHookMethodEntryRewriteRequest wrongRequest =
        new SteelHookMethodEntryRewriteRequest(
            "target-25.rewrite-test.001",
            "Target-25 method-entry transformer",
            "patch",
            "placement",
            "contract",
            "com/example/Main",
            "com.example.Main",
            "com/example/Main.class",
            TARGET_METHOD,
            TARGET_DESCRIPTOR,
            0,
            "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
            "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher",
            "beforeMinecraftServerMain",
            "()V",
            "invokestatic",
            "b8",
            3,
            false,
            false,
            false,
            false);
    SteelHookMethodEntryRewriteResult result =
        rewriter.rewrite(wrongRequest, readResourceBytes("net/minecraft/server/Main.class"));

    assertEquals(SteelHookMethodEntryRewriteStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("Target class internal name mismatch"));
  }

  @Test
  void nonzeroInsertionOffsetIsRejectedBeforeRewrite() throws Exception {
    SteelHookMethodEntryRewriteResult result =
        rewriter.rewrite(
            requestBuilder(validRequest()).insertionOffset(1).build(),
            readResourceBytes("net/minecraft/server/Main.class"));

    assertRejectedWith(result, "insertion offset 0 only");
  }

  @Test
  void nonInvokestaticOpcodeMnemonicIsRejectedBeforeRewrite() throws Exception {
    SteelHookMethodEntryRewriteResult result =
        rewriter.rewrite(
            requestBuilder(validRequest()).opcodeMnemonic("invokevirtual").build(),
            readResourceBytes("net/minecraft/server/Main.class"));

    assertRejectedWith(result, "invokestatic only");
  }

  @Test
  void nonB8OpcodeHexIsRejectedBeforeRewrite() throws Exception {
    SteelHookMethodEntryRewriteResult result =
        rewriter.rewrite(
            requestBuilder(validRequest()).opcodeHex("b6").build(),
            readResourceBytes("net/minecraft/server/Main.class"));

    assertRejectedWith(result, "opcode hex b8 only");
  }

  @Test
  void nonThreeByteInstructionLengthIsRejectedBeforeRewrite() throws Exception {
    SteelHookMethodEntryRewriteResult result =
        rewriter.rewrite(
            requestBuilder(validRequest()).instructionLength(5).build(),
            readResourceBytes("net/minecraft/server/Main.class"));

    assertRejectedWith(result, "instruction length 3 only");
  }

  @Test
  void runtimeClassLoadingPathEnabledRemainsSupportedForTarget26Reuse() throws Exception {
    SteelHookMethodEntryRewriteResult result =
        rewriter.rewrite(
            requestBuilder(validRequest()).runtimeClassLoadingPathEnabled(true).build(),
            readResourceBytes("net/minecraft/server/Main.class"));

    assertEquals(SteelHookMethodEntryRewriteStatus.TRANSFORMED, result.status());
  }

  @Test
  void publicApiExposureIsRejectedBeforeRewrite() throws Exception {
    SteelHookMethodEntryRewriteResult result =
        rewriter.rewrite(
            requestBuilder(validRequest()).publicApiExposed(true).build(),
            readResourceBytes("net/minecraft/server/Main.class"));

    assertRejectedWith(result, "must not expose public API");
  }

  @Test
  void sandboxClaimIsRejectedBeforeRewrite() throws Exception {
    SteelHookMethodEntryRewriteResult result =
        rewriter.rewrite(
            requestBuilder(validRequest()).javaModExecutionSandboxed(true).build(),
            readResourceBytes("net/minecraft/server/Main.class"));

    assertRejectedWith(result, "must not claim Java mod execution sandboxing");
  }

  @Test
  void blankTargetOrDispatcherFieldsAreRejectedBeforeRewrite() throws Exception {
    byte[] classBytes = readResourceBytes("net/minecraft/server/Main.class");
    SteelHookMethodEntryRewriteResult blankTargetOwner =
        rewriter.rewrite(
            requestBuilder(validRequest()).targetOwnerInternalName(" ").build(), classBytes);
    SteelHookMethodEntryRewriteResult blankTargetMethod =
        rewriter.rewrite(requestBuilder(validRequest()).targetMethodName("").build(), classBytes);
    SteelHookMethodEntryRewriteResult blankTargetDescriptor =
        rewriter.rewrite(requestBuilder(validRequest()).targetDescriptor(" ").build(), classBytes);
    SteelHookMethodEntryRewriteResult blankDispatcherOwner =
        rewriter.rewrite(
            requestBuilder(validRequest()).dispatcherOwnerInternalName("").build(), classBytes);
    SteelHookMethodEntryRewriteResult blankDispatcherMethod =
        rewriter.rewrite(
            requestBuilder(validRequest()).dispatcherMethodName(" ").build(), classBytes);
    SteelHookMethodEntryRewriteResult blankDispatcherDescriptor =
        rewriter.rewrite(
            requestBuilder(validRequest()).dispatcherDescriptor("").build(), classBytes);

    assertRejectedWith(blankTargetOwner, "must be nonblank");
    assertRejectedWith(blankTargetMethod, "must be nonblank");
    assertRejectedWith(blankTargetDescriptor, "must be nonblank");
    assertRejectedWith(blankDispatcherOwner, "must be nonblank");
    assertRejectedWith(blankDispatcherMethod, "must be nonblank");
    assertRejectedWith(blankDispatcherDescriptor, "must be nonblank");
  }

  @Test
  void framedMethodWithStackMapTableRewriteSupportedFalseIsRejectedWithExistingFailure() {
    SteelHookMethodEntryRewriteResult result =
        rewriter.rewrite(
            framedRequestBuilder().build(), SteelHook03TestFixtures.framedFixtureClassBytes());

    assertRejectedWith(result, "StackMapTable rewriting is not supported");
    assertTrue(result.stackMapTablePresent());
    assertTrue(result.stackMapTableRejected());
  }

  @Test
  void framedMethodWithStackMapTableRewriteSupportedTrueTransformsSuccessfully() throws Exception {
    SteelHookMethodEntryRewriteResult result =
        rewriter.rewrite(
            framedRequestBuilder().stackMapTableRewriteSupported(true).build(),
            SteelHook03TestFixtures.framedFixtureClassBytes());

    assertEquals(SteelHookMethodEntryRewriteStatus.TRANSFORMED, result.status());
    assertTrue(result.stackMapTablePresent());
    assertTrue(result.stackMapTableRewriteSupported());
    assertTrue(result.stackMapTableRewriteApplied());
    assertEquals(1, result.stackMapTableEntryCountBefore());
    assertEquals(1, result.stackMapTableEntryCountAfter());
    assertEquals(5, result.firstFrameOffsetDeltaBefore());
    assertEquals(8, result.firstFrameOffsetDeltaAfter());

    byte[] transformedMethodCode =
        methodCodeReader
            .readDecodedCode(
                result.transformedClass().classBytes(),
                "com/spindle/steelhook/Target28FramedMain",
                TARGET_METHOD,
                TARGET_DESCRIPTOR)
            .code();
    assertEquals(0xb8, transformedMethodCode[0] & 0xFF);
  }

  @Test
  void emptyStackMapTableIsAcceptedWhenRewriteSupportIsTrueAndReportsRewriteAppliedFalse() {
    SteelHookMethodEntryRewriteResult result =
        rewriter.rewrite(
            requestBuilder(validRequest()).stackMapTableRewriteSupported(true).build(),
            SteelHook02TestFixtures.fixtureClassBytes(
                "net/minecraft/server/Main", TARGET_DESCRIPTOR, true, true));

    assertEquals(SteelHookMethodEntryRewriteStatus.TRANSFORMED, result.status());
    assertTrue(result.stackMapTablePresent());
    assertFalse(result.stackMapTableRewriteApplied());
    assertEquals(0, result.stackMapTableEntryCountBefore());
    assertEquals(0, result.stackMapTableEntryCountAfter());
  }

  @Test
  void malformedStackMapTableIsRejectedDeterministically() {
    SteelHookMethodEntryRewriteResult result =
        rewriter.rewrite(
            requestBuilder(validRequest())
                .targetOwnerInternalName("com/spindle/steelhook/TestTarget")
                .targetBinaryName("com.spindle.steelhook.TestTarget")
                .targetClassEntryName("com/spindle/steelhook/TestTarget.class")
                .stackMapTableRewriteSupported(true)
                .build(),
            SteelHook03TestFixtures.malformedStackMapFixtureClassBytes());

    assertRejectedWith(result, "Malformed StackMapTable");
  }

  private void assertRejectedWith(SteelHookMethodEntryRewriteResult result, String fragment) {
    assertEquals(SteelHookMethodEntryRewriteStatus.REJECTED, result.status());
    assertFalse(result.methodEntryTransformationOccurred());
    assertFalse(result.bytecodeModified());
    assertFalse(result.transformedClassBytesProduced());
    assertNotNull(result.failureReason());
    assertTrue(result.failureReason().contains(fragment));
  }

  private RequestBuilder framedRequestBuilder() {
    return requestBuilder(validRequest())
        .targetOwnerInternalName("com/spindle/steelhook/Target28FramedMain")
        .targetBinaryName("com.spindle.steelhook.Target28FramedMain")
        .targetClassEntryName("com/spindle/steelhook/Target28FramedMain.class");
  }

  private RequestBuilder requestBuilder(SteelHookMethodEntryRewriteRequest request) {
    return new RequestBuilder(request);
  }

  private SteelHookMethodEntryRewriteRequest validRequest() {
    return new SteelHookMethodEntryRewriteRequest(
        "target-25.rewrite-test.001",
        "Target-25 method-entry transformer",
        "target-7.minecraft.server.main.method-entry-dispatch-patch",
        "target-5.minecraft.server.main.method-entry-placement",
        "minecraft.26_1_2.server.main.entrypoint",
        TARGET_CLASS,
        "net.minecraft.server.Main",
        "net/minecraft/server/Main.class",
        TARGET_METHOD,
        TARGET_DESCRIPTOR,
        0,
        "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
        "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher",
        "beforeMinecraftServerMain",
        "()V",
        "invokestatic",
        "b8",
        3,
        false,
        false,
        false,
        false);
  }

  private byte[] readResourceBytes(String resourceName) throws IOException {
    try (var inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + resourceName);
      }
      return inputStream.readAllBytes();
    }
  }

  private static final class RequestBuilder {
    private String id;
    private String scope;
    private String sourcePatchId;
    private String sourcePlacementId;
    private String sourceContractId;
    private String targetOwnerInternalName;
    private String targetBinaryName;
    private String targetClassEntryName;
    private String targetMethodName;
    private String targetDescriptor;
    private int insertionOffset;
    private String dispatcherOwnerInternalName;
    private String dispatcherBinaryName;
    private String dispatcherMethodName;
    private String dispatcherDescriptor;
    private String opcodeMnemonic;
    private String opcodeHex;
    private int instructionLength;
    private boolean stackMapTableRewriteSupported;
    private boolean runtimeClassLoadingPathEnabled;
    private boolean publicApiExposed;
    private boolean javaModExecutionSandboxed;

    private RequestBuilder(SteelHookMethodEntryRewriteRequest request) {
      id = request.id();
      scope = request.scope();
      sourcePatchId = request.sourcePatchId();
      sourcePlacementId = request.sourcePlacementId();
      sourceContractId = request.sourceContractId();
      targetOwnerInternalName = request.targetOwnerInternalName();
      targetBinaryName = request.targetBinaryName();
      targetClassEntryName = request.targetClassEntryName();
      targetMethodName = request.targetMethodName();
      targetDescriptor = request.targetDescriptor();
      insertionOffset = request.insertionOffset();
      dispatcherOwnerInternalName = request.dispatcherOwnerInternalName();
      dispatcherBinaryName = request.dispatcherBinaryName();
      dispatcherMethodName = request.dispatcherMethodName();
      dispatcherDescriptor = request.dispatcherDescriptor();
      opcodeMnemonic = request.opcodeMnemonic();
      opcodeHex = request.opcodeHex();
      instructionLength = request.instructionLength();
      stackMapTableRewriteSupported = request.stackMapTableRewriteSupported();
      runtimeClassLoadingPathEnabled = request.runtimeClassLoadingPathEnabled();
      publicApiExposed = request.publicApiExposed();
      javaModExecutionSandboxed = request.javaModExecutionSandboxed();
    }

    private RequestBuilder targetOwnerInternalName(String value) {
      targetOwnerInternalName = value;
      return this;
    }

    private RequestBuilder targetBinaryName(String value) {
      targetBinaryName = value;
      return this;
    }

    private RequestBuilder targetClassEntryName(String value) {
      targetClassEntryName = value;
      return this;
    }

    private RequestBuilder targetMethodName(String value) {
      targetMethodName = value;
      return this;
    }

    private RequestBuilder targetDescriptor(String value) {
      targetDescriptor = value;
      return this;
    }

    private RequestBuilder insertionOffset(int value) {
      insertionOffset = value;
      return this;
    }

    private RequestBuilder dispatcherOwnerInternalName(String value) {
      dispatcherOwnerInternalName = value;
      return this;
    }

    private RequestBuilder dispatcherMethodName(String value) {
      dispatcherMethodName = value;
      return this;
    }

    private RequestBuilder dispatcherDescriptor(String value) {
      dispatcherDescriptor = value;
      return this;
    }

    private RequestBuilder opcodeMnemonic(String value) {
      opcodeMnemonic = value;
      return this;
    }

    private RequestBuilder opcodeHex(String value) {
      opcodeHex = value;
      return this;
    }

    private RequestBuilder instructionLength(int value) {
      instructionLength = value;
      return this;
    }

    private RequestBuilder stackMapTableRewriteSupported(boolean value) {
      stackMapTableRewriteSupported = value;
      return this;
    }

    private RequestBuilder runtimeClassLoadingPathEnabled(boolean value) {
      runtimeClassLoadingPathEnabled = value;
      return this;
    }

    private RequestBuilder publicApiExposed(boolean value) {
      publicApiExposed = value;
      return this;
    }

    private RequestBuilder javaModExecutionSandboxed(boolean value) {
      javaModExecutionSandboxed = value;
      return this;
    }

    private SteelHookMethodEntryRewriteRequest build() {
      return new SteelHookMethodEntryRewriteRequest(
          id,
          scope,
          sourcePatchId,
          sourcePlacementId,
          sourceContractId,
          targetOwnerInternalName,
          targetBinaryName,
          targetClassEntryName,
          targetMethodName,
          targetDescriptor,
          insertionOffset,
          dispatcherOwnerInternalName,
          dispatcherBinaryName,
          dispatcherMethodName,
          dispatcherDescriptor,
          opcodeMnemonic,
          opcodeHex,
          instructionLength,
          stackMapTableRewriteSupported,
          runtimeClassLoadingPathEnabled,
          publicApiExposed,
          javaModExecutionSandboxed);
    }
  }
}
