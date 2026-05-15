package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchEligibility;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchKind;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchMode;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class SteelHook02MethodEntryTransformerTest {
  private final SteelHook02MethodEntryTransformer transformer =
      new SteelHook02MethodEntryTransformer();

  @Test
  void validTarget24AnalysisPlusValidTargetClassBytesProducesTransformedResult() throws Exception {
    SteelHook02MethodEntryTransformerResult result =
        transformer.transform(
            validContractGeneralizationAnalysis(),
            readResourceBytes("net/minecraft/server/Main.class"));

    assertEquals(SteelHook02MethodEntryTransformerStatus.TRANSFORMED, result.status());
    assertEquals("Target-25", result.milestoneName());
    assertFalse(result.minecraftRuntimeTransformReady());
    assertFalse(result.runtimeClassLoadingPathEnabled());
    assertFalse(result.classLoadingOccurred());
    assertFalse(result.hookInstallationOccurred());
    assertFalse(result.runtimeDispatchOccurred());
    assertFalse(result.realMinecraftRuntimeTransformed());
    assertTrue(result.transformedClassBytesProduced());
    assertTrue(result.bytecodeModified());
    assertTrue(result.eligibleForTarget26GatedRuntimeTransformation());
    assertEquals(
        SteelHook02MethodEntryTransformerNextDirection
            .MOVE_TO_TARGET_26_GATED_REAL_RUNTIME_TRANSFORMATION,
        result.nextDirection());
    assertNotNull(result.transformedClassSha256());
  }

  @Test
  void nullAnalysisFailsWithUpstreamGateBlocked() {
    SteelHook02MethodEntryTransformerResult result = transformer.transform(null, new byte[] {1});

    assertEquals(SteelHook02MethodEntryTransformerStatus.UPSTREAM_GATE_BLOCKED, result.status());
  }

  @Test
  void invariantDriftFailsDeterministically() throws Exception {
    SteelHook02ContractGeneralizationAnalysis analysis =
        new SteelHook02ContractGeneralizationAnalysis(
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
            true,
            true,
            false,
            true,
            null,
            SteelHook02ContractGeneralizationStatus.CONTRACT_GENERALIZATION_READY,
            SteelHook02ContractGeneralizationNextDirection
                .MOVE_TO_TARGET_25_RUNTIME_SAFE_METHOD_ENTRY_TRANSFORMER,
            "drift",
            validTargetDescriptor(),
            validDispatcherDescriptor(),
            validPrimitiveContract(),
            validGeneralizedPatchPlan(),
            List.of());

    SteelHook02MethodEntryTransformerResult result =
        transformer.transform(analysis, readResourceBytes("net/minecraft/server/Main.class"));

    assertEquals(SteelHook02MethodEntryTransformerStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("invariants drifted"));
  }

  private SteelHook02ContractGeneralizationAnalysis validContractGeneralizationAnalysis() {
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

  private SteelHook02TargetDescriptor validTargetDescriptor() {
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

  private SteelHook02DispatcherDescriptor validDispatcherDescriptor() {
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

  private SteelHook02PrimitiveContract validPrimitiveContract() {
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

  private SteelHook02GeneralizedPatchPlan validGeneralizedPatchPlan() {
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

  private byte[] readResourceBytes(String resourceName) throws IOException {
    try (var inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IOException("Missing class bytes for " + resourceName);
      }
      return inputStream.readAllBytes();
    }
  }
}
