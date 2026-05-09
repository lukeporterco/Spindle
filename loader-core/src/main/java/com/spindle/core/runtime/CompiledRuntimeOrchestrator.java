package com.spindle.core.runtime;

import com.spindle.core.diagnostics.DiagnosticEvent;
import com.spindle.core.diagnostics.DiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.launch.LaunchPhase;
import com.spindle.core.lifecycle.LifecycleExecutionReport;
import com.spindle.core.lifecycle.LifecycleExecutionReportWriter;
import com.spindle.core.lifecycle.LifecycleExecutor;
import com.spindle.core.lifecycle.LifecyclePlan;
import com.spindle.core.lifecycle.LifecyclePlanBuilder;
import com.spindle.core.pipeline.ModpackPlanningResult;
import com.spindle.core.quality.RuntimeQualityReport;
import com.spindle.core.quality.RuntimeQualityReportWriter;
import com.spindle.core.quality.RuntimeQualityReporter;
import com.spindle.core.report.DiagnosticMeasurements;
import com.spindle.core.report.DisplayPaths;
import java.nio.file.Path;

public final class CompiledRuntimeOrchestrator {
  private final CompiledModpackProfileFingerprint fingerprint =
      new CompiledModpackProfileFingerprint();
  private final CompiledModpackProfileBuilder builder = new CompiledModpackProfileBuilder();
  private final CompiledModpackProfileWriter writer = new CompiledModpackProfileWriter();
  private final CompiledModpackProfileCache cache = new CompiledModpackProfileCache();
  private final LifecyclePlanBuilder lifecyclePlanBuilder = new LifecyclePlanBuilder();
  private final LifecycleExecutor lifecycleExecutor = new LifecycleExecutor();
  private final LifecycleExecutionReportWriter lifecycleExecutionReportWriter =
      new LifecycleExecutionReportWriter();
  private final RuntimeQualityReporter runtimeQualityReporter = new RuntimeQualityReporter();
  private final RuntimeQualityReportWriter runtimeQualityReportWriter =
      new RuntimeQualityReportWriter();

  public CompiledRuntimeResult compile(
      LaunchContext context,
      ModpackPlanningResult planningResult,
      String gameSide,
      DiagnosticSink diagnosticSink)
      throws LoaderException {
    String inputFingerprint = fingerprint.computeInputFingerprint(context, planningResult, gameSide);
    CompiledModpackProfileCache.CacheLookup cacheLookup = cache.lookup(context, inputFingerprint);
    diagnosticSink.record(
        new DiagnosticEvent(
            "runtime.compiled_profile.cache",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            null,
            DiagnosticMeasurements.details(
                "cacheStatus",
                cacheLookup.hit() ? "hit" : "miss",
                "cacheReason",
                cacheLookup.reason(),
                "inputFingerprint",
                inputFingerprint)));

    LifecyclePlan lifecyclePlan =
        lifecyclePlanBuilder.build(planningResult.resolvedMods(), planningResult.classOwnershipIndex());
    RuntimeQualityReport qualityReport = runtimeQualityReporter.create(planningResult);
    CompiledModpackProfile compiledProfile =
        cacheLookup.hit()
            ? cacheLookup
                .profile()
                .withCache(new CompiledModpackProfile.Cache("hit", cacheLookup.reason()))
            : builder.build(
                context,
                planningResult,
                gameSide,
                inputFingerprint,
                new CompiledModpackProfile.Cache("miss", cacheLookup.reason()),
                lifecyclePlan,
                qualityReport);
    if (!cacheLookup.hit()) {
      cache.store(context, inputFingerprint, compiledProfile);
    }

    Path outputPath = context.workingDirectory().resolve("spindle.profile.json");
    CompiledModpackProfileResult profileResult =
        writer.write(outputPath, compiledProfile.withCache(compiledProfile.cache()));
    runtimeQualityReportWriter.write(
        context.workingDirectory().resolve("spindle.quality-report.json"), qualityReport);
    LifecycleExecutionReport lifecycleReport = lifecycleExecutor.plannedOnly(compiledProfile);
    lifecycleExecutionReportWriter.write(
        context.workingDirectory().resolve("spindle.lifecycle-report.json"), lifecycleReport);
    diagnosticSink.record(
        new DiagnosticEvent(
            "runtime.compiled_profile.write",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            null,
            DiagnosticMeasurements.details(
                "compiledProfileOutputPath",
                DisplayPaths.displayPath(context, outputPath),
                "compiledProfileSchemaVersion",
                Integer.toString(profileResult.schemaVersion()),
                "compiledProfileFingerprint",
                profileResult.fingerprint())));
    return new CompiledRuntimeResult(compiledProfile, profileResult, qualityReport, lifecycleReport);
  }
}
