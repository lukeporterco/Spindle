package com.spindle.core.minecraft.hook.bytecode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.hook.place.MinecraftMethodCodeReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftInstructionStreamDecoderTest {
  private final MinecraftMethodCodeReader methodCodeReader = new MinecraftMethodCodeReader();
  private final MinecraftInstructionStreamDecoder decoder = new MinecraftInstructionStreamDecoder();

  @Test
  void validFixtureMethodDecodesIntoNonEmptyInstructionList() throws Exception {
    MinecraftDecodedCodeAttribute codeAttribute =
        methodCodeReader.readDecodedCode(
            readResourceBytes("net/minecraft/server/Main.class"),
            "net/minecraft/server/Main",
            "main",
            "([Ljava/lang/String;)V");

    MinecraftInstructionStreamDecoder.Result result = decoder.decode(codeAttribute.code());

    assertTrue(result.instructionBoundaryValidationPassed());
    assertTrue(result.branchTargetValidationPassed());
    assertTrue(result.switchTargetValidationPassed());
    assertFalse(result.instructions().isEmpty());
    assertEquals(0, result.instructions().getFirst().offset());
    assertEquals(
        codeAttribute.codeLength(),
        result.instructions().stream().mapToInt(MinecraftDecodedInstruction::length).sum());
  }

  @Test
  void fixedLengthInstructionsDecodeRepresentativeOperands() throws Exception {
    MinecraftInstructionStreamDecoder.Result result =
        decoder.decode(new byte[] {0x00, 0x10, 0x7f, 0x11, 0x01, 0x02, (byte) 0xb1});

    assertTrue(result.instructionBoundaryValidationPassed());
    assertEquals(List.of("nop", "bipush", "sipush", "return"), mnemonics(result.instructions()));
    assertEquals("", result.instructions().get(0).operandHex());
    assertEquals("7f", result.instructions().get(1).operandHex());
    assertEquals("0102", result.instructions().get(2).operandHex());
  }

  @Test
  void branchInstructionsRecordTargetsAndValidateBoundaries() throws Exception {
    MinecraftInstructionStreamDecoder.Result result =
        decoder.decode(new byte[] {0x03, (byte) 0x99, 0x00, 0x04, 0x04, (byte) 0xac});

    assertTrue(result.instructionBoundaryValidationPassed());
    assertTrue(result.branchTargetValidationPassed());
    assertEquals(1, result.branchInstructionCount());
    assertEquals(List.of(5), result.instructions().get(1).branchTargetOffsets());
  }

  @Test
  void tableSwitchDecodingHandlesPaddingAndTargets() throws Exception {
    byte[] code =
        concat(tableSwitch(24, 1, 2, List.of(24, 25)), new byte[] {(byte) 0xb1, (byte) 0xbf});

    MinecraftInstructionStreamDecoder.Result result = decoder.decode(code);
    MinecraftDecodedInstruction instruction = result.instructions().getFirst();

    assertTrue(result.instructionBoundaryValidationPassed());
    assertTrue(result.switchTargetValidationPassed());
    assertEquals("tableswitch", instruction.mnemonic());
    assertEquals(24, instruction.length());
    assertEquals(24, instruction.switchDefaultTargetOffset());
    assertEquals(2, instruction.switchMatchTargetPairs().size());
    assertEquals(1, instruction.switchMatchTargetPairs().get(0).matchValue());
    assertEquals(24, instruction.switchMatchTargetPairs().get(0).targetOffset());
    assertEquals(2, instruction.switchMatchTargetPairs().get(1).matchValue());
    assertEquals(25, instruction.switchMatchTargetPairs().get(1).targetOffset());
  }

  @Test
  void lookupSwitchDecodingHandlesSortedPairsAndTargets() throws Exception {
    byte[] code =
        concat(
            lookupSwitch(
                28,
                List.of(
                    new MinecraftDecodedBranchTarget(3, 28),
                    new MinecraftDecodedBranchTarget(7, 29))),
            new byte[] {(byte) 0xb1, (byte) 0xbf});

    MinecraftInstructionStreamDecoder.Result result = decoder.decode(code);
    MinecraftDecodedInstruction instruction = result.instructions().getFirst();

    assertTrue(result.instructionBoundaryValidationPassed());
    assertTrue(result.switchTargetValidationPassed());
    assertEquals("lookupswitch", instruction.mnemonic());
    assertEquals(28, instruction.length());
    assertEquals(28, instruction.switchDefaultTargetOffset());
    assertEquals(2, instruction.switchMatchTargetPairs().size());
    assertEquals(3, instruction.switchMatchTargetPairs().get(0).matchValue());
    assertEquals(28, instruction.switchMatchTargetPairs().get(0).targetOffset());
    assertEquals(7, instruction.switchMatchTargetPairs().get(1).matchValue());
    assertEquals(29, instruction.switchMatchTargetPairs().get(1).targetOffset());
  }

  @Test
  void wideInstructionsDecodeWidenedOperands() throws Exception {
    MinecraftInstructionStreamDecoder.Result result =
        decoder.decode(
            new byte[] {
              (byte) 0xc4,
              0x15,
              0x01,
              0x02,
              (byte) 0xc4,
              (byte) 0x84,
              0x01,
              0x03,
              0x00,
              0x04,
              (byte) 0xb1
            });

    assertTrue(result.instructionBoundaryValidationPassed());
    assertEquals(2, result.wideInstructionCount());
    assertEquals(Integer.valueOf(0x15), result.instructions().get(0).wideModifiedOpcode());
    assertEquals(Integer.valueOf(0x84), result.instructions().get(1).wideModifiedOpcode());
  }

  @Test
  void malformedTruncatedInstructionFailsBoundaryValidationDeterministically() throws Exception {
    MinecraftInstructionStreamDecoder.Result result = decoder.decode(new byte[] {0x10});

    assertFalse(result.instructionBoundaryValidationPassed());
    assertEquals(1, result.instructions().size());
    assertEquals(1, result.instructions().getFirst().length());
    assertNotNull(result.validationFailureReason());
  }

  @Test
  void branchTargetIntoMiddleOfInstructionFailsValidation() throws Exception {
    MinecraftInstructionStreamDecoder.Result result =
        decoder.decode(new byte[] {0x03, (byte) 0x99, 0x00, 0x04, 0x10, 0x7f, (byte) 0xac});

    assertTrue(result.instructionBoundaryValidationPassed());
    assertFalse(result.branchTargetValidationPassed());
  }

  @Test
  void switchTargetIntoMiddleOfInstructionFailsValidation() throws Exception {
    byte[] code =
        concat(tableSwitch(25, 1, 1, 1, List.of(24)), new byte[] {0x10, 0x7f, (byte) 0xb1});

    MinecraftInstructionStreamDecoder.Result result = decoder.decode(code);

    assertTrue(result.instructionBoundaryValidationPassed());
    assertFalse(result.switchTargetValidationPassed());
  }

  @Test
  void reservedOpcodeIsReportedAndFailsValidation() throws Exception {
    MinecraftInstructionStreamDecoder.Result result = decoder.decode(new byte[] {(byte) 0xca});

    assertFalse(result.instructionBoundaryValidationPassed());
    assertEquals(1, result.reservedOpcodeCount());
    assertEquals("breakpoint", result.instructions().getFirst().mnemonic());
  }

  private List<String> mnemonics(List<MinecraftDecodedInstruction> instructions) {
    return instructions.stream().map(MinecraftDecodedInstruction::mnemonic).toList();
  }

  private byte[] readResourceBytes(String resourceName) throws IOException {
    try (var inputStream =
        MinecraftInstructionStreamDecoderTest.class
            .getClassLoader()
            .getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + resourceName);
      }
      return inputStream.readAllBytes();
    }
  }

  private byte[] tableSwitch(int defaultTargetOffset, int low, int high, List<Integer> caseTargets)
      throws IOException {
    return tableSwitch(defaultTargetOffset, low, high, caseTargets.size(), caseTargets);
  }

  private byte[] tableSwitch(
      int defaultTargetOffset, int low, int high, int expectedCaseCount, List<Integer> caseTargets)
      throws IOException {
    assertEquals(expectedCaseCount, caseTargets.size());
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream output = new DataOutputStream(bytes)) {
      output.writeByte(0xaa);
      output.write(new byte[] {0x00, 0x00, 0x00});
      output.writeInt(defaultTargetOffset);
      output.writeInt(low);
      output.writeInt(high);
      for (Integer caseTarget : caseTargets) {
        output.writeInt(caseTarget);
      }
    }
    return bytes.toByteArray();
  }

  private byte[] lookupSwitch(int defaultTargetOffset, List<MinecraftDecodedBranchTarget> pairs)
      throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (DataOutputStream output = new DataOutputStream(bytes)) {
      output.writeByte(0xab);
      output.write(new byte[] {0x00, 0x00, 0x00});
      output.writeInt(defaultTargetOffset);
      output.writeInt(pairs.size());
      for (MinecraftDecodedBranchTarget pair : pairs) {
        output.writeInt(pair.matchValue());
        output.writeInt(pair.targetOffset());
      }
    }
    return bytes.toByteArray();
  }

  private byte[] concat(byte[] left, byte[] right) {
    byte[] combined = new byte[left.length + right.length];
    System.arraycopy(left, 0, combined, 0, left.length);
    System.arraycopy(right, 0, combined, left.length, right.length);
    return combined;
  }
}
