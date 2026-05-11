package com.spindle.core.minecraft.hook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretation;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedClass;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedField;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedJar;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedMethod;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftHookContractValidatorTest {
  private final MinecraftHookContractValidator validator = new MinecraftHookContractValidator();

  @Test
  void validContractsMatchInterpretedSymbols() {
    MinecraftHookContractReport report =
        validator.validate(
            interpretation(),
            catalog(
                List.of(
                    contract(
                        "class-contract",
                        MinecraftHookPointKind.CLASS,
                        "net/minecraft/server/MinecraftServer",
                        null,
                        null,
                        MinecraftHookRequirement.REQUIRED),
                    contract(
                        "method-contract",
                        MinecraftHookPointKind.METHOD,
                        "net/minecraft/server/MinecraftServer",
                        "tick",
                        "()V",
                        MinecraftHookRequirement.REQUIRED),
                    contract(
                        "constructor-contract",
                        MinecraftHookPointKind.CONSTRUCTOR,
                        "net/minecraft/server/MinecraftServer",
                        "<init>",
                        "(Ljava/lang/String;)V",
                        MinecraftHookRequirement.REQUIRED),
                    contract(
                        "field-contract",
                        MinecraftHookPointKind.FIELD,
                        "net/minecraft/server/MinecraftServer",
                        "LOGGER",
                        "Lorg/slf4j/Logger;",
                        MinecraftHookRequirement.REQUIRED))));

    assertTrue(report.validationPassed());
    assertEquals(2, report.schema());
    assertEquals("Target-3", report.milestoneName());
    assertEquals("test-catalog", report.catalogId());
    assertEquals(4, report.contractCount());
    assertEquals(4, report.validContractCount());
    assertEquals(0, report.invalidContractCount());
    assertEquals(0, report.diagnostics().size());
    assertEquals("net/minecraft/server/MinecraftServer", report.contracts().get(0).matchedClass());
    assertNull(report.contracts().get(0).matchedMember());
    assertEquals("tick()V", report.contracts().get(1).matchedMember());
    assertEquals("<init>(Ljava/lang/String;)V", report.contracts().get(2).matchedMember());
    assertEquals("LOGGER:Lorg/slf4j/Logger;", report.contracts().get(3).matchedMember());
  }

  @Test
  void missingRequiredClassProducesErrorAndFailsValidation() {
    MinecraftHookContractReport report =
        validator.validate(
            interpretation(),
            catalog(
                List.of(
                    contract(
                        "missing-class",
                        MinecraftHookPointKind.CLASS,
                        "net/minecraft/server/MissingServer",
                        null,
                        null,
                        MinecraftHookRequirement.REQUIRED))));

    assertFalse(report.validationPassed());
    assertEquals(1, report.errorCount());
    assertEquals("MISSING_CLASS", report.contracts().getFirst().status());
    assertEquals("minecraft.hook_contract.missing_class", report.diagnostics().getFirst().code());
    assertEquals(MinecraftHookDiagnosticSeverity.ERROR, report.diagnostics().getFirst().severity());
    assertEquals("MISSING_CLASS", report.diagnostics().getFirst().status());
  }

  @Test
  void missingOptionalMethodProducesWarningWithoutFailingValidation() {
    MinecraftHookContractReport report =
        validator.validate(
            interpretation(),
            catalog(
                List.of(
                    contract(
                        "optional-method",
                        MinecraftHookPointKind.METHOD,
                        "net/minecraft/server/MinecraftServer",
                        "missingTick",
                        "()V",
                        MinecraftHookRequirement.OPTIONAL))));

    assertTrue(report.validationPassed());
    assertEquals(1, report.warningCount());
    assertEquals(0, report.errorCount());
    assertEquals("MISSING_MEMBER", report.contracts().getFirst().status());
    assertEquals(
        MinecraftHookDiagnosticSeverity.WARNING, report.diagnostics().getFirst().severity());
  }

  @Test
  void sideMismatchProducesSideMismatchStatus() {
    MinecraftHookContractReport report =
        validator.validate(
            interpretation(),
            catalog(
                List.of(
                    new MinecraftHookPointContract(
                        "client-only",
                        "client contract",
                        MinecraftSide.CLIENT,
                        MinecraftHookPointKind.CLASS,
                        "net/minecraft/server/MinecraftServer",
                        null,
                        null,
                        MinecraftHookRequirement.REQUIRED))));

    assertFalse(report.contracts().getFirst().valid());
    assertEquals("SIDE_MISMATCH", report.contracts().getFirst().status());
    assertEquals("minecraft.hook_contract.side_mismatch", report.diagnostics().getFirst().code());
  }

  @Test
  void malformedContractsReportMalformedStatus() {
    MinecraftHookContractReport dotOwnerReport =
        validator.validate(
            interpretation(),
            catalog(
                List.of(
                    contract(
                        "dot-owner",
                        MinecraftHookPointKind.CLASS,
                        "net.minecraft.server.MinecraftServer",
                        null,
                        null,
                        MinecraftHookRequirement.REQUIRED))));
    MinecraftHookContractReport badConstructorReport =
        validator.validate(
            interpretation(),
            catalog(
                List.of(
                    contract(
                        "bad-constructor",
                        MinecraftHookPointKind.CONSTRUCTOR,
                        "net/minecraft/server/MinecraftServer",
                        "ctor",
                        "(Ljava/lang/String;)V",
                        MinecraftHookRequirement.REQUIRED))));

    assertEquals("MALFORMED_CONTRACT", dotOwnerReport.contracts().getFirst().status());
    assertEquals("MALFORMED_CONTRACT", badConstructorReport.contracts().getFirst().status());
    assertEquals(
        "minecraft.hook_contract.malformed_contract",
        dotOwnerReport.diagnostics().getFirst().code());
    assertEquals(
        "minecraft.hook_contract.malformed_contract",
        badConstructorReport.diagnostics().getFirst().code());
  }

  @Test
  void duplicateIdsProduceDuplicateStatus() {
    MinecraftHookContractReport report =
        validator.validate(
            interpretation(),
            catalog(
                List.of(
                    contract(
                        "duplicate",
                        MinecraftHookPointKind.CLASS,
                        "net/minecraft/server/MinecraftServer",
                        null,
                        null,
                        MinecraftHookRequirement.REQUIRED),
                    contract(
                        "duplicate",
                        MinecraftHookPointKind.CLASS,
                        "net/minecraft/server/MinecraftServer",
                        null,
                        null,
                        MinecraftHookRequirement.REQUIRED))));

    assertTrue(report.contracts().get(0).valid());
    assertFalse(report.contracts().get(1).valid());
    assertEquals("DUPLICATE_ID", report.contracts().get(1).status());
    assertEquals("minecraft.hook_contract.duplicate_id", report.diagnostics().getFirst().code());
  }

  @Test
  void malformedFirstOccurrenceStillReservesDuplicateId() {
    MinecraftHookContractReport report =
        validator.validate(
            interpretation(),
            catalog(
                List.of(
                    contract(
                        "duplicate",
                        MinecraftHookPointKind.METHOD,
                        "net/minecraft/server/MinecraftServer",
                        null,
                        null,
                        MinecraftHookRequirement.REQUIRED),
                    contract(
                        "duplicate",
                        MinecraftHookPointKind.CLASS,
                        "net/minecraft/server/MinecraftServer",
                        null,
                        null,
                        MinecraftHookRequirement.REQUIRED))));

    assertEquals("MALFORMED_CONTRACT", report.contracts().get(0).status());
    assertEquals("DUPLICATE_ID", report.contracts().get(1).status());
    assertEquals("minecraft.hook_contract.malformed_contract", report.diagnostics().get(0).code());
    assertEquals("minecraft.hook_contract.duplicate_id", report.diagnostics().get(1).code());
  }

  @Test
  void emptyCatalogProducesInfoDiagnostic() {
    MinecraftHookContractReport report =
        validator.validate(interpretation(), MinecraftHookContractCatalog.empty());

    assertTrue(report.validationPassed());
    assertEquals(0, report.contractCount());
    assertEquals("empty", report.catalogId());
    assertEquals(1, report.diagnostics().size());
    assertEquals(
        "minecraft.hook_contract.no_contracts_declared", report.diagnostics().getFirst().code());
    assertEquals(MinecraftHookDiagnosticSeverity.INFO, report.diagnostics().getFirst().severity());
  }

  private MinecraftHookContractCatalog catalog(List<MinecraftHookPointContract> contracts) {
    return new MinecraftHookContractCatalog(
        "test-catalog", "Validator test catalog.", "26.1.2", MinecraftSide.SERVER, contracts);
  }

  private MinecraftHookPointContract contract(
      String id,
      MinecraftHookPointKind kind,
      String ownerInternalName,
      String memberName,
      String descriptor,
      MinecraftHookRequirement requirement) {
    return new MinecraftHookPointContract(
        id,
        id + " description",
        MinecraftSide.SERVER,
        kind,
        ownerInternalName,
        memberName,
        descriptor,
        requirement);
  }

  private MinecraftArtifactInterpretation interpretation() {
    MinecraftInterpretedClass interpretedClass =
        new MinecraftInterpretedClass(
            "net.minecraft.server.MinecraftServer",
            "net/minecraft/server/MinecraftServer",
            "net.minecraft.server",
            "java/lang/Object",
            List.of(),
            1,
            List.of("public"),
            List.of(
                new MinecraftInterpretedField(
                    "LOGGER", "Lorg/slf4j/Logger;", 10, List.of("private", "static"))),
            List.of(
                new MinecraftInterpretedMethod(
                    "<init>", "(Ljava/lang/String;)V", 1, List.of("public"), true, false),
                new MinecraftInterpretedMethod("tick", "()V", 1, List.of("public"), false, false)));
    return new MinecraftArtifactInterpretation(
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
                2,
                1,
                List.of("net.minecraft.server"),
                List.of(interpretedClass))),
        1,
        1,
        1,
        2,
        1,
        List.of("net.minecraft.server"),
        List.of());
  }
}
