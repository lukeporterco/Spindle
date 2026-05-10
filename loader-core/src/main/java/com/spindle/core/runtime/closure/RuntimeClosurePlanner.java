package com.spindle.core.runtime.closure;

import com.spindle.core.runtime.capability.RuntimeCapabilityCatalog;
import java.util.ArrayList;
import java.util.List;

public final class RuntimeClosurePlanner {
  public RuntimeClosureContract plan() {
    List<RuntimeClosureSurface> surfaces = new ArrayList<>();
    surfaces.add(
        new RuntimeClosureSurface(
            "compiled-profile",
            RuntimeClosureContract.STATE_IMPLEMENTED,
            "loader-core",
            null,
            null,
            "spindle.profile.json",
            "Compiled runtime profile contract is written deterministically."));
    surfaces.add(
        new RuntimeClosureSurface(
            "lifecycle-contract",
            RuntimeClosureContract.STATE_IMPLEMENTED,
            "loader-core",
            null,
            "com.spindle.api.ModInitializer",
            "lifecycle",
            "Runtime lifecycle phases and handler plans are compiled before execution."));
    surfaces.add(
        new RuntimeClosureSurface(
            "mod-context",
            RuntimeClosureContract.STATE_IMPLEMENTED,
            "loader-api",
            null,
            "com.spindle.api.ModContext",
            "contexts",
            "ModContext exposes owned directories and declared runtime services/config only."));
    surfaces.add(
        new RuntimeClosureSurface(
            "storage-directories",
            RuntimeClosureContract.STATE_IMPLEMENTED,
            "loader-api",
            "storage.*",
            "com.spindle.api.ModContext",
            "contexts",
            "Owned storage directories are planned deterministically per mod."));
    surfaces.add(
        new RuntimeClosureSurface(
            "capability-grants",
            RuntimeClosureContract.STATE_IMPLEMENTED,
            "loader-core",
            "permissions",
            null,
            "permissions",
            "Capability grants record granted, denied, unavailable, unknown, and visibility-only states."));
    surfaces.add(
        new RuntimeClosureSurface(
            "config-schema-runtime",
            RuntimeClosureContract.STATE_IMPLEMENTED,
            "loader-api",
            RuntimeCapabilityCatalog.CONFIG_READ,
            "com.spindle.api.config.ModConfig",
            "config",
            "Declared schema-2 config entries are materialized before lifecycle execution."));
    surfaces.add(
        new RuntimeClosureSurface(
            "deterministic-service-registry",
            RuntimeClosureContract.STATE_IMPLEMENTED,
            "loader-api",
            RuntimeCapabilityCatalog.SERVICE_CONSUME,
            "com.spindle.api.service.ServiceRegistry",
            "services",
            "Service bindings are compiled deterministically and providers remain lazy singletons."));
    for (String capability : RuntimeCapabilityCatalog.unavailableCapabilities()) {
      surfaces.add(
          new RuntimeClosureSurface(
              capability,
              RuntimeClosureContract.STATE_UNAVAILABLE,
              "loader-core",
              capability,
              null,
              "permissions",
              "Runtime-5 records this planned surface as explicitly unavailable."));
    }
    for (String capability : RuntimeCapabilityCatalog.visibilityOnlyCapabilities()) {
      surfaces.add(
          new RuntimeClosureSurface(
              capability,
              RuntimeClosureContract.STATE_VISIBILITY_ONLY,
              "runtime-honesty",
              capability,
              null,
              "permissions",
              "Runtime-5 records this broad Java behavior for visibility only and does not sandbox it."));
    }

    List<RuntimeClosureGate> gates =
        List.of(
            new RuntimeClosureGate(
                1,
                "security-gate",
                "pre-classloading",
                true,
                "Fatal security findings stop execution before mod classloading.",
                "Security validation runs before the mod classloader is created."),
            new RuntimeClosureGate(
                2,
                "runtime-config-contract-gate",
                "pre-classloading",
                true,
                "Fatal runtime config findings stop execution before mod classloading.",
                "Schema-2 config contract validation/materialization completes before classloading."),
            new RuntimeClosureGate(
                3,
                "runtime-service-contract-gate",
                "pre-classloading",
                true,
                "Fatal runtime service findings stop execution before mod classloading.",
                "Deterministic service binding validation completes before classloading."),
            new RuntimeClosureGate(
                4,
                "mod-classloader-create",
                "classloading-boundary",
                false,
                "None",
                "This gate creates the mod classloader and marks the classloading boundary."),
            new RuntimeClosureGate(
                5,
                "lifecycle-execute",
                "post-classloading",
                false,
                "Lifecycle execution occurs only after fatal runtime gates pass.",
                "Lifecycle handlers run after classloader creation."),
            new RuntimeClosureGate(
                6,
                "game-launch",
                "post-classloading",
                false,
                "Game launch occurs only after fatal runtime gates pass.",
                "Game launch follows lifecycle planning/execution in the standard runtime path."));

    RuntimeClosureLoaderApiBoundary loaderApiBoundary =
        new RuntimeClosureLoaderApiBoundary(
            "runtime-api-stabilized",
            "Loader API Hardening",
            List.of(
                "com.spindle.api.LoaderApi",
                "com.spindle.api.ModContext",
                "com.spindle.api.ModInitializer",
                "com.spindle.api.config.ModConfig",
                "com.spindle.api.exception.CapabilityDeniedException",
                "com.spindle.api.exception.ConfigAccessException",
                "com.spindle.api.exception.ServiceAccessException",
                "com.spindle.api.exception.SpindleApiException",
                "com.spindle.api.lifecycle.LifecyclePhase",
                "com.spindle.api.service.ServiceRegistry"),
            List.of(
                "com.spindle.api.minecraft.MinecraftServerModContext",
                "com.spindle.api.minecraft.MinecraftServerModInitializer"),
            List.of("com.spindle.api.internal", "com.spindle.core"));

    return new RuntimeClosureContract(
        RuntimeClosureContract.CONTRACT_VERSION,
        RuntimeClosureContract.ARC_STATUS,
        RuntimeClosureContract.SCOPE,
        RuntimeClosureContract.TARGET_MODEL,
        RuntimeClosureContract.RUNTIME_EXECUTION_ISOLATION_MODE,
        RuntimeClosureContract.SANDBOXED,
        RuntimeClosureContract.SANDBOX_CLAIM,
        surfaces,
        gates,
        loaderApiBoundary,
        null);
  }
}
