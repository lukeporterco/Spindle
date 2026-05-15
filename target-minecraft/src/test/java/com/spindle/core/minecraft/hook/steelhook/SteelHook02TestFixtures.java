package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.MinecraftRuntimeFile;
import com.spindle.core.minecraft.MinecraftServerRuntimeClasspath;
import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchEligibility;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchKind;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchMode;
import com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class SteelHook02TestFixtures {
  private SteelHook02TestFixtures() {}

  public static SteelHook02ContractGeneralizationAnalysis validContractGeneralizationAnalysis() {
    return new SteelHook02ContractGeneralizationAnalysis(
        1,
        "Target-24",
        "minecraft",
        "0.2",
        "26.1.2",
        MinecraftSide.SERVER,
        "Target-7",
        "Target-23",
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
        true,
        true,
        false,
        true,
        false,
        true,
        null,
        SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_READY,
        SteelHook02ContractGeneralizationNextDirection
            .MOVE_TO_TARGET_25_RUNTIME_SAFE_METHOD_ENTRY_TRANSFORMER,
        "Use Target-25.",
        validTargetDescriptor(),
        validDispatcherDescriptor(),
        validPrimitiveContract(),
        validGeneralizedPatchPlan(),
        List.of());
  }

  public static SteelHook02TargetDescriptor validTargetDescriptor() {
    return new SteelHook02TargetDescriptor(
        "target-24.steelhook-0-2.target.001",
        "net/minecraft/server/Main",
        "net.minecraft.server.Main",
        "net/minecraft/server/Main.class",
        "main",
        "([Ljava/lang/String;)V",
        MinecraftSide.SERVER,
        "26.1.2",
        "minecraft.26_1_2.server.main.entrypoint",
        "target-5.minecraft.server.main.method-entry-placement",
        "target-7.minecraft.server.main.method-entry-dispatch-patch",
        0,
        true);
  }

  public static SteelHook02DispatcherDescriptor validDispatcherDescriptor() {
    return new SteelHook02DispatcherDescriptor(
        "target-24.steelhook-0-2.dispatcher.001",
        "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
        "com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher",
        "beforeMinecraftServerMain",
        "()V",
        "invokestatic",
        "b8",
        3,
        0,
        0,
        true,
        false);
  }

  public static SteelHook02PrimitiveContract validPrimitiveContract() {
    return new SteelHook02PrimitiveContract(
        "target-24.steelhook-0-2.primitive-contract.001",
        SteelHook02PrimitiveKind.METHOD_ENTRY_STATIC_DISPATCH,
        "target-23.steelhook-0-2.candidate.001",
        "target-24.steelhook-0-2.target.001",
        "target-24.steelhook-0-2.dispatcher.001",
        MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH,
        MinecraftHookPatchMode.STEELHOOK_0_2_CONTRACT_GENERALIZED_STATIC_DISPATCH_INVOKESTATIC,
        MinecraftHookPatchEligibility.STEELHOOK_0_2_CONTRACT_READY_RUNTIME_CANDIDATE,
        "method-entry-offset-zero-only",
        true,
        true,
        false,
        false,
        false);
  }

  public static SteelHook02GeneralizedPatchPlan validGeneralizedPatchPlan() {
    return new SteelHook02GeneralizedPatchPlan(
        "target-24.steelhook-0-2.generalized-patch-plan.001",
        "Target-7",
        "target-7.minecraft.server.main.method-entry-dispatch-patch",
        "target-23.steelhook-0-2.candidate.001",
        "target-24.steelhook-0-2.target.001",
        "target-24.steelhook-0-2.dispatcher.001",
        MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH,
        MinecraftHookPatchMode.STEELHOOK_0_2_CONTRACT_GENERALIZED_STATIC_DISPATCH_INVOKESTATIC,
        MinecraftHookPatchEligibility.STEELHOOK_0_2_CONTRACT_READY_RUNTIME_CANDIDATE,
        6,
        true,
        true,
        false,
        false,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        false,
        true,
        false,
        List.of("Target-25 performs offline-only verification."));
  }

  public static byte[] readResourceBytes(String resourceName) throws IOException {
    try (var inputStream =
        SteelHook02TestFixtures.class.getClassLoader().getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + resourceName);
      }
      return inputStream.readAllBytes();
    }
  }

  public static SteelHook02MethodEntryTransformerResult validMethodEntryTransformerResult()
      throws IOException {
    SteelHook02TargetClassBytes targetClassBytes =
        new SteelHook02TargetClassBytes(
            validTargetDescriptor().classEntryName(),
            "memory:/net/minecraft/server/Main.class",
            "test-resource",
            null,
            readResourceBytes("net/minecraft/server/Main.class"),
            true,
            true,
            null);
    SteelHook02MethodEntryTransformerResult base =
        new SteelHook02MethodEntryTransformer()
            .transform(validContractGeneralizationAnalysis(), targetClassBytes.classBytes());
    return new SteelHook02MethodEntryTransformerResult(
        base.schema(),
        base.milestoneName(),
        base.target(),
        base.steelHookVersion(),
        base.sourcePatchPlanMilestone(),
        base.sourcePrimitiveBoundaryMilestone(),
        base.sourceContractGeneralizationMilestone(),
        base.localTransformationOnly(),
        base.runtimeClassLoadingPathEnabled(),
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
        targetClassBytes,
        base.findings());
  }

  public static MinecraftServerRuntimePlan runtimePlan(Path serverJar) {
    return new MinecraftServerRuntimePlan(
        1,
        "Milestone-Execution",
        "25",
        "26.1.2",
        "26.1.2",
        "test",
        "test",
        "local",
        "local",
        serverJar.toAbsolutePath().normalize().toString(),
        "test",
        "server-sha1",
        "server-sha256",
        1L,
        "dry-run",
        "test",
        "net.minecraft.server.Main",
        List.<MinecraftServerRuntimeClasspath.Entry>of(),
        List.<MinecraftRuntimeFile>of(),
        List.<String>of(),
        List.<String>of(),
        serverJar.getParent().toAbsolutePath().normalize().toString(),
        "java",
        List.of("java", "-jar", serverJar.toString()),
        serverJar.getParent().resolve("cache").toString(),
        serverJar.getParent().resolve("runtime-cache").toString(),
        true,
        true,
        0,
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
        null);
  }

  public static Path createRuntimeJar(Path jarPath, byte[] mainClassBytes) throws IOException {
    Files.createDirectories(jarPath.getParent());
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "net.minecraft.server.Main");
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jar = new JarOutputStream(outputStream, manifest)) {
      for (Map.Entry<String, byte[]> entry :
          Map.of("net/minecraft/server/Main.class", mainClassBytes).entrySet()) {
        jar.putNextEntry(new JarEntry(entry.getKey()));
        jar.write(entry.getValue());
        jar.closeEntry();
      }
    }
    return jarPath;
  }

  public static byte[] fixtureClassBytes(
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

  public static void resetDispatcher() {
    SteelHookDispatcher.resetForBootstrap();
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
