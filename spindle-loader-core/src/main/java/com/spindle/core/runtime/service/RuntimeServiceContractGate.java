package com.spindle.core.runtime.service;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.runtime.CompiledModpackProfile;

public final class RuntimeServiceContractGate {
  public void ensureLifecycleExecutionAllowed(CompiledModpackProfile profile) throws LoaderException {
    RuntimeServiceSummary summary = profile.services().summary();
    if (summary == null || summary.fatalCount() == 0) {
      return;
    }
    throw new LoaderException(
        "Runtime service contract has fatal findings: requiredUnbound="
            + summary.requiredUnbound()
            + ", conflictingProviders="
            + summary.conflictingProviders()
            + ", missingImplementations="
            + summary.missingImplementations()
            + ", implementationOwnershipViolations="
            + summary.implementationOwnershipViolations()
            + ", typeMismatches="
            + summary.typeMismatches()
            + ".");
  }
}
