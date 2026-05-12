package com.spindle.core.minecraft.flow;

import com.google.gson.Gson;
import com.spindle.core.diagnostics.DiagnosticEvent;
import com.spindle.core.diagnostics.DiagnosticSink;
import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.io.ProcessOutputReader;
import com.spindle.core.launch.LaunchContext;
import com.spindle.core.launch.LaunchPhase;
import com.spindle.core.minecraft.MinecraftDryRunResult;
import com.spindle.core.minecraft.MinecraftPlanFingerprint;
import com.spindle.core.minecraft.MinecraftProviderConfig;
import com.spindle.core.minecraft.bootstrap.MinecraftBootstrapExitCode;
import com.spindle.core.minecraft.bootstrap.MinecraftBootstrapResult;
import com.spindle.core.minecraft.bootstrap.MinecraftServerBootstrapMain;
import com.spindle.core.process.JavaExecutableResolver;
import com.spindle.core.report.DiagnosticMeasurements;
import com.spindle.core.report.DisplayPaths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class MinecraftBootstrapFlow {
  private final Gson gson = new Gson();

  public MinecraftBootstrapResult launch(
      LaunchContext context,
      MinecraftProviderConfig config,
      MinecraftDryRunResult dryRunResult,
      DiagnosticSink diagnosticSink)
      throws LoaderException {
    if (dryRunResult.executionPlan() == null) {
      throw new LoaderException(
          "Minecraft bootstrap requires a generated minecraft-mod-execution-plan.json");
    }
    Path runtimePlanPath = context.workingDirectory().resolve("minecraft-server-runtime-plan.json");
    Path boundaryPlanPath = context.workingDirectory().resolve("minecraft-runtime-boundary.json");
    Path integrationPlanPath =
        context.workingDirectory().resolve("minecraft-mod-integration-plan.json");
    Path executionPlanPath =
        context.workingDirectory().resolve("minecraft-mod-execution-plan.json");
    Path hookInstallationPlanPath =
        context.workingDirectory().resolve("minecraft-hook-installation-plan.json");
    Path hookPatchPlanPath = context.workingDirectory().resolve("minecraft-hook-patch-plan.json");
    MinecraftPlanFingerprint runtimeFingerprint =
        MinecraftPlanFingerprint.fromFile("runtime-plan", runtimePlanPath);
    MinecraftPlanFingerprint boundaryFingerprint =
        MinecraftPlanFingerprint.fromFile("boundary-plan", boundaryPlanPath);
    MinecraftPlanFingerprint integrationFingerprint =
        MinecraftPlanFingerprint.fromFile("integration-plan", integrationPlanPath);
    MinecraftPlanFingerprint executionFingerprint =
        MinecraftPlanFingerprint.fromFile("execution-plan", executionPlanPath);
    MinecraftPlanFingerprint hookInstallationFingerprint = null;
    MinecraftPlanFingerprint hookPatchPlanFingerprint = null;

    List<String> command = new ArrayList<>();
    Path javaExecutable = new JavaExecutableResolver().resolve();
    command.add(javaExecutable.toString());
    command.add("-cp");
    command.add(System.getProperty("java.class.path", ""));
    command.add(MinecraftServerBootstrapMain.class.getName());
    command.add("--working-directory");
    command.add(context.workingDirectory().toString());
    command.add("--runtime-plan");
    command.add("minecraft-server-runtime-plan.json");
    command.add("--boundary-plan");
    command.add("minecraft-runtime-boundary.json");
    command.add("--integration-plan");
    command.add("minecraft-mod-integration-plan.json");
    command.add("--execution-plan");
    command.add("minecraft-mod-execution-plan.json");
    command.add("--expected-runtime-fingerprint");
    command.add(runtimeFingerprint.sha256());
    command.add("--expected-boundary-fingerprint");
    command.add(boundaryFingerprint.sha256());
    command.add("--expected-integration-fingerprint");
    command.add(integrationFingerprint.sha256());
    command.add("--expected-execution-fingerprint");
    command.add(executionFingerprint.sha256());
    if (config.bootstrapTransformHooks()) {
      if (!Files.isRegularFile(hookPatchPlanPath)) {
        throw new LoaderException(
            "Minecraft bootstrap hook transformation requires a generated minecraft-hook-patch-plan.json");
      }
      hookPatchPlanFingerprint =
          MinecraftPlanFingerprint.fromFile("hook-patch-plan", hookPatchPlanPath);
      command.add("--transform-hooks");
      command.add("--hook-patch-plan");
      command.add("minecraft-hook-patch-plan.json");
      command.add("--expected-hook-patch-plan-fingerprint");
      command.add(hookPatchPlanFingerprint.sha256());
    }
    if (config.installHooks()) {
      if (!Files.isRegularFile(hookInstallationPlanPath)) {
        throw new LoaderException(
            "Minecraft bootstrap requires a generated minecraft-hook-installation-plan.json");
      }
      hookInstallationFingerprint =
          MinecraftPlanFingerprint.fromFile("hook-installation-plan", hookInstallationPlanPath);
      command.add("--install-hooks");
      command.add("--hook-installation-plan");
      command.add("minecraft-hook-installation-plan.json");
      command.add("--expected-hook-installation-plan-fingerprint");
      command.add(hookInstallationFingerprint.sha256());
    }
    if (config.verifyPlanFingerprints()) {
      command.add("--verify-plan-fingerprints");
    }
    if (config.strictExecution()) {
      command.add("--strict-execution");
    }
    if (config.bootstrapOffline()) {
      command.add("--offline-bootstrap");
    }

    diagnosticSink.record(
        new DiagnosticEvent(
            "minecraft.bootstrap.start",
            LaunchPhase.COMPLETE.name(),
            0L,
            "ok",
            "Minecraft bootstrap child JVM starting",
            DiagnosticMeasurements.details(
                "javaExecutable",
                DisplayPaths.displayPath(context, javaExecutable),
                "executionPlanOutputPath",
                DisplayPaths.displayPath(context, executionPlanPath))));
    Process process;
    try {
      process = new ProcessBuilder(command).directory(context.workingDirectory().toFile()).start();
    } catch (IOException exception) {
      throw new LoaderException("Failed to start Minecraft bootstrap JVM", exception);
    }

    String stdout = ProcessOutputReader.readProcessStream(process.getInputStream());
    String stderr = ProcessOutputReader.readProcessStream(process.getErrorStream());
    int exitCode;
    try {
      if (!process.waitFor(120, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        throw new LoaderException("Minecraft bootstrap child JVM timed out");
      }
      exitCode = process.exitValue();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      process.destroyForcibly();
      throw new LoaderException("Interrupted while waiting for Minecraft bootstrap JVM", exception);
    }
    if (exitCode != 0 && !stderr.isBlank()) {
      System.err.print(stderr);
    }
    Path bootstrapResultPath =
        context.workingDirectory().resolve("minecraft-server-bootstrap-result.json");
    if (!Files.isRegularFile(bootstrapResultPath)) {
      throw new LoaderException(
          "Minecraft bootstrap child JVM did not write minecraft-server-bootstrap-result.json");
    }
    diagnosticSink.record(
        new DiagnosticEvent(
            "minecraft.bootstrap.complete",
            LaunchPhase.COMPLETE.name(),
            0L,
            exitCode == MinecraftBootstrapExitCode.SUCCESS.code() ? "ok" : "error",
            "Minecraft bootstrap child JVM finished",
            DiagnosticMeasurements.details(
                "exitCode",
                Integer.toString(exitCode),
                "stdoutBytes",
                Integer.toString(stdout.getBytes(StandardCharsets.UTF_8).length),
                "stderrBytes",
                Integer.toString(stderr.getBytes(StandardCharsets.UTF_8).length),
                "bootstrapResultOutputPath",
                DisplayPaths.displayPath(context, bootstrapResultPath))));
    return gson.fromJson(
        ProcessOutputReader.readFile(bootstrapResultPath), MinecraftBootstrapResult.class);
  }
}
