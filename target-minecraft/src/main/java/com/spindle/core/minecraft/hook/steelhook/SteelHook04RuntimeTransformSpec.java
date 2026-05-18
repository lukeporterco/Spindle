package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.bootstrap.MinecraftBootstrapHookTransformationMode;
import com.spindle.core.minecraft.hook.transform.SteelHookInvokeCallsiteRewriteMode;
import com.spindle.core.minecraft.hook.transform.SteelHookInvokeOpcode;
import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptKind;
import com.spindle.core.minecraft.hook.transform.SteelHookReturnValueInterceptMode;

public record SteelHook04RuntimeTransformSpec(
    String id,
    SteelHook04PrimitiveKind primitiveKind,
    String sourceMilestone,
    String sourceReportId,
    String targetOwnerInternalName,
    String targetBinaryName,
    String targetClassEntryName,
    String targetMethodName,
    String targetDescriptor,
    SteelHookReturnValueInterceptMode returnValueInterceptMode,
    SteelHookReturnValueInterceptKind returnValueInterceptKind,
    Integer replacementPrimitiveValue,
    String replacementReferenceValue,
    SteelHookInvokeCallsiteRewriteMode invokeRewriteMode,
    String expectedInvokeOwnerInternalName,
    String expectedInvokeName,
    String expectedInvokeDescriptor,
    SteelHookInvokeOpcode expectedInvokeOpcode,
    String replacementInvokeOwnerInternalName,
    String replacementInvokeName,
    String replacementInvokeDescriptor,
    SteelHookInvokeOpcode replacementInvokeOpcode,
    boolean runtimeClassLoadingPathEnabled,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed) {
  public static SteelHook04RuntimeTransformSpec returnValueInterceptPrimitiveReplacement() {
    return new SteelHook04RuntimeTransformSpec(
        "target-35.runtime-spec.001",
        SteelHook04PrimitiveKind.RETURN_VALUE_INTERCEPT,
        "Target-33",
        SteelHook04ReturnValueInterceptOfflineProofReportWriter.REPORT_FILE_NAME,
        SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_BINARY_NAME,
        SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_METHOD_NAME,
        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_DESCRIPTOR,
        SteelHookReturnValueInterceptMode.REPLACE_RETURN_VALUE,
        SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
        42,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        true,
        false,
        false);
  }

  public static SteelHook04RuntimeTransformSpec invokeRedirect() {
    return new SteelHook04RuntimeTransformSpec(
        "target-35.runtime-spec.002",
        SteelHook04PrimitiveKind.INVOKE_REDIRECT,
        "Target-34",
        SteelHook04InvokeRedirectWrapOfflineProofReportWriter.REPORT_FILE_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INVOKE_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        null,
        null,
        null,
        null,
        SteelHookInvokeCallsiteRewriteMode.REDIRECT,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.REDIRECTED_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        true,
        false,
        false);
  }

  public static SteelHook04RuntimeTransformSpec invokeWrap() {
    return new SteelHook04RuntimeTransformSpec(
        "target-35.runtime-spec.003",
        SteelHook04PrimitiveKind.INVOKE_WRAP,
        "Target-34",
        SteelHook04InvokeRedirectWrapOfflineProofReportWriter.REPORT_FILE_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_BINARY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INVOKE_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        null,
        null,
        null,
        null,
        SteelHookInvokeCallsiteRewriteMode.WRAP,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.ORIGINAL_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        SteelHook04InvokeCallsiteFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.WRAPPED_METHOD_NAME,
        SteelHook04InvokeCallsiteFixtureClassFactory.INT_DESCRIPTOR,
        SteelHookInvokeOpcode.INVOKESTATIC,
        true,
        false,
        false);
  }

  public static SteelHook04RuntimeTransformSpec unsupportedOrMalformedPlanForRejectionProof() {
    return new SteelHook04RuntimeTransformSpec(
        "target-35.runtime-spec.reject.001",
        null,
        "Target-35",
        SteelHook04GatedRuntimeProofReportWriter.REPORT_FILE_NAME,
        SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_OWNER_INTERNAL_NAME,
        SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_BINARY_NAME,
        SteelHook04ReturnValueInterceptFixtureClassFactory.TARGET_CLASS_ENTRY_NAME,
        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_METHOD_NAME,
        SteelHook04ReturnValueInterceptFixtureClassFactory.PRIMITIVE_DESCRIPTOR,
        SteelHookReturnValueInterceptMode.REPLACE_RETURN_VALUE,
        SteelHookReturnValueInterceptKind.PRIMITIVE_INT,
        42,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        true,
        false,
        false);
  }

  public String targetInternalName() {
    return targetOwnerInternalName;
  }

  public MinecraftBootstrapHookTransformationMode transformationMode() {
    return switch (primitiveKind) {
      case RETURN_VALUE_INTERCEPT ->
          MinecraftBootstrapHookTransformationMode
              .STEELHOOK_0_4_GATED_RUNTIME_RETURN_VALUE_INTERCEPT_TRANSFORM;
      case INVOKE_REDIRECT ->
          MinecraftBootstrapHookTransformationMode
              .STEELHOOK_0_4_GATED_RUNTIME_INVOKE_REDIRECT_TRANSFORM;
      case INVOKE_WRAP ->
          MinecraftBootstrapHookTransformationMode
              .STEELHOOK_0_4_GATED_RUNTIME_INVOKE_WRAP_TRANSFORM;
      case null -> null;
    };
  }

  public String transformationModeId() {
    MinecraftBootstrapHookTransformationMode transformationMode = transformationMode();
    return transformationMode == null ? null : transformationMode.id();
  }
}
