package com.spindle.core.security.trust;

import com.spindle.core.security.SecurityFinding;
import java.util.List;

public record ArtifactTrustEvaluation(
    List<ArtifactTrustEntry> entries,
    ArtifactTrustSummary summary,
    List<SecurityFinding> findings) {
  public ArtifactTrustEvaluation {
    entries = List.copyOf(entries);
    findings = List.copyOf(findings);
  }
}
