package com.spindle.core.security.risk;

import java.util.List;

public record StaticRiskSummary(int signalCount, int modCountWithSignals) {
  public static final StaticRiskSummary EMPTY = new StaticRiskSummary(0, 0);

  public static StaticRiskSummary from(List<StaticRiskSignal> signals) {
    return new StaticRiskSummary(
        signals.size(),
        (int)
            signals.stream()
                .map(StaticRiskSignal::modId)
                .filter(id -> id != null)
                .distinct()
                .count());
  }
}
