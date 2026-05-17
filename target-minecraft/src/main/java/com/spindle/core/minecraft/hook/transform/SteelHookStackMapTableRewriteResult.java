package com.spindle.core.minecraft.hook.transform;

public record SteelHookStackMapTableRewriteResult(
    SteelHookStackMapTableRewriteStatus status,
    String failureReason,
    int originalEntryCount,
    int transformedEntryCount,
    boolean rewriteApplied,
    Integer firstFrameOffsetDeltaBefore,
    Integer firstFrameOffsetDeltaAfter,
    int originalBodyLength,
    int transformedBodyLength,
    byte[] transformedBody) {
  public SteelHookStackMapTableRewriteResult {
    transformedBody = transformedBody == null ? null : transformedBody.clone();
  }

  public SteelHookStackMapTablePatch patch(boolean rewriteSupported) {
    return new SteelHookStackMapTablePatch(
        true,
        rewriteSupported,
        rewriteApplied,
        status == SteelHookStackMapTableRewriteStatus.REJECTED,
        originalEntryCount,
        transformedEntryCount,
        firstFrameOffsetDeltaBefore,
        firstFrameOffsetDeltaAfter,
        originalBodyLength,
        transformedBodyLength);
  }
}
