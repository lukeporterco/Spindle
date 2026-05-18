package com.spindle.core.minecraft.hook.transform;

public record SteelHookMethodExitConstantPoolPatch(
    int constantPoolCountBefore,
    int constantPoolCountAfter,
    int addedEntryCount,
    int dispatcherOwnerUtf8Index,
    int dispatcherClassIndex,
    int dispatcherMethodNameUtf8Index,
    int dispatcherDescriptorUtf8Index,
    int dispatcherNameAndTypeIndex,
    int dispatcherMethodrefIndex) {}
