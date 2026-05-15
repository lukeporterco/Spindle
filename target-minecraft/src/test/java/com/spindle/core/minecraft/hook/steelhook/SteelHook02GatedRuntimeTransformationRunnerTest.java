package com.spindle.core.minecraft.hook.steelhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.hook.runtime.SteelHookDispatcher;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SteelHook02GatedRuntimeTransformationRunnerTest {
  @TempDir Path tempDir;

  @Test
  void validRuntimeJarContainingTargetClassIsTransformedAndDefined() throws Exception {
    Path runtimeJar =
        SteelHook02TestFixtures.createRuntimeJar(
            tempDir.resolve("runtime.jar"),
            SteelHook02TestFixtures.readResourceBytes("net/minecraft/server/Main.class"));
    SteelHook02TestFixtures.resetDispatcher();

    SteelHook02GatedRuntimeTransformationResult result =
        new SteelHook02GatedRuntimeTransformationRunner()
            .run(
                SteelHook02TestFixtures.runtimePlan(runtimeJar),
                SteelHook02TestFixtures.validContractGeneralizationAnalysis(),
                SteelHook02TestFixtures.validMethodEntryTransformerResult());

    assertEquals(
        SteelHook02GatedRuntimeTransformationStatus.TRANSFORMED_AND_DEFINED, result.status());
    assertTrue(result.runtimeClassLoadingAttempted());
    assertTrue(result.runtimeClassLoadingSucceeded());
    assertTrue(result.classLoadingOccurred());
    assertTrue(result.targetClassDefined());
    assertTrue(result.definedBySteelHookRuntimeClassLoader());
    assertFalse(result.minecraftMainInvoked());
    assertFalse(result.minecraftServerLaunched());
    assertFalse(result.hookInstallationOccurred());
    assertFalse(result.runtimeDispatchOccurred());
    assertFalse(result.dispatcherInvocationObserved());
    assertTrue(result.minecraftRuntimeTransformReady());
    assertTrue(result.eligibleForTarget27CompletionVerification());
    assertEquals(0, SteelHookDispatcher.beforeMinecraftServerMainInvocationCount());

    Files.delete(runtimeJar);
    assertFalse(Files.exists(runtimeJar));
  }

  @Test
  void missingRuntimePlanFailsDeterministically() throws Exception {
    SteelHook02GatedRuntimeTransformationResult result =
        new SteelHook02GatedRuntimeTransformationRunner()
            .run(
                null,
                SteelHook02TestFixtures.validContractGeneralizationAnalysis(),
                SteelHook02TestFixtures.validMethodEntryTransformerResult());

    assertEquals(SteelHook02GatedRuntimeTransformationStatus.REJECTED, result.status());
    assertFalse(result.runtimeClassLoadingAttempted());
    assertTrue(result.failureReason().contains("runtime plan"));
  }

  @Test
  void missingServerJarFailsDeterministically() throws Exception {
    SteelHook02GatedRuntimeTransformationResult result =
        new SteelHook02GatedRuntimeTransformationRunner()
            .run(
                SteelHook02TestFixtures.runtimePlan(tempDir.resolve("missing.jar")),
                SteelHook02TestFixtures.validContractGeneralizationAnalysis(),
                SteelHook02TestFixtures.validMethodEntryTransformerResult());

    assertEquals(SteelHook02GatedRuntimeTransformationStatus.REJECTED, result.status());
    assertFalse(result.runtimeClassLoadingAttempted());
    assertTrue(result.failureReason().contains("readable runtime classpath file"));
  }

  @Test
  void runtimeJarWithoutTargetClassFailsDeterministically() throws Exception {
    Path runtimeJar =
        SteelHook02TestFixtures.createRuntimeJar(
            tempDir.resolve("empty.jar"),
            SteelHook02TestFixtures.fixtureClassBytes(
                "com/example/Main", "([Ljava/lang/String;)V", true, false));

    SteelHook02GatedRuntimeTransformationResult result =
        new SteelHook02GatedRuntimeTransformationRunner()
            .run(
                SteelHook02TestFixtures.runtimePlan(runtimeJar),
                SteelHook02TestFixtures.validContractGeneralizationAnalysis(),
                SteelHook02TestFixtures.validMethodEntryTransformerResult());

    assertEquals(SteelHook02GatedRuntimeTransformationStatus.REJECTED, result.status());
  }

  @Test
  void runtimeJarWithStackMapTableTargetClassFailsDeterministically() throws Exception {
    byte[] classBytes =
        SteelHook02TestFixtures.fixtureClassBytes(
            "net/minecraft/server/Main", "([Ljava/lang/String;)V", true, true);
    Path runtimeJar =
        SteelHook02TestFixtures.createRuntimeJar(tempDir.resolve("stackmap.jar"), classBytes);
    SteelHook02MethodEntryTransformerResult target25 =
        SteelHook02TestFixtures.validMethodEntryTransformerResult();
    target25 =
        new SteelHook02MethodEntryTransformerResult(
            target25.schema(),
            target25.milestoneName(),
            target25.target(),
            target25.steelHookVersion(),
            target25.sourcePatchPlanMilestone(),
            target25.sourcePrimitiveBoundaryMilestone(),
            target25.sourceContractGeneralizationMilestone(),
            target25.localTransformationOnly(),
            target25.runtimeClassLoadingPathEnabled(),
            target25.classLoadingOccurred(),
            target25.hookInstallationOccurred(),
            target25.runtimeDispatchOccurred(),
            target25.realMinecraftRuntimeTransformed(),
            target25.publicApiExposed(),
            target25.javaAgentUsed(),
            target25.mixinUsed(),
            target25.javaModExecutionSandboxed(),
            target25.minecraftRuntimeTransformReady(),
            target25.target25TransformerExtractionOccurred(),
            target25.methodEntryTransformationOccurred(),
            target25.bytecodeModified(),
            target25.transformedClassBytesProduced(),
            target25.eligibleForTarget26GatedRuntimeTransformation(),
            target25.gatePassed(),
            target25.status(),
            target25.nextDirection(),
            target25.failureReason(),
            sha256Hex(classBytes),
            target25.transformedClassSha256(),
            target25.originalCodeSha256(),
            target25.transformedCodeSha256(),
            target25.originalCodeLength(),
            target25.transformedCodeLength(),
            target25.constantPoolCountBefore(),
            target25.constantPoolCountAfter(),
            target25.methodrefIndex(),
            target25.insertedInstructionHex(),
            target25.gate(),
            target25.targetDescriptor(),
            target25.dispatcherDescriptor(),
            target25.primitiveContract(),
            target25.generalizedPatchPlan(),
            new SteelHook02TargetClassBytes(
                target25.targetClassBytes().classEntryName(),
                runtimeJar.toString(),
                "runtime-jar",
                null,
                classBytes,
                true,
                true,
                null),
            target25.findings());

    SteelHook02GatedRuntimeTransformationResult result =
        new SteelHook02GatedRuntimeTransformationRunner()
            .run(
                SteelHook02TestFixtures.runtimePlan(runtimeJar),
                SteelHook02TestFixtures.validContractGeneralizationAnalysis(),
                target25);

    assertEquals(SteelHook02GatedRuntimeTransformationStatus.REJECTED, result.status());
    assertTrue(result.failureReason().contains("StackMapTable"));
  }

  private String sha256Hex(byte[] bytes) throws Exception {
    byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
    StringBuilder builder = new StringBuilder(hash.length * 2);
    for (byte value : hash) {
      builder.append(Character.forDigit((value >>> 4) & 0xF, 16));
      builder.append(Character.forDigit(value & 0xF, 16));
    }
    return builder.toString();
  }
}
