package com.spindle.core.minecraft.hook.place;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftModExecutionPlan;
import com.spindle.core.minecraft.MinecraftRuntimeFile;
import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import com.spindle.core.minecraft.hook.MinecraftHookContractReport;
import com.spindle.core.minecraft.hook.MinecraftHookContractResult;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MinecraftHookPlacementPlannerTest {
  @TempDir Path tempDirectory;

  private final MinecraftHookPlacementPlanner planner = new MinecraftHookPlacementPlanner();

  @Test
  void validGatePlansSingleMethodEntryPlacement() throws Exception {
    Path serverJar =
        createJar(
            tempDirectory.resolve("server.jar"),
            readResourceBytes("net/minecraft/server/Main.class"),
            "net/minecraft/server/Main.class");

    MinecraftHookPlacementPlan plan =
        planner.plan(
            validContractReport(),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan(serverJar));

    assertTrue(plan.gatePassed());
    assertTrue(plan.placementPlanned());
    assertEquals(1, plan.plannedPlacementCount());
    assertEquals("Target-5", plan.milestoneName());
    assertTrue(plan.codeInspectionOccurred());
    assertTrue(plan.codeAttributeParsed());
    assertEquals(
        "target-5.minecraft.server.main.method-entry-placement",
        plan.plannedPlacements().getFirst().id());
    assertEquals(0, plan.plannedPlacements().getFirst().bytecodeOffset());
  }

  @Test
  void failedTargetThreeValidationFailsGate() throws Exception {
    Path serverJar =
        createJar(
            tempDirectory.resolve("server-invalid.jar"),
            readResourceBytes("net/minecraft/server/Main.class"),
            "net/minecraft/server/Main.class");

    MinecraftHookPlacementPlan plan =
        planner.plan(
            contractReport(
                "minecraft-26.1.2-server-known-symbols",
                false,
                1,
                List.of(validEntrypointContract())),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan(serverJar));

    assertFalse(plan.gatePassed());
    assertFalse(plan.placementPlanned());
    assertTrue(plan.gateFailureReason().contains("validation failed"));
  }

  @Test
  void unsupportedCatalogFailsGate() throws Exception {
    Path serverJar =
        createJar(
            tempDirectory.resolve("server-unsupported.jar"),
            readResourceBytes("net/minecraft/server/Main.class"),
            "net/minecraft/server/Main.class");

    MinecraftHookPlacementPlan plan =
        planner.plan(
            contractReport("unsupported-catalog", true, 0, List.of(validEntrypointContract())),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan(serverJar));

    assertFalse(plan.gatePassed());
    assertTrue(plan.gateFailureReason().contains("Unsupported hook contract catalog"));
  }

  @Test
  void missingEntrypointContractFailsGate() throws Exception {
    Path serverJar =
        createJar(
            tempDirectory.resolve("server-missing-contract.jar"),
            readResourceBytes("net/minecraft/server/Main.class"),
            "net/minecraft/server/Main.class");

    MinecraftHookPlacementPlan plan =
        planner.plan(
            contractReport("minecraft-26.1.2-server-known-symbols", true, 0, List.of()),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan(serverJar));

    assertFalse(plan.gatePassed());
    assertTrue(plan.gateFailureReason().contains("missing or invalid"));
  }

  @Test
  void executionMainClassMismatchFailsGate() throws Exception {
    Path serverJar =
        createJar(
            tempDirectory.resolve("server-main-mismatch.jar"),
            readResourceBytes("net/minecraft/server/Main.class"),
            "net/minecraft/server/Main.class");

    MinecraftHookPlacementPlan plan =
        planner.plan(
            validContractReport(), executionPlan("com.example.NotMain"), runtimePlan(serverJar));

    assertFalse(plan.gatePassed());
    assertTrue(plan.gateFailureReason().contains("main class"));
  }

  @Test
  void missingClassFileFailsGateWithoutThrowing() throws Exception {
    Path serverJar =
        createJar(
            tempDirectory.resolve("server-no-main.jar"),
            readResourceBytes(
                "com/spindle/core/minecraft/hook/place/MinecraftMethodCodeReaderTest$NoCodeMain.class"),
            "com/example/Other.class");

    MinecraftHookPlacementPlan plan =
        planner.plan(
            validContractReport(),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan(serverJar));

    assertFalse(plan.gatePassed());
    assertFalse(plan.placementPlanned());
    assertEquals(0, plan.plannedPlacementCount());
    assertTrue(plan.gateFailureReason().contains("does not contain"));
  }

  @Test
  void missingCodeAttributeFailsGateWithoutThrowing() throws Exception {
    Path serverJar =
        createJar(
            tempDirectory.resolve("server-no-code.jar"),
            fixtureClassBytes("net/minecraft/server/Main", "([Ljava/lang/String;)V", false),
            "net/minecraft/server/Main.class");

    MinecraftHookPlacementPlan plan =
        planner.plan(
            validContractReport(),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan(serverJar));

    assertFalse(plan.gatePassed());
    assertFalse(plan.codeInspectionOccurred());
    assertFalse(plan.codeAttributeParsed());
    assertTrue(plan.gateFailureReason().contains("no Code attribute"));
    assertNull(plan.plannedPlacements().stream().findFirst().orElse(null));
  }

  private MinecraftHookContractReport validContractReport() {
    return contractReport(
        "minecraft-26.1.2-server-known-symbols", true, 0, List.of(validEntrypointContract()));
  }

  private MinecraftHookContractReport contractReport(
      String catalogId,
      boolean validationPassed,
      int errorCount,
      List<MinecraftHookContractResult> contracts) {
    return new MinecraftHookContractReport(
        2,
        "Target-3",
        "minecraft",
        "26.1.2",
        "server",
        catalogId,
        "catalog",
        "26.1.2",
        "server",
        true,
        false,
        false,
        false,
        false,
        false,
        1,
        "Target-1",
        contracts.size(),
        (int) contracts.stream().filter(MinecraftHookContractResult::valid).count(),
        (int) contracts.stream().filter(contract -> !contract.valid()).count(),
        (int) contracts.stream().filter(MinecraftHookContractResult::required).count(),
        (int) contracts.stream().filter(MinecraftHookContractResult::optional).count(),
        0,
        errorCount,
        validationPassed,
        contracts,
        List.of());
  }

  private MinecraftHookContractResult validEntrypointContract() {
    return new MinecraftHookContractResult(
        "minecraft.26_1_2.server.main.entrypoint",
        "entrypoint",
        "server",
        "METHOD",
        "net/minecraft/server/Main",
        "main",
        "([Ljava/lang/String;)V",
        "REQUIRED",
        "VALID",
        true,
        true,
        false,
        List.of(),
        "net/minecraft/server/Main",
        "main([Ljava/lang/String;)V");
  }

  private MinecraftModExecutionPlan executionPlan(String mainClass) {
    return new MinecraftModExecutionPlan(
        1,
        "Milestone 8",
        "26.1.2",
        "25",
        "server",
        null,
        null,
        null,
        List.of(),
        List.of(),
        List.of(),
        null,
        mainClass,
        List.of(),
        List.of(),
        null,
        null);
  }

  private MinecraftServerRuntimePlan runtimePlan(Path serverJar) {
    return new MinecraftServerRuntimePlan(
        1,
        "Mega-Milestone 7",
        "25",
        "26.1.2",
        "26.1.2",
        "26.1.2",
        "test",
        "local",
        "local",
        serverJar.toAbsolutePath().normalize().toString().replace('\\', '/'),
        "local",
        "sha1",
        "sha256",
        10L,
        "simple-jar",
        "test",
        null,
        List.of(),
        List.<MinecraftRuntimeFile>of(),
        List.of(),
        List.of(),
        ".",
        "java",
        List.of("java"),
        "minecraft-cache",
        "runtime-cache",
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

  private Path createJar(Path jarPath, byte[] classBytes, String entryName) throws IOException {
    Files.createDirectories(jarPath.getParent());
    Manifest manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "net.minecraft.server.Main");
    try (OutputStream outputStream = Files.newOutputStream(jarPath);
        JarOutputStream jar = new JarOutputStream(outputStream, manifest)) {
      jar.putNextEntry(new JarEntry(entryName));
      jar.write(classBytes);
      jar.closeEntry();
    }
    return jarPath;
  }

  private byte[] readResourceBytes(String resourceName) throws IOException {
    try (var inputStream =
        MinecraftHookPlacementPlannerTest.class
            .getClassLoader()
            .getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + resourceName);
      }
      return inputStream.readAllBytes();
    }
  }

  private byte[] fixtureClassBytes(
      String internalName, String mainDescriptor, boolean includeCode) {
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
        writeMainMethod(output, mainUtf8, mainDescriptorUtf8, codeUtf8, includeCode);
        output.writeShort(0);
      }
      return bytes.toByteArray();
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to build fixture class bytes.", exception);
    }
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
    byte[] code =
        new byte[] {
          0x2a,
          (byte) 0xb7,
          (byte) (objectInitMethodref >>> 8),
          (byte) objectInitMethodref,
          (byte) 0xb1
        };
    byte[] codeBody = codeAttributeBody(1, 1, code);
    output.writeInt(codeBody.length);
    output.write(codeBody);
  }

  private void writeMainMethod(
      DataOutputStream output,
      int mainUtf8,
      int mainDescriptorUtf8,
      int codeUtf8,
      boolean includeCode)
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
    byte[] codeBody = codeAttributeBody(0, 1, new byte[] {(byte) 0xb1});
    output.writeInt(codeBody.length);
    output.write(codeBody);
  }

  private byte[] codeAttributeBody(int maxStack, int maxLocals, byte[] code) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream output = new DataOutputStream(bytes)) {
      output.writeShort(maxStack);
      output.writeShort(maxLocals);
      output.writeInt(code.length);
      output.write(code);
      output.writeShort(0);
      output.writeShort(0);
    }
    return bytes.toByteArray();
  }

  private static final class ConstantPoolBuilder {
    private final java.util.List<ConstantPoolEntry> entries = new java.util.ArrayList<>();

    private int addUtf8(String value) {
      entries.add(
          output -> {
            output.writeByte(1);
            output.writeUTF(value);
          });
      return entries.size();
    }

    private int addClass(int nameIndex) {
      entries.add(
          output -> {
            output.writeByte(7);
            output.writeShort(nameIndex);
          });
      return entries.size();
    }

    private int addNameAndType(int nameIndex, int descriptorIndex) {
      entries.add(
          output -> {
            output.writeByte(12);
            output.writeShort(nameIndex);
            output.writeShort(descriptorIndex);
          });
      return entries.size();
    }

    private int addMethodref(int classIndex, int nameAndTypeIndex) {
      entries.add(
          output -> {
            output.writeByte(10);
            output.writeShort(classIndex);
            output.writeShort(nameAndTypeIndex);
          });
      return entries.size();
    }

    private void write(DataOutputStream output) throws IOException {
      output.writeShort(entries.size() + 1);
      for (ConstantPoolEntry entry : entries) {
        entry.write(output);
      }
    }
  }

  @FunctionalInterface
  private interface ConstantPoolEntry {
    void write(DataOutputStream output) throws IOException;
  }
}
