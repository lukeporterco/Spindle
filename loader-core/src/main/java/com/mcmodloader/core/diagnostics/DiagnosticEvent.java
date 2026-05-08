package com.mcmodloader.core.diagnostics;

public record DiagnosticEvent(String name, String phase, long durationMs, String status) {
}
