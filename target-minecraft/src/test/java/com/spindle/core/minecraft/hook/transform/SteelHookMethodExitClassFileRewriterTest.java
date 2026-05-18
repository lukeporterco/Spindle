package com.spindle.core.minecraft.hook.transform;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.hook.place.MinecraftMethodCodeReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SteelHookMethodExitClassFileRewriterTest {
  private static final String TARGET_OWNER = "com/spindle/steelhook/Target29Main";
  private static final String TARGET_METHOD = "target";

  private final SteelHookMethodExitClassFileRewriter rewriter =
      new SteelHookMethodExitClassFileRewriter();
  private final MinecraftMethodCodeReader methodCodeReader = new MinecraftMethodCodeReader();

  @Test
  void voidMethodWithReturnTransformsSuccessfully() throws Exception {
    byte[] classBytes =
        fixtureBuilder().methodDescriptor("()V").code(new byte[] {(byte) 0xb1}).build();
    SteelHookMethodExitRewriteResult result = rewriter.rewrite(validRequest("()V"), classBytes);

    assertEquals(SteelHookMethodExitRewriteStatus.TRANSFORMED, result.status());
    assertTrue(result.methodExitTransformationOccurred());
    assertTrue(result.bytecodeModified());
    assertTrue(result.transformedClassBytesProduced());
    assertNotNull(result.transformedClass());
    assertEquals(1, result.normalReturnOpcodeCount());
    assertEquals(1, result.insertionCount());
  }

  @Test
  void transformedVoidMethodInsertsInvokestaticImmediatelyBeforeReturn() throws Exception {
    byte[] classBytes =
        fixtureBuilder().methodDescriptor("()V").code(new byte[] {(byte) 0xb1}).build();
    SteelHookMethodExitRewriteResult result = rewriter.rewrite(validRequest("()V"), classBytes);

    byte[] transformedCode =
        methodCodeReader
            .readDecodedCode(
                result.transformedClass().classBytes(), TARGET_OWNER, TARGET_METHOD, "()V")
            .code();
    assertEquals(0xb8, transformedCode[0] & 0xFF);
    assertEquals(0xb1, transformedCode[3] & 0xFF);
  }

  @Test
  void intMethodWithIreturnTransformsSuccessfullyAndPreservesIreturnAfterDispatcher()
      throws Exception {
    byte[] classBytes =
        fixtureBuilder()
            .methodDescriptor("()I")
            .maxStack(1)
            .code(new byte[] {0x04, (byte) 0xac})
            .build();
    SteelHookMethodExitRewriteResult result = rewriter.rewrite(validRequest("()I"), classBytes);

    byte[] transformedCode =
        methodCodeReader
            .readDecodedCode(
                result.transformedClass().classBytes(), TARGET_OWNER, TARGET_METHOD, "()I")
            .code();
    assertEquals(0x04, transformedCode[0] & 0xFF);
    assertEquals(0xb8, transformedCode[1] & 0xFF);
    assertEquals(0xac, transformedCode[4] & 0xFF);
  }

  @Test
  void referenceMethodWithAreturnTransformsSuccessfullyAndPreservesAreturnAfterDispatcher()
      throws Exception {
    byte[] classBytes =
        fixtureBuilder()
            .methodDescriptor("()Ljava/lang/Object;")
            .maxStack(1)
            .code(new byte[] {0x01, (byte) 0xb0})
            .build();
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(validRequest("()Ljava/lang/Object;"), classBytes);

    byte[] transformedCode =
        methodCodeReader
            .readDecodedCode(
                result.transformedClass().classBytes(),
                TARGET_OWNER,
                TARGET_METHOD,
                "()Ljava/lang/Object;")
            .code();
    assertEquals(0x01, transformedCode[0] & 0xFF);
    assertEquals(0xb8, transformedCode[1] & 0xFF);
    assertEquals(0xb0, transformedCode[4] & 0xFF);
  }

  @Test
  void methodWithNoNormalReturnIsRejected() throws Exception {
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(
            validRequest("()V"),
            fixtureBuilder().methodDescriptor("()V").code(new byte[] {0x00}).build());

    assertRejectedWith(result, "requires at least one supported normal return opcode");
  }

  @Test
  void methodWithDescriptorOpcodeMismatchIsRejected() throws Exception {
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(
            validRequest("()I"),
            fixtureBuilder().methodDescriptor("()I").code(new byte[] {(byte) 0xb1}).build());

    assertRejectedWith(result, "does not match method descriptor");
  }

  @Test
  void methodWithStackMapTableIsRejected() throws Exception {
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(
            validRequest("()V"),
            fixtureBuilder()
                .methodDescriptor("()V")
                .code(new byte[] {(byte) 0xb1})
                .stackMapTable(new byte[] {0, 0})
                .build());

    assertRejectedWith(result, "rejects StackMapTable");
    assertTrue(result.stackMapTablePresent());
  }

  @Test
  void methodWithExceptionTableIsRejected() throws Exception {
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(
            validRequest("()V"),
            fixtureBuilder()
                .methodDescriptor("()V")
                .code(new byte[] {(byte) 0xb1})
                .exceptionTableEntry()
                .build());

    assertRejectedWith(result, "rejects exception table entries");
    assertTrue(result.exceptionTablePresent());
  }

  @Test
  void methodWithBranchOpcodeIsRejected() throws Exception {
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(
            validRequest("()V"),
            fixtureBuilder()
                .methodDescriptor("()V")
                .code(new byte[] {(byte) 0xa7, 0x00, 0x00})
                .build());

    assertRejectedWith(result, "rejects branch opcodes");
  }

  @Test
  void methodWithTableswitchIsRejected() throws Exception {
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(
            validRequest("()V"),
            fixtureBuilder().methodDescriptor("()V").code(new byte[] {(byte) 0xaa}).build());

    assertRejectedWith(result, "rejects tableswitch");
  }

  @Test
  void methodWithLookupswitchIsRejected() throws Exception {
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(
            validRequest("()V"),
            fixtureBuilder().methodDescriptor("()V").code(new byte[] {(byte) 0xab}).build());

    assertRejectedWith(result, "rejects lookupswitch");
  }

  @Test
  void methodWithWideIsRejected() throws Exception {
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(
            validRequest("()V"),
            fixtureBuilder().methodDescriptor("()V").code(new byte[] {(byte) 0xc4}).build());

    assertRejectedWith(result, "rejects wide");
  }

  @Test
  void methodWithAthrowIsRejected() throws Exception {
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(
            validRequest("()V"),
            fixtureBuilder().methodDescriptor("()V").code(new byte[] {(byte) 0xbf}).build());

    assertRejectedWith(result, "rejects athrow");
  }

  @Test
  void synchronizedMethodIsRejected() throws Exception {
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(
            validRequest("()V"),
            fixtureBuilder()
                .methodDescriptor("()V")
                .accessFlags(0x0029)
                .code(new byte[] {(byte) 0xb1})
                .build());

    assertRejectedWith(result, "does not support synchronized methods");
  }

  @Test
  void constructorIsRejected() throws Exception {
    byte[] classBytes =
        fixtureBuilder()
            .methodName("<init>")
            .methodDescriptor("()V")
            .code(new byte[] {(byte) 0xb1})
            .build();
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(validRequest("<init>", "()V"), classBytes);

    assertRejectedWith(result, "does not support constructors");
  }

  @Test
  void classInitializerIsRejected() throws Exception {
    byte[] classBytes =
        fixtureBuilder()
            .methodName("<clinit>")
            .methodDescriptor("()V")
            .accessFlags(0x0008)
            .code(new byte[] {(byte) 0xb1})
            .build();
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(validRequest("<clinit>", "()V"), classBytes);

    assertRejectedWith(result, "does not support class initializers");
  }

  @Test
  void malformedClassBytesAreRejectedDeterministically() {
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(validRequest("()V"), new byte[] {0, 1, 2});

    assertRejectedWith(result, "Malformed class file");
  }

  @Test
  void constantPoolOverflowIsRejected() throws Exception {
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(validRequest("()V"), overflowFixtureBuilder().build());

    assertRejectedWith(result, "Constant pool count exceeds the JVM limit");
  }

  @Test
  void codeLengthOverflowIsRejected() throws Exception {
    byte[] hugeCode = new byte[65535];
    java.util.Arrays.fill(hugeCode, 0, 65534, (byte) 0x00);
    hugeCode[65534] = (byte) 0xb1;
    SteelHookMethodExitRewriteResult result =
        rewriter.rewrite(
            validRequest("()V"), fixtureBuilder().methodDescriptor("()V").code(hugeCode).build());

    assertRejectedWith(result, "Code length exceeds the JVM limit");
  }

  @Test
  void rawTransformedBytesAreAvailableOnlyInInternalResultObject() throws Exception {
    byte[] classBytes =
        fixtureBuilder().methodDescriptor("()V").code(new byte[] {(byte) 0xb1}).build();
    byte[] originalSnapshot = classBytes.clone();
    SteelHookMethodExitRewriteResult result = rewriter.rewrite(validRequest("()V"), classBytes);

    assertEquals(SteelHookMethodExitRewriteStatus.TRANSFORMED, result.status());
    assertNotNull(result.transformedClass());
    assertNotNull(result.transformedClass().classBytes());
    assertArrayEquals(originalSnapshot, classBytes);
  }

  private SteelHookMethodExitRewriteRequest validRequest(String descriptor) {
    return validRequest(TARGET_METHOD, descriptor);
  }

  private SteelHookMethodExitRewriteRequest validRequest(String methodName, String descriptor) {
    return new SteelHookMethodExitRewriteRequest(
        "target-29.rewrite-test.001",
        "Target-29 method-exit transformer",
        "Target-28",
        "minecraft-steelhook-0-3-framed-method-foundation.json",
        TARGET_OWNER,
        "com.spindle.steelhook.Target29Main",
        TARGET_OWNER + ".class",
        methodName,
        descriptor,
        "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
        "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher",
        "afterMinecraftServerMain",
        "()V",
        "invokestatic",
        "b8",
        3,
        false,
        false,
        false,
        false);
  }

  private void assertRejectedWith(SteelHookMethodExitRewriteResult result, String fragment) {
    assertEquals(SteelHookMethodExitRewriteStatus.REJECTED, result.status());
    assertFalse(result.methodExitTransformationOccurred());
    assertFalse(result.bytecodeModified());
    assertFalse(result.transformedClassBytesProduced());
    assertNotNull(result.failureReason());
    assertTrue(result.failureReason().contains(fragment), result.failureReason());
  }

  private FixtureBuilder fixtureBuilder() {
    return new FixtureBuilder();
  }

  private FixtureBuilder overflowFixtureBuilder() {
    FixtureBuilder builder = new FixtureBuilder();
    builder.extraUtf8Count = 65520;
    return builder;
  }

  private static final class FixtureBuilder {
    private String methodName = TARGET_METHOD;
    private String methodDescriptor = "()V";
    private int accessFlags = 0x0009;
    private int maxStack = 0;
    private int maxLocals = 1;
    private byte[] code = new byte[] {(byte) 0xb1};
    private byte[] stackMapTable;
    private boolean includeExceptionTable;
    private int extraUtf8Count;

    private FixtureBuilder methodName(String value) {
      methodName = value;
      return this;
    }

    private FixtureBuilder methodDescriptor(String value) {
      methodDescriptor = value;
      return this;
    }

    private FixtureBuilder accessFlags(int value) {
      accessFlags = value;
      return this;
    }

    private FixtureBuilder maxStack(int value) {
      maxStack = value;
      return this;
    }

    private FixtureBuilder code(byte[] value) {
      code = value.clone();
      return this;
    }

    private FixtureBuilder stackMapTable(byte[] value) {
      stackMapTable = value.clone();
      return this;
    }

    private FixtureBuilder exceptionTableEntry() {
      includeExceptionTable = true;
      return this;
    }

    private byte[] build() throws IOException {
      ConstantPoolBuilder constantPool = new ConstantPoolBuilder();
      int thisClassUtf8 = constantPool.addUtf8(TARGET_OWNER);
      int thisClass = constantPool.addClass(thisClassUtf8);
      int objectUtf8 = constantPool.addUtf8("java/lang/Object");
      int objectClass = constantPool.addClass(objectUtf8);
      int initUtf8 = constantPool.addUtf8("<init>");
      int voidDescriptorUtf8 = constantPool.addUtf8("()V");
      int initNameAndType = constantPool.addNameAndType(initUtf8, voidDescriptorUtf8);
      int objectInitMethodref = constantPool.addMethodref(objectClass, initNameAndType);
      int codeUtf8 = constantPool.addUtf8("Code");
      int methodNameUtf8 = constantPool.addUtf8(methodName);
      int methodDescriptorUtf8 = constantPool.addUtf8(methodDescriptor);
      int stackMapTableUtf8 = stackMapTable == null ? -1 : constantPool.addUtf8("StackMapTable");
      for (int index = 0; index < extraUtf8Count; index++) {
        constantPool.addUtf8("x" + index);
      }

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
        output.writeShort("<init>".equals(methodName) ? 1 : 2);
        if (!"<init>".equals(methodName)) {
          writeConstructor(output, initUtf8, voidDescriptorUtf8, codeUtf8, objectInitMethodref);
        }
        writeTargetMethod(
            output, methodNameUtf8, methodDescriptorUtf8, codeUtf8, stackMapTableUtf8);
        output.writeShort(0);
      }
      return bytes.toByteArray();
    }

    private void writeConstructor(
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
      byte[] constructorCode =
          new byte[] {
            0x2a,
            (byte) 0xb7,
            (byte) (objectInitMethodref >>> 8),
            (byte) objectInitMethodref,
            (byte) 0xb1
          };
      byte[] body = codeAttributeBody(1, 1, constructorCode, -1);
      output.writeInt(body.length);
      output.write(body);
    }

    private void writeTargetMethod(
        DataOutputStream output,
        int methodNameUtf8,
        int methodDescriptorUtf8,
        int codeUtf8,
        int stackMapTableUtf8)
        throws IOException {
      output.writeShort(accessFlags);
      output.writeShort(methodNameUtf8);
      output.writeShort(methodDescriptorUtf8);
      output.writeShort(1);
      output.writeShort(codeUtf8);
      byte[] body = codeAttributeBody(maxStack, maxLocals, code, stackMapTableUtf8);
      output.writeInt(body.length);
      output.write(body);
    }

    private byte[] codeAttributeBody(
        int actualMaxStack, int actualMaxLocals, byte[] actualCode, int stackMapTableUtf8)
        throws IOException {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (DataOutputStream output = new DataOutputStream(bytes)) {
        output.writeShort(actualMaxStack);
        output.writeShort(actualMaxLocals);
        output.writeInt(actualCode.length);
        output.write(actualCode);
        output.writeShort(includeExceptionTable ? 1 : 0);
        if (includeExceptionTable) {
          output.writeShort(0);
          output.writeShort(1);
          output.writeShort(1);
          output.writeShort(0);
        }
        output.writeShort(stackMapTableUtf8 > 0 ? 1 : 0);
        if (stackMapTableUtf8 > 0) {
          output.writeShort(stackMapTableUtf8);
          output.writeInt(stackMapTable.length);
          output.write(stackMapTable);
        }
      }
      return bytes.toByteArray();
    }
  }

  private static final class ConstantPoolBuilder {
    private final List<byte[]> entries = new ArrayList<>();

    private int addUtf8(String value) throws IOException {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (DataOutputStream output = new DataOutputStream(bytes)) {
        output.writeByte(1);
        output.writeUTF(value);
      }
      entries.add(bytes.toByteArray());
      return entries.size();
    }

    private int addClass(int nameIndex) throws IOException {
      return addEntry((byte) 7, nameIndex);
    }

    private int addNameAndType(int nameIndex, int descriptorIndex) throws IOException {
      return addEntry((byte) 12, nameIndex, descriptorIndex);
    }

    private int addMethodref(int classIndex, int nameAndTypeIndex) throws IOException {
      return addEntry((byte) 10, classIndex, nameAndTypeIndex);
    }

    private int addEntry(byte tag, int... values) throws IOException {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (DataOutputStream output = new DataOutputStream(bytes)) {
        output.writeByte(tag);
        for (int value : values) {
          output.writeShort(value);
        }
      }
      entries.add(bytes.toByteArray());
      return entries.size();
    }

    private void write(DataOutputStream output) throws IOException {
      output.writeShort(entries.size() + 1);
      for (byte[] entry : entries) {
        output.write(entry);
      }
    }
  }
}
