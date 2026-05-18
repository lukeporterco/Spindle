package com.spindle.core.minecraft.hook.transform;

public record SteelHookReturnValueInterceptRewriteResult(
    SteelHookReturnValueInterceptRewriteStatus status,
    String failureReason,
    SteelHookReturnValueInterceptRewriteRequest request,
    String originalClassSha256,
    String transformedClassSha256,
    String originalCodeSha256,
    String transformedCodeSha256,
    Integer originalCodeLength,
    Integer transformedCodeLength,
    String matchedReturnOpcode,
    String matchedProducerOpcode,
    Integer matchCount,
    boolean bytecodeModified,
    boolean transformedClassBytesProduced,
    boolean exceptionTablePresent,
    boolean stackMapTablePresent,
    boolean synchronizedMethod,
    boolean branchingMethod,
    boolean switchMethod,
    String replacementSummary,
    byte[] transformedClassBytes) {
  public SteelHookReturnValueInterceptRewriteResult {
    transformedClassBytes = transformedClassBytes == null ? null : transformedClassBytes.clone();
  }

  @Override
  public byte[] transformedClassBytes() {
    return transformedClassBytes == null ? null : transformedClassBytes.clone();
  }
}
