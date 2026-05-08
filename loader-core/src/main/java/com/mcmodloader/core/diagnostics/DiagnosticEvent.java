package com.mcmodloader.core.diagnostics;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public record DiagnosticEvent(String name, String phase, long durationMs, String status, String message, Map<String, String> details) {
    public DiagnosticEvent {
        details = details == null ? null : Collections.unmodifiableMap(new TreeMap<>(details));
    }

    public DiagnosticEvent(String name, String phase, long durationMs, String status) {
        this(name, phase, durationMs, status, null, null);
    }
}
