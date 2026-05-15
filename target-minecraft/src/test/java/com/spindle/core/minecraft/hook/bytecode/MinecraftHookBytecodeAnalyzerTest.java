package com.spindle.core.minecraft.hook.bytecode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftModExecutionPlan;
import com.spindle.core.minecraft.MinecraftRuntimeFile;
import com.spindle.core.minecraft.MinecraftServerRuntimePlan;
import com.spindle.core.minecraft.hook.MinecraftHookContractReport;
import com.spindle.core.minecraft.hook.MinecraftHookContractResult;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementKind;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementMode;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementPlan;
import com.spindle.core.minecraft.hook.place.MinecraftHookPlacementPlanner;
import com.spindle.core.minecraft.hook.place.MinecraftMethodCodeReader;
import com.spindle.core.minecraft.hook.place.MinecraftMethodCodeSummary;
import com.spindle.core.minecraft.hook.place.MinecraftPlannedHookPlacement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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

class MinecraftHookBytecodeAnalyzerTest {
  @TempDir Path tempDirectory;

  private final MinecraftHookBytecodeAnalyzer analyzer = new MinecraftHookBytecodeAnalyzer();
  private final MinecraftHookPlacementPlanner placementPlanner =
      new MinecraftHookPlacementPlanner();
  private final MinecraftMethodCodeReader methodCodeReader = new MinecraftMethodCodeReader();

  @Test
  void validTargetFivePlacementProducesSuccessfulTargetSixReport() throws Exception {
    Path serverJar =
        createJar(
            tempDirectory.resolve("server.jar"),
            readResourceBytes("net/minecraft/server/Main.class"),
            "net/minecraft/server/Main.class");
    MinecraftHookPlacementPlan placementPlan =
        placementPlanner.plan(
            validContractReport(),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan(serverJar));

    MinecraftHookBytecodeAnalysisReport report =
        analyzer.analyze(
            placementPlan, executionPlan("net.minecraft.server.Main"), runtimePlan(serverJar));

    assertTrue(report.gatePassed());
    assertTrue(report.bytecodeAnalysisSucceeded());
    assertTrue(report.codeAttributeParsed());
    assertTrue(report.instructionStreamDecoded());
    assertTrue(report.exceptionTableValidationPassed());
    assertFalse(report.decodedInstructions().isEmpty());
    assertEquals(0, report.firstInstructionOffset());
  }

  @Test
  void targetFivePlacementGateFailureProducesFailedReportWithoutThrowing() throws Exception {
    MinecraftHookBytecodeAnalysisReport report =
        analyzer.analyze(
            new MinecraftHookPlacementPlan(
                1,
                "Target-5",
                "minecraft",
                "26.1.2",
                "server",
                "minecraft-26.1.2-server-known-symbols",
                false,
                1,
                "net.minecraft.server.Main",
                false,
                "Target-5 hook placement gate failed.",
                false,
                0,
                List.of(),
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false),
            executionPlan("net.minecraft.server.Main"),
            null);

    assertFalse(report.gatePassed());
    assertFalse(report.bytecodeAnalysisSucceeded());
    assertTrue(report.gateFailureReason().contains("Target-5"));
  }

  @Test
  void unsupportedPlacementIdFailsTargetSixGate() throws Exception {
    MinecraftMethodCodeSummary summary =
        methodCodeReader.read(
            readResourceBytes("net/minecraft/server/Main.class"),
            "net/minecraft/server/Main",
            "main",
            "([Ljava/lang/String;)V");

    MinecraftHookBytecodeAnalysisReport report =
        analyzer.analyze(
            new MinecraftHookPlacementPlan(
                1,
                "Target-5",
                "minecraft",
                "26.1.2",
                "server",
                "minecraft-26.1.2-server-known-symbols",
                true,
                0,
                "net.minecraft.server.Main",
                true,
                null,
                true,
                1,
                List.of(
                    new MinecraftPlannedHookPlacement(
                        "unsupported-placement",
                        "minecraft.26_1_2.server.main.entrypoint",
                        "minecraft-26.1.2-server-known-symbols",
                        MinecraftHookPlacementKind.METHOD_ENTRY,
                        "net/minecraft/server/Main",
                        "main",
                        "([Ljava/lang/String;)V",
                        0,
                        MinecraftHookPlacementMode.METHOD_ENTRY_ANALYSIS_ONLY,
                        true,
                        summary)),
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
                false,
                false),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan(
                createJar(
                    tempDirectory.resolve("server-unsupported.jar"),
                    readResourceBytes("net/minecraft/server/Main.class"),
                    "net/minecraft/server/Main.class")));

    assertFalse(report.gatePassed());
    assertTrue(report.gateFailureReason().contains("Unsupported hook placement id"));
  }

