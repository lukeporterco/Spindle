package com.spindle.core.minecraft.hook.steelhook;

import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchEligibility;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchKind;
import com.spindle.core.minecraft.hook.patch.MinecraftHookPatchMode;

public record SteelHook02PrimitiveContract(
    String id,
    SteelHook02PrimitiveKind primitiveKind,
    String sourceCandidateId,
    String targetDescriptorId,
    String dispatcherDescriptorId,
    MinecraftHookPatchKind patchKind,
    MinecraftHookPatchMode patchMode,
    MinecraftHookPatchEligibility patchEligibility,
    String insertionOffsetPolicy,
    boolean contractGeneralized,
    boolean fixtureTransformReady,
    boolean minecraftRuntimeTransformReady,
    boolean publicApiExposed,
    boolean javaModExecutionSandboxed) {}
