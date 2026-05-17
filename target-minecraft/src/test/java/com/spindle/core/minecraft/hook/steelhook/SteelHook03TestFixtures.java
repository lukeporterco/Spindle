package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.verify.SteelHook02CompletionHandoffStatus;
import com.spindle.core.minecraft.hook.verify.SteelHook02CompletionNextDirection;
import com.spindle.core.minecraft.hook.verify.SteelHook02CompletionReport;
import com.spindle.core.minecraft.hook.verify.SteelHook02CompletionStatus;
import com.spindle.core.minecraft.hook.verify.SteelHookCapabilityBoundary;
import com.spindle.core.minecraft.hook.verify.SteelHookSafetyInvariant;
import com.spindle.core.minecraft.hook.verify.SteelHookStageVerification;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class SteelHook03TestFixtures {
  private SteelHook03TestFixtures() {}

  public static byte[] framedFixtureClassBytes() {
    return new SteelHook03FramedMethodFoundationRunner().defaultFramedFixtureClassBytes();
  }

  public static byte[] emptyStackMapFixtureClassBytes() {
    return fixtureClassBytes(new byte[] {0x00, 0x00});
  }

  public static byte[] malformedStackMapFixtureClassBytes() {
    return fixtureClassBytes(new byte[] {0x00, 0x01});
  }

  public static SteelHook02CompletionReport passedCompletionReport() {
    return completionReport(
        SteelHook02CompletionStatus.PASSED,
        SteelHook02CompletionHandoffStatus.STEELHOOK_0_2_COMPLETE,
        true);
  }

  public static SteelHook02CompletionReport failedCompletionReport() {
    return completionReport(
        SteelHook02CompletionStatus.FAILED,
        SteelHook02CompletionHandoffStatus.STEELHOOK_0_2_BLOCKED,
        false);
  }

  public static SteelHook02CompletionReport blockedCompletionReport() {
    return completionReport(
        SteelHook02CompletionStatus.PASSED,
        SteelHook02CompletionHandoffStatus.STEELHOOK_0_2_BLOCKED,
        false);
  }

  private static SteelHook02CompletionReport completionReport(
      SteelHook02CompletionStatus status,
      SteelHook02CompletionHandoffStatus handoffStatus,
      boolean completionReady) {
    return new SteelHook02CompletionReport(
        1,
        "Target-27",
        "minecraft",
        "0.2",
        status,
        handoffStatus,
        completionReady
            ? SteelHook02CompletionNextDirection.MOVE_TO_STEELHOOK_0_3_STACKMAP_AND_EXIT_PRIMITIVES
            : SteelHook02CompletionNextDirection.RESTORE_UPSTREAM_STEELHOOK_0_2_CHAIN,
        completionReady,
        completionReady,
        completionReady ? 0 : 1,
        0,
        1,
        "net.minecraft.server.Main",
        "net/minecraft/server/Main.class",
        "METHOD_ENTRY_STATIC_DISPATCH",
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        List.of(
            new SteelHookStageVerification(
                "target-27",
                "Target-27",
                "fixture",
                completionReady,
                completionReady ? null : "blocked")),
        List.of(
            new SteelHookSafetyInvariant(
                "target-27",
                "true",
                Boolean.toString(completionReady),
                completionReady,
                completionReady ? null : "blocked")),
        List.of(
            new SteelHookCapabilityBoundary(
                "stackmaptable-rewriting",
                "not-supported",
                "SteelHook 0.2 does not support StackMapTable rewriting.")),
        completionReady ? null : "blocked");
  }

  private static byte[] fixtureClassBytes(byte[] stackMapTableBody) {
    try {
      ConstantPoolBuilder constantPool = new ConstantPoolBuilder();
      int thisClassUtf8 = constantPool.addUtf8("com/spindle/steelhook/TestTarget");
      int thisClass = constantPool.addClass(thisClassUtf8);
      int objectUtf8 = constantPool.addUtf8("java/lang/Object");
      int objectClass = constantPool.addClass(objectUtf8);
      int initUtf8 = constantPool.addUtf8("<init>");
      int voidDescriptorUtf8 = constantPool.addUtf8("()V");
      int initNameAndType = constantPool.addNameAndType(initUtf8, voidDescriptorUtf8);
      int objectInitMethodref = constantPool.addMethodref(objectClass, initNameAndType);
      int codeUtf8 = constantPool.addUtf8("Code");
      int mainUtf8 = constantPool.addUtf8("main");
      int mainDescriptorUtf8 = constantPool.addUtf8("([Ljava/lang/String;)V");
      int stackMapTableUtf8 = constantPool.addUtf8("StackMapTable");

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
            output, mainUtf8, mainDescriptorUtf8, codeUtf8, stackMapTableUtf8, stackMapTableBody);

        output.writeShort(0);
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Failed to build SteelHook 0.3 test fixture bytes.", exception);
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
      byte[] stackMapTableBody)
      throws IOException {
    output.writeShort(0x0009);
    output.writeShort(mainUtf8);
    output.writeShort(mainDescriptorUtf8);
    output.writeShort(1);
    output.writeShort(codeUtf8);
    byte[] code = new byte[] {(byte) 0xb1};
    byte[] codeBody =
        codeAttributeBody(
            0, 1, code, List.of(new AttributeBytes(stackMapTableUtf8, stackMapTableBody)));
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
      return addConstantPoolEntry((byte) 7, nameIndex);
    }

    private int addNameAndType(int nameIndex, int descriptorIndex) {
      return addConstantPoolEntry((byte) 12, nameIndex, descriptorIndex);
    }

    private int addMethodref(int classIndex, int nameAndTypeIndex) {
      return addConstantPoolEntry((byte) 10, classIndex, nameAndTypeIndex);
    }

    private int addConstantPoolEntry(byte tag, int... values) {
      try {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
          output.writeByte(tag);
          for (int value : values) {
            output.writeShort(value);
          }
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
