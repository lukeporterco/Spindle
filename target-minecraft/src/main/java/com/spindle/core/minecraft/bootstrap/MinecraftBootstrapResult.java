package com.spindle.core.minecraft.bootstrap;

import com.spindle.core.minecraft.MinecraftPlanFingerprint;
import java.util.List;

public record MinecraftBootstrapResult(
    int schema,
    String milestoneName,
    List<String> bootstrapJvmArgs,
    String javaExecutable,
    MinecraftPlanFingerprint runtimePlanFingerprint,
    MinecraftPlanFingerprint executionPlanFingerprint,
    MinecraftPlanFingerprint integrationPlanFingerprint,
    MinecraftPlanFingerprint classloaderGraphFingerprint,
    MinecraftPlanFingerprint modExecutionResultFingerprint,
    boolean minecraftMainInvoked,
    int exitCode,
    String failureCategory,
    String failureMessage,
    boolean mixinUsed,
    boolean remappingUsed,
    boolean bytecodeTransformationUsed,
    boolean minecraftPatchingUsed,
    boolean accessWidenersUsed,
    boolean fabricCompatibilityUsed,
    boolean forgeCompatibilityUsed) {
  public MinecraftBootstrapResult {
    bootstrapJvmArgs = List.copyOf(bootstrapJvmArgs);
  }
}
