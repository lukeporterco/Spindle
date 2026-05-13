package com.spindle.core.minecraft.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.concept.MinecraftTargetConceptCatalog;
import com.spindle.core.minecraft.hook.MinecraftHookContractCatalogProvider;
import com.spindle.core.minecraft.hook.MinecraftHookContractReport;
import com.spindle.core.minecraft.hook.MinecraftHookContractValidator;
import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretation;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedClass;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedJar;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedMethod;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftServerLifecycleBindingAnalyzerTest {
  private final MinecraftServerLifecycleBindingAnalyzer analyzer =
      new MinecraftServerLifecycleBindingAnalyzer();
  private final MinecraftTargetConceptCatalog conceptCatalog = new MinecraftTargetConceptCatalog();
  private final MinecraftHookContractCatalogProvider catalogProvider =
      new MinecraftHookContractCatalogProvider();
  private final MinecraftHookContractValidator validator = new MinecraftHookContractValidator();

  @Test
  void validHookContractReportBindsStartingPhaseOnly() {
    MinecraftServerLifecycleBindingReport report =
        analyzer.analyze(conceptCatalog, validHookReport(true));

    assertTrue(report.gatePassed());
    assertTrue(report.sourceContractValidationPassed());
    assertEquals(1, report.boundPhaseCount());
    assertEquals(5, report.unboundPhaseCount());

    MinecraftServerLifecycleBinding starting = report.bindings().getFirst();
    assertEquals("target-11.minecraft.server.lifecycle.starting", starting.id());
    assertEquals("minecraft.server.lifecycle.starting", starting.phaseId());
    assertEquals(MinecraftServerLifecycleBindingStatus.BOUND, starting.status());
    assertTrue(starting.supportedInThisPass());
    assertEquals("minecraft.26_1_2.server.main.entrypoint", starting.boundContractId());
    assertEquals("net/minecraft/server/Main", starting.ownerInternalName());
    assertEquals("main", starting.memberName());
    assertEquals("([Ljava/lang/String;)V", starting.descriptor());
    assertEquals("known-main-entrypoint-analysis", starting.bindingKind());
    assertNull(report.gateFailureReason());
  }

  @Test
  void missingOrInvalidEntrypointContractFailsGate() {
    MinecraftServerLifecycleBindingReport report =
        analyzer.analyze(conceptCatalog, validHookReport(false));

    assertFalse(report.gatePassed());
    assertEquals(
        "Target-11 requires a passing Target-3 hook contract validation report.",
        report.gateFailureReason());
    assertEquals(0, report.boundPhaseCount());
    assertEquals(5, report.unboundPhaseCount());
    assertEquals(
        MinecraftServerLifecycleBindingStatus.UNSUPPORTED, report.bindings().getFirst().status());
    assertEquals(report.gateFailureReason(), report.bindings().getFirst().notes());
  }

  @Test
  void allSixLifecyclePhasesAreAlwaysReported() {
    MinecraftServerLifecycleBindingReport report =
        analyzer.analyze(conceptCatalog, validHookReport(true));

    assertEquals(6, report.lifecyclePhaseCount());
    assertEquals(6, report.bindingCount());
    assertEquals(
        List.of(
            "minecraft.server.lifecycle.starting",
            "minecraft.server.lifecycle.started",
            "minecraft.server.lifecycle.stopping",
            "minecraft.server.lifecycle.stopped",
            "minecraft.server.lifecycle.crashed",
            "minecraft.server.lifecycle.reload_requested"),
        report.bindings().stream().map(MinecraftServerLifecycleBinding::phaseId).toList());
  }

  @Test
  void unboundPhasesRemainDeclaredUnbound() {
    MinecraftServerLifecycleBindingReport report =
        analyzer.analyze(conceptCatalog, validHookReport(true));

    List<MinecraftServerLifecycleBinding> futureBindings = report.bindings().subList(1, 6);
    assertEquals(
        List.of(
            MinecraftServerLifecycleBindingStatus.DECLARED_UNBOUND,
            MinecraftServerLifecycleBindingStatus.DECLARED_UNBOUND,
            MinecraftServerLifecycleBindingStatus.DECLARED_UNBOUND,
            MinecraftServerLifecycleBindingStatus.DECLARED_UNBOUND,
            MinecraftServerLifecycleBindingStatus.DECLARED_UNBOUND),
        futureBindings.stream().map(MinecraftServerLifecycleBinding::status).toList());
    assertTrue(
        futureBindings.stream().noneMatch(MinecraftServerLifecycleBinding::supportedInThisPass));
  }

  @Test
  void reportFlagsRemainAnalysisOnlyAndMutationFree() {
    MinecraftServerLifecycleBindingReport report =
        analyzer.analyze(conceptCatalog, validHookReport(true));

    assertEquals(1, report.schema());
    assertEquals("Target-11", report.milestoneName());
    assertEquals("minecraft.concept.server_lifecycle", report.conceptId());
    assertEquals(1, report.conceptOrder());
    assertEquals("Server Lifecycle", report.conceptDisplayName());
    assertTrue(report.analysisOnly());
    assertFalse(report.classLoadingOccurred());
    assertFalse(report.injectionOccurred());
    assertFalse(report.transformationOccurred());
    assertFalse(report.patchingOccurred());
    assertFalse(report.hookInstallationOccurred());
    assertFalse(report.publicApiExposed());
    assertFalse(report.javaModExecutionSandboxed());
  }

  private MinecraftHookContractReport validHookReport(boolean includeMainMethod) {
    MinecraftArtifactInterpretation interpretation =
        new MinecraftArtifactInterpretation(
            1,
            "Target-1",
            "minecraft",
            "26.1.2",
            "server",
            true,
            false,
            false,
            false,
            false,
            false,
            "dry-run-analysis",
            List.of(
                new MinecraftInterpretedJar(
                    "minecraft-server.jar",
                    "minecraft-server-jar",
                    "fixture",
                    "sha256-fixture",
                    1,
                    1,
                    0,
                    includeMainMethod ? 1 : 0,
                    List.of("net.minecraft.server"),
                    List.of(
                        new MinecraftInterpretedClass(
                            "net.minecraft.server.Main",
                            "net/minecraft/server/Main",
                            "net.minecraft.server",
                            "java/lang/Object",
                            List.of(),
                            17,
                            List.of("public", "final"),
                            List.of(),
                            includeMainMethod
                                ? List.of(
                                    new MinecraftInterpretedMethod(
                                        "main",
                                        "([Ljava/lang/String;)V",
                                        9,
                                        List.of("public", "static"),
                                        false,
                                        true))
                                : List.of())))),
            1,
            1,
            0,
            includeMainMethod ? 1 : 0,
            0,
            List.of("net.minecraft.server"),
            List.of());
    return validator.validate(
        interpretation, catalogProvider.catalogFor("26.1.2", MinecraftSide.SERVER));
  }
}
