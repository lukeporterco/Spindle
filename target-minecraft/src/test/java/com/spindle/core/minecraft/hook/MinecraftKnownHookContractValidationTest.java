package com.spindle.core.minecraft.hook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spindle.core.minecraft.MinecraftSide;
import com.spindle.core.minecraft.interpret.MinecraftArtifactInterpretation;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedClass;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedJar;
import com.spindle.core.minecraft.interpret.MinecraftInterpretedMethod;
import java.util.List;
import org.junit.jupiter.api.Test;

class MinecraftKnownHookContractValidationTest {
  private final MinecraftHookContractCatalogProvider provider =
      new MinecraftHookContractCatalogProvider();
  private final MinecraftHookContractValidator validator = new MinecraftHookContractValidator();

  @Test
  void knownCatalogValidatesAgainstInterpretedMainClassAndMethod() {
    MinecraftHookContractReport report =
        validator.validate(
            interpretationWithMainMethod(true),
            provider.catalogFor("26.1.2", MinecraftSide.SERVER));

    assertTrue(report.validationPassed());
    assertEquals(2, report.contractCount());
    assertEquals(2, report.validContractCount());
    assertEquals(0, report.errorCount());
    assertEquals("VALID", report.contracts().get(0).status());
    assertEquals("net/minecraft/server/Main", report.contracts().get(0).matchedClass());
    assertNull(report.contracts().get(0).matchedMember());
    assertEquals("VALID", report.contracts().get(1).status());
    assertEquals("main([Ljava/lang/String;)V", report.contracts().get(1).matchedMember());
  }

  @Test
  void missingMainMethodProducesMissingMember() {
    MinecraftHookContractReport report =
        validator.validate(
            interpretationWithMainMethod(false),
            provider.catalogFor("26.1.2", MinecraftSide.SERVER));

    assertFalse(report.validationPassed());
    assertEquals("VALID", report.contracts().get(0).status());
    assertEquals("MISSING_MEMBER", report.contracts().get(1).status());
    assertEquals(1, report.errorCount());
    assertEquals("minecraft.hook_contract.missing_member", report.diagnostics().getFirst().code());
  }

  @Test
  void missingMainClassProducesMissingClass() {
    MinecraftHookContractReport report =
        validator.validate(
            interpretationWithoutMainClass(), provider.catalogFor("26.1.2", MinecraftSide.SERVER));

    assertFalse(report.validationPassed());
    assertEquals("MISSING_CLASS", report.contracts().get(0).status());
    assertEquals("MISSING_CLASS", report.contracts().get(1).status());
    assertEquals(2, report.errorCount());
    assertEquals("minecraft.hook_contract.missing_class", report.diagnostics().getFirst().code());
  }

  private MinecraftArtifactInterpretation interpretationWithMainMethod(boolean includeMainMethod) {
    List<MinecraftInterpretedMethod> methods =
        includeMainMethod
            ? List.of(
                new MinecraftInterpretedMethod(
                    "main", "([Ljava/lang/String;)V", 9, List.of("public", "static"), false, true))
            : List.of();
    return interpretation(
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
                methods)));
  }

  private MinecraftArtifactInterpretation interpretationWithoutMainClass() {
    return interpretation(List.of());
  }

  private MinecraftArtifactInterpretation interpretation(List<MinecraftInterpretedClass> classes) {
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
                classes.isEmpty() ? 0 : 1,
                classes.size(),
                0,
                classes.stream().mapToInt(c -> c.methods().size()).sum(),
                List.of(classes.isEmpty() ? List.<String>of() : List.of("net.minecraft.server"))
                    .stream()
                    .flatMap(List::stream)
                    .toList(),
                classes)),
        classes.isEmpty() ? 0 : 1,
        classes.size(),
        0,
        classes.stream().mapToInt(c -> c.methods().size()).sum(),
        0,
        classes.isEmpty() ? List.of() : List.of("net.minecraft.server"),
        List.of());
  }
}
