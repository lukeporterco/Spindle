package com.spindle.core.minecraft.hook.verify;

public record SteelHookStageVerification(
    String stageId, String milestoneName, String summary, boolean passed, String failureReason) {}
