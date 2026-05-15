package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook02PrimitiveBoundaryAnalysisWriterTest {
  @TempDir Path tempDirectory;

  private final SteelHook02PrimitiveBoundaryAnalysisWriter writer =
      new SteelHook02PrimitiveBoundaryAnalysisWriter();

  @Test
  void deterministicJsonOutputAcrossTwoPaths() throws Exception {
    SteelHook02PrimitiveBoundaryAnalysis analysis = sampleAnalysis();
    Path first = tempDirectory.resolve("one/minecraft-steelhook-0-2-primitive-boundary.json");
    Path second = tempDirectory.resolve("two/minecraft-steelhook-0-2-primitive-boundary.json");

    writer.write(first, analysis);
    writer.write(second, analysis);

    assertEquals(
        Files.readString(first, StandardCharsets.UTF_8),
        Files.readString(second, StandardCharsets.UTF_8));
  }

  @Test
  void serializesSchemaStatusCountsFindingsAndCandidates() {
    String json = writer.toJson(sampleAnalysis()).toString();

    assertTrue(json.contains("\"schema\":1"));
    assertTrue(json.contains("\"milestoneName\":\"Target-23\""));
    assertTrue(json.contains("\"steelHookVersion\":\"0.2\""));
    assertTrue(json.contains("\"boundaryStatus\":\"PRIMITIVE_BOUNDARY_SELECTED\""));
    assertTrue(
        json.contains(
            "\"nextDirection\":\"MOVE_TO_TARGET_24_CONTRACT_AND_PATCH_PLAN_GENERALIZATION\""));
    assertTrue(json.contains("\"approvedCandidateCount\":1"));
    assertTrue(json.contains("\"id\":\"target-23.steelhook-0-2.primitive.finding.001\""));
    assertTrue(
        json.contains("\"candidateStatus\":\"APPROVED_FOR_TARGET_24_CONTRACT_GENERALIZATION\""));
  }

  @Test
  void serializesNullGateFailureReason() {
    String json = writer.toJson(sampleAnalysis()).toString();

    assertTrue(json.contains("\"gateFailureReason\":null"));
  }

  @Test
  void doesNotSerializeLargeBytecodePayloads() {
    String json = writer.toJson(sampleAnalysis()).toString();

    assertFalse(json.contains("decodedInstructions"));
    assertFalse(json.contains("rawBytecode"));
    assertFalse(json.contains("instructionBytes"));
    assertFalse(json.contains("bytecodePayload"));
  }

  private SteelHook02PrimitiveBoundaryAnalysis sampleAnalysis() {
    return new SteelHook02PrimitiveBoundaryAnalysis(
        1,
        "Target-23",
        "minecraft",
        "0.2",
        "26.1.2",
        MinecraftSide.SERVER,
        "Target-7",
        "Target-10",
        "Target-22",
        true,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        1,
        1,
        0,
        0,
        true,
        null,
        SteelHook02PrimitiveBoundaryStatus.PRIMITIVE_BOUNDARY_SELECTED,
        SteelHook02NextDirection.MOVE_TO_TARGET_24_CONTRACT_AND_PATCH_PLAN_GENERALIZATION,
        "Move next to Target-24 contract and patch-plan generalization for the approved method-entry static-dispatch primitive.",
        List.of(
            new SteelHook02PrimitiveCandidate(
                "target-23.steelhook-0-2.primitive.candidate.001",
                SteelHook02PrimitiveKind.METHOD_ENTRY_STATIC_DISPATCH,
                SteelHook02PrimitiveCandidateStatus.APPROVED_FOR_TARGET_24_CONTRACT_GENERALIZATION,
                "target-7.minecraft.server.main.method-entry-dispatch-patch",
                "net/minecraft/server/Main",
                "main",
                "([Ljava/lang/String;)V",
                0,
                "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
                "beforeMinecraftServerMain",
                "()V",
                true,
                false,
                true,
                true,
                false,
                List.of("Approved only as a Target-24 planning candidate.")),
            new SteelHook02PrimitiveCandidate(
                "target-23.steelhook-0-2.primitive.candidate.002",
                SteelHook02PrimitiveKind.METHOD_ENTRY_STATIC_DISPATCH,
                SteelHook02PrimitiveCandidateStatus.REJECTED_UNSUPPORTED_SHAPE,
                "target-7.minecraft.server.main.method-entry-dispatch-patch",
                "net/minecraft/server/Bootstrap",
                "main",
                "([Ljava/lang/String;)V",
                4,
                "com/spindle/core/minecraft/hook/runtime/SteelHookDispatcher",
                "beforeMinecraftServerMain",
                "()V",
                true,
                false,
                false,
                false,
                false,
                List.of("Rejected due to unsupported shape."))),
        List.of(
            new SteelHook02PrimitiveFinding(
                "target-23.steelhook-0-2.primitive.finding.001",
                "Target-7 patch plan exists.",
                SteelHook02PrimitiveFindingStatus.PASS,
                true,
                "Target-23 received an upstream Target-7 patch plan.",
                "Patch plan input is present."),
            new SteelHook02PrimitiveFinding(
                "target-23.steelhook-0-2.primitive.finding.002",
                "Target-23 remains analysis-only.",
                SteelHook02PrimitiveFindingStatus.PASS,
                true,
                "The upstream patch plan remains analysis-only.",
                "No runtime transformation occurred.")));
  }
}
