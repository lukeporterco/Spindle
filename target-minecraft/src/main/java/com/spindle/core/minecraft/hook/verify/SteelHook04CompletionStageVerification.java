package com.spindle.core.minecraft.hook.verify;

public record SteelHook04CompletionStageVerification(
    String stageId, String milestoneName, String summary, boolean passed, String failureReason) {}
