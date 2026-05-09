package com.spindle.core.runtime;

import com.spindle.core.lifecycle.LifecycleExecutionReport;
import com.spindle.core.quality.RuntimeQualityReport;

public record CompiledRuntimeResult(
    CompiledModpackProfile profile,
    CompiledModpackProfileResult profileResult,
    RuntimeQualityReport qualityReport,
    LifecycleExecutionReport lifecycleReport) {}
