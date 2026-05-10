package com.spindle.core.resolve;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VersionRequirementHardeningTest {
  @Test
  void versionRequirementRejectsOverflowSegments() {
    assertThrows(
        IllegalArgumentException.class,
        () -> VersionRequirement.parse(">=999999999999999999999999999999"));

    VersionRequirement requirement = VersionRequirement.parse(">=1.2.3");
    assertFalse(requirement.matches("999999999999999999999999999999"));
    assertTrue(requirement.matches("2.0.0"));
  }
}
