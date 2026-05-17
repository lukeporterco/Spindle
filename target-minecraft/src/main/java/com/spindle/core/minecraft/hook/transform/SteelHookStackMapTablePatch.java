package com.spindle.core.minecraft.hook.transform;

public record SteelHookStackMapTablePatch(
    boolean present,
    boolean rewriteSupported,
    boolean rewriteApplied,
    boolean rejected,
    Integer entryCountBefore,
    Integer entryCountAfter,
    Integer firstFrameOffsetDeltaBefore,
    Integer firstFrameOffsetDeltaAfter,
    Integer originalBodyLength,
    Integer transformedBodyLength) {}