  @Test
  void exceptionHandlerIntoMiddleOfInstructionFailsValidation() throws Exception {
    Path validServerJar =
        createJar(
            tempDirectory.resolve("server-valid-exception.jar"),
            readResourceBytes("net/minecraft/server/Main.class"),
            "net/minecraft/server/Main.class");
    MinecraftHookPlacementPlan validPlacementPlan =
        placementPlanner.plan(
            validContractReport(),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan(validServerJar));
    MinecraftHookBytecodeAnalysisReport validReport =
        analyzer.analyze(
            validPlacementPlan,
            executionPlan("net.minecraft.server.Main"),
            runtimePlan(validServerJar));
    int invalidHandlerPc = findNonInstructionBoundary(validReport);

    Path serverJar =
        createJar(
            tempDirectory.resolve("server-invalid-exception.jar"),
            withInvalidHandlerPc(
                readResourceBytes("net/minecraft/server/Main.class"), invalidHandlerPc),
            "net/minecraft/server/Main.class");
    MinecraftHookPlacementPlan placementPlan =
        placementPlanner.plan(
            validContractReport(),
            executionPlan("net.minecraft.server.Main"),
            runtimePlan(serverJar));

    MinecraftHookBytecodeAnalysisReport report =
        analyzer.analyze(
            placementPlan, executionPlan("net.minecraft.server.Main"), runtimePlan(serverJar));

    assertFalse(report.gatePassed());
    assertFalse(report.exceptionTableValidationPassed());
    assertNotNull(report.gateFailureReason());
  }

