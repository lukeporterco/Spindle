package com.spindle.core.minecraft.hook.verify;

public record SteelHook04CompletionSafetyInvariant(
    String id, String expectedValue, String actualValue, boolean passed, String failureReason) {}
