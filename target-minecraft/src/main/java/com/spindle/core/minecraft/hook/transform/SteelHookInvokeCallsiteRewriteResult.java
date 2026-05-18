package com.spindle.core.minecraft.hook.transform;

public record SteelHookInvokeCallsiteRewriteResult(
    SteelHookInvokeCallsiteRewriteStatus status,
    String failureReason,
    SteelHookInvokeCallsiteRewriteRequest request,
    String originalClassSha256,
    String transformedClassSha256,
    String originalCodeSha256,
    String transformedCodeSha256,
    Integer originalCodeLength,
    Integer transformedCodeLength,
    String matchedInvokeOpcode,
    Integer matchedCallsiteCount,
    boolean bytecodeModified,
    boolean transformedClassBytesProduced,
    boolean exceptionTablePresent,
    boolean stackMapTablePresent,
    boolean synchronizedMethod,
    boolean branchingMethod,
    boolean switchMethod,
    String replacementSummary,
    byte[] transformedClassBytes) {
  public SteelHookInvokeCallsiteRewriteResult {
    transformedClassBytes = transformedClassBytes == null ? null : transformedClassBytes.clone();
  }

  @Override
  public byte[] transformedClassBytes() {
    return transformedClassBytes == null ? null : transformedClassBytes.clone();
  }
}
