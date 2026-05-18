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
  public static final String INVOKE_TARGET_OWNER_INTERNAL_NAME =
      "net/minecraft/server/Target34InvokeCallsiteFixture";
  public static final String INVOKE_TARGET_BINARY_NAME =
      "net.minecraft.server.Target34InvokeCallsiteFixture";
  public static final String INVOKE_TARGET_CLASS_ENTRY_NAME =
      INVOKE_TARGET_OWNER_INTERNAL_NAME + ".class";
  public static final String INVOKE_ORIGINAL_METHOD_NAME = "originalValue";
  public static final String INVOKE_REDIRECTED_METHOD_NAME = "redirectedValue";
  public static final String INVOKE_WRAPPED_METHOD_NAME = "wrappedValue";
  public static final String INVOKE_METHOD_NAME = "invokeValue";
  public static final String INVOKE_NO_INVOKE_METHOD_NAME = "noInvokeValue";
  public static final String INVOKE_AMBIGUOUS_METHOD_NAME = "ambiguousInvokeValue";
  public static final String INVOKE_CONSTRUCTOR_METHOD_NAME = "constructorInvokeValue";
  public static final String INVOKE_SPECIAL_METHOD_NAME = "specialInvokeValue";
  public static final String INVOKE_BRANCHING_METHOD_NAME = "branchingInvokeValue";
  public static final String INVOKE_SWITCH_METHOD_NAME = "switchInvokeValue";
  public static final String INVOKE_EXCEPTION_TABLE_METHOD_NAME = "exceptionTableInvokeValue";
  public static final String INVOKE_STACKMAP_METHOD_NAME = "stackMapInvokeValue";
  public static final String INVOKE_SYNCHRONIZED_METHOD_NAME = "synchronizedInvokeValue";
  public static final String INVOKE_INT_DESCRIPTOR = "()I";

  private SteelHook04TestFixtures() {}

  public static byte[] returnValueInterceptFixtureClassBytes() {
    return new SteelHook04ReturnValueInterceptFixtureClassFactory().createFixtureClassBytes();
  }

  public static byte[] invokeCallsiteFixtureClassBytes() {
    return new SteelHook04InvokeCallsiteFixtureClassFactory().createFixtureClassBytes();
  }

  public static SteelHook04PrimitiveBoundaryReport passedTarget32Report() {
    return new SteelHook04PrimitiveBoundaryAnalyzer().analyze(passedTarget31Report());
  }

  public static SteelHook04ReturnValueInterceptOfflineProofReport passedTarget33Report() {
    return new SteelHook04ReturnValueInterceptOfflineProofRunner().run(passedTarget32Report());
  }

  public static SteelHook04InvokeRedirectWrapOfflineProofReport passedTarget34Report() {
    return new SteelHook04InvokeRedirectWrapOfflineProofRunner()
        .run(passedTarget32Report(), passedTarget33Report());
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
