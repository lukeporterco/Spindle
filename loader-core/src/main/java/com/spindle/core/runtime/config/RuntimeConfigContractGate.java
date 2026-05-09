package com.spindle.core.runtime.config;

import com.spindle.core.diagnostics.LoaderException;
import com.spindle.core.runtime.CompiledModpackProfile;

public final class RuntimeConfigContractGate {
  public void ensureLifecycleExecutionAllowed(CompiledModpackProfile profile) throws LoaderException {
    RuntimeConfigSummary summary = profile.config().summary();
    if (summary == null || summary.fatalCount() == 0) {
      return;
    }
    throw new LoaderException(
        "Runtime config contract has fatal findings: storageNotGranted="
            + summary.storageNotGranted()
            + ", invalid="
            + summary.invalid()
            + ".");
  }
}