  private int findNonInstructionBoundary(MinecraftHookBytecodeAnalysisReport report) {
    for (MinecraftDecodedInstruction instruction : report.decodedInstructions()) {
      for (int offset = instruction.offset() + 1;
          offset < instruction.offset() + instruction.length();
          offset++) {
        return offset;
      }
    }
    throw new IllegalStateException(
        "Expected at least one non-boundary bytecode offset in fixture method.");
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
        MinecraftHookBytecodeAnalyzerTest.class
            .getClassLoader()
            .getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + resourceName);
      }
      return inputStream.readAllBytes();
    }
  }

  private byte[] withInvalidHandlerPc(byte[] classBytes, int invalidHandlerPc) throws IOException {
    try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(classBytes));
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(outputBytes)) {
      output.writeInt(input.readInt());
      output.writeShort(input.readUnsignedShort());
      output.writeShort(input.readUnsignedShort());
      int constantPoolCount = input.readUnsignedShort();
      output.writeShort(constantPoolCount);
      String[] utf8Values = new String[constantPoolCount];
      for (int index = 1; index < constantPoolCount; index++) {
        int tag = input.readUnsignedByte();
        output.writeByte(tag);
        switch (tag) {
          case 1 -> {
            String value = input.readUTF();
            utf8Values[index] = value;
            output.writeUTF(value);
          }
          case 3 -> output.writeInt(input.readInt());
          case 4 -> output.writeFloat(input.readFloat());
          case 5 -> {
            output.writeLong(input.readLong());
            index++;
          }
          case 6 -> {
            output.writeDouble(input.readDouble());
            index++;
          }
          case 7, 8, 16, 19, 20 -> output.writeShort(input.readUnsignedShort());
          case 9, 10, 11, 12, 17, 18 -> {
            output.writeShort(input.readUnsignedShort());
            output.writeShort(input.readUnsignedShort());
          }
          case 15 -> {
            output.writeByte(input.readUnsignedByte());
            output.writeShort(input.readUnsignedShort());
          }
          default -> throw new IOException("Unsupported constant pool tag " + tag);
        }
      }

      copyUnsignedShort(input, output);
      copyUnsignedShort(input, output);
      copyUnsignedShort(input, output);
      int interfaceCount = input.readUnsignedShort();
      output.writeShort(interfaceCount);
      for (int index = 0; index < interfaceCount; index++) {
        copyUnsignedShort(input, output);
      }

      int fieldCount = input.readUnsignedShort();
      output.writeShort(fieldCount);
      for (int index = 0; index < fieldCount; index++) {
        copyMember(input, output);
      }

      int methodCount = input.readUnsignedShort();
      output.writeShort(methodCount);
      for (int index = 0; index < methodCount; index++) {
        int accessFlags = input.readUnsignedShort();
        int nameIndex = input.readUnsignedShort();
        int descriptorIndex = input.readUnsignedShort();
        int attributeCount = input.readUnsignedShort();
        output.writeShort(accessFlags);
        output.writeShort(nameIndex);
        output.writeShort(descriptorIndex);
        output.writeShort(attributeCount);
        boolean targetMethod =
            "main".equals(utf8Values[nameIndex])
                && "([Ljava/lang/String;)V".equals(utf8Values[descriptorIndex]);
        for (int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
          int attributeNameIndex = input.readUnsignedShort();
          String attributeName = utf8Values[attributeNameIndex];
          int attributeLength = input.readInt();
          byte[] body = input.readNBytes(attributeLength);
          if (targetMethod && "Code".equals(attributeName)) {
            body = mutateHandlerPc(body, invalidHandlerPc);
            attributeLength = body.length;
          }
          output.writeShort(attributeNameIndex);
          output.writeInt(attributeLength);
          output.write(body);
        }
      }

      int classAttributeCount = input.readUnsignedShort();
      output.writeShort(classAttributeCount);
      for (int index = 0; index < classAttributeCount; index++) {
        copyAttribute(input, output);
      }
      return outputBytes.toByteArray();
    }
  }

  private byte[] mutateHandlerPc(byte[] codeAttributeBody, int invalidHandlerPc)
      throws IOException {
    try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(codeAttributeBody));
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(outputBytes)) {
      output.writeShort(input.readUnsignedShort());
      output.writeShort(input.readUnsignedShort());
      int codeLength = input.readInt();
      output.writeInt(codeLength);
      output.write(input.readNBytes(codeLength));
      int exceptionTableCount = input.readUnsignedShort();
      if (exceptionTableCount == 0) {
        output.writeShort(1);
        output.writeShort(0);
        output.writeShort(codeLength);
        output.writeShort(invalidHandlerPc);
        output.writeShort(0);
      } else {
        output.writeShort(exceptionTableCount);
        for (int index = 0; index < exceptionTableCount; index++) {
          int startPc = input.readUnsignedShort();
          int endPc = input.readUnsignedShort();
          int handlerPc = input.readUnsignedShort();
          int catchType = input.readUnsignedShort();
          output.writeShort(startPc);
          output.writeShort(endPc);
          output.writeShort(index == 0 ? invalidHandlerPc : handlerPc);
          output.writeShort(catchType);
        }
      }
      output.write(input.readAllBytes());
      return outputBytes.toByteArray();
    }
  }

  private void copyMember(DataInputStream input, DataOutputStream output) throws IOException {
    copyUnsignedShort(input, output);
    copyUnsignedShort(input, output);
    copyUnsignedShort(input, output);
    int attributeCount = input.readUnsignedShort();
    output.writeShort(attributeCount);
    for (int attributeIndex = 0; attributeIndex < attributeCount; attributeIndex++) {
      copyAttribute(input, output);
    }
  }

  private void copyAttribute(DataInputStream input, DataOutputStream output) throws IOException {
    copyUnsignedShort(input, output);
    int attributeLength = input.readInt();
    output.writeInt(attributeLength);
    output.write(input.readNBytes(attributeLength));
  }

  private void copyUnsignedShort(DataInputStream input, DataOutputStream output)
      throws IOException {
    output.writeShort(input.readUnsignedShort());
  }
}
