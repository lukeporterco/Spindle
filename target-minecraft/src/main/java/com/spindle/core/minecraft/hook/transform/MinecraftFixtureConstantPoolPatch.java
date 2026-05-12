package com.spindle.core.minecraft.hook.transform;

public record MinecraftFixtureConstantPoolPatch(
    Integer constantPoolCountBefore,
    Integer constantPoolCountAfter,
    int appendedEntryCount,
    Integer dispatcherOwnerUtf8Index,
    Integer dispatcherClassIndex,
    Integer dispatcherMethodNameUtf8Index,
    Integer dispatcherDescriptorUtf8Index,
    Integer dispatcherNameAndTypeIndex,
    Integer dispatcherMethodrefIndex) {}
