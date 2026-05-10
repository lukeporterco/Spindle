package com.spindle.core.runtime.closure;

import java.util.Comparator;
import java.util.List;

public record RuntimeClosureContract(
    int contractVersion,
    String arcStatus,
    String scope,
    String targetModel,
    String runtimeExecutionIsolationMode,
    boolean sandboxed,
    String sandboxClaim,
    List<RuntimeClosureSurface> surfaces,
    List<RuntimeClosureGate> gates,
    RuntimeClosureLoaderApiBoundary loaderApiBoundary,
    RuntimeClosureSummary summary) {
  public static final int CONTRACT_VERSION = 2;
  public static final String ARC_STATUS = "runtime-arc-closed";
  public static final String SCOPE = "spindle-runtime-core";
  public static final String TARGET_MODEL = "minecraft-as-target-not-foundation";
  public static final String RUNTIME_EXECUTION_ISOLATION_MODE = "in-process-unrestricted-java";
  public static final boolean SANDBOXED = false;
  public static final String SANDBOX_CLAIM = "not-sandboxed";
  public static final String STATE_IMPLEMENTED = "implemented";
  public static final String STATE_UNAVAILABLE = "unavailable";
  public static final String STATE_VISIBILITY_ONLY = "visibility-only";

  private static final Comparator<RuntimeClosureSurface> SURFACE_COMPARATOR =
      Comparator.comparingInt(
              (RuntimeClosureSurface surface) ->
                  switch (surface.state()) {
                    case STATE_IMPLEMENTED -> 0;
                    case STATE_UNAVAILABLE -> 1;
                    case STATE_VISIBILITY_ONLY -> 2;
                    default -> 3;
                  })
          .thenComparing(RuntimeClosureSurface::id);

  public RuntimeClosureContract {
    surfaces = surfaces == null ? List.of() : surfaces.stream().sorted(SURFACE_COMPARATOR).toList();
    gates = gates == null ? List.of() : gates.stream().sorted(Comparator.comparingInt(RuntimeClosureGate::order)).toList();
    loaderApiBoundary =
        loaderApiBoundary == null
            ? new RuntimeClosureLoaderApiBoundary(null, null, List.of(), List.of(), List.of())
            : loaderApiBoundary;
    summary = summary == null ? RuntimeClosureSummary.from(surfaces, gates, loaderApiBoundary) : summary;
  }

  public static RuntimeClosureContract empty() {
    return new RuntimeClosureContract(
        CONTRACT_VERSION,
        ARC_STATUS,
        SCOPE,
        TARGET_MODEL,
        RUNTIME_EXECUTION_ISOLATION_MODE,
        SANDBOXED,
        SANDBOX_CLAIM,
        List.of(),
        List.of(),
        new RuntimeClosureLoaderApiBoundary(null, null, List.of(), List.of(), List.of()),
        new RuntimeClosureSummary(0, 0, 0, 0, 0, 0));
  }
}
