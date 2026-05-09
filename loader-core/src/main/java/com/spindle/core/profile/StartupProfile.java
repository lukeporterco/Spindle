package com.spindle.core.profile;

import com.spindle.core.diagnostics.DiagnosticEvent;
import java.util.Comparator;
import java.util.List;

public record StartupProfile(
    int schema, long totalDurationMs, List<DiagnosticEvent> events, List<SlowEvent> slowestEvents) {
  public static StartupProfile from(List<DiagnosticEvent> events) {
    long totalDurationMs = events.stream().mapToLong(DiagnosticEvent::durationMs).sum();
    List<SlowEvent> slowestEvents =
        events.stream()
            .map(event -> new SlowEvent(event.name(), event.durationMs()))
            .sorted(
                Comparator.comparingLong(SlowEvent::durationMs)
                    .reversed()
                    .thenComparing(SlowEvent::name))
            .limit(5)
            .toList();
    return new StartupProfile(1, totalDurationMs, List.copyOf(events), slowestEvents);
  }

  public StartupProfile {
    events = List.copyOf(events);
    slowestEvents = List.copyOf(slowestEvents);
  }

  public record SlowEvent(String name, long durationMs) {}
}
