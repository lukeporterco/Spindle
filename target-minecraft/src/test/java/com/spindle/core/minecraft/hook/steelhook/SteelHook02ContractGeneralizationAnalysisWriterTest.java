package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchEligibility;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchKind;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook02ContractGeneralizationAnalysisWriterTest {
  @TempDir Path tempDirectory;

  private final SteelHook02ContractGeneralizationAnalysisWriter writer =
      new SteelHook02ContractGeneralizationAnalysisWriter();

  @Test
  void deterministicJsonOutputAcrossTwoPaths() throws Exception {
    SteelHook02ContractGeneralizationAnalysis analysis = sampleAnalysis();
    Path first = tempDirectory.resolve("one/minecraft-steelhook-0-2-contract-generalization.json");
    Path second = tempDirectory.resolve("two/minecraft-steelhook-0-2-contract-generalization.json");

    writer.write(first, analysis);
    writer.write(second, analysis);

    assertEquals(
        Files.readString(first, StandardCharsets.UTF_8),
        Files.readString(second, StandardCharsets.UTF_8));
  }

  @Test
  void serializesSchemaStatusDescriptorsAndFindings() {
    String json = writer.toJson(sampleAnalysis()).toString();

    assertTrue(json.contains("\"schema\":1"));
    assertTrue(json.contains("\"milestoneName\":\"Target-24\""));
    assertTrue(json.contains("\"steelHookVersion\":\"0.2\""));
    assertTrue(json.contains("\"status\":\"CONTRACT_GENERALIZATION_READY\""));
    assertTrue(
        json.contains(
            "\"nextDirection\":\"MOVE_TO_TARGET_25_RUNTIME_SAFE_METHOD_ENTRY_TRANSFORMER\""));
    assertTrue(json.contains("\"gatePassed\":true"));
    assertTrue(json.contains("\"targetDescriptor\""));
    assertTrue(json.contains("\"dispatcherDescriptor\""));
    assertTrue(json.contains("\"primitiveContract\""));
    assertTrue(json.contains("\"generalizedPatchPlan\""));
    assertTrue(json.contains("\"id\":\"target-24.finding.001\""));
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
    assertFalse(json.contains("transformedClassBytes"));
    assertFalse(json.contains("classfilePayload"));
  }

  private SteelHook02ContractGeneralizationAnalysis sampleAnalysis() {
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
        "Use Target-24 descriptors as Target-25 input.",
        new SteelHook02TargetDescriptor(
            "target-24.target.001",
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
            true),
        new SteelHook02DispatcherDescriptor(
            "target-24.dispatcher.001",
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
            false),
        new SteelHook02PrimitiveContract(
            "target-24.contract.001",
            SteelHook02PrimitiveKind.METHOD_ENTRY_STATIC_DISPATCH,
            "target-23.steelhook-0-2.primitive.candidate.001",
            "target-24.target.001",
            "target-24.dispatcher.001",
            MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH,
            MinecraftHookPatchMode.STEELHOOK_0_2_CONTRACT_GENERALIZED_STATIC_DISPATCH_INVOKESTATIC,
            MinecraftHookPatchEligibility.STEELHOOK_0_2_CONTRACT_READY_RUNTIME_CANDIDATE,
            "METHOD_ENTRY_OFFSET_ZERO_ONLY",
            true,
            true,
            false,
            false,
            false),
        new SteelHook02GeneralizedPatchPlan(
            "target-24.patch-plan.001",
            "Target-7",
            "target-7.minecraft.server.main.method-entry-dispatch-patch",
            "target-23.steelhook-0-2.primitive.candidate.001",
            "target-24.target.001",
            "target-24.dispatcher.001",
            MinecraftHookPatchKind.METHOD_ENTRY_STATIC_DISPATCH,
            MinecraftHookPatchMode.STEELHOOK_0_2_CONTRACT_GENERALIZED_STATIC_DISPATCH_INVOKESTATIC,
            MinecraftHookPatchEligibility.STEELHOOK_0_2_CONTRACT_READY_RUNTIME_CANDIDATE,
            1,
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
            List.of("analysis-only")),
        List.of(
            new SteelHook02ContractGeneralizationFinding(
                "target-24.finding.001",
                "pass",
                SteelHook02ContractGeneralizationFindingStatus.PASS,
                true,
                "pass",
                "pass")));
  }
}
