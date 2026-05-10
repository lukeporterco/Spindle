package com.spindle.core.security;

import com.spindle.core.launch.LaunchContext;
import com.spindle.core.pipeline.ModpackPlanningResult;
import com.spindle.core.runtime.CompiledModpackProfile;

public record SecurityValidationContext(
    LaunchContext launchContext,
    ModpackPlanningResult planningResult,
    CompiledModpackProfile compiledProfile,
    String currentRuntimePolicyFingerprint) {}
