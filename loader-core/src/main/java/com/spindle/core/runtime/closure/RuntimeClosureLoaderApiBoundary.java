package com.spindle.core.runtime.closure;

import java.util.List;

public record RuntimeClosureLoaderApiBoundary(
    String status,
    String nextArc,
    List<String> stableCandidates,
    List<String> deferredReview,
    List<String> internalPackagesExcluded) {
  public RuntimeClosureLoaderApiBoundary {
    stableCandidates = stableCandidates == null ? List.of() : stableCandidates.stream().sorted().toList();
    deferredReview = deferredReview == null ? List.of() : deferredReview.stream().sorted().toList();
    internalPackagesExcluded =
        internalPackagesExcluded == null
            ? List.of()
            : internalPackagesExcluded.stream().sorted().toList();
  }
}
