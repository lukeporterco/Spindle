package com.spindle.core.minecraft.hook.verify;

public record SteelHook03CompletionStageVerification(
    String stageId, String milestoneName, String summary, boolean passed, String failureReason) {}
