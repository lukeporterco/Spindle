package com.spindle.core.runtime.closure;

import java.util.List;

public record RuntimeClosureSummary(
    int implemented,
    int unavailable,
    int visibilityOnly,
    int gates,
    int stableApiCandidates,
    int deferredApiReview) {
  public static RuntimeClosureSummary from(
      List<RuntimeClosureSurface> surfaces,
      List<RuntimeClosureGate> gates,
      RuntimeClosureLoaderApiBoundary loaderApiBoundary) {
    int implementedCount = 0;
    int unavailableCount = 0;
    int visibilityOnlyCount = 0;
    for (RuntimeClosureSurface surface : surfaces) {
      switch (surface.state()) {
        case RuntimeClosureContract.STATE_IMPLEMENTED -> implementedCount++;
        case RuntimeClosureContract.STATE_UNAVAILABLE -> unavailableCount++;
        case RuntimeClosureContract.STATE_VISIBILITY_ONLY -> visibilityOnlyCount++;
        default -> {}
      }
    }
    return new RuntimeClosureSummary(
        implementedCount,
        unavailableCount,
        visibilityOnlyCount,
        gates.size(),
        loaderApiBoundary.stableCandidates().size(),
        loaderApiBoundary.deferredReview().size());
  }
}
