package com.spindle.core.minecraft.hook.verify;

public record SteelHookSafetyInvariant(
    String id, String expectedValue, String actualValue, boolean passed, String failureReason) {}
