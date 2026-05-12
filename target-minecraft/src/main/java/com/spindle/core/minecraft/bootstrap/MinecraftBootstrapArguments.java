package com.spindle.core.minecraft.bootstrap;

import com.spindle.core.diagnostics.LoaderException;
import java.nio.file.Path;

public record MinecraftBootstrapArguments(
    Path workingDirectory,
    Path runtimePlanPath,
    Path boundaryPath,
    Path integrationPlanPath,
    Path executionPlanPath,
    Path hookInstallationPlanPath,
    String expectedRuntimeFingerprint,
    String expectedBoundaryFingerprint,
    String expectedIntegrationFingerprint,
    String expectedExecutionFingerprint,
    String expectedHookInstallationPlanFingerprint,
    boolean installHooks,
    boolean verifyPlanFingerprints,
    boolean strictExecution,
    boolean offlineBootstrap) {
  public static MinecraftBootstrapArguments parse(String[] args) throws LoaderException {
    Path workingDirectory = null;
    Path runtimePlanPath = null;
    Path boundaryPath = null;
    Path integrationPlanPath = null;
    Path executionPlanPath = null;
    Path hookInstallationPlanPath = null;
    String expectedRuntimeFingerprint = null;
    String expectedBoundaryFingerprint = null;
    String expectedIntegrationFingerprint = null;
    String expectedExecutionFingerprint = null;
    String expectedHookInstallationPlanFingerprint = null;
    boolean installHooks = false;
    boolean verifyPlanFingerprints = false;
    boolean strictExecution = false;
    boolean offlineBootstrap = false;

    for (int index = 0; index < args.length; index++) {
      String argument = args[index];
      switch (argument) {
        case "--working-directory" ->
            workingDirectory = Path.of(requireValue(argument, args, ++index));
        case "--runtime-plan" -> runtimePlanPath = Path.of(requireValue(argument, args, ++index));
        case "--boundary-plan" -> boundaryPath = Path.of(requireValue(argument, args, ++index));
        case "--integration-plan" ->
            integrationPlanPath = Path.of(requireValue(argument, args, ++index));
        case "--execution-plan" ->
            executionPlanPath = Path.of(requireValue(argument, args, ++index));
        case "--hook-installation-plan" ->
            hookInstallationPlanPath = Path.of(requireValue(argument, args, ++index));
        case "--expected-runtime-fingerprint" ->
            expectedRuntimeFingerprint = requireValue(argument, args, ++index);
        case "--expected-boundary-fingerprint" ->
            expectedBoundaryFingerprint = requireValue(argument, args, ++index);
        case "--expected-integration-fingerprint" ->
            expectedIntegrationFingerprint = requireValue(argument, args, ++index);
        case "--expected-execution-fingerprint" ->
            expectedExecutionFingerprint = requireValue(argument, args, ++index);
        case "--expected-hook-installation-plan-fingerprint" ->
            expectedHookInstallationPlanFingerprint = requireValue(argument, args, ++index);
        case "--install-hooks" -> installHooks = true;
        case "--verify-plan-fingerprints" -> verifyPlanFingerprints = true;
        case "--strict-execution" -> strictExecution = true;
        case "--offline-bootstrap" -> offlineBootstrap = true;
        default -> throw new LoaderException("Unknown Minecraft bootstrap argument " + argument);
      }
    }

    if (workingDirectory == null
        || runtimePlanPath == null
        || boundaryPath == null
        || integrationPlanPath == null
        || executionPlanPath == null) {
      throw new LoaderException(
          "Minecraft bootstrap requires working directory plus runtime, boundary, integration, and execution plan paths.");
    }
    if (installHooks && hookInstallationPlanPath == null) {
      throw new LoaderException(
          "Minecraft bootstrap requires --hook-installation-plan when --install-hooks is enabled.");
    }
    return new MinecraftBootstrapArguments(
        workingDirectory.toAbsolutePath().normalize(),
        workingDirectory.resolve(runtimePlanPath).toAbsolutePath().normalize(),
        workingDirectory.resolve(boundaryPath).toAbsolutePath().normalize(),
        workingDirectory.resolve(integrationPlanPath).toAbsolutePath().normalize(),
        workingDirectory.resolve(executionPlanPath).toAbsolutePath().normalize(),
        hookInstallationPlanPath == null
            ? null
            : workingDirectory.resolve(hookInstallationPlanPath).toAbsolutePath().normalize(),
        expectedRuntimeFingerprint,
        expectedBoundaryFingerprint,
        expectedIntegrationFingerprint,
        expectedExecutionFingerprint,
        expectedHookInstallationPlanFingerprint,
        installHooks,
        verifyPlanFingerprints,
        strictExecution,
        offlineBootstrap);
  }

  private static String requireValue(String argument, String[] args, int index)
      throws LoaderException {
    if (index >= args.length) {
      throw new LoaderException("Missing value for " + argument);
    }
    return args[index];
  }
}
