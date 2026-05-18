package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.verify.SteelHook03CompletionHandoffStatus;
import com.spindle.core.minecraft.hook.verify.SteelHook03CompletionReport;
import com.spindle.core.minecraft.hook.verify.SteelHook03CompletionStatus;
import java.util.List;

public final class SteelHook04TestFixtures {
  public static final String TARGET_OWNER_INTERNAL_NAME =
      "net/minecraft/server/Target33ReturnValueFixture";
  public static final String TARGET_BINARY_NAME = "net.minecraft.server.Target33ReturnValueFixture";
  public static final String TARGET_CLASS_ENTRY_NAME = TARGET_OWNER_INTERNAL_NAME + ".class";
  public static final String PRIMITIVE_METHOD_NAME = "primitiveValue";
  public static final String PRIMITIVE_DESCRIPTOR = "()I";
  public static final String REFERENCE_METHOD_NAME = "referenceValue";
  public static final String REFERENCE_DESCRIPTOR = "()Ljava/lang/String;";
  public static final String VOID_METHOD_NAME = "voidValue";
  public static final String VOID_DESCRIPTOR = "()V";
  public static final String MULTIPLE_RETURNS_METHOD_NAME = "multipleReturns";
  public static final String BRANCHING_METHOD_NAME = "branchingValue";
  public static final String SWITCH_METHOD_NAME = "switchValue";
  public static final String EXCEPTION_TABLE_METHOD_NAME = "exceptionTableValue";
  public static final String STACKMAP_METHOD_NAME = "stackMapValue";
  public static final String SYNCHRONIZED_METHOD_NAME = "synchronizedValue";
  public static final String MISSING_PRODUCER_METHOD_NAME = "missingProducerValue";
  public static final String REPLACEMENT_REFERENCE = "replacement";

  private SteelHook04TestFixtures() {}

  public static byte[] returnValueInterceptFixtureClassBytes() {
    return new SteelHook04ReturnValueInterceptFixtureClassFactory().createFixtureClassBytes();
  }

  public static SteelHook04PrimitiveBoundaryReport passedTarget32Report() {
    return new SteelHook04PrimitiveBoundaryAnalyzer().analyze(passedTarget31Report());
  }

  public static SteelHook03CompletionReport passedTarget31Report() {
    return new SteelHook03CompletionReport(
        1,
        "Target-31",
        "minecraft",
        "0.3",
        true,
        SteelHook03CompletionStatus.PASSED,
        SteelHook03CompletionHandoffStatus.STEELHOOK_0_3_COMPLETE,
        "Target-27",
        "passed",
        true,
        "steelhook-0-2-complete",
        "Target-28",
        "passed",
        true,
        "Target-29",
        "passed",
        true,
        "Target-30",
        "passed",
        true,
        List.of("bounded METHOD_ENTRY_STATIC_DISPATCH", "bounded METHOD_EXIT_STATIC_DISPATCH"),
        List.of("public-api", "sandboxing"),
        List.of(),
        List.of(),
        List.of(),
        2,
        2,
        true,
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
        false,
        null,
        List.of());
  }
}
