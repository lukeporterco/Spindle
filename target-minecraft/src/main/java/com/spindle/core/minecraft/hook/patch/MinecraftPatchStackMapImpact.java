package com.spindle.core.minecraft.hook.patch;

public record MinecraftPatchStackMapImpact(
    boolean stackMapTablePresent,
    Integer stackMapTableEntryCount,
    boolean futureRewriteRequired,
    String rewriteReason) {}
